/**************************************************************************************
 * MIT License                                                                        *
 *                                                                                    *
 * Copyright (c) 2023. Kanzaji                                                        *
 *                                                                                    *
 * Permission is hereby granted, free of charge, to any person obtaining a copy       *
 * of this software and associated documentation files (the "Software"), to deal      *
 * in the Software without restriction, including without limitation the rights       *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell          *
 * copies of the Software, and to permit persons to whom the Software is              *
 * furnished to do so, subject to the following conditions:                           *
 *                                                                                    *
 * The above copyright notice and this permission notice shall be included in all     *
 * copies or substantial portions of the Software.                                    *
 *                                                                                    *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR         *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,           *
 * FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE       *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER             *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,      *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE      *
 * SOFTWARE.                                                                          *
 **************************************************************************************/

package com.kanzaji.catdownloaderlegacy;

import com.kanzaji.catdownloaderlegacy.data.CDLInstance;
import com.kanzaji.catdownloaderlegacy.data.CFManifest;
import com.kanzaji.catdownloaderlegacy.loggers.LoggerCustom;
import com.kanzaji.catdownloaderlegacy.utils.FileUtils;
import com.kanzaji.catdownloaderlegacy.utils.RandomUtils;
import com.kanzaji.catdownloaderlegacy.utils.SettingsManager;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Stream;

import static com.kanzaji.catdownloaderlegacy.CatDownloader.WORKPATH;

/**
 * SyncManager is a class used to manage synchronization of the mods.
 */
public class SyncManager {
    private static final LoggerCustom logger = new LoggerCustom("Sync Manager");
    private static final ArgumentDecoder ARD = ArgumentDecoder.getInstance();
    private static Path CDLTemp;
    private final CDLInstance CDLInstanceData;
    private ExecutorService downloadExecutor;
    private ExecutorService verificationExecutor;
    private final HashSet<Integer> missing = new HashSet<>();
    private final HashSet<Integer> corrupted = new HashSet<>();
    private final HashSet<String> removed = new HashSet<>();
    private final HashSet<Integer> failedDownloads = new HashSet<>();
    private final HashSet<Integer> failedVerifications = new HashSet<>();
    private final HashSet<String> failedRemovals = new HashSet<>();
    private final HashSet<Integer> IgnoredVerification = new HashSet<>();
    private final HashSet<Integer> IgnoredRemoval = new HashSet<>();

    /**
     * Constructor of SyncManager Object.
     * @param CDLInstanceData CDLInstance with data about the instance.
     */
    public SyncManager(CDLInstance CDLInstanceData) {
        this.CDLInstanceData = CDLInstanceData;
    }

    /**
     * This method is used to run Synchronization routines for specified instance in the Constructor of this Object.
     * @throws InterruptedException when any of the executors are interrupted.
     * @throws TimeoutException when any of the executors take more than 24 hours to complete.
     * @throws IOException when IO Exception occurs.
     */
    public void runSync() throws InterruptedException, TimeoutException, IOException {
        Objects.requireNonNull(CDLInstanceData, "CDLInstanceData is null!");

        verificationExecutor = Executors.newFixedThreadPool(ARD.getThreads());
        downloadExecutor = Executors.newFixedThreadPool(ARD.getThreads());
        CDLTemp = Path.of(WORKPATH.toString(), "CDLTemp");

        logger.log("Running GC to clear out memory before running synchronization process...");
        RandomUtils.runGCL();
        logger.print("Starting synchronization process!");
        System.out.println("---------------------------------------------------------------------");

        verifyInstalledMods();
        printVerificationResults();
        RandomUtils.runGCL();

        removeRemovedMods();

        downloadRequiredMods();
        RandomUtils.runGCL();

        printStatistics();

        cleanup();

        System.out.println("Synchronization of the profile finished!");
    }

    /**
     * This method is used internally by {@link SyncManager} to query verification and lookup tasks for mods in the specified Instance. Respects Blacklist from the Settings File.
     * @throws InterruptedException when Executor is interrupted.
     * @throws TimeoutException if the Executor doesn't finish before 24-hours pass.
     */
    private void verifyInstalledMods() throws InterruptedException, TimeoutException {
        List<Future<Integer[]>> verificationResults = new LinkedList<>();
        System.out.println("Looking for already installed mods...");
        logger.log("Requesting of lookups for installed mods and their verification started.");

        for (int index = 0; index < CDLInstanceData.files.length; index++) {
            if (ARD.isPackMode()) CDLInstanceData.gatherCFModInformation(index);
            CDLInstance.ModFile mod = CDLInstanceData.files[index];

            if (Objects.equals(mod.fileName, "CF-PACK_MOD")) {
                failedDownloads.add(index);
                continue;
            }

            if (SettingsManager.ModBlackList.contains(mod.fileName)) {
                logger.warn("Skipping verification of  " + mod.fileName + " because its present on the blacklist!");
                IgnoredVerification.add(index);
                continue;
            }

            logger.log("Lookup and verification of file " + mod.fileName + " has been requested.");
            verificationResults.add(verificationExecutor.submit(CDLInstanceData.getVerificationTask(index)));
        }

        RandomUtils.waitForExecutor(verificationExecutor, 1, TimeUnit.DAYS, "Verification takes over a day!");
        decodeVerificationResults(verificationResults);
    }

    /**
     * This method is used internally by {@link SyncManager} to decode results from the verification tasks.
     * @param verificationResults A list with Future objects from the executor.
     * @throws NullPointerException when verificationResults are null.
     */
    @SuppressWarnings("ForLoopReplaceableByForEach")
    private void decodeVerificationResults(@NotNull List<Future<Integer[]>> verificationResults) {
        Objects.requireNonNull(verificationResults);
        // LinkedList<>#forEach() and enhanced for use Iterators.
        // For some reason, I got issues with random NullPointerExceptions while using them in the old SyncManager.
        for (int i = 0; i < verificationResults.size(); i++) {
            Future<Integer[]> Future = verificationResults.get(i);
            try {
                Integer[] results = Future.get();
                CDLInstance.ModFile mod = CDLInstanceData.files[results[0]];
                Objects.requireNonNull(results, "Null value got while gathering verification results!");
                if (!Objects.equals(results.length, 2)) {
                    throw new IllegalStateException("Results from the verification are not in correct schema! => " + Arrays.toString(results));
                }
                switch (results[1]) {
                    case 0 -> logger.log("File \"" + mod.path + "\" has been verified successfully.");
                    case 1 -> {
                        logger.log("File \"" + mod.path + "\" not found!");
                        missing.add(results[0]);
                    }
                    case -1 -> {
                        logger.warn("File \"" + mod.path + "\" is corrupted!");
                        corrupted.add(results[0]);
                    }
                    default -> throw new IllegalStateException("Invalid value in the verification results! => " + Arrays.toString(results));
                }
            } catch (Exception e) {
                if (Objects.equals(e.getClass(),ExecutionException.class)) {
                    Throwable e2 = e.getCause();
                    logger.logStackTrace("Exception found in the verification results!", e2.getCause());
                    failedVerifications.add(Integer.parseInt(e2.getMessage()));
                } else {
                    throw new RuntimeException("Exception thrown while gathering results from the verification!", e);
                }
            }
        }
    }

    /**
     * This method is used internally by {@link SyncManager} to print results of the verification and mod lookup.
     */
    private void printVerificationResults() {
        if (CDLInstanceData.files.length - missing.size() > 0) {
            logger.print(
                (CDLInstanceData.files.length - missing.size() - failedDownloads.size()) + " out of " +
                RandomUtils.intGrammar(CDLInstanceData.files.length - failedDownloads.size(), " mod", " mods", true) +
                " have been found on the hard drive!"
            );

            if (missing.size() > 0) {
                logger.print("> " + RandomUtils.intGrammar(missing.size(), " mod is", " mods are", true) + " missing and designated to download.");
            }

            logger.print(
                "> " + RandomUtils.intGrammar(CDLInstanceData.files.length - missing.size() - corrupted.size() - failedDownloads.size(), " mod", " mods", true) +
                " have been verified successfully."
            );

            if (corrupted.size() > 0) {
                logger.print("> " + RandomUtils.intGrammar(corrupted.size(), " mod is", " mods are", true) + " corrupted and designated to re-download.");
            } else {
                logger.print("> No mods are corrupted.");
            }
        } else {
            logger.print("Any of the required mods have been found on the hard drive.");
            logger.print("> " + RandomUtils.intGrammar(missing.size(), " mod is", " mods are", true) + " missing and designated to download.");
        }
    }

    /**
     * This method is used internally by {@link SyncManager} to download any mods that are missing from the local installation of the instance passed to the constructor.
     * @throws InterruptedException when Executor is interrupted.
     * @throws TimeoutException if the Executor doesn't finish before 24-hours pass.
     */
    private void downloadRequiredMods() throws InterruptedException, TimeoutException {
        HashSet<Integer> downloads = new HashSet<>(missing);
        downloads.addAll(corrupted);
        HashSet<Callable<Integer[]>> downloadTasks = new HashSet<>();
        if (downloads.size() < 1) {
            return;
        }

        logger.print("Download process has been started!");

        downloads.forEach((index) -> {
            CDLInstance.ModFile mod = CDLInstanceData.files[index];
            logger.log("Downloading of " + mod.fileName + " has been requested.");
            downloadTasks.add(CDLInstanceData.getDownloadTask(index));
        });

        List<Future<Integer[]>> downloadResults = new LinkedList<>(downloadExecutor.invokeAll(downloadTasks));
        RandomUtils.waitForExecutor(downloadExecutor, 1, TimeUnit.DAYS, "Downloads take over a day!");

        decodeDownloadResults(downloadResults);
    }

    /**
     * This method is used internally by {@link SyncManager} to decode results from the Download tasks.
     * @param downloadResults A list with Future objects from the executor.
     * @throws NullPointerException when downloadResults are null.
     */
    private void decodeDownloadResults(@NotNull List<Future<Integer[]>> downloadResults) {
        int initFailedDownloadsSize = failedDownloads.size();
        Objects.requireNonNull(downloadResults);
        for (int i = downloadResults.size() - 1; i >= 0; i--) {
            try {
                Integer[] results = downloadResults.get(i).get();
                if (!Objects.equals(results.length, 2)) {
                    throw new IllegalStateException("Results from the downloads are not in correct schema! => " + Arrays.toString(results));
                }
                switch (results[1]) {
                    case 0 -> logger.log("File \"" + CDLInstanceData.files[results[0]].path + "\" has been downloaded successfully.");
                    case -1 -> {
                        logger.log("File \"" + CDLInstanceData.files[results[0]].path + "\" has failed to download correctly!!");
                        failedDownloads.add(results[0]);
                    }
                    default -> throw new IllegalStateException("Invalid value in the download results! => " + Arrays.toString(results));
                }
            } catch (Exception e) {
                if (Objects.equals(e.getClass(),ExecutionException.class)) {
                    Throwable e2 = e.getCause();
                    logger.logStackTrace("Exception found in the download results!", e2.getCause());
                    failedDownloads.add(Integer.parseInt(e2.getMessage().substring(0, e2.getMessage().indexOf(";")-1)));
                } else {
                    throw new RuntimeException("Exception thrown while gathering results from the downloads!", e);
                }
            }
        }

        logger.print(
            "Finished downloading process! " +
                ((failedDownloads.size()-initFailedDownloadsSize > 0)?
                    RandomUtils.intGrammar(
                            missing.size() + corrupted.size() - failedDownloads.size(),
                            " mod out of " + (missing.size() + corrupted.size()) +" was",
                            " mods out of " + (missing.size() + corrupted.size()) +" were",
                            true
                    ) +
                    " downloaded successfully.":
                    "All mods have been downloaded successfully"
                ),
            (failedDownloads.size() > 0)? 2 : 0
        );
        System.out.println("---------------------------------------------------------------------");
    }

    /**
     * This method is used internally by {@link SyncManager} to remove any mods that are not present in the mod list of the instance specified in the constructor. Respects Blacklist from the Settings File.
     */
    private void removeRemovedMods() throws IOException {
        logger.log("Looking for removed mods...");
        try (Stream<Path> pathStream = Files.list(Path.of(WORKPATH.toString(), "mods"))) {
            pathStream.forEach(File -> {
                String FileName = File.getFileName().toString();
                if (Arrays.stream(CDLInstanceData.files).noneMatch((mod) -> Objects.equals(mod.fileName, FileName) && mod.path.startsWith("mods"))) {
                    if (Files.exists(Path.of(CDLTemp.toString(), RandomUtils.removeCommonPart(WORKPATH.toAbsolutePath().toString(), File.toAbsolutePath().toString())))) return;

                    int index = SettingsManager.ModBlackList.indexOf(FileName);
                    if (index >= 0) {
                        logger.warn("Found removed mod " + FileName + ", but its present on the blacklist. Skipping!");
                        IgnoredRemoval.add(index);
                        return;
                    }

                    logger.log("Found removed mod " + File.getFileName() + "! Deleting...");
                    try {
                        FileUtils.delete(File);
                        removed.add(File.getFileName().toString());
                    } catch (IOException e) {
                        logger.logStackTrace("Failed deleting " + File.getFileName() + "!", e);
                        failedRemovals.add(File.getFileName().toString());
                    }
                }
            });
        }

        if (removed.size() > 0) {
            logger.print("> " + RandomUtils.intGrammar(removed.size(), " mod was", " mods were", true) + " removed!");
        } else {
            logger.print("> No mods were removed!");
        }
        System.out.println("---------------------------------------------------------------------");
    }

    /**
     * This method is used internally by {@link SyncManager} to clean up any temporary files and directories created by the app.
     */
    private void cleanup() {
        try {
            if (Files.exists(CDLTemp)) {
                logger.log("Found CDLTemp folder in the working directory!");
                Path overrides = Path.of(CDLTemp.toString(), CDLInstanceData.modpackData.overrides);
                if (Files.notExists(overrides)) throw new NoSuchFileException("No overrides folder in the CDLTemp!");

                logger.log("Moving overrides content to the Working Directory...");
                try (Stream<Path> dirListing = Files.list(overrides)) {
                    dirListing.forEach((File) -> {
                        try {
                            FileUtils.move(File, WORKPATH, true);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                } catch (Exception e)  {
                    logger.logStackTrace("Exception thrown while moving override's content to the root directory!", e);
                    logger.print("Failed to move override's content to the root directory! You will have to do that manually from the zip file or try again.",3);
                    System.out.println("---------------------------------------------------------------------");
                }

                logger.log("Deleting CDLTemp folder...");

                FileUtils.delete(CDLTemp);
                logger.log("Cleanup completed.");
            }
        } catch (Exception e) {
            logger.logStackTrace("Exception thrown while cleaning up working space!", e);
            logger.print("Failed to properly cleanup working directory! CDLTemp was most likely not deleted. Please verify all config files from the overrides are intact, and delete CDLTemp manually.", 3);
            System.out.println("---------------------------------------------------------------------");
        }
    }

    /**
     * This method is used internally by {@link SyncManager} to print Synchronization statistics, like failed download tasks.
     */
    private void printStatistics() {
        if (IgnoredRemoval.size() > 0 || IgnoredVerification.size() > 0) {
            logger.print("Ignored mods found in the config file! (" + RandomUtils.intGrammar(SettingsManager.ModBlackList.size(), " file)", " files)", true), 1);
            logger.log("Mods contained in the blacklist:");
            SettingsManager.ModBlackList.forEach((mod) -> logger.log("- " + mod));

            if (IgnoredVerification.size() > 0) {
                logger.print("> " + RandomUtils.intGrammar(IgnoredVerification.size(), " mod was", " mods were", true) + " not verified!", 1);
                IgnoredVerification.forEach((mod) -> logger.warn("- " + CDLInstanceData.files[mod].fileName));
            } else {
                logger.print("> All mods have been verified.");
            }

            if (IgnoredRemoval.size() > 0) {
                logger.print("> " + RandomUtils.intGrammar(IgnoredRemoval.size(), " mod was", " mods were", true) + " not removed!", 1);
                IgnoredRemoval.forEach((mod) -> logger.warn("- " + CDLInstanceData.files[mod].fileName));
            } else {
                logger.print("> All mods designated to removal were removed.");
            }

            System.out.println(
                "\nFor more details, check your configuration file or the log at:\n" +
                "\"" + logger.getLogPath() + "\"\n" +
                "\"" + Path.of(ArgumentDecoder.getInstance().getSettingsPath()).toAbsolutePath() + "\\Cat-Downloader-Legacy-Settings.json5\""
            );
            System.out.println("---------------------------------------------------------------------");
        }

        int errors = failedRemovals.size() + failedDownloads.size() + failedVerifications.size();

        if (errors > 0) {
            logger.print("Errors were found while doing synchronisation of the profile!", 2);

            if (failedVerifications.size() > 0) logger.print("> Failed Verifications: " + failedVerifications.size(),2);
            if (failedRemovals.size() > 0) logger.print("> Failed removals: " + failedRemovals.size(),2);
            if (failedDownloads.size() > 0) logger.print("> Download errors: " + failedDownloads.size(),2);

            System.out.println("For more details, check log file at " + logger.getLogPath());

            if (failedVerifications.size() > 0) {
                logger.error("Files that failed verification with an exception:");
                failedVerifications.forEach((index) -> logger.error("  " + CDLInstanceData.files[index].fileName));
            }

            if (failedRemovals.size() > 0) {
                logger.error("Files that weren't possible to remove:");
                failedRemovals.forEach((FileName) -> logger.error("  " + FileName));
            }

            if (failedDownloads.size() > 0) {
                logger.error("Files that failed to Download:");
                failedDownloads.forEach((index) -> {
                    CDLInstance.ModFile mod = CDLInstanceData.files[index];
                    if (Objects.equals(mod.fileName, "CF-PACK_MOD")) {
                        logger.error("  A CurseForge mod from the project with id " + mod.fileLength + " (" + mod.downloadURL + ") was not possible to found! Project URL: \"https://cfwidget.com/" + mod.fileLength + "\"");
                    } else {
                        logger.error("  " + mod.fileName);
                    }
                });
            }
            System.out.println("---------------------------------------------------------------------");
        }
        //TODO: Proper warning from the data gathering,
        // this is here just to give info that some mods were not found
        // and fallback was found to give possibility to play that instance!
        if (CFManifest.DataGatheringWarnings.size() > 0) {
            logger.print("Data gathering warnings found!",1);
            logger.print("This might signal that some mods were not found and fallback was used.",1);
            System.out.println("Please inspect your log file at " + logger.getLogPath() + " for more information.");
            CFManifest.DataGatheringWarnings.forEach(warn -> logger.warn("\n"+warn));
            System.out.println("---------------------------------------------------------------------");
        }
    }
}

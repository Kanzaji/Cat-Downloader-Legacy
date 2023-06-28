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

import com.kanzaji.catdownloaderlegacy.jsons.Manifest;
import com.kanzaji.catdownloaderlegacy.utils.DownloadUtils;
import com.kanzaji.catdownloaderlegacy.utils.SettingsManager;
import com.kanzaji.catdownloaderlegacy.loggers.LoggerCustom;

import static com.kanzaji.catdownloaderlegacy.utils.FileVerUtils.verifyFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class SyncManager {
    private static final LoggerCustom logger = new LoggerCustom("SyncManager");
    private static final class InstanceHolder {private static final SyncManager instance = new SyncManager();}
    private SyncManager() {}
    private ExecutorService downloadExecutor;
    private ExecutorService verificationExecutor;
    private Path ModFolderPath;
    private Manifest ManifestData;
    private int RemovedCount = 0;
    private int DownloadSuccess = 0;
    private int ReDownloadQueue = 0;
    private int NullMods = 0;
    private final List<Runnable> DownloadQueueL = new LinkedList<>();
    private final List<Runnable> VerificationQueueL = new LinkedList<>();
    private final List<String> ModFileNames = new LinkedList<>();
    private final List<String> FailedVerifications = new LinkedList<>();
    private final List<String> FailedDownloads = new LinkedList<>();
    private final List<String> FailedRemovals = new LinkedList<>();
    private final List<String> IgnoredVerification = new LinkedList<>();
    private final List<String> IgnoredRemoval = new LinkedList<>();
    public final List<String> DataGatheringWarnings = new LinkedList<>();

    /**
     * Used to get a reference to an instance of the SyncManager.
     * @return Reference to an instance of the SyncManager.
     */
    public static SyncManager getInstance() {
        return InstanceHolder.instance;
    }

    /**
     * Used to pass required Data to FileManager instance.
     * @param modFolderPath Path to mods folder.
     * @param manifest Manifest Data
     * @param nThreadsCount Amount of Threads for downloader to use.
     */
    public void passData(Path modFolderPath, Manifest manifest, int nThreadsCount) {
        this.ModFolderPath = modFolderPath;
        this.ManifestData = manifest;
        this.downloadExecutor = Executors.newFixedThreadPool(nThreadsCount);
        this.verificationExecutor = Executors.newFixedThreadPool(nThreadsCount);
    }

    /**
     * Used to start process of Syncing a Profile. It uses a data passed by FileManager#passData to a FileManager instance.
     * @throws NullPointerException when Path to Mod Folder, Manifest Data or Executor is null.
     * @throws IOException when IO Operation fails.
     * @throws InterruptedException when Executor is interrupted.
     * @throws RuntimeException when downloading process takes over a day.
     */
    public void runSync() throws NullPointerException, IOException, InterruptedException, RuntimeException {
        if (ModFolderPath == null || ManifestData == null || downloadExecutor == null || verificationExecutor == null) {
            if (ModFolderPath == null) {
                throw new NullPointerException("ModFolderPath is null!");
            }
            if (ManifestData == null) {
                throw new NullPointerException("ManifestData is null!");
            }
            throw new NullPointerException("Executor is null! passData seems to have failed?");
        }

        logger.print("Started syncing process!");

        System.out.println("---------------------------------------------------------------------");
        System.out.println("Looking for already installed mods...");
        logger.log("Checking for already existing files, and getting download queue ready...");

        for (Manifest.ModFile mod: ManifestData.files) {
            if (mod == null || mod.downloadUrl == null || mod.fileSize == null) {
                if (mod != null) {
                    if (mod.error403) {
                        logger.error("Mod with 403 error found! Skipping...");
                        continue;
                    }
                    if (mod.error202) {
                        logger.error("Mod with 202/500 Response code and failed regathering found! Skipping...");
                        continue;
                    }
                }
                logger.error("Mod without any data found! Skipping...");
                NullMods += 1;
                continue;
            }

            String FileName = mod.getFileName();
            Path ModFile = Path.of(ModFolderPath.toString(), FileName);
            ModFileNames.add(FileName);

            if (SettingsManager.ModBlackList.contains(FileName)) {
                logger.warn("Skipping " + FileName + " in verification because its present on the blacklist!");
                IgnoredVerification.add(FileName);
                continue;
            }

            if (Files.notExists(ModFile, LinkOption.NOFOLLOW_LINKS)) {
                logger.log(FileName + " not found! Added to the download queue.");
                DownloadQueueL.add(() -> {
                    DownloadUtils.download(ModFile, mod.downloadUrl, FileName);
                    try {
                        logger.log("Verifying " + FileName + " after download...");
                        if (!verifyFile(ModFile, mod.fileSize, mod.downloadUrl)) {
                            logger.warn("Mod " + FileName + " appears to have not downloaded correctly! Re-downloading...");
                            if(DownloadUtils.reDownload(ModFile, mod.downloadUrl, FileName, mod.fileSize)) {
                                logger.log("Re-download of " + FileName + " was successful!");
                                DownloadSuccess += 1;
                            } else {
                                logger.error("Re-download of " + FileName + " after " + ArgumentDecoder.getInstance().getDownloadAttempts() + " attempts failed!");
                                FailedDownloads.add(FileName);
                            }
                        } else {
                            logger.log("Verification of " + FileName + " was successful!");
                            DownloadSuccess += 1;
                        }
                    } catch (Exception e) {
                        logger.logStackTrace("Exception thrown while re-downloading " + FileName + "!", e);
                        FailedDownloads.add(FileName);
                    }
                });
            } else {
                logger.log("Found mod " + FileName + " on the drive! Added to the Verification queue.");
                VerificationQueueL.add(() -> {
                    logger.log("Verifying " + FileName + " ...");
                    try {
                        if (!verifyFile(ModFile, mod.fileSize, mod.downloadUrl)) {
                            logger.warn("Mod " + FileName + " seems to be corrupted! Added to re-download queue.");
                            ReDownloadQueue += 1;
                            DownloadQueueL.add(() -> {
                                logger.log("Re-downloading " + FileName + " ...");
                                try {
                                    if(DownloadUtils.reDownload(ModFile, mod.downloadUrl, FileName, mod.fileSize)) {
                                        logger.log("Re-download of " + FileName + " was successful!");
                                        DownloadSuccess += 1;
                                    } else {
                                        logger.error("Re-download of " + FileName + " after " + ArgumentDecoder.getInstance().getDownloadAttempts() + " attempts failed!");
                                        FailedDownloads.add(FileName);
                                    }
                                } catch (Exception e) {
                                    logger.logStackTrace("Exception thrown while re-downloading " + FileName + "!", e);
                                    FailedDownloads.add(FileName);
                                }
                            });
                        } else {
                            logger.log("Verification of " + FileName + " was successful!");
                        }
                    } catch (Exception e) {
                        logger.logStackTrace("Verification of " + FileName + " failed with exception!", e);
                        FailedVerifications.add(FileName);
                    }
                });
            }
        }

        logger.print(
            ((VerificationQueueL.size() > 0)?
                "Found " + VerificationQueueL.size() + ((VerificationQueueL.size() == 1)? " mod": " mods") + " on the drive. Verifying installation..." :
                "No installed mods found!"
        ));

        for (Runnable Task: VerificationQueueL) { verificationExecutor.submit(Task); }
        verificationExecutor.shutdown();

        if (!verificationExecutor.awaitTermination(1, TimeUnit.DAYS)) {
            logger.error("Verification takes over a day! This for sure isn't right???");
            System.out.println("Verification interrupted due to taking over a day! This for sure isn't right???");
            throw new RuntimeException("Verification is taking over a day! Something is horribly wrong.");
        }

        if (VerificationQueueL.size() > 0) logger.print("Verification of existing files finished!");

        logger.log("Required data received for " + ModFileNames.size() + " out of " + ManifestData.files.length + ((ManifestData.files.length == 1)? " mod.": " mods"));
        if (ArgumentDecoder.getInstance().isPackMode()) System.out.println("> Data received for " + ModFileNames.size() + " out of " + ManifestData.files.length + ((ManifestData.files.length == 1)? " mod.": " mods."));

        if (DownloadQueueL.size() > 0) {
            logger.log("Mods designated to download: " + DownloadQueueL.size());
            System.out.println("> Mods designated to download: " + DownloadQueueL.size());
        } else {
            logger.log("No mods designated to download!");
            System.out.println("> No mods designated to download!");
        }

        if (ReDownloadQueue > 0) {
            logger.warn("Found corrupted mods! Mods designated to re-download: " + ReDownloadQueue);
            System.out.println("> Found corrupted mods! Mods designated to re-download: " + ReDownloadQueue);
        } else if (VerificationQueueL.size() > 0) {
            logger.log("No corrupted mods found!");
            System.out.println("> No corrupted mods found!");
        }

        logger.log("Checking Mods folder for removed mods...");
        System.out.println("---------------------------------------------------------------------");
        System.out.println("Checking for removed mods...");
        try (Stream<Path> pathStream = Files.list(ModFolderPath)) {
            pathStream.forEach(File -> {
                String FileName = File.getFileName().toString();
                if (!ModFileNames.contains(FileName)) {

                    // Check if the filename exists in the Blacklist
                    if (SettingsManager.ModBlackList.contains(FileName)) {
                        logger.warn("Found removed mod " + FileName + ", but its present on the blacklist. Skipping!");
                        IgnoredRemoval.add(FileName);
                        return;
                    }

                    logger.log("Found removed mod " + File.getFileName() + "! Deleting...");
                    try {
                        Files.delete(File);
                        logger.log("Deleted " + File.getFileName() + ".");
                        RemovedCount += 1;
                    } catch (IOException e) {
                        logger.logStackTrace("Failed deleting " + File.getFileName() + "!", e);
                        FailedRemovals.add(File.getFileName().toString());
                    }
                }
            });
        }

        logger.log("Check for removed mods done.");
        System.out.println("Finished checking for removed mods!");
        if (RemovedCount > 0) {
            logger.log("Removed " + RemovedCount + " mods!");
            System.out.println("> Removed " + RemovedCount + " mods!");
        } else {
            logger.log("No mods were removed.");
            System.out.println("> No mods were removed!");
        }


        if (DownloadQueueL.size() > 0 || ReDownloadQueue > 0) {
            System.out.println("---------------------------------------------------------------------");
            logger.log("Starting download process...");
            System.out.println("Starting download process... This may take a while.");
        } else {
            logger.log("Download queue is empty.");
        }

        for (Runnable Task: DownloadQueueL) { downloadExecutor.submit(Task); }
        downloadExecutor.shutdown();
        if (!downloadExecutor.awaitTermination(1, TimeUnit.DAYS)) {
            logger.error("Downloading takes over a day! This for sure isn't right???");
            System.out.println("Downloads interrupted due to taking over a day! This for sure isn't right???");
            throw new RuntimeException("Downloads are taking over a day! Something is horribly wrong.");
        }

        if (DownloadQueueL.size() > 0 || ReDownloadQueue > 0) {
            logger.log("Finished downloading process.");
            System.out.println("Finished downloading process, " + DownloadSuccess + " mods downloaded successfully!");
        }

        System.out.println("---------------------------------------------------------------------");
        if (SettingsManager.ModBlackList.size() > 0) {

            System.out.println("Ignored mods found in the config file! (" + SettingsManager.ModBlackList.size() + " " + ((SettingsManager.ModBlackList.size() == 1)? "file":"files") + ")");
            logger.log("Mods contained in the blacklist:");
            SettingsManager.ModBlackList.forEach((mod) -> logger.log("- " + mod));

            if (IgnoredVerification.size() > 0) {
                logger.warn(IgnoredVerification.size() + " " + ((IgnoredVerification.size() == 1)? "mod was":"mods were") + " not verified!");
                System.out.println("> " + IgnoredVerification.size() + " " + ((IgnoredVerification.size() == 1)? "mod was":"mods were") + " not verified!");
                IgnoredVerification.forEach((mod) -> logger.warn("- " + mod));
            } else {
                logger.log("All mods have been verified.");
                System.out.println("> All mods have been verified.");
            }

            if (IgnoredRemoval.size() > 0) {
                logger.warn(IgnoredRemoval.size() + " " + ((IgnoredRemoval.size() == 1)? "mod was":"mods were") + " not removed!");
                System.out.println("> " + IgnoredVerification.size() + " " + ((IgnoredRemoval.size() == 1)? "mod was":"mods were") + " not removed!");
                IgnoredRemoval.forEach((mod) -> logger.warn("- " + mod));
            } else {
                logger.log("All mods designated to removal were removed!");
                System.out.println("> All mods designated to removal were removed!");
            }

            System.out.println("\nFor more details, check your configuration file or the log at:\n\"" + logger.getLogPath() + "\"");
            System.out.println("---------------------------------------------------------------------");
        }

        if (DataGatheringWarnings.size() > 0) {
            logger.warn("Warnings were found while doing synchronisation of the profile!");
            System.out.println("Warnings were found while doing synchronisation of the profile!");
            System.out.println("Warnings present: " + DataGatheringWarnings.size());
            for (String Warning : DataGatheringWarnings) {
                logger.warn(Warning);
            }
            System.out.println("For more details, check log file at " + logger.getLogPath());
            System.out.println("---------------------------------------------------------------------");
        }

        int errors = FailedRemovals.size() + FailedDownloads.size() + FailedVerifications.size() + NullMods;

        if (errors > 0) {
            logger.error("Errors were found while doing synchronisation of the profile!");
            System.out.println("Errors were found while doing synchronisation of the profile!");

            if (FailedVerifications.size() > 0) {
                logger.error("Failed Verifications: " + FailedVerifications.size());
                System.out.println("> Failed Verifications: " + FailedVerifications.size());
            } else {
                logger.error("No verification errors found.");
            }

            if (FailedRemovals.size() > 0) {
                logger.error("Failed removals: " + FailedRemovals.size());
                System.out.println("> Failed removals: " + FailedRemovals.size());
            } else {
                logger.error("No removal errors occurred.");
            }

            if (FailedDownloads.size() > 0) {
                logger.error("Download errors: " + FailedDownloads.size());
                System.out.println("> Download errors: " + FailedDownloads.size());
            } else {
                logger.error("No download errors occurred.");
            }

            if (NullMods > 0) {
                logger.error("Mods without any data: " + NullMods);
                System.out.println("> Mods without any data: " + NullMods);
            } else {
                logger.error("No Null mods found.");
            }

            System.out.println("For more details, check log file at " + logger.getLogPath());
            logger.error("Files that failed verification with an exception:");

            if (FailedVerifications.size() > 0) {
                for (String FileName : FailedVerifications) {
                    logger.error("  " + FileName);
                }
            } else {
                logger.error(" None \\o/");
            }

            logger.error("Files that weren't possible to remove:");
            if (FailedRemovals.size() > 0) {
                for (String FileName : FailedRemovals) {
                    logger.error("  " + FileName);
                }
            } else {
                logger.error(" None \\o/");
            }

            logger.error("Files that failed to Download:");
            if (FailedDownloads.size() > 0) {
                for (String FileName : FailedDownloads) {
                    logger.error("  " + FileName);
                }
            } else {
                logger.error(" None \\o/");
            }

        } else {
            logger.print("Finished syncing profile!");
        }
    }
}

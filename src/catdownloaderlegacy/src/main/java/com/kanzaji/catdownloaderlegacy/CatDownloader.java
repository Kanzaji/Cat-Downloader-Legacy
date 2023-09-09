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
import com.kanzaji.catdownloaderlegacy.data.MRIndex;
import com.kanzaji.catdownloaderlegacy.exceptions.FormatVersionMismatchException;
import com.kanzaji.catdownloaderlegacy.guis.GUIUtils;
import com.kanzaji.catdownloaderlegacy.data.CFMinecraftInstance;
import com.kanzaji.catdownloaderlegacy.loggers.LoggerCustom;
import com.kanzaji.catdownloaderlegacy.utils.*;

import com.google.gson.Gson;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;


/**
 * Main class holding Global variables for the app and containing the main method.
 * @see CatDownloader#main(String[])
 */
public final class CatDownloader {
    // Launch fresh instances of required utilities.
    private static final LoggerCustom logger = new LoggerCustom("Main");
    private static final Gson gson = new Gson();
    static final ArgumentDecoder ARD = ArgumentDecoder.getInstance();

    // Global variables
    public static final String VERSION = "2.1.3";
    public static final String REPOSITORY = "https://github.com/Kanzaji/Cat-Downloader-Legacy";
    public static final String NAME = "Cat Downloader Legacy";
    /**
     * Path to the java environment running the app.
     */
    public static Path JAVAPATH = null;
    /**
     * Path to the .jar containing the app.
     */
    public static Path APPPATH = null;
    /**
     * Path to the working directory.
     */
    public static Path WORKPATH = null;
    /**
     * Arguments passed to the app.
     */
    public static String[] ARGUMENTS = null;

    // Locally used variables
    /**
     * Path to the manifest file used by the app.
     */
    public static Path manifestFile;
    /**
     * Used to hold data for the SyncManager and manifest parsing.
     */
    private static final CDLInstance CDLInstanceData = CDLInstance.create();

    /**
     * Main method of the app.
     * @param args String[] arguments for the app.
     */
    public static void main(String[] args) {
        long StartingTime = System.currentTimeMillis();
        ARGUMENTS = args;

        try {
            Services.init();
            Services.postInit();

            determineAppMode();

            verifyAndPrepareWorkspace();
            System.out.println("---------------------------------------------------------------------");

            fetchAndVerifyManifestFile();

            new SyncManager(CDLInstanceData).runSync();

            createCacheFile();

            logger.print("Entire Process took " + (float) (System.currentTimeMillis() - StartingTime) / 1000F + "s");
            RandomUtils.closeTheApp(0);
        } catch (Exception | Error e) {
            System.out.println("---------------------------------------------------------------------");
            System.out.println("CatDownloader crashed! More details are in the log file at \"" + logger.getLogPath() + "\".");
            logger.logStackTrace("Exception thrown while executing main app code!", e);
            RandomUtils.closeTheApp(1);
        }
    }

    /**
     * Used to determine what mode the app should run in. Handles automatic mode detection.
     * @throws RuntimeException when unsupported mode is returned from the ARD.
     */
    private static void determineAppMode() {
        if (ARD.isAutomaticModeDetectionActive()) {
            logger.log("Trying to automatically determine required mode for the app...");
            Map<String, String> supportedFiles = new HashMap<>();
            // "file", "mode"
            // #r at the beginning -> regex searching. Requires special handling in the decoding phase for each mode.
            supportedFiles.put("modrinth.index.json", "modrinth");
            supportedFiles.put("minecraftinstance.json", "cf-instance");
            supportedFiles.put("manifest.json", "cf-pack");
            supportedFiles.put("#r.mrpack", "modrinth");
            try (Stream<Path> WorkPathDir = Files.list(WORKPATH)) {
                WorkPathDir.forEach((File) -> {
                    if (Files.isDirectory(File)) return;
                    String fileName = File.getFileName().toString();
                    supportedFiles.forEach((supportedFile, mode) -> {
                        if (!ARD.isAutomaticModeDetectionActive()) return;
                        if (supportedFile.startsWith("#r")?
                                fileName.contains(supportedFile.substring(2)):
                                fileName.equals(supportedFile)
                        ) {
                            logger.log("Found compatible manifest file in the working directory!");
                            logger.log("Manifest file => " + File.toAbsolutePath());
                            logger.log("App will be running in \"" + mode + "\" mode.");
                            manifestFile = File;
                            ARD.setCurrentMode(mode);
                        }
                    });
                });
            } catch (Exception e) {
                logger.logStackTrace("Failed to automatically determine the mode with an exception. Defaulting to CF Instance mode.", e);
                manifestFile = Path.of(WORKPATH.toAbsolutePath().toString(), "minecraftinstance.json");
                ARD.setCurrentMode("cf-instance");
            }

            if (ARD.isAutomaticModeDetectionActive() || Files.notExists(manifestFile)) {
                logger.print("Couldn't find any compatible manifest file in the working directory.", 1);
                RandomUtils.closeTheApp(0);
            }

        } else {
            switch (ARD.getCurrentMode()) {
                case "modrinth" -> manifestFile = Path.of(WORKPATH.toAbsolutePath().toString(), "modrinth.index.json");
                case "cf-instance" -> manifestFile = Path.of(WORKPATH.toAbsolutePath().toString(), "minecraftinstance.json");
                case "cf-pack" -> {
                    System.out.println("CurseForge site format support is experimental! Use at your own responsibility.");
                    manifestFile = Path.of(WORKPATH.toAbsolutePath().toString(), "manifest.json");
                }
                default -> throw new RuntimeException("Unknown mode passed mode validation step! This shouldn't happen. Mode -> " + ARD.getCurrentMode());
            }

             if (Files.notExists(manifestFile)) {
                logger.print("Couldn't find the manifest file for a chosen mode in the working directory!", 1);
                RandomUtils.closeTheApp(0);
            }
        }
        System.out.println("App is running in \"" + ARD.getCurrentMode() + "\" mode.");
    }

    /**
     * Used to prepare working directory for the operation of the app.
     * <h2>Order of operations</h2>
     * <ul>
     *     <li>Checks if Mods file does not exists</li>
     *     <li>Checks if Mods folder Exists, if not, creates one.</li>
     *     <li>Checks if the app mode is Modrinth, and if the Manifest file is .mrpack file.</li>
     *     <li>If above step is true, repeats first two steps for CDLTemp directory, and unzips the .mrpack file.</li>
     *     <li>Changes manifest file to point to the Temp directory</li>
     * </ul>
     * @throws IOException when IO Exception occurs.
     */
    private static void verifyAndPrepareWorkspace() throws IOException {
        Path ModsFolder = Path.of(WORKPATH.toAbsolutePath().toString(), "mods");

        if(Files.exists(ModsFolder) && !Files.isDirectory(ModsFolder)) {
            System.out.println("---------------------------------------------------------------------");
            logger.print("Folder \"mods\" exists, but it is a file!", 3);
            RandomUtils.closeTheApp(1);
        }

        if(Files.notExists(ModsFolder)) {
            logger.warn("Folder \"mods\" is missing. Creating...");
            Files.createDirectory(ModsFolder);
            logger.log("Created \"mods\" folder in working directory. Path: " + ModsFolder.toAbsolutePath());
        } else {
            logger.log("Found \"mods\" folder in working directory. Path: " + ModsFolder.toAbsolutePath());
        }

        if (ARD.isModrinthMode() && manifestFile.getFileName().toString().endsWith(".mrpack")) {
            logger.log("Manifest file is a Modrinth zip file! Uncompressing...");
            Path CDLTemp = Path.of(WORKPATH.toString(), "CDLTemp");
            FileUtils.delete(CDLTemp);
            FileUtils.unzip(manifestFile, CDLTemp);
            manifestFile = Path.of(CDLTemp.toAbsolutePath().toString(), "modrinth.index.json");
        }
    }

    /**
     * This method is responsible for fetching and verifying the Manifest file.
     */
    private static void fetchAndVerifyManifestFile() {
        logger.log("Fetching data from the manifest file and translating it to CDLInstance Format...");
        try {
            switch (ARD.getCurrentMode()) {
                case "modrinth" -> CDLInstanceData.importModrinthPack(gson.fromJson(Files.readString(manifestFile), MRIndex.class));
                case "cf-instance" -> CDLInstanceData.importCFInstance(gson.fromJson(Files.readString(manifestFile), CFMinecraftInstance.class));
                case "cf-pack" -> CDLInstanceData.importCFPack(gson.fromJson(Files.readString(manifestFile), CFManifest.class), false);
                default -> throw new RuntimeException("Unknown mode passed mode validation step! This shouldn't happen. Mode -> " + ARD.getCurrentMode());
            }
        } catch (Exception e) {
            System.out.println("Failed to parse data from the manifest.");
            logger.logStackTrace("Failed to parse or interpret Manifest File.", e);
            RandomUtils.closeTheApp(1);
        }

        logger.log("Data fetched successfully.");

        parseCachedInstanceFile();

        logger.print("Installing modpack " +
            CDLInstanceData.modpackData.name +
            ((CDLInstanceData.modpackData.name.endsWith(" "))? "": " ") +
            ((
                Objects.equals(CDLInstanceData.modpackData.version, "") ||
                Objects.equals(CDLInstanceData.modpackData.version, " ") ||
                Objects.equals(CDLInstanceData.modpackData.version, null)
            )? "": CDLInstanceData.modpackData.version + " ") +
            ((
                Objects.equals(CDLInstanceData.modpackData.author, "") ||
                Objects.equals(CDLInstanceData.modpackData.author, " ") ||
                Objects.equals(CDLInstanceData.modpackData.author, null)
            )? "": "created by " + CDLInstanceData.modpackData.author)
        );

        if (!(
                Objects.equals(CDLInstanceData.modpackData.summary, "") ||
                Objects.equals(CDLInstanceData.modpackData.summary, " ") ||
                Objects.equals(CDLInstanceData.modpackData.summary, null)
        )) {
            System.out.println("\"" + CDLInstanceData.modpackData.summary + "\"");
        }

        if (Objects.equals(CDLInstanceData.modLoaderData.modLoader, "unknown")) {
            logger.print("Using Minecraft " + CDLInstanceData.minecraftData.version + " with possibly unknown mod loader.");
        } else {
            logger.print(
                "Using mod loader " +
                Character.toUpperCase(CDLInstanceData.modLoaderData.modLoader.charAt(0)) +
                CDLInstanceData.modLoaderData.modLoader.substring(1) +
                " " +
                CDLInstanceData.modLoaderData.version +
                " for Minecraft " +
                CDLInstanceData.minecraftData.version
            );
        }
        logger.log("Mods found in the manifest file: " + CDLInstanceData.files.length + ((CDLInstanceData.files.length == 1)? " mod": " mods."));
        logger.log("Instance name: " + CDLInstanceData.instanceName);

        if (CDLInstanceData.files.length < 1) {
            System.out.println("---------------------------------------------------------------------");
            logger.print("It appears that this instance doesn't have any mods!");
            RandomUtils.closeTheApp(0);
        }

    }

    /**
     * Used to parse Cached Instance File and fill missing hash values for the main CDLInstanceData.
     * @apiNote This method is CDL exclusive! Instance files are going to be used properly in the launcher version.
     */
    private static void parseCachedInstanceFile() {
        if (!ARD.isCacheEnabled() || ARD.isPackMode()) {
            if (ARD.isPackMode()) {
                logger.warn("Caches are not-available in the CF-Pack mode! Looking for cached versions of the CDLInstance will be skipped.");
            } else {
                logger.warn("Caches are disabled! Looking for cached version of the CDLInstance will be skipped.");
            }
            return;
        }

        logger.log("Looking for cached version of the CDLInstance...");
        try {
            Path cachedPath = Path.of(ARD.getCachePath(), "CDL-Instance-cache.json");
            if (Files.exists(cachedPath)) {
                CDLInstance cachedCDLInstance = CDLInstance.parseJson(cachedPath);

                if (!CDLInstanceData.equals(cachedCDLInstance, true)) {
                    FileUtils.delete(cachedPath);
                    if (CDLInstanceData.cdlFormatVersion.equals(cachedCDLInstance.cdlFormatVersion)) {
                        throw new IllegalArgumentException("Cached CDLInstance json isn't for the pack currently being installed, or the details for the pack has changed. Cache file will be regenerated at the end of the sync process.");
                    } else {
                        throw new FormatVersionMismatchException("Cached CDLInstance json is different version than currently supported! Cache file will be regenerated at the end of the sync process.");
                    }
                }

                int length = cachedCDLInstance.files.length;

                // Dropping any mods that aren't present in the current Instance data.
                cachedCDLInstance.files = Arrays.stream(cachedCDLInstance.files).filter(
                    (cachedFile) -> Arrays.stream(CDLInstanceData.files).anyMatch(
                        (file) ->
                            Objects.equals(cachedFile.fileLength, file.fileLength) &&
                            Objects.equals(cachedFile.fileName, file.fileName) &&
                            ((Objects.isNull(file.path))? Objects.equals(cachedFile.path, "mods/" + file.fileName): Objects.equals(cachedFile.path, file.path)) &&
                            Objects.equals(cachedFile.downloadURL, file.downloadURL)
                        )
                ).toList().toArray(new CDLInstance.ModFile[]{});

                int removedCount = length - cachedCDLInstance.files.length;

                logger.log("Removed " + RandomUtils.intGrammar(removedCount,  " mod", " mods", true) + " from the cached instance file due to them missing from the main data set.");
                logger.log("Updating information for " + cachedCDLInstance.files.length + " out of " + RandomUtils.intGrammar(CDLInstanceData.files.length,  " mod.", " mods.", true));

                // Another try block because if something goes wrong here, it is not safe to continue execution.
                try {
                    for (CDLInstance.ModFile file : CDLInstanceData.files) {
                        for (CDLInstance.ModFile cachedFile: cachedCDLInstance.files) {
                            if (
                                Objects.nonNull(cachedFile) &&
                                Objects.equals(file.fileLength, cachedFile.fileLength) &&
                                Objects.equals(file.fileName, cachedFile.fileName) &&
                                ((Objects.isNull(file.path))? Objects.equals(cachedFile.path, "mods/" + file.fileName): Objects.equals(cachedFile.path, file.path)) &&
                                Objects.equals(file.downloadURL, cachedFile.downloadURL)
                            ) {
                                file.hashes = cachedFile.hashes;
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                    throw new IllegalStateException("Exception thrown while updating hash information of the main data set. Execution can't continue.", e);
                }
            } else {
                logger.log("Couldn't find cached version of the CDLInstance. Verification will be performed from the source.");
            }
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            logger.logStackTrace("Exception thrown while looking for cached version of CDLInstance! Verification will be performed from the source.", e);
        }
    }

    /**
     * Used to create Cache file based on the information that was acquired by the app at the runtime.
     * Increases the speed of the instance verification process.
     */
    private static void createCacheFile() {
        if (!ARD.isCacheEnabled() || ARD.isPackMode()) {
            if (ARD.isPackMode()) {
                logger.warn("Caches are not-available in the CF-Pack mode! Cache file is not going to be generated in this session.");
            } else {
                logger.warn("Caches are disabled! Cache file is not going to be generated in this session.");
            }
            return;
        }

        try {
            Path cachedPath = Path.of(ARD.getCachePath(), "CDL-Instance-cache.json");
            if (Files.notExists(cachedPath)) {
                logger.log("Cache file not found! Creating required path for the Cache file...");
                FileUtils.createRequiredPathToAFile(cachedPath);
                Files.createFile(cachedPath);
            }

            logger.log("Saving cache data...");
            Files.writeString(cachedPath, CDLInstanceData.toString(), StandardOpenOption.TRUNCATE_EXISTING);
            logger.log("Cache data has been saved.");
        } catch (Exception e) {
            logger.logStackTrace("Exception thrown while saving Cache data!", e);
            try {
                Files.deleteIfExists(Path.of(ARD.getCachePath(), "CDL-Instance-cache.json"));
            } catch (Exception e2) {
                logger.logStackTrace("Exception thrown while deleting cache file after main exception! Was the original exception IO Error? Is the path write-protected?", e);
            }
        }
    }

    private static class Services {
        private static final LoggerCustom logger = new LoggerCustom("Main.Services");

        /**
         * This method is used to initialize requires services, utilities and global "utility" variables.
         * @throws IOException when IO Exception occurs.
         */
        public static void init() throws IOException {
            logger.init();
            logger.log(NAME + " version: " + VERSION);
            try {
                APPPATH = Path.of(CatDownloader.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath().replaceFirst("/", ""));
                logger.log("App Path: " + APPPATH.toAbsolutePath());
            } catch (Exception e) {
                logger.logStackTrace("Failed to get App directory!", e);
            }
            try {
                JAVAPATH = Path.of(ProcessHandle.current().info().command().orElseThrow());
                logger.log("Java Path: " + JAVAPATH.toAbsolutePath());
            } catch (Exception e) {
                logger.logStackTrace("Failed to get Java directory!", e);
            }

            GUIUtils.setLookAndFeel();
            // All arguments should be decoded in the ARD.
            // However, this method Overrides arguments, so it is required to run before ARD decoding.
            if (Arrays.stream(ARGUMENTS).toList().contains("-PostUpdateRoutine")) Updater.updateCleanup();

            System.out.println("---------------------------------------------------------------------");
            System.out.println("     " + NAME + " " + VERSION);
            System.out.println("     Created by: Kanzaji");
            System.out.println("---------------------------------------------------------------------");

            ARD.decodeArguments(ARGUMENTS);
            ARD.printConfiguration("Program Configuration from Arguments:");

            if (ARD.areSettingsEnabled()) SettingsManager.initSettings();

            WORKPATH = Path.of(ARD.getWorkingDir());
            System.out.println("Running in " + WORKPATH.toAbsolutePath());
        }

        /**
         * This method is used to launch all post-init methods of services.
         * @throws IOException when IO Exception occurs.
         */
        public static void postInit() throws IOException {
            logger.postInit();

            logger.log("Checking network connection...");
            if (ARD.isBypassNetworkCheckActive()) {
                logger.warn("Network Bypass active! Be aware, Un-intended behaviour due to missing network connection is possible!");
            } else {
                long StartingTime = System.currentTimeMillis();
                if (NetworkingUtils.checkConnection("https://github.com/")) {
                    float CurrentTime = (float) (System.currentTimeMillis() - StartingTime) / 1000F;
                    logger.log("Network connection checked! Time to verify network: " + CurrentTime + " seconds.");
                    if (CurrentTime > 2) {
                        logger.print("It appears you have slow network connection! This might or might not cause issues with Verification or Download steps. Use with caution.", 1);
                    }
                } else {
                    logger.critical("No network connection! This app can not run properly without access to the internet.");
                    System.out.println("It appears you are running this app without access to the internet. This app requires internet connection to function properly.");
                    System.out.println("If you have network connection, and the Check host is unavailable (github.com), run the app with -BypassNetworkCheck argument!");
                    RandomUtils.closeTheApp(2);
                }
            }

            Updater.checkForUpdates();

            // Redirects the entire output of any Logger to a console!
            if (!ARD.isLoggerActive()) logger.exit();
        }
    }
}
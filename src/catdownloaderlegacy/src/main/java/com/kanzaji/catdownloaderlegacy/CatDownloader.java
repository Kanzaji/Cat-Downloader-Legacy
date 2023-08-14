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

import com.kanzaji.catdownloaderlegacy.data.CFManifest;
import com.kanzaji.catdownloaderlegacy.guis.GUIUtils;
import com.kanzaji.catdownloaderlegacy.data.CFMinecraftInstance;
import com.kanzaji.catdownloaderlegacy.loggers.LoggerCustom;
import com.kanzaji.catdownloaderlegacy.utils.*;

import com.google.gson.Gson;

import java.io.IOException;
import java.nio.file.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;


/**
 * Main class holding Global variables for the app and containing the main method.
 * @see CatDownloader#main(String[])
 */
public final class CatDownloader {
    // Launch fresh instances of required utilities.
    private static final LoggerCustom logger = new LoggerCustom("Main");
    static final ArgumentDecoder ARD = ArgumentDecoder.getInstance();

    // Global variables
    public static final String VERSION = "2.0.1-DEVELOP";
    public static final String REPOSITORY = "https://github.com/Kanzaji/Cat-Downloader-Legacy";
    public static final String NAME = "Cat Downloader Legacy";
    public static Path JAVAPATH = null;
    public static Path APPPATH = null;
    public static String[] ARGUMENTS = null;

    // Locally used variables
    public static Path manifestFile;
    private static CFManifest CFManifestData = new CFManifest();
    public static List<Runnable> dataGatheringFails = new LinkedList<>();
    
    //TODO: Simplify Main Method

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

            Path workingDirectory = Path.of(ARD.getWorkingDir());
            System.out.println("Running in " + workingDirectory.toAbsolutePath());

            if (ARD.isPackMode()) {
                System.out.println("CurseForge site format support is experimental! Use at your own responsibility.");
                manifestFile = Path.of(workingDirectory.toAbsolutePath().toString(), "manifest.json");
            } else {
                manifestFile = Path.of(workingDirectory.toAbsolutePath().toString(), "minecraftinstance.json");
            }

            if (!manifestFile.toFile().exists()) {
                String msg = null;

                if (ARD.isPackMode()) {
                    if (Files.exists(Path.of(workingDirectory.toAbsolutePath().toString(), "minecraftinstance.json"))) {
                        msg = "Couldn't find required Manifest file, however it appears `minecraftinstance.json` exists in the current working directory." +
                                "Did you mean to run the app in \"Instance\" mode?";
                    }
                } else if (Files.exists(Path.of(workingDirectory.toAbsolutePath().toString(), "manifest.json"))) {
                    msg = "Couldn't find required Manifest file, however it appears `manifest.json` exists in the current working directory." +
                            "Did you mean to run the app in \"Pack\" mode?";
                } else {
                    msg = "Couldn't find Manifest file in the working directory!";
                }

                logger.print(msg, 2);
                RandomUtils.closeTheApp(1);
            }

            Gson gson = new Gson();
            logger.log("Reading data from Manifest file...");
            if (ARD.isPackMode()) {
                CFManifestData = gson.fromJson(Files.readString(manifestFile), CFManifest.class);
            } else {
                // Translating from MinecraftInstance format to Manifest format.
                CFMinecraftInstance MI = gson.fromJson(Files.readString(manifestFile), CFMinecraftInstance.class);
                CFManifestData = MIInterpreter.decode(MI);
            }

            logger.log("Data fetched. Found " + CFManifestData.files.length + " Mods, on version " + CFManifestData.minecraft.version + " " + CFManifestData.minecraft.modLoaders[0].id);

            if (CFManifestData.name == null) {
                logger.print("The name of the instance is missing!", 1);
            } else {
                System.out.println("Installing modpack " +
                    CFManifestData.name +
                    ((CFManifestData.name.endsWith(" "))? "": " ") +
                    CFManifestData.version +
                    ((
                        Objects.equals(CFManifestData.author, "") ||
                        Objects.equals(CFManifestData.author, " ") ||
                        Objects.equals(CFManifestData.author, null)
                    )? "": " created by " + CFManifestData.author)
                );

                logger.log("Instance name: " + CFManifestData.name);
            }

            logger.log("Minecraft version: " + CFManifestData.minecraft.version);

            if (CFManifestData.minecraft.modLoaders[0].id == null) {
                System.out.println("For Minecraft " + CFManifestData.minecraft.version + " Vanilla");
                logger.warn("No mod loader found! Is this vanilla?");
            } else {
                System.out.println("For Minecraft " + CFManifestData.minecraft.version + " using " + CFManifestData.minecraft.modLoaders[0].id);
                logger.log("Mod Loader: " + CFManifestData.minecraft.modLoaders[0].id);
            }

            System.out.println("---------------------------------------------------------------------");

            Path ModsFolder = Path.of(workingDirectory.toAbsolutePath().toString(), "mods");
            if(ModsFolder.toFile().exists() && !ModsFolder.toFile().isDirectory()) {
                logger.print("Folder \"mods\" exists, but it is a file!", 2);
                RandomUtils.closeTheApp(1);
            }

            if(!ModsFolder.toFile().exists()) {
                logger.log("Folder \"mods\" is missing. Creating...");
                Files.createDirectory(ModsFolder);
                logger.log("Created \"mods\" folder in working directory. Path: " + workingDirectory.toAbsolutePath() + "\\mods");
            } else {
                logger.log("Found \"mods\" folder in working directory. Path: " + workingDirectory.toAbsolutePath() + "\\mods");
            }

            if (CFManifestData.files == null || CFManifestData.files.length == 0) {
                logger.print("Manifest files does not have any mods in it! No job for me :D",2);
                RandomUtils.closeTheApp(0);
            }

            System.out.println("Found " + CFManifestData.files.length + " " + ((CFManifestData.files.length == 1)? "mod":"mods") +" in Manifest file!");

            if (ARD.isPackMode()) {
                logger.print("Gathering Data about mods... This may take a while.");
                ExecutorService Executor, FailExecutor;
                if (ARD.isExperimental()) {
                    logger.warn("Experimental mode for CurseForge support turned on! This may cause unexpected behaviour and issues with data gathering process.");
                    logger.warn("Use at your own risk! Try to not over-use it.");
                    Executor = Executors.newFixedThreadPool(ARD.getThreads());
                    FailExecutor = Executors.newFixedThreadPool(ARD.getThreads()/4);
                } else {
                    // If without Experimental there are issues with Data Gathering process, this is to blame.
                    Executor = Executors.newFixedThreadPool(2);
                    FailExecutor = Executors.newFixedThreadPool(1);
                }

                int Index = 0;
                for (CFManifest.ModFile mod : CFManifestData.files) {
                    int finalIndex = Index;
                    Executor.submit(() -> {
                        CFManifestData.files[finalIndex] = mod.getData(CFManifestData.minecraft);
                        if (CFManifestData.files[finalIndex] != null && (CFManifestData.files[finalIndex].error202 || CFManifestData.files[finalIndex].error403)) {
                            mod.error403 = CFManifestData.files[finalIndex].error403;
                            mod.error202 = CFManifestData.files[finalIndex].error202;
                            dataGatheringFails.add(() -> CFManifestData.files[finalIndex] = mod.getData(CFManifestData.minecraft));
                        }
                    });
                    Index += 1;
                }

                Executor.shutdown();
                if (!Executor.awaitTermination(1, TimeUnit.DAYS)) {
                    logger.print("Data gathering takes over a day! This for sure isn't right???",3);
                    throw new RuntimeException("Data gathering is taking over a day! Something is horribly wrong.");
                }

                if (dataGatheringFails.size() > 0) {
                    logger.warn("Data gathering errors present! Trying to re-run unsuccessful data requests. Errors present: " + dataGatheringFails.size());
                    dataGatheringFails.forEach(FailExecutor::submit);
                }

                FailExecutor.shutdown();
                if (!FailExecutor.awaitTermination(1, TimeUnit.DAYS)) {
                    logger.print("Data gathering takes over a day! This for sure isn't right???",3);
                    throw new RuntimeException("Data gathering is taking over a day! Something is horribly wrong.");
                }

                logger.print("Finished gathering data!");
            }

            new SyncManager(ModsFolder, CFManifestData, ARD.getThreads()).runSync();

            logger.print("Entire Process took " + (float) (System.currentTimeMillis() - StartingTime) / 1000F + "s");
            RandomUtils.closeTheApp(0);
        } catch (Exception | Error e) {
            System.out.println("CatDownloader crashed! More details are in the log file at \"" + logger.getLogPath() + "\".");
            logger.logStackTrace("Exception thrown while executing main app code!", e);
            RandomUtils.closeTheApp(1);
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
                JAVAPATH = Path.of(ProcessHandle.current().info().command().orElseThrow());
                logger.log("Java Path: " + JAVAPATH.toAbsolutePath());
            } catch (Exception e) {
                logger.logStackTrace("Failed to get App or Java directory!", e);
            }

            GUIUtils.setLookAndFeel();
            // All arguments should be decoded in the ARD, however this method Overrides arguments, so it is required to run before ARD decoding.
            if (Arrays.stream(ARGUMENTS).toList().contains("-PostUpdateRoutine")) Updater.updateCleanup();

            System.out.println("---------------------------------------------------------------------");
            System.out.println("     " + NAME + " " + VERSION);
            System.out.println("     Created by: Kanzaji");
            System.out.println("---------------------------------------------------------------------");

            ARD.decodeArguments(ARGUMENTS);
            ARD.printConfiguration("Program Configuration from Arguments:");

            if (ARD.areSettingsEnabled()) SettingsManager.initSettings();
        }

        /**
         * This method is used to launch all post-init methods of services.
         * @throws IOException when IO Exception occurs.
         */
        public static void postInit() throws IOException {
            logger.postInit();

            if (!NetworkingUtils.checkConnection("https://github.com/") && !ARD.isBypassNetworkCheckActive()) {
                System.out.println("It appears you are running this app without access to the internet. This app requires internet connection to function properly.");
                System.out.println("If you have network connection, and the Check host is unavailable (github.com), run the app with -BypassNetworkCheck argument!");
                RandomUtils.closeTheApp(1);
            }

            Updater.checkForUpdates();

            // Redirects entire output of any Logger to a console!
            if (!ARD.isLoggerActive()) logger.exit();
        }
    }
}
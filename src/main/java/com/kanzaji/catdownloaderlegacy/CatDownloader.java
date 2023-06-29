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
import com.kanzaji.catdownloaderlegacy.jsons.MinecraftInstance;
import com.kanzaji.catdownloaderlegacy.loggers.LoggerCustom;
import com.kanzaji.catdownloaderlegacy.utils.*;

import com.google.gson.Gson;

import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;

public final class CatDownloader {
    // Launch fresh instances of required utilities.
    private static final LoggerCustom logger = new LoggerCustom("Main");
    private static final ArgumentDecoder ARD = ArgumentDecoder.getInstance();

    // Global variables
    public static final String VERSION = "2.0-DEVELOP";
    public static final String REPOSITORY = "https://github.com/Kanzaji/Cat-Downloader-Legacy";
    public static final String NAME = "Cat Downloader Legacy";
    public static Path APPPATH = null;

    // Locally used variables
    public static Path manifestFile;
    private static Manifest ManifestData = new Manifest();
    public static List<Runnable> dataGatheringFails = new LinkedList<>();

    //TODO: Add a bit more documentation.
    // What I mean is add docs to the classes (because some of them have it and some don't) and add links etc to all docs that are currently live.
    // Trust me future Kanz, IT WILL BE WORTH IT.
    public static void main(String[] args) {
        long StartingTime = System.currentTimeMillis();

        logger.init();
        logger.log(NAME + " version: " + VERSION);
        try {
            APPPATH = Path.of(Updater.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath().replaceFirst("/", ""));
            logger.log("App path: " + APPPATH.toAbsolutePath());
        } catch (URISyntaxException e) {
            logger.logStackTrace("Failed to get App directory!", e);
        }

        try {
            System.out.println("---------------------------------------------------------------------");
            System.out.println("     " + NAME + " " + VERSION);
            System.out.println("     Created by: Kanzaji");
            System.out.println("---------------------------------------------------------------------");

            ARD.decodeArguments(args);
            ARD.printConfiguration("Program Configuration from Arguments:");

            if (ARD.areSettingsEnabled()) SettingsManager.initSettings();

            logger.postInit();

            // Redirects entire output to a console!
            if (!ARD.isLoggerActive()) logger.exit();

            if (Updater.checkUpdates()) {

            }

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
                System.exit(1);
            }

            Gson gson = new Gson();
            logger.log("Reading data from Manifest file...");
            if (ARD.isPackMode()) {
                ManifestData = gson.fromJson(Files.readString(manifestFile),Manifest.class);
            } else {
                // Translating from MinecraftInstance format to Manifest format.
                MinecraftInstance MI = gson.fromJson(Files.readString(manifestFile),MinecraftInstance.class);
                ManifestData = MIInterpreter.decode(MI);
            }

            logger.log("Data fetched. Found " + ManifestData.files.length + " Mods, on version " + ManifestData.minecraft.version + " " + ManifestData.minecraft.modLoaders[0].id);

            if (ManifestData.name == null) {
                logger.print("The name of the instance is missing!", 1);
            } else {
                System.out.println("Installing modpack " +
                    ManifestData.name +
                    ((ManifestData.name.endsWith(" "))? "": " ") +
                    ManifestData.version +
                    ((
                        Objects.equals(ManifestData.author, "") ||
                        Objects.equals(ManifestData.author, " ") ||
                        Objects.equals(ManifestData.author, null)
                    )? "": " created by " + ManifestData.author)
                );

                logger.log("Instance name: " + ManifestData.name);
            }

            logger.log("Minecraft version: " + ManifestData.minecraft.version);

            if (ManifestData.minecraft.modLoaders[0].id == null) {
                System.out.println("For Minecraft " + ManifestData.minecraft.version + " Vanilla");
                logger.warn("No mod loader found! Is this vanilla?");
            } else {
                System.out.println("For Minecraft " + ManifestData.minecraft.version + " using " + ManifestData.minecraft.modLoaders[0].id);
                logger.log("Mod Loader: " + ManifestData.minecraft.modLoaders[0].id);
            }

            System.out.println("---------------------------------------------------------------------");

            Path ModsFolder = Path.of(workingDirectory.toAbsolutePath().toString(), "mods");
            if(ModsFolder.toFile().exists() && !ModsFolder.toFile().isDirectory()) {
                logger.print("Folder \"mods\" exists, but it is a file!", 2);
                System.exit(1);
            }

            if(!ModsFolder.toFile().exists()) {
                logger.log("Folder \"mods\" is missing. Creating...");
                Files.createDirectory(ModsFolder);
                logger.log("Created \"mods\" folder in working directory. Path: " + workingDirectory.toAbsolutePath() + "\\mods");
            } else {
                logger.log("Found \"mods\" folder in working directory. Path: " + workingDirectory.toAbsolutePath() + "\\mods");
            }

            if (ManifestData.files == null || ManifestData.files.length == 0) {
                logger.print("Manifest files does not have any mods in it! No job for me :D",2);
                System.exit(0);
            }

            System.out.println("Found " + ManifestData.files.length + " " + ((ManifestData.files.length == 1)? "mod":"mods") +" in Manifest file!");

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
                for (Manifest.ModFile mod : ManifestData.files) {
                    int finalIndex = Index;
                    Executor.submit(() -> {
                        ManifestData.files[finalIndex] = mod.getData(ManifestData.minecraft);
                        if (ManifestData.files[finalIndex] != null && (ManifestData.files[finalIndex].error202 || ManifestData.files[finalIndex].error403)) {
                            mod.error403 = ManifestData.files[finalIndex].error403;
                            mod.error202 = ManifestData.files[finalIndex].error202;
                            dataGatheringFails.add(() -> ManifestData.files[finalIndex] = mod.getData(ManifestData.minecraft));
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

            // This shouldn't be a singleton, however it is good enough for now.
            SyncManager fm = SyncManager.getInstance();
            fm.passData(ModsFolder, ManifestData, ARD.getThreads());
            fm.runSync();

            logger.print("Entire Process took " + (float) (System.currentTimeMillis() - StartingTime) / 1000F + "s");
            logger.log("Cat-Downloader Legacy is created and maintained by Kanzaji! Find the source code and issue tracker here:");
            logger.log("https://github.com/Kanzaji/Cat-Downloader-Legacy");
            System.exit(0);
        } catch (Exception | Error e) {
            System.out.println("CatDownloader crashed! More details are in the log file at \"" + logger.getLogPath() + "\".");
            logger.logStackTrace("Something horrible happened...", e);
            logger.error("For bug reports and help with issues, go to my github at: https://github.com/Kanzaji/Cat-Downloader-Legacy");
            System.exit(1);
        }
    }
}
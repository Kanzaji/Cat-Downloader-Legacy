package com.kanzaji.catdownloaderlegacy;

import com.kanzaji.catdownloaderlegacy.jsons.Manifest;
import com.kanzaji.catdownloaderlegacy.jsons.MinecraftInstance;
import com.kanzaji.catdownloaderlegacy.utils.*;

import com.google.gson.Gson;
import com.kanzaji.catdownloaderlegacy.loggers.LoggerCustom;

import java.nio.file.*;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public final class CatDownloader {
    // Launch fresh instances of required utilities.
    private static final LoggerCustom logger = new LoggerCustom("Main");
    private static final ArgumentDecoder ARD = ArgumentDecoder.getInstance();

    // Some other variables
    public static final String VERSION = "1.1";
    public static Path manifestFile;
    private static Manifest ManifestData = new Manifest();
    private static boolean Legacy = false;

    public static void main(String[] args) {
        // Initialize Logger.
        logger.init();
        logger.log("Cat Downloader version: " + VERSION);

        try {
            // Decode Arguments and store them in ARD Instance.
            ARD.decodeArguments(args);

            // Printing out Argument / Configuration values.
            ARD.printConfiguration("Program Configuration from Arguments:");

            // Run Post-int on Logger.
            logger.postInit();

            // Turns off Logger if user wants it (NOT RECOMMENDED!!!!)
            // Redirects entire output to a console!
            if (!ARD.isLoggerActive()){
                logger.exit();
            }

            // "What the hell did I just run" section.
            System.out.println("---------------------------------------------------------------------");
            System.out.println("     Cat Downloader " + VERSION);
            System.out.println("     Created by: Kanzaji");
            System.out.println("---------------------------------------------------------------------");

            // Initialize SettingsManager
            if (ARD.areSettingsEnabled()) { SettingsManager.initSettings(); }

            // Setting directory where program was turned on
            Path dir = Path.of(ARD.getWorkingDir());
            System.out.println("Running in " + dir.toAbsolutePath());

            // Checking Program mode and getting required Manifest File.
            if (Objects.equals(ARD.getMode(), "pack")) {
                manifestFile = Path.of(dir.toAbsolutePath().toString(), "manifest.json");
            } else {
                manifestFile = Path.of(dir.toAbsolutePath().toString(), "minecraftinstance.json");
            }

            if (!manifestFile.toFile().exists()) {
                System.out.println("No Manifest file exists in this directory, aborting!");
                logger.error("Manifest file not found! Make sure you are running in correct mode.");
                System.exit(1);
            }

            // Parsing data from Manifest file.
            Gson gson = new Gson();
            logger.log("Reading data from Manifest file...");
            if (Objects.equals(ARD.getMode(), "instance")) {
                // Translating from MinecraftInstance format to Manifest format.
                MinecraftInstance MI = gson.fromJson(Files.readString(manifestFile),MinecraftInstance.class);
                ManifestData = MIInterpreter.decode(MI);
            } else {
                Legacy = true;
                ManifestData = gson.fromJson(Files.readString(manifestFile),Manifest.class);
            }

            logger.log("Data fetched. Found " + ManifestData.files.length + " Mods, on version " + ManifestData.minecraft.version + " " + ManifestData.minecraft.modLoaders[0].id);

            // Checking if Manifest contains modpack name.
            if (ManifestData.name == null) {
                System.out.println("manifest.json doesn't have modpack name!");
                logger.warn("The name of the instance is missing!");
            } else {
                System.out.println("Installing modpack: " + ManifestData.name + " " + ManifestData.version);
                logger.log("Instance name: " + ManifestData.name);
            }
            // Checking if Manifest file contains required modLoader.
            if (ManifestData.minecraft.modLoaders[0].id == null) {
                System.out.println("Manifest file doesn't have any mod loader specified! Is this vanilla?");
                logger.warn("This instance seems to be vanilla? No mod loader found!");
            } else {
                System.out.println("That requires ModLoader: " + ManifestData.minecraft.version + " " + ManifestData.minecraft.modLoaders[0].id);
                logger.log("Mod Loader: " + ManifestData.minecraft.modLoaders[0].id);
            }
            // Checking if Manifest has any mods.
            if (ManifestData.files == null || ManifestData.files.length == 0) {
                System.out.println("Manifest file doesn't have any mods in it!");
                logger.error("Manifest files does not have any mods in it. Is this intentional?");
                System.exit(0);
            }

            System.out.println("---------------------------------------------------------------------");

            System.out.println("Found " + ManifestData.files.length + " mods!");
            // If in Legacy mode, gather data from CFWidget API required for downloads. (THIS IS REALLY UNSTABLE).
            if (Legacy) {
                logger.log("Getting data for ids specified in the Manifest file...");
                System.out.println("Gathering Data about mods... This may take a while.");
                if (ARD.isExperimental()) {
                    logger.warn("EXPERIMENTAL MODE TURNED ON. USE ON YOUR OWN RISK!");
                    ExecutorService Executor = Executors.newFixedThreadPool(ARD.getThreads());
                    int Index = 0;
                    for (Manifest.ModFile mod : ManifestData.files) {
                        int finalIndex = Index;
                        Executor.submit(() -> {
                            ManifestData.files[finalIndex] = mod.getData(ManifestData.minecraft);
                        });
                        Index += 1;
                    }
                    Executor.shutdown();
                    if (!Executor.awaitTermination(1, TimeUnit.DAYS)) {
                        logger.error("Data gathering takes over a day! This for sure isn't right???");
                        System.out.println("Data gathering interrupted due to taking over a day! This for sure isn't right???");
                        throw new RuntimeException("Data gathering is taking over a day! Something is horribly wrong.");
                    }
                } else {
                    int Index = 0;
                    for (Manifest.ModFile mod : ManifestData.files) {
                        ManifestData.files[Index] = mod.getData(ManifestData.minecraft);
                        Index += 1;
                    }
                }
                logger.log("Finished gathering data!");
                System.out.println("Finished gathering data!");
            }

            // Checking if /mods directory exists and can be used
            Path ModsFolder = Path.of(dir.toAbsolutePath().toString(), "mods");
            if(ModsFolder.toFile().exists() && !ModsFolder.toFile().isDirectory()) {
                System.out.println("Folder \"mods\" exists, but it is a file!");
                logger.error("Folder \"mods\" exists, but it is a file!");
                System.exit(1);
            }

            if(!ModsFolder.toFile().exists()) {
                logger.log("Folder \"mods\" is missing. Creating...");
                Files.createDirectory(ModsFolder);
                logger.log("Created \"mods\" folder in working directory. Path: " + dir.toAbsolutePath() + "\\mods");
            } else {
                logger.log("Found \"mods\" folder in working directory. Path: " + dir.toAbsolutePath() + "\\mods");
            }

            // Getting FileManager ready and starting sync of the profile.
            SyncManager fm = SyncManager.getInstance();
            fm.passData(ModsFolder, ManifestData, ARD.getThreads());
            fm.runSync();
            logger.log("Cat-Downloader Legacy is created and maintained by Kanzaji! Find the source and issue tracker here:");
            logger.log("https://github.com/Kanzaji/Cat-Downloader-Legacy");
        } catch (Exception | Error e) {
            System.out.println("CatDownloader crashed! More details are in the log file at \"" + logger.getLogPath() + "\".");
            logger.logStackTrace("Something horrible happened...", e);
            logger.error("For bug reports and help with issues, go to my github at: https://github.com/Kanzaji/Cat-Downloader-Legacy");
            System.exit(1);
        }
    }
}
package com.kanzaji.catdownloaderlegacy;

import com.kanzaji.catdownloaderlegacy.jsons.Manifest;
import com.kanzaji.catdownloaderlegacy.jsons.MinecraftInstance;
import com.kanzaji.catdownloaderlegacy.utils.ArgumentDecoder;
import com.kanzaji.catdownloaderlegacy.utils.Logger;
import com.kanzaji.catdownloaderlegacy.utils.MIInterpreter;

import com.google.gson.Gson;

import java.nio.file.*;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public final class CatDownloader {
    public static final String VERSION = "DEVELOP";
    private static final Logger logger = Logger.getInstance();
    public static Path manifestFile;
    private static Manifest ManifestData = new Manifest();

    public static void main(String[] args) {
        logger.init();
        logger.log("Cat Downloader version: " + VERSION);

        try {
            // Initialize required Utilities.
            ArgumentDecoder ARD = ArgumentDecoder.getInstance();

            // Decode Arguments and store them in ARD Instance.
            ARD.decodeArguments(args);

            // Turns off Logger if user wants it (NOT RECOMMENDED!!!!)
            // Redirects entire output to a console!
            if (!Boolean.parseBoolean(ARD.getData("Logger"))){
                logger.exit();
            }

            // "What the hell did I just run" section.
            System.out.println("---------------------------------------------------------------------");
            System.out.println("     Cat Downloader " + VERSION);
            System.out.println("     Created by: Kanzaji");
            System.out.println("---------------------------------------------------------------------");

            // Setting directory where program was turned on
            Path dir = Path.of(ARD.getData("Wdir"));
            System.out.println("Running in " + dir.toAbsolutePath());

            // Printing out Argument values.
            logger.log("Working directory = " + ARD.getData("Wdir"));
            logger.log("Thread count for downloads = " + ARD.getData("Threads"));
            logger.log("Program Mode: " + ARD.getData("Mode"));

            // Checking Program mode and getting required Manifest File.
            if (Objects.equals(ARD.getData("Mode"), "Pack")) {
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
            if (Objects.equals(ARD.getData("Mode"), "Instance")) {
                // Translating from MinecraftInstance format to Manifest format.
                MinecraftInstance MI = gson.fromJson(Files.readString(manifestFile),MinecraftInstance.class);
                ManifestData = MIInterpreter.decode(MI);
            } else {
                logger.log("Getting data for ids specified in the Manifest file...");
                ManifestData = gson.fromJson(Files.readString(manifestFile),Manifest.class);
                if (Boolean.parseBoolean(ARD.getData("Experimental"))) {
                    ExecutorService Executor = Executors.newFixedThreadPool(Integer.parseInt(ARD.getData("Threads")));
                    for (Manifest.ModFile mod: ManifestData.files) {
                        Executor.submit(() -> {
                           mod.getData(ManifestData.minecraft);
                        });
                    }
                    Executor.shutdown();
                    if (Executor.awaitTermination(1, TimeUnit.DAYS)) {
                        logger.error("Data gathering takes over a day! This for sure isn't right???");
                        System.out.println("Data gathering interrupted due to taking over a day! This for sure isn't right???");
                        throw new RuntimeException("Data gathering is taking over a day! Something is horribly wrong.");
                    }
                } else {
                    for (Manifest.ModFile mod: ManifestData.files) {
                        mod.getData(ManifestData.minecraft);
                    }
                }
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

            // Some more info about modpack
            System.out.println("Found " + ManifestData.files.length + " mods!");

            // Checking if /mods directory exists and can be used
            Path ModsFolder = Path.of(dir.toAbsolutePath().toString(), "mods");
            if(ModsFolder.toFile().exists() && !ModsFolder.toFile().isDirectory()) {
                System.out.println("Folder\"mods\" exists, but it is a file!");
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
            fm.passData(ModsFolder, ManifestData, Integer.parseInt(ARD.getData("Threads")));
            fm.runSync();
        } catch (Exception | Error e) {
            System.out.println("CatDownloader crashed! More details are in the log file at \"" + logger.getLogPath() + "\".");
            logger.logStackTrace("Something horrible happened...", e);
            System.exit(1);
        }
    }
}
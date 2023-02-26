package com.kanzaji.catdownloader;

import com.kanzaji.catdownloader.jsons.Manifest;
import com.kanzaji.catdownloader.jsons.MinecraftInstance;
import com.kanzaji.catdownloader.utils.Logger;
import com.google.gson.Gson;
import com.vazkii.instancesync.DownloadManager;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public final class CatDownloader {

    public static final String VERSION = "DEVELOP";
    public static String WorkingDirectory = ".";
    public static String Mode = "Pack";
    public static Path manifestFile;

    public static void main(String[] args) {
        Logger logger = Logger.getInstance();
        logger.init();
        logger.log("Cat Downloader version: " + VERSION);

        try {
            // "What the hell did I just run" section
            System.out.println("---------------------------------------------------------------------");
            System.out.println("     Cat Downloader " + VERSION);
            System.out.println("     Created by: Kanzaji");
            System.out.println("---------------------------------------------------------------------");
            logger.log("Running with arguments:");
            for (String argument : args) {
                logger.log(argument);
                if (argument.startsWith("-WorkingDirectory:")) {
                    WorkingDirectory = argument.substring(18);
                }
                if (argument.startsWith("-Mode:")) {
                    Mode = argument.substring(6);
                }
            }
            // Setting directory where program was turned on
            logger.log("Working directory = " + WorkingDirectory);
            Path dir = Path.of(WorkingDirectory);
            System.out.println("Running in " + dir.toAbsolutePath());
            
            // Checking mode and finding required file in each mode
            if (!Objects.equals(Mode, "Pack") && !Objects.equals(Mode, "Instance")) {
                logger.error("Wrong mode selected!" + Mode);
                logger.error("Available modes: Pack // Instance");
                logger.error("Check Github https://github.com/Kanzaji/Cat-Downloader-Legacy for more explanation!");
                System.exit(1);
            }

            logger.log("Program Mode: " + Mode);

            if (Objects.equals(Mode, "Pack")) {
                manifestFile = Path.of(dir.toAbsolutePath().toString(), "manifest.json");
            } else {
                manifestFile = Path.of(dir.toAbsolutePath().toString(), "minecraftinstance.json");
            }

            if (!manifestFile.toFile().exists()) {
                System.out.println("No Manifest file exists in this directory, aborting!");
                logger.error("Manifest file not found! Make sure you are running in correct mode.");
                System.exit(1);
            }

            // Getting data from manifest file
            Gson gson = new Gson();
            try {
                // TODO: Fix this to actually translate installedAddons to manifest.files... I have no fucking idea how to do that.
                Manifest manifest = gson.fromJson("{}",Manifest.class);
                if (Objects.equals(Mode, "Instance")) {
                    MinecraftInstance MI = gson.fromJson(Files.readString(manifestFile),MinecraftInstance.class);
                    manifest.version = "";
                    manifest.name = MI.name;
                    manifest.minecraft = gson.fromJson("{\"version\":\"" + MI.baseModLoader.minecraftVersion + "\",\"modLoaders\": [{\"id\":" + MI.baseModLoader.name + "\"}]}",Manifest.minecraft.class);
                    int index = 0;
                    for (MinecraftInstance.installedAddons File: MI.installedAddons) {
                        Manifest.Files mf = gson.fromJson("{}", Manifest.Files.class);
                        mf.projectID = -1;
                        mf.fileID = -1;
                        mf.downloadUrl = File.installedFile.downloadUrl;
                        mf.required = true;
                    }
                } else {
                    manifest = gson.fromJson(Files.readString(manifestFile),Manifest.class);
                }

                // Checking if manifest contains modpack name, it doesn't matter that much, but it's better to check :P
                if (manifest.name == null) {
                    System.out.println("manifest.json doesn't have modpack name!");
                } else {
                    System.out.println("Installing modpack: " + manifest.name + " " + manifest.version);
                    System.out.println("That requires ModLoader: " + manifest.minecraft.version + " " + manifest.minecraft.modLoaders[0].id);
                }
                // Checking if manifest has any mods.
                if (manifest.files.length == 0) {
                    System.out.println("Manifest file doesn't have any mods in it! So no job for me :D");
                    logger.warn("Manifest files does not have any mods in it. Is this intentional?");
                    System.exit(0);
                }
                // Some more info about modpack
                System.out.println("Found " + manifest.files.length + " mods!");


                // Checking if /mods directory exists and can be used
                Path mods = Path.of("mods");
                if(mods.toFile().exists() && !mods.toFile().isDirectory()) {
                    System.out.println("/mods exists but is a file, aborting");
                    System.exit(1);
                }
                
                if(!mods.toFile().exists()) {
                    System.out.println("/mods does not exist, creating");
                    Files.createDirectory(mods);
                }

                // Using modified Vazkii DownloadManager to download mods
                DownloadManager dm = new DownloadManager(mods);
                dm.downloadInstance(gson.fromJson(Files.readString(manifestFile),Manifest.class));

            } catch (IOException e) {
                System.out.println("[ERROR]: Something bad happened...");
                e.printStackTrace();
            }
        } catch (Exception e) {
            System.out.println("CatDownloader crashed! More details are in the log file at \"" + logger.getLogPath() + "\".");
            logger.logStackTrace("Something horrible happened...", e);
            System.exit(1);
        }
    }
}
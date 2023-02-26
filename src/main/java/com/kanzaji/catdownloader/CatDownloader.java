package com.kanzaji.catdownloader;

import com.kanzaji.catdownloader.jsons.Manifest;
import com.kanzaji.catdownloader.utils.Logger;
import com.google.gson.Gson;
import com.vazkii.instancesync.DownloadManager;

import java.io.IOException;
import java.nio.file.*;

public final class CatDownloader {

    public static final String VERSION = "DEVELOP";

    public static void main(String[] args) {
        // DEVELOP SECTION

        // GUI.startGUI(); // Yea this is broken :kek:
        Logger logger = Logger.getInstance();
        logger.init();
        logger.log("Cat Downloader version: " + VERSION);

        try {
            SettingsManager sm = SettingsManager.getInstance();
            sm.init();
            logger.postInit();

            // "What the hell did I just run" section
            System.out.println("---------------------------------------------------------------------");
            System.out.println("     Cat Downloader " + VERSION);
            System.out.println("     Created by: Kanzaji -> My first project in java \\o/");
            System.out.println("---------------------------------------------------------------------");

            // Setting directory where program was turned on
            Path dir = Path.of(".");
            System.out.println("Running in " + dir.toAbsolutePath());
            
            // Checking if manifest.json is present
            Path manifestFile = Path.of(dir.toAbsolutePath().toString(), "manifest.json");
            if (!manifestFile.toFile().exists()) {
                System.out.println("No manifest.json file exists in this directory, aborting!");
                System.exit(1);
            }

            // Getting data from manifest.json
            Gson gson = new Gson();
            try {
                Manifest manifest = gson.fromJson(Files.readString(manifestFile),Manifest.class);
                // Checking if manifest contains modpack name, it doesn't matter that much, but it's better to check :P
                if (manifest.name == null) {
                    System.out.println("manifest.json doesn't have modpack name!");
                } else {
                    System.out.println("Installing modpack: " + manifest.name + " " + manifest.version);
                    System.out.println("That requires ModLoader: " + manifest.minecraft.version + " " + manifest.minecraft.modLoaders[0].id);
                }
                // Checking if manifest has any mods.
                if (manifest.files.length == 0) {
                    System.out.println("manifest.json doesn't have any mods in it! So no job for me :D");
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
                DownloadManager manager = new DownloadManager(mods);
                manager.downloadInstance(manifest); 

            } catch (IOException e) {
                System.out.println("[ERROR]: Something bad happened...");
                e.printStackTrace();
            }
        } catch (Exception e) {
            System.out.println("CatDownloader crashed! More details are in the log file at \"" + SettingsManager.getSettingsPath() + "\".")
            logger.logStackTrace("Something horrible happened...", e);
            System.exit(1);
        }
    }
}
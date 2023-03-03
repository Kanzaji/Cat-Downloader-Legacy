package com.kanzaji.catdownloaderlegacy;

import com.kanzaji.catdownloaderlegacy.jsons.Manifest;
import com.kanzaji.catdownloaderlegacy.jsons.MinecraftInstance;
import com.kanzaji.catdownloaderlegacy.utils.ArgumentDecoder;
import com.kanzaji.catdownloaderlegacy.utils.Logger;
import com.kanzaji.catdownloaderlegacy.utils.MIInterpreter;

import com.google.gson.Gson;

import java.nio.file.*;
import java.util.Objects;

public final class CatDownloader {

    public static final String VERSION = "DEVELOP";
    public static Path manifestFile;

    public static void main(String[] args) {
        Logger logger = Logger.getInstance();
        logger.init();
        logger.log("Cat Downloader version: " + VERSION);

        try {
            // Initialize required Utilities.
            ArgumentDecoder ARD = ArgumentDecoder.getInstance();

            // Decode Arguments and store them in ARD Instance.
            ARD.decodeArguments(args);

            // Turns off Logger if user wants it (NOT RECOMMENDED!!!!)
            // Redirects entire output to a console!
            if (Objects.equals(ARD.getData("Logger"), "off")){
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
            Manifest manifest = new Manifest();
            logger.log("Reading data from Manifest file...");
            if (Objects.equals(ARD.getData("Mode"), "Instance")) {
                // Translating from MinecraftInstance format to Manifest format.
                MinecraftInstance MI = gson.fromJson(Files.readString(manifestFile),MinecraftInstance.class);
                manifest = MIInterpreter.decode(MI);
            } else {
                manifest = gson.fromJson(Files.readString(manifestFile),Manifest.class);
            }
            logger.log("Data fetched. Found " + manifest.files.length + " Mods, on version " + manifest.minecraft.version + " " + manifest.minecraft.modLoaders[0].id);

            // Checking if Manifest contains modpack name.
            if (manifest.name == null) {
                System.out.println("manifest.json doesn't have modpack name!");
                logger.warn("The name of the instance is missing!");
            } else {
                System.out.println("Installing modpack: " + manifest.name + " " + manifest.version);
                logger.log("Instance name: " + manifest.name);
            }
            // Checking if Manifest file contains required modLoader.
            if (manifest.minecraft.modLoaders[0].id == null) {
                System.out.println("Manifest file doesn't have any mod loader specified! Is this vanilla?");
                logger.warn("This instance seems to be vanilla? No mod loader found!");
            } else {
                System.out.println("That requires ModLoader: " + manifest.minecraft.version + " " + manifest.minecraft.modLoaders[0].id);
                logger.log("Mod Loader: " + manifest.minecraft.modLoaders[0].id);
            }
            // Checking if Manifest has any mods.
            if (manifest.files == null || manifest.files.length == 0) {
                System.out.println("Manifest file doesn't have any mods in it!");
                logger.error("Manifest files does not have any mods in it. Is this intentional?");
                System.exit(0);
            }

            System.out.println("---------------------------------------------------------------------");

            // Some more info about modpack
            System.out.println("Found " + manifest.files.length + " mods!");

            // Checking if /mods directory exists and can be used
            Path mods = Path.of("mods");
            if(mods.toFile().exists() && !mods.toFile().isDirectory()) {
                System.out.println("Folder\"mods\" exists, but it is a file!");
                logger.error("Folder \"mods\" exists, but it is a file!");
                System.exit(1);
            }

            if(!mods.toFile().exists()) {
                logger.log("Folder \"mods\" is missing. Creating...");
                Files.createDirectory(mods);
                logger.log("Created \"mods\" folder in working directory. Path: " + dir.toAbsolutePath() + "\\mods");
            } else {
                logger.log("Found \"mods\" folder in working directory. Path: " + dir.toAbsolutePath() + "\\mods");
            }

            // Getting FileManager ready and starting sync of the profile.
            FileManager fm = FileManager.getInstance();
            fm.passData(mods,manifest, Integer.parseInt(ARD.getData("Threads")));
            fm.runSync();
        } catch (Exception | Error e) {
            System.out.println("CatDownloader crashed! More details are in the log file at \"" + logger.getLogPath() + "\".");
            logger.logStackTrace("Something horrible happened...", e);
            System.exit(1);
        }
    }
}
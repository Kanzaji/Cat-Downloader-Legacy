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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public final class CatDownloader {
    // Launch fresh instances of required utilities.
    private static final LoggerCustom logger = new LoggerCustom("Main");
    private static final ArgumentDecoder ARD = ArgumentDecoder.getInstance();

    // Global variables
    public static final String VERSION = "1.0";
    public static final String REPOSITORY = "https://github.com/Kanzaji/Cat-Downloader-Legacy";
    public static final String NAME = "Cat Downloader Legacy";
    public static Path APPPATH = null;

    static {
        try {
            APPPATH = Path.of(Updater.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath().replaceFirst("/", ""));
        } catch (URISyntaxException e) {
            // I think this should not happen? But just for safety better to log it.
            logger.logStackTrace("Failed to get App directory!", e);
        }
    }

    // Locally used variables
    public static Path manifestFile;
    private static Manifest ManifestData = new Manifest();
    public static List<Runnable> dataGatheringFails = new LinkedList<>();

    public static void main(String[] args) {
        //TODO: Add a bit more documentation.
        // What I mean is add docs to the classes (because some of them have it and some don't) and add links etc to all docs that are currently live.
        // Trust me future Kanz, IT WILL BE WORTH IT.

        // Time estimate! Okay more like how long it took but still.
        long StartingTime = System.currentTimeMillis();

        // Initialize Logger.
        logger.init();
        logger.log(NAME + " version: " + VERSION);
        logger.log("App path: " + APPPATH.toAbsolutePath());

        try {
            // Decode Arguments and store them in ARD Instance.
            ARD.decodeArguments(args);

            // Printing out Argument / Configuration values.
            ARD.printConfiguration("Program Configuration from Arguments:");

            // Run Post-int on Logger.
            logger.postInit();

            // "What the hell did I just run" section.
            System.out.println("---------------------------------------------------------------------");
            System.out.println("     " + NAME + " " + VERSION);
            System.out.println("     Created by: Kanzaji");
            System.out.println("---------------------------------------------------------------------");

            // Initialize SettingsManager
            if (ARD.areSettingsEnabled()) { SettingsManager.initSettings(); }

            // Turns off Logger if user wants it (NOT RECOMMENDED!!!!)
            // Redirects entire output to a console!
            if (!ARD.isLoggerActive()){
                logger.exit();
            }

            // Setting directory where program was turned on
            Path dir = Path.of(ARD.getWorkingDir());
            System.out.println("Running in " + dir.toAbsolutePath());

            // Checking Program mode and getting required Manifest File.
            if (ARD.isPackMode()) {
                manifestFile = Path.of(dir.toAbsolutePath().toString(), "manifest.json");
                System.out.println("CurseForge site format support is experimental! Use at your own responsibility.");
            } else {
                manifestFile = Path.of(dir.toAbsolutePath().toString(), "minecraftinstance.json");
            }

            if (!manifestFile.toFile().exists()) {
                System.out.println("Manifest file not found!");
                logger.error("Manifest file not found!");
                String msg = null;
                if (ARD.isPackMode()) {
                    if (Files.exists(Path.of(dir.toAbsolutePath().toString(), "minecraftinstance.json"))) {
                        msg = "It appears `minecraftinstance.json` exists in the current working directory, did you mean to run the app in \"Instance\" mode?";
                    }
                } else {
                    if (Files.exists(Path.of(dir.toAbsolutePath().toString(), "manifest.json"))) {
                        msg = "It appears `manifest.json` exists in the current working directory, did you mean to run the app in \"Pack\" mode?";
                    }
                }
                if (msg != null) {
                    System.out.println(msg);
                    logger.error(msg);
                }
                System.exit(1);
            }

            // Parsing data from Manifest file.
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

            // Checking if Manifest contains modpack name.
            if (ManifestData.name == null) {
                System.out.println("Manifest file doesn't have an instance name!");
                logger.warn("The name of the instance is missing!");
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
            // Checking if Manifest file contains required modLoader.
            if (ManifestData.minecraft.modLoaders[0].id == null) {
                System.out.println("For Minecraft " + ManifestData.minecraft.version + " Vanilla");
                logger.warn("This instance seems to be vanilla? No mod loader found!");
            } else {
                System.out.println("For Minecraft " + ManifestData.minecraft.version + " using " + ManifestData.minecraft.modLoaders[0].id);
                logger.log("Mod Loader: " + ManifestData.minecraft.modLoaders[0].id);
            }

            System.out.println("---------------------------------------------------------------------");

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

            // Checking if Manifest has any mods.
            if (ManifestData.files == null || ManifestData.files.length == 0) {
                System.out.println("Manifest file doesn't have any mods in it. Is this vanilla?");
                logger.error("Manifest files does not have any mods in it. Is this intentional?");
                System.exit(0);
            }

            System.out.println("Found " + ManifestData.files.length + " mods in Manifest file!");
            // If in Legacy mode, gather data from CFWidget API required for downloads. (THIS IS REALLY UNSTABLE).
            if (ARD.isPackMode()) {
                logger.log("Getting data for ids specified in the Manifest file...");
                System.out.println("Gathering Data about mods... This may take a while.");
                ExecutorService Executor, FailExecutor;
                if (ARD.isExperimental()) {
                    logger.warn("Experimental mode for CurseForge support turned on! This may cause unexpected behaviour and issues with data gathering process.");
                    logger.warn("Use at your own risk! Try to not over-use it.");
                    Executor = Executors.newFixedThreadPool(ARD.getThreads());
                    FailExecutor = Executors.newFixedThreadPool(ARD.getThreads()/4);
                } else {
                    // I think improving the gathering speed by like, the factor of 2 is good enough here, Executor with 2 threads should do the job.
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
                            dataGatheringFails.add(() -> {
                                ManifestData.files[finalIndex] = mod.getData(ManifestData.minecraft);
                            });
                        }
                    });
                    Index += 1;
                }

                Executor.shutdown();
                if (!Executor.awaitTermination(1, TimeUnit.DAYS)) {
                    logger.error("Data gathering takes over a day! This for sure isn't right???");
                    System.out.println("Data gathering interrupted due to taking over a day! This for sure isn't right???");
                    throw new RuntimeException("Data gathering is taking over a day! Something is horribly wrong.");
                }

                if (dataGatheringFails.size() > 0) {
                    logger.warn("Data gathering errors present! Trying to re-run unsuccessful data requests. Errors present: " + dataGatheringFails.size());
                    dataGatheringFails.forEach(FailExecutor::submit);
                }

                FailExecutor.shutdown();
                if (!FailExecutor.awaitTermination(1, TimeUnit.DAYS)) {
                    logger.error("Data gathering takes over a day! This for sure isn't right???");
                    System.out.println("Data gathering interrupted due to taking over a day! This for sure isn't right???");
                    throw new RuntimeException("Data gathering is taking over a day! Something is horribly wrong.");
                }

                logger.log("Finished gathering data!");
                System.out.println("Finished gathering data!");
            }

            // Getting FileManager ready and starting sync of the profile.
            // This shouldn't be a god-damn singleton, however I am too lazy to change it now and I will fix that on the Launcher version.
            SyncManager fm = SyncManager.getInstance();
            fm.passData(ModsFolder, ManifestData, ARD.getThreads());
            fm.runSync();

            System.out.println(" (Entire process took " + (float) (System.currentTimeMillis() - StartingTime) / 1000F + "s)");
            logger.log("Sync took " + (float) (System.currentTimeMillis() - StartingTime) / 1000F + "s");

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
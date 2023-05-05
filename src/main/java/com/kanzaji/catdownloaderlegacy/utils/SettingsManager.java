package com.kanzaji.catdownloaderlegacy.utils;

import com.kanzaji.catdownloaderlegacy.jsons.Settings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kanzaji.catdownloaderlegacy.utils.loggers.LoggerCustom;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * SettingsManager is a class used to manage, create, update and load Settings file for Cat-Downloader.
 * @see SettingsManager#initSettings()
 */
public class SettingsManager {
    private static final LoggerCustom logger = new LoggerCustom("SettingsManager");
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().setLenient().create();
    private static final ArgumentDecoder ARD = ArgumentDecoder.getInstance();
    private static final Path SettingsFile = Path.of(ARD.getSettingsPath(),"Cat-Downloader-Legacy Settings.json5");
    public static List<String> ModBlackList = new LinkedList<>();

    /**
     * Used to initialize {@link Settings}.
     * Creates a template of the Settings file, and sets everything up if argument `DefaultSettingsFromArguments` is true.
     * @see ArgumentDecoder
     */
    public static void initSettings() throws IOException {

        logger.log("Settings initialization started.");

        if (Files.exists(SettingsFile)) {
            logger.log("Found settings file!");
            logger.log("- Path to settings file: " + SettingsFile.toAbsolutePath());

            if (!ARD.shouldDefaultSettings()) {
                logger.warn("Generating Settings from arguments is enabled! Overriding settings file with argument values...");
                saveSettingsFromARD();
                logger.log("Override of the settings finished!");
            } else {
                Settings SettingsFileData = parseSettings();
                if (validateSettings(SettingsFileData)) {
                    loadSettings(SettingsFileData);
                } else {
                    throw new IllegalArgumentException("Settings contain illegal values! \n Check the settings file at " + SettingsFile.toAbsolutePath());
                }
            }

        } else {

            logger.log("No settings file found!");

            if (ARD.shouldDefaultSettings()) {
                logger.log("Getting a template for settings file out of internal assets...");
                Files.copy(FileUtils.getInternalFile("templates/settings.json5"), SettingsFile);
                logger.log("Template created! Halting program to allow configuration changes.");

                // Console output to the user
                System.out.println("It seems like you are running Cat-Downloader-Legacy for the first time!");
                System.out.println("In the working directory, a settings file has been generated, go take a look!");
                System.out.println("---------------------------------------------------------------------");
                System.out.println("Path to configuration file: " + SettingsFile.toAbsolutePath());
                System.out.println("---------------------------------------------------------------------");
                System.exit(0);
            } else {
                saveSettingsFromARD();
            }
        }

        logger.log("Initialization of Settings finished.");
    }

    /**
     * Used to parse Settings data from Settings file.
     * @return {@link Settings} contained in Settings file.
     */
    private static Settings parseSettings() {
        logger.log("Parsing data from settings file...");
        try {
            Settings SettingsFileData = gson.fromJson(Files.readString(SettingsFile), Settings.class);
            ModBlackList = Arrays.stream(SettingsFileData.modBlacklist).toList();
            logger.log("Parsing of Settings was successful!");
            return SettingsFileData;
        } catch (Exception e) {
            System.out.println("Settings file seems to be corrupted! Make sure you have all values as correct data type!");
            logger.logStackTrace("Failed parsing settings file!", e);
            System.exit(2);
        }
        return null;
    }

    /**
     * Used to validate {@link Settings} data.
     * @param SettingsData {@link Settings} Object containing data to validate.
     * @return {@link Boolean} with the result of the validation.
     */
    private static boolean validateSettings(Settings SettingsData) {
        logger.log("Validating settings...");
        List<String> errors = new LinkedList<>();
        if (!ArgumentDecoder.validateMode(SettingsData.mode)) {
            errors.add("Mode: " + SettingsData.mode + " is not correct! Available modes are: Pack // Instance");
        }
        if (!Files.exists(Path.of(SettingsData.workingDirectory))) {
            errors.add("Working Directory in Settings file does not exists!");
        }
        if (SettingsData.threadCount < 1) {
            errors.add("Thread count can't be below 1!");
        }
        if (SettingsData.downloadAttempts < 1) {
            errors.add("Re-Download attempts can't be below 1!");
        }
        if (errors.size() > 0) {
            logger.error("---------------------------------------------------------------------");
            logger.error("Failed to validate settings!");
            System.out.println("There appear to be mistakes in your current settings file!");
            errors.forEach((String error) -> {
                System.out.println("- " + error);
                logger.error("- " + error);
            });
            logger.error("---------------------------------------------------------------------");
            return false;
        } else {
            return true;
        }
    }

    /**
     * Used to load settings into {@link ArgumentDecoder}.
     * @param SettingsData {@link Settings} data to load into {@link ArgumentDecoder}.
     */
    private static void loadSettings(Settings SettingsData) {
        logger.log("Loading Settings data to Argument Decoder...");
        ARD.loadFromSettings(SettingsData, true);
        logger.log("Loading data to ARD finished!");
    }

    /**
     * Used to save {@link Settings} to a settings file! Also adds a note about auto-generation of the file, and where to find documentation.
     * @param SettingsData {@link Settings} object containing data to save.
     * @throws IOException when IO Exception occurs.
     */
    private static void saveSettings(Settings SettingsData) throws IOException {
        if (!Files.exists(SettingsFile)) {
            logger.warn("Settings file appears to be missing??? Creating empty file...");
            Files.createFile(SettingsFile);
            logger.warn("Created empty Settings file at " + SettingsFile.toAbsolutePath());
        } else {
            logger.log("Erasing current settings for saving purposes...");
            Files.writeString(SettingsFile, "", StandardOpenOption.TRUNCATE_EXISTING);
            logger.log("Settings cleared!");
        }

        logger.log("Saving settings to a file...");
        Files.writeString(SettingsFile, gson.toJson(SettingsData));
        logger.log("Settings have been saved!");
        logger.log("Adding auto-generation note...");
        Files.writeString(
            SettingsFile,
        "\n// This file was auto-generated with use of Arguments! If you want to know what things in here do, delete this file and run the app without any arguments!\n" +
            "// You can also check out my github (https://github.com/Kanzaji/Cat-Downloader-Legacy) for more details.",
            StandardOpenOption.APPEND
        );
        logger.log("Note added!");
    }

    /**
     * Generates {@link Settings} object from data in {@link ArgumentDecoder}.
     * @return {@link Settings} with values from {@link ArgumentDecoder}.
     */
    private static Settings generateSettingsFromARD() {
        logger.log("Generating settings file based out of argument values...");
        Settings ARDConfig = new Settings();
        ARDConfig.mode = ARD.getMode();
        ARDConfig.workingDirectory = ARD.getWorkingDir();
        ARDConfig.threadCount = ARD.getThreads();
        ARDConfig.downloadAttempts = ARD.getDownloadAttempts();
        ARDConfig.isLoggerActive = ARD.isLoggerActive();
        ARDConfig.isHashVerificationActive = ARD.isHashVerActive();
        ARDConfig.isFileSizeVerificationActive = ARD.isFileSizeVerActive();
        ARDConfig.modBlacklist = new String[0];
        logger.log("Generation of Settings from ARD finished!");
        return ARDConfig;
    }

    /**
     * Used to automatically generate, load and save {@link Settings} data from and to {@link ArgumentDecoder}.
     * @throws IOException when IO Exception occurs at saving-to-file stage.
     */
    private static void saveSettingsFromARD() throws IOException {
        Settings ARDS = generateSettingsFromARD();
        loadSettings(ARDS);
        saveSettings(ARDS);
    }
}

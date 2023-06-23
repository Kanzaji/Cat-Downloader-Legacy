package com.kanzaji.catdownloaderlegacy.utils;

import com.kanzaji.catdownloaderlegacy.ArgumentDecoder;
import com.kanzaji.catdownloaderlegacy.jsons.Settings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kanzaji.catdownloaderlegacy.loggers.LoggerCustom;

import javax.sound.sampled.Line;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

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
                // Saving will automatically re-add missing values!
                saveSettings(SettingsFileData);
                // Parse Data again to get any missing values!
                SettingsFileData = parseSettings();
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
                System.out.println("You can configure the Path to the Settings file with use of \"-SettingsPath:\" app argument!");
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
            ModBlackList = (Objects.isNull(SettingsFileData.modBlacklist))? new Settings.BlackList<>(): SettingsFileData.modBlacklist;
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
        if (Objects.isNull(SettingsData.mode)) {
            errors.add("Mode is null! Available modes are: Pack // Instance");
        } else if (!ArgumentDecoder.validateMode(SettingsData.mode.toLowerCase(Locale.ROOT))) {
            errors.add("Mode: " + SettingsData.mode + " is not correct! Available modes are: Pack // Instance");
        }
        if (Objects.isNull(SettingsData.workingDirectory)) {
            errors.add("Working Directory is null!");
        } else if (!Files.exists(Path.of(SettingsData.workingDirectory))) {
            errors.add("Working Directory in Settings file does not exists!");
        }
        if (Objects.isNull(SettingsData.logDirectory)) {
            errors.add("Log Directory is null!");
        }
        if (SettingsData.threadCount < 1) {
            errors.add("Thread count can't be below 1!");
        }
        if (SettingsData.downloadAttempts < 1) {
            errors.add("Re-Download attempts can't be below 1!");
        }
        if (SettingsData.logStockpileSize < 1) {
            errors.add("LogStockpileSize can't be below 1!");
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
            System.out.println("---------------------------------------------------------------------");
            System.out.println("Check the settings file at \"" + SettingsFile.toAbsolutePath() + "\"");
            System.out.println("---------------------------------------------------------------------");
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
     * Used to save {@link Settings} to a settings file! Adds all missing entries to the settings file, if not present.
     * @param SettingsData {@link Settings} object containing data to save.
     * @throws IOException when IO Exception occurs.
     */
    private static void saveSettings(Settings SettingsData) throws IOException {
        if (!Files.exists(SettingsFile)) {
            logger.warn("Settings file appears to be missing??? Creating default settings file...");
            Files.copy(FileUtils.getInternalFile("templates/settings.json5"), SettingsFile, StandardCopyOption.REPLACE_EXISTING);
            logger.warn("Created default Settings file at " + SettingsFile.toAbsolutePath());
        }

        logger.log("Saving settings to a file...");
        // Reading all lines of the Settings file
        List<String> SettingsLines = Files.readAllLines(SettingsFile);
        // Clearing a file!
        Files.writeString(SettingsFile, "", StandardOpenOption.TRUNCATE_EXISTING);
        AtomicReference<List<String>> existingKeys = new AtomicReference<>();
        // Manually copying the List to the LinkedList<>, Just because Arrays#asList() doesn't allow removal of the elements.
        existingKeys.set(new LinkedList<>());
        for (String settingsKey : Settings.SettingsKeys) {
            existingKeys.get().add(settingsKey);
        }

        SettingsLines.forEach(Line -> {
            for (String settingsKey : Settings.SettingsKeys) {
                if (Line.contains("\"" + settingsKey + "\":") && existingKeys.get().contains(settingsKey)) {
                    existingKeys.get().remove(settingsKey);

                    Line = Line.substring(0,Line.indexOf(settingsKey)).replaceFirst("//","") + settingsKey + "\": " + switch (settingsKey) {
                        case "mode" -> "\"" + SettingsData.mode + "\"";
                        case "workingDirectory" -> "\"" + SettingsData.workingDirectory + "\"";
                        case "logDirectory" -> "\"" + SettingsData.logDirectory + "\"";
                        case "threadCount" -> SettingsData.threadCount;
                        case "downloadAttempts" -> SettingsData.downloadAttempts;
                        case "logStockpileSize" -> SettingsData.logStockpileSize;
                        case "isLoggerActive" -> SettingsData.isLoggerActive;
                        case "shouldStockpileLogs" -> SettingsData.shouldStockpileLogs;
                        case "shouldCompressLogFiles" -> SettingsData.shouldCompressLogFiles;
                        case "isFileSizeVerificationActive" -> SettingsData.isFileSizeVerificationActive;
                        case "isHashVerificationActive" -> SettingsData.isHashVerificationActive;
                        case "modBlacklist" -> (Objects.isNull(SettingsData.modBlacklist))? "[]": SettingsData.modBlacklist.toString();
                        default -> throw new IllegalArgumentException("Illegal key in the SettingsFile!");
                    } + ",";

                    if (existingKeys.get().size() < 1 && Line.contains(",")) Line = Line.substring(0,Line.lastIndexOf(","));

                    break;
                }
            }

            try {
                Files.writeString(SettingsFile, Line + "\n", StandardOpenOption.APPEND);
            } catch (IOException e) {
                logger.logStackTrace("Exception occurred while writing to a Settings file!", e);
                throw new RuntimeException("Failed to save Settings!");
            }
        });

        if (existingKeys.get().size() > 0) {
            logger.warn("Found missing entries in the Settings file! Adding missing entries...");
            Files.copy(FileUtils.getInternalFile("templates/settings.json5"), SettingsFile, StandardCopyOption.REPLACE_EXISTING);
            saveSettings(SettingsData);
            logger.warn("Settings file replaced with default one, and values from the old file has been saved!");
        }

        logger.log("Settings have been saved!");
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
        ARDConfig.logDirectory = ARD.getLogPath();
        ARDConfig.isLoggerActive = ARD.isLoggerActive();
        ARDConfig.shouldCompressLogFiles = ARD.shouldCompressLogs();
        ARDConfig.shouldStockpileLogs = ARD.shouldStockpileLogs();
        ARDConfig.logStockpileSize = ARD.getLogStockSize();
        ARDConfig.threadCount = ARD.getThreads();
        ARDConfig.downloadAttempts = ARD.getDownloadAttempts();
        ARDConfig.isHashVerificationActive = ARD.isHashVerActive();
        ARDConfig.isFileSizeVerificationActive = ARD.isFileSizeVerActive();
        ARDConfig.modBlacklist = new Settings.BlackList<>();
        logger.log("Generation of Settings from ARD finished!");
        return ARDConfig;
    }

    /**
     * Used to automatically generate, load and save {@link Settings} data from and to {@link ArgumentDecoder}.
     * @throws IOException when IO Exception occurs at saving-to-file stage.
     */
    private static void saveSettingsFromARD() throws IOException {
        Settings ARDSettings = generateSettingsFromARD();
        loadSettings(ARDSettings);
        saveSettings(ARDSettings);
    }
}

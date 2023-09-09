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

package com.kanzaji.catdownloaderlegacy.utils;

import com.kanzaji.catdownloaderlegacy.ArgumentDecoder;
import com.kanzaji.catdownloaderlegacy.data.Settings;
import com.kanzaji.catdownloaderlegacy.loggers.LoggerCustom;
import static com.kanzaji.catdownloaderlegacy.utils.RandomUtils.checkIfJsonObject;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.atomic.AtomicReference;
import java.util.*;
import java.io.IOException;


/**
 * <h3>Class Description</h3>
 * SettingsManager is a class used to manage, create, update and load Settings file for Cat-Downloader.
 * <h3>Adding new key to Settings file</h3>
 * <ul>
 *     <li>Add new key to {@code settings.json5} file in the assets folder.</li>
 *     <li>Add new key to {@link Settings#SettingsKeys} array and create new field with the name of the key.</li>
 *     <li>Add new field and get method to {@link ArgumentDecoder}.</li>
 *     <li>Add key handlers to: <ul>
 *         <li>{@link ArgumentDecoder#loadFromSettings(Settings, boolean)}</li>
 *         <li>{@link SettingsManager#generateSettingsFromARD()}</li>
 *         <li>{@link SettingsManager#saveSettings(Settings)}</li>
 *     </ul></li>
 * </ul><h3>Optional steps</h3>
 * <ul>
 *     <li>If key is not Settings exclusive, add Argument Handler to the {@link ArgumentDecoder#decodeArguments(String[])}.</li>
 *     <li>If Required, add special handler in {@link SettingsManager#parseSettings()}.</li>
 *     <li>If Required, add special handler in {@link SettingsManager#validateSettings(Settings)}.</li>
 * </ul>
 * @see SettingsManager#initSettings()
 * @see SettingsManager#getSettings()
 * @see SettingsManager#updateSettings(Settings)
 */
public class SettingsManager {
    private static final LoggerCustom logger = new LoggerCustom("SettingsManager");
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().setLenient().create();
    private static final ArgumentDecoder ARD = ArgumentDecoder.getInstance();
    private static final Path SettingsFile = Path.of(ARD.getSettingsPath(),"Cat-Downloader-Legacy-Settings.json5");
    private static boolean SettingsInitialized = false;
    public static Settings.BlackList<String> ModBlackList = new Settings.BlackList<>();

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
                // Saving will automatically re-add missing values
                saveSettings(SettingsFileData);
                // Parse Data again to get any missing values
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
                Files.copy(FileUtils.getInternalAsset("templates/settings.json5"), SettingsFile);
                logger.log("Template created! Halting program to allow configuration changes.");

                System.out.println("It seems like you are running Cat-Downloader-Legacy for the first time!");
                System.out.println("In the working directory, a settings file has been generated, go take a look!");
                System.out.println("---------------------------------------------------------------------");
                System.out.println("Path to configuration file: " + SettingsFile.toAbsolutePath());
                System.out.println("---------------------------------------------------------------------");
                System.out.println("You can configure the Path to the Settings file with use of \"-SettingsPath:\" app argument!");
                System.out.println("---------------------------------------------------------------------");
                RandomUtils.closeTheApp(0);
            } else {
                logger.log("Generating Settings from arguments...");
                saveSettingsFromARD();
                logger.log("Settings from ARD generated!");
            }
        }
        logger.log("Initialization of Settings finished.");
        SettingsInitialized = true;
    }

    /**
     * Used to update Settings file and {@link ArgumentDecoder}.
     * @param newSettingsData Settings object with new Settings configuration.
     * @throws IOException when IO Operation fails.
     * @throws IllegalArgumentException when failed to validate settings.
     * @throws IllegalStateException when Settings weren't initialized.
     * @see SettingsManager#initSettings()
     * @see SettingsManager#areSettingsInitialized()
     * @see SettingsManager#validateSettings(Settings)
     * @see ArgumentDecoder#areSettingsEnabled()
     */
    public static void updateSettings(Settings newSettingsData) throws IOException, IllegalArgumentException, IllegalStateException {
        if (!areSettingsInitialized()) throw new IllegalStateException("Tried to update settings without initializing them!");
        logger.log("Updating settings...");
        if (!validateSettings(newSettingsData)) throw new IllegalArgumentException("Failed to validate updated settings!");
        loadSettings(newSettingsData);
        saveSettings(newSettingsData);
    }

    /**
     * This method is used to refresh app configuration from a Settings file.
     * @apiNote This method is not yet implemented. It does not have any practical use case in the Legacy version of the app.
     */
    @ApiStatus.Experimental
    public static void refreshSettings() {}

    /**
     * This method simply returns a Boolean if Settings have been initialized.
     * @return {@link Boolean} true if Settings have been initialized, otherwise false.
     */
    @Contract(pure = true)
    public static boolean areSettingsInitialized() {return SettingsInitialized;}

    /**
     * This method returns full Settings Object from {@link ArgumentDecoder} with parsed blacklist information.<br>
     * @return {@link Settings} object with app configuration.
     * @apiNote This method <i>does not</i> return fresh information from a Settings file. For parsing new Data from a Settings File, call method {@link SettingsManager#refreshSettings()} before this one.
     * @see SettingsManager#refreshSettings()
     */
    public static @NotNull Settings getSettings() {
        Settings settings = generateSettingsFromARD();
        settings.modBlackList = ModBlackList;
        return settings;
    }

    /**
     * Used to parse Settings data from Settings file.
     * @return {@link Settings} contained in Settings file.
     */
    private static @NotNull Settings parseSettings() {
        logger.log("Parsing data from settings file...");
        try {
            Settings SettingsFileData = gson.fromJson(Files.readString(SettingsFile), Settings.class);

            if (Objects.isNull(SettingsFileData.logDirectory)) SettingsFileData.logDirectory = "";
            if (Objects.isNull(SettingsFileData.workingDirectory)) SettingsFileData.workingDirectory = "";
            if (Objects.isNull(SettingsFileData.dataCacheDirectory)) SettingsFileData.dataCacheDirectory = SettingsFileData.logDirectory;

            SettingsFileData.mode = SettingsFileData.mode.toLowerCase(Locale.ROOT);
            ModBlackList = (Objects.isNull(SettingsFileData.modBlackList))? new Settings.BlackList<>(): SettingsFileData.modBlackList;

            logger.log("Parsing of Settings was successful!");
            return SettingsFileData;
        } catch (Exception e) {
            System.out.println("Settings file seems to be corrupted! Make sure you have all values as correct data type!");
            logger.logStackTrace("Failed parsing settings file!", e);
            RandomUtils.closeTheApp(1);
            // Only here due to IDE complaining.
            return new Settings();
        }
    }

    /**
     * Used to validate {@link Settings} data.
     * @param SettingsData {@link Settings} Object containing data to validate.
     * @return {@link Boolean} with the result of the validation.
     */
    private static boolean validateSettings(@NotNull Settings SettingsData) {
        logger.log("Validating settings...");
        List<String> errors = new LinkedList<>();
        if (Objects.isNull(SettingsData.mode)) {
            errors.add("Mode is null! Available modes are: CF-Pack // CF-Instance // Modrinth // Automatic");
        } else if (!ArgumentDecoder.validateMode(SettingsData.mode.toLowerCase(Locale.ROOT))) {
            errors.add("Mode: " + SettingsData.mode + " is not correct! Available modes are: CF-Pack // CF-Instance // Modrinth // Automatic");
        }
        if (Objects.isNull(SettingsData.workingDirectory)) {
            SettingsData.workingDirectory = "";
        } else if (!Files.exists(Path.of(SettingsData.workingDirectory))) {
            errors.add("Working Directory in Settings file does not exists!");
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
            Files.copy(FileUtils.getInternalAsset("templates/settings.json5"), SettingsFile, StandardCopyOption.REPLACE_EXISTING);
            logger.warn("Created default Settings file at " + SettingsFile.toAbsolutePath());
        }

        logger.log("Saving settings to a file...");
        List<String> SettingsLines = Files.readAllLines(SettingsFile);
        Files.writeString(SettingsFile, "", StandardOpenOption.TRUNCATE_EXISTING);
        AtomicReference<List<String>> existingKeys = new AtomicReference<>(new LinkedList<>(Arrays.asList(Settings.SettingsKeys)));

        Iterator<String> it = SettingsLines.listIterator();

        //NOTE: The simplest and fastest way to update Settings file would be to use GSON.toJson(), however, that would remove all documentation and user notes.
        // That is why I went with the "Read all lines and update entries manually" way.
        while (it.hasNext()) {
            String Line = it.next();
            if (existingKeys.get().size() > 0) for (String settingsKey : Settings.SettingsKeys) {
                if (Line.contains("\"" + settingsKey + "\":") && existingKeys.get().removeIf(settingsKey::equals)) {

                    // While adding new Settings Keys, this requires implementing handler for that key.
                    Line = Line.substring(0,Line.indexOf(settingsKey)).replaceFirst("//","") + settingsKey + "\": " + switch (settingsKey) {
                        case "mode" -> "\"" + SettingsData.mode + "\"";
                        case "workingDirectory" -> "\"" + SettingsData.workingDirectory.replaceAll("\\\\", "/") + "\"";
                        case "logDirectory" -> "\"" + SettingsData.logDirectory.replaceAll("\\\\", "/") + "\"";
                        case "dataCacheDirectory" -> "\"" + SettingsData.dataCacheDirectory.replaceAll("\\\\", "/") + "\"";
                        case "threadCount" -> SettingsData.threadCount;
                        case "downloadAttempts" -> SettingsData.downloadAttempts;
                        case "logStockpileSize" -> SettingsData.logStockpileSize;
                        case "dataCache" -> SettingsData.dataCache;
                        case "isLoggerActive" -> SettingsData.isLoggerActive;
                        case "shouldStockpileLogs" -> SettingsData.shouldStockpileLogs;
                        case "shouldCompressLogFiles" -> SettingsData.shouldCompressLogFiles;
                        case "isUpdaterActive" -> SettingsData.isUpdaterActive;
                        case "isFileSizeVerificationActive" -> SettingsData.isFileSizeVerificationActive;
                        case "isHashVerificationActive" -> SettingsData.isHashVerificationActive;
                        // Special Handling cases
                        case "modBlackList" -> saveModBlackList(Line, SettingsData.modBlackList, it);
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
        }

        if (existingKeys.get().size() > 0) {
            logger.warn("Found missing entries in the Settings file! Adding missing entries...");
            Files.copy(FileUtils.getInternalAsset("templates/settings.json5"), SettingsFile, StandardCopyOption.REPLACE_EXISTING);
            saveSettings(SettingsData);
            logger.warn("Settings file replaced with default one, and values from the old file has been saved!");

            System.out.println("Settings file has been updated! Go check out new options!");
            System.out.println("---------------------------------------------------------------------");
            System.out.println("Path to configuration file: " + SettingsFile.toAbsolutePath());
            System.out.println("---------------------------------------------------------------------");
            System.out.println("You can configure the Path to the Settings file with use of \"-SettingsPath:\" app argument!");
            System.out.println("---------------------------------------------------------------------");
            RandomUtils.closeTheApp(0);
        }

        logger.log("Settings have been saved!");
    }

    /**
     * Generates {@link Settings} object from data in {@link ArgumentDecoder}.
     * @return {@link Settings} with values from {@link ArgumentDecoder}.
     */
    private static @NotNull Settings generateSettingsFromARD() {
        logger.log("Generating settings file based out of argument values...");
        Settings ARDConfig = new Settings();
        ARDConfig.mode = ARD.getCurrentMode();
        ARDConfig.workingDirectory = ARD.getWorkingDir();
        ARDConfig.logDirectory = ARD.getLogPath();
        ARDConfig.isLoggerActive = ARD.isLoggerActive();
        ARDConfig.isUpdaterActive = ARD.isUpdaterActive();
        ARDConfig.shouldCompressLogFiles = ARD.shouldCompressLogs();
        ARDConfig.shouldStockpileLogs = ARD.shouldStockpileLogs();
        ARDConfig.logStockpileSize = ARD.getLogStockSize();
        ARDConfig.threadCount = ARD.getThreads();
        ARDConfig.downloadAttempts = ARD.getDownloadAttempts();
        ARDConfig.isHashVerificationActive = ARD.isHashVerActive();
        ARDConfig.isFileSizeVerificationActive = ARD.isFileSizeVerActive();
        ARDConfig.modBlackList = new Settings.BlackList<>();
        ARDConfig.dataCache = ARD.isCacheEnabled();
        ARDConfig.dataCacheDirectory = ARD.getCachePath();
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

    /**
     * Used to create a String required to save ModBlackList into the settings file!
     * @param Line {@link String} current line in the settings file.
     * @param blackList {@link com.kanzaji.catdownloaderlegacy.data.Settings.BlackList}<{@link String}> with blacklist entries.
     * @param iterator {@link Iterator}<{@link String}> with iterator over Settings file.
     * @return {@link String} with formatted ModBlackList ready to write to Settings File.
     */
    private static @NotNull String saveModBlackList(String Line, Settings.BlackList<String> blackList, Iterator<String> iterator) {
        if(checkIfJsonObject("{\n" + Line + "\n}")) return (Objects.isNull(blackList)) ? "[]" : blackList.toString();

        StringBuilder modBlackListString = new StringBuilder();
        LinkedList<String> blackListEntries = new LinkedList<>(blackList);

        if (blackListEntries.size() > 0) {
            if (blackListEntries.removeIf(Line::contains)) {
                modBlackListString
                .append(Line.substring(
                    Line.indexOf("\"modBlackList\":") + "\"modBlackList\":".length()
                )).append("\n");
            } else {
                modBlackListString.append("[\n");
            }
            while (iterator.hasNext()) {
                String blackListLine = iterator.next();
                blackListEntries.removeIf(blackListLine::contains);
                modBlackListString.append(blackListLine);
                if (blackListLine.contains("]") && checkIfJsonObject("{\"t\":\n" + modBlackListString + "\n}")) break;
                modBlackListString.append("\n");
            }
        } else {
            modBlackListString.append("[\n");
            while (iterator.hasNext()) {
                modBlackListString.append(iterator.next());
                if (modBlackListString.toString().contains("]") && checkIfJsonObject("{\"t\":\n" + modBlackListString + "\n}")) break;
                modBlackListString.append("\n");
            }
        }
        if (blackListEntries.size() > 0) blackListEntries.forEach(modBlackListString::append);
        return modBlackListString.toString().strip();
    }
}

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

import com.kanzaji.catdownloaderlegacy.data.Settings;
import com.kanzaji.catdownloaderlegacy.loggers.LoggerCustom;

import com.kanzaji.catdownloaderlegacy.utils.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.rmi.UnexpectedException;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;

public class ArgumentDecoder {
    private static final LoggerCustom logger = new LoggerCustom("Argument Decoder");
    private static final class InstanceHolder {private static final ArgumentDecoder instance = new ArgumentDecoder();}
    private static final String[] modes = {
            "cf-pack",
            "cf-instance",
            "modrinth",
            "automatic"
    };
    private ArgumentDecoder() {}
    private String WorkingDirectory = "";
    private String SettingsPath = "";
    private String LogPath = "";
    private String CachePath = "";
    private String Mode = "automatic";
    private int ThreadCount = 16;
    private int DownloadAttempts = 5;
    private int LogStockSize = 10;
    private boolean UpdaterActive = true;
    private boolean CacheActive = true;
    private boolean LoggerActive = true;
    private boolean StockpileLogs = true;
    private boolean CompressStockPiledLogs = true;
    private boolean FileSizeVerification = true;
    private boolean HashVerification = true;
    private boolean Settings = true;
    private boolean DefaultSettingsFromTemplate = true;
    private boolean Experimental = false;
    private boolean BypassNetworkCheck = false;

    /**
     * Used to get a reference to {@link ArgumentDecoder} instance.
     *
     * @return ArgumentDecoder with reference to the single instance of it.
     */
    public static ArgumentDecoder getInstance() {
        return InstanceHolder.instance;
    }

    /**
     * Decodes all arguments passed to the program, and saves necessary data in the instance of the ArgumentDecoder.
     * @param arguments String[] with arguments passed to the program.
     */
    public void decodeArguments(String @NotNull [] arguments) throws IllegalArgumentException, FileNotFoundException, UnexpectedException {
        logger.log("Running with arguments:");
        for (String fullArgument : arguments) {
            logger.log(fullArgument);

            String[] splitArgument = fullArgument.split(":", 2);
            String argument = splitArgument[0].startsWith("-") ?
                    splitArgument[0].toLowerCase(Locale.ROOT).replaceFirst("-", "") :
                    splitArgument[0].toLowerCase(Locale.ROOT);
            String value = "";
            if (splitArgument.length > 1) {
                value = splitArgument[1];
            }

            switch (argument) {
                // Path Arguments
                case "workingdirectory" -> this.WorkingDirectory = validatePath(value, "-WorkingDirectory");
                case "settingspath" -> this.SettingsPath = validatePath(value, "-SettingsPath");
                case "logspath" -> this.LogPath = validatePath(value, "-LogsPath", true);
                case "cachepath" -> this.CachePath = validatePath(value, "-CachePath", true);

                // Int Arguments
                case "threadcount" -> this.ThreadCount = getIntValue(value, "-ThreadCount", 1, 128);
                case "downloadattempts" -> this.DownloadAttempts = getIntValue(value, "-DownloadAttempts", 1, 255);
                case "logstocksize" -> this.LogStockSize = getIntValue(value, "-LogStockSize", 0, Integer.MAX_VALUE);

                // Boolean Arguments
                case "sizeverification" -> this.FileSizeVerification = getBooleanValue(value);
                case "hashverification" -> this.HashVerification = getBooleanValue(value);
                case "updater" -> this.UpdaterActive = getBooleanValue(value);
                case "cache" -> this.CacheActive = getBooleanValue(value);
                case "logger" -> this.LoggerActive = getBooleanValue(value);
                case "stockpilelogs" -> this.StockpileLogs = getBooleanValue(value);
                case "compresslogs" -> this.CompressStockPiledLogs = getBooleanValue(value);
                case "settings" -> this.Settings = getBooleanValue(value);
                case "defaultsettings" -> this.DefaultSettingsFromTemplate = getBooleanValue(value);
                case "experimental" -> this.Experimental = getBooleanValue(value);
                case "bypassnetworkcheck" -> this.BypassNetworkCheck = true;

                // Custom
                case "mode" -> {
                    value = value.toLowerCase(Locale.ROOT);
                    if (!validateMode(value)) {
                        logger.print("Wrong mode selected!", 3);
                        logger.print("Available modes: CF-Pack // CF-Instance // Modrinth // Automatic", 3);
                        logger.print("Check my Github Page at https://github.com/Kanzaji/Cat-Downloader-Legacy for more details!", 3);
                        throw new IllegalArgumentException("Incorrect Mode detected (" + value + ")!");
                    }
                    this.Mode = value;
                }

                default -> {
                }
            }
        }

        if (Objects.equals(this.CachePath, "")) {
            this.CachePath = this.LogPath;
        }
    }

    /**
     * Used to determine if provided {@link String} is one of the accepted Strings for boolean value.
     * Defaults to {@code true} if incorrect String is passed.
     * @param Value {@link String} with boolean value
     * @return {@link Boolean} with the result of the check.
     */
    private boolean getBooleanValue(String Value) {
        Value = Value.toLowerCase(Locale.ROOT);
        return !(
            Objects.equals(Value,"false") ||
            Objects.equals(Value,"disabled") ||
            Objects.equals(Value,"off") ||
            Objects.equals(Value,"0")
        );
    }

    /**
     * Used to parse {@link Integer} value from a {@link String}, and validate if it's contained in a specified threshold.
     * @param Value {@link String} with Integer value to parse.
     * @param Argument {@link String} with Name of the argument to provide in Exception.
     * @param MinValue {@link Integer} with Minimal threshold for parsed Integer.
     * @param MaxValue {@link Integer} with Maximal threshold for parsed Integer.
     * @return {@link Integer} parsed from provided String.
     * @throws IllegalArgumentException when parsed Integer is out of the provided threshold.
     * @throws NumberFormatException when String doesn't contain Integer values!
     */
    private int getIntValue(String Value, String Argument, int MinValue, int MaxValue) throws IllegalArgumentException, NumberFormatException {
        int IntValue;
        try {
            IntValue = Integer.parseInt(Value);
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Invalid String (" + Value + ") passed into the argument " + Argument + "!");
        }
        if (IntValue < MinValue || IntValue > MaxValue) {
            throw new IllegalArgumentException(
                "Value " +
                ((IntValue < MinValue)? "below minimal": "above maximum") +
                " threshold passed into the argument \"-" + Argument + "! " +
                ((IntValue < MinValue)? "Minimal": "Maximal") +
                " allowed value is " +
                ((IntValue < MinValue)? MinValue: MaxValue)
            );
        }
        return IntValue;
    }

    /**
     * Used to validate if provided Path exists.
     * @param path {@link String} with Path to validate.
     * @param Argument {@link String} with Name of the argument to provide in Exception.
     * @return {@link String} with provided Path, if it exists.
     * @throws FileNotFoundException when provided Path doesn't exist.
     */
    private String validatePath(String path, String Argument, boolean override) throws FileNotFoundException, UnexpectedException {
        Path ArgumentPath = Path.of(path);
        if (!Files.exists(ArgumentPath)) {
            if (override) {
                FileUtils.createRequiredPath(ArgumentPath);
                return path;
            }
            logger.error("Specified " + Argument + " does not exists!");
            logger.error(ArgumentPath.toAbsolutePath().toString());
            throw new FileNotFoundException("Specified " + Argument + " does not exists!");
        }
        return path;
    }

    /**
     * Used to validate if provided Path exists.
     * @param path {@link String} with Path to validate.
     * @param Argument {@link String} with Name of the argument to provide in Exception.
     * @return {@link String} with provided Path, if it exists.
     * @throws FileNotFoundException when provided Path doesn't exist.
     */
    private String validatePath(String path, String Argument) throws FileNotFoundException, UnexpectedException {
        return validatePath(path, Argument, false);
    }

    /**
     * Used to validate selected mode!
     * @param Mode Mode to verify.
     * @return boolean True when mode is available.
     */
    public static boolean validateMode(String Mode) {
        return Arrays.asList(modes).contains(Mode);
    }

    /**
     * Prints entire {@link ArgumentDecoder} Configuration to a log file.
     */
    public void printConfiguration(String message) {
        logger.log("---------------------------------------------------------------------");
        logger.log(message);
        logger.log("> Working directory: " + this.WorkingDirectory);
        logger.log("- Full Path: " + Path.of(this.WorkingDirectory).toAbsolutePath());
        logger.log("> Program Mode: " + ((Objects.nonNull(this.Mode))? this.Mode: "Automatic Mode Determination"));
        logger.log("> Network Check Bypass: " + this.BypassNetworkCheck);
        logger.log("> Updater enabled: " + this.UpdaterActive);
        logger.log("> Settings enabled: " + this.Settings);
        logger.log("> Default Settings from the template: " + this.DefaultSettingsFromTemplate);
        logger.log("> Settings Path: " + this.SettingsPath);
        logger.log("- Full Path: " + Path.of(this.SettingsPath).toAbsolutePath());
        logger.log("> Logger enabled: " + this.LoggerActive);
        logger.log("> Stockpiling logs active: " + this.StockpileLogs);
        logger.log("> Amount of logs to keep: " + this.LogStockSize);
        logger.log("> Compressing of logs active: " + this.CompressStockPiledLogs);
        logger.log("> Logs Path: " + this.LogPath);
        logger.log("- Full Path: " + Path.of(this.LogPath).toAbsolutePath());
        logger.log("> Caches enabled: " + this.CacheActive);
        logger.log("> Caches Path: " + this.CachePath);
        logger.log("- Full Path: " + Path.of(this.CachePath).toAbsolutePath());
        logger.log("> Thread count for downloads: " + this.ThreadCount);
        logger.log("> Download attempts for re-downloads: " + this.DownloadAttempts);
        logger.log("> Hash Verification: " + this.HashVerification);
        logger.log("> File Size Verification: " + this.FileSizeVerification);
        logger.log("---------------------------------------------------------------------");
    }

    /**
     * Used to load data to ARD from the Settings Manager.
     * @param SettingsData {@link Settings} object with data to load.
     */
    public void loadFromSettings(@NotNull Settings SettingsData, boolean Print) {
        this.Mode = SettingsData.mode;
        this.UpdaterActive = SettingsData.isUpdaterActive;
        this.WorkingDirectory = SettingsData.workingDirectory;
        this.LogPath = SettingsData.logDirectory;
        this.LoggerActive = SettingsData.isLoggerActive;
        this.CompressStockPiledLogs = SettingsData.shouldCompressLogFiles;
        this.StockpileLogs = SettingsData.shouldStockpileLogs;
        this.LogStockSize = SettingsData.logStockpileSize;
        this.ThreadCount = SettingsData.threadCount;
        this.DownloadAttempts = SettingsData.downloadAttempts;
        this.FileSizeVerification = SettingsData.isFileSizeVerificationActive;
        this.HashVerification = SettingsData.isHashVerificationActive;
        this.Experimental = SettingsData.experimental;
        this.CacheActive = SettingsData.dataCache;
        this.CachePath = (Objects.equals(SettingsData.dataCacheDirectory, "")? this.LogPath: SettingsData.dataCacheDirectory);
        if (Print) { printConfiguration("Program Configuration from Settings:");}
    }

    /**
     * Used to set the app mode in ARD after Automatic Detection.
     * <h3>Currently available modes: </h3>
     * <ul>
     *     <li>cf-pack</li>
     *     <li>cf-instance</li>
     *     <li>modrinth</li>
     * </ul>
     * @param mode String with app mode.
     * @throws IllegalArgumentException when illegal mode is passed as argument.
     */
    public void setCurrentMode(String mode) throws IllegalArgumentException {
        Objects.requireNonNull(mode);
        mode = mode.toLowerCase(Locale.ROOT);
        if (!validateMode(mode) || Objects.equals(mode, "automatic")) throw new IllegalArgumentException("Tried to set invalid or automatic mode (" + mode + ")");
        this.Mode = mode;
    }

    // Just a spam of Get methods. Nothing spectacular to see here.
    public String[] getAvailableModes() {return modes;}
    public String getCurrentMode() {return this.Mode;}
    public boolean isPackMode() {return Objects.equals(this.Mode, "cf-pack");}
    public boolean isInstanceMode() {return Objects.equals(this.Mode, "cf-instance");}
    public boolean isModrinthMode() {return Objects.equals(this.Mode, "modrinth");}
    public boolean isAutomaticModeDetectionActive() {return Objects.equals(this.Mode, "automatic");}
    public String getWorkingDir() {return this.WorkingDirectory;}
    public String getSettingsPath() {return this.SettingsPath;}
    public String getLogPath() {return this.LogPath;}
    public String getCachePath() {return this.CachePath;};
    public int getDownloadAttempts() {return this.DownloadAttempts;}
    public int getThreads() {return this.ThreadCount;}
    public int getLogStockSize() {return this.LogStockSize;}
    public boolean areSettingsEnabled() {return this.Settings;}
    public boolean shouldDefaultSettings() {return this.DefaultSettingsFromTemplate;}
    public boolean isUpdaterActive() {return this.UpdaterActive;}
    public boolean isLoggerActive() {return this.LoggerActive;}
    public boolean shouldStockpileLogs() {return this.StockpileLogs;}
    public boolean shouldCompressLogs() {return this.CompressStockPiledLogs;}
    public boolean isFileSizeVerActive() {return this.FileSizeVerification;}
    public boolean isHashVerActive() {return this.HashVerification;}
    public boolean isExperimental() {return this.Experimental;}
    public boolean isBypassNetworkCheckActive() {return this.BypassNetworkCheck;}
    public boolean isCacheEnabled() {return this.CacheActive;}
}

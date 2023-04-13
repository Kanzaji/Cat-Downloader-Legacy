package com.kanzaji.catdownloaderlegacy.utils;

import com.kanzaji.catdownloaderlegacy.jsons.Settings;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Objects;

public class ArgumentDecoder {
    private final Logger logger = Logger.getInstance();
    private static ArgumentDecoder instance;
    private ArgumentDecoder() {};
    private String WorkingDirectory = "";
    private String SettingsPath = "";
    private String LogPath = "";
    private String Mode = "instance";
    private int ThreadCount = 16;
    private int DownloadAttempts = 5;
    private int LogStockSize = 10;
    private boolean LoggerActive = true;
    private boolean StockPileLogs = true;
    private boolean CompressStockPiledLogs = true;
    private boolean FileSizeVerification = true;
    private boolean HashVerification = true;
    private boolean Settings = true;
    private boolean DefaultSettingsFromTemplate = true;
    private boolean Experimental = false;

    /**
     * Used to create first instance of ArgumentDecoder, and get reference to a single Instance of it in any other place.
     *
     * @return ArgumentDecoder with reference to the single instance of it.
     */
    public static ArgumentDecoder getInstance() {
        if (instance == null) {
            instance = new ArgumentDecoder();
        }
        return instance;
    }

    /**
     * Decodes all arguments passed to the program, and saves necessary data in the instance of the ArgumentDecoder.
     *
     * @param arguments String[] with arguments passed to the program.
     */
    public void decodeArguments(String[] arguments) throws IllegalArgumentException, FileNotFoundException {
        logger.log("Running with arguments:");
        for (String Argument : arguments) {
            logger.log(Argument);
            this.WorkingDirectory = decodePathArgument(Argument, "-WorkingDirectory:", "Working directory", this.WorkingDirectory);
            this.SettingsPath = decodePathArgument(Argument, "-SettingsPath", "Settings path", this.SettingsPath);
            this.LogPath = decodePathArgument(Argument, "-LogsPath:", "Logs path", this.LogPath);
            this.ThreadCount = decodeIntArgument(Argument, "-ThreadCount:", this.ThreadCount);
            this.DownloadAttempts = decodeIntArgument(Argument, "-DownloadAttempts:", this.DownloadAttempts);
            this.LogStockSize = decodeIntArgument(Argument, "-LogStockSize:", this.LogStockSize);
            if (decodeArgument(Argument,"-Mode:")) {
                this.Mode = Argument.substring(6).toLowerCase(Locale.ROOT);
                if (!validateMode(Mode)) {
                    logger.error("Wrong mode selected!");
                    logger.error("Available modes: Pack // Instance");
                    logger.error("Check my Github Page at https://github.com/Kanzaji/Cat-Downloader-Legacy for more details!");
                    throw new IllegalArgumentException("Incorrect Mode detected!" + this.Mode);
                }
            }
            if (decodeArgument(Argument,"-SizeVerification:")) {
                if (getOffBoolean(Argument)) {
                    this.FileSizeVerification = false;
                }
            }
            if (decodeArgument(Argument,"-FileHashVerification:")) {
                if (getOffBoolean(Argument)) {
                    this.HashVerification = false;
                }
            }
            if (decodeArgument(Argument,"-Logger:")) {
                if (getOffBoolean(Argument)) {
                    this.LoggerActive = false;
                }
            }
            if (decodeArgument(Argument,"-StockPileLogs:")) {
                if (getOffBoolean(Argument)) {
                    this.StockPileLogs = false;
                }
            }
            if (decodeArgument(Argument,"-CompressLogs:")) {
                if (getOffBoolean(Argument)) {
                    this.CompressStockPiledLogs = false;
                }
            }
            if (decodeArgument(Argument,"-Settings:")) {
                if (getOffBoolean(Argument)) {
                    this.Settings = false;
                }
            }
            if (decodeArgument(Argument,"-DefaultSettings:")) {
                if (getOffBoolean(Argument)) {
                    this.DefaultSettingsFromTemplate = false;
                }
            }
            if (decodeArgument(Argument,"-Experimental:")) {
                if (getOnBoolean(Argument)) {
                    this.Experimental = true;
                }
            }
        }
    }

    /**
     * Used to validate selected mode!
     * @param Mode Mode to verify.
     * @return boolean True when mode is available.
     */
    public static boolean validateMode(String Mode) {
        return Objects.equals(Mode, "pack") || Objects.equals(Mode, "instance");
    }

    /**
     * Used to determine if provided String is one of the accepted ones for turning on a feature.<br>
     * Tries to automatically determine the position of ":". If data of your argument can contain ":" or argument itself has it, please provide the Index for a data search manually.
     * @param Argument String with argument.
     * @throws IllegalArgumentException when Index < 0;
     * @return "True" Boolean when argument has acceptable value.
     */
    private boolean getOnBoolean(String Argument) {
        return getOnBoolean(Argument, Argument.lastIndexOf(":")+1);
    }

    /**
     * Used to determine if provided String is one of the accepted ones for turning on a feature.
     * @param Argument String with argument.
     * @param Index Index of `:` in the argument.
     * @throws IllegalArgumentException when Index < 0;
     * @return "True" Boolean when argument has acceptable value.
     */
    private boolean getOnBoolean(String Argument, int Index) {
        if (Index < 0) {
            throw new IllegalArgumentException("Index can not be below 0!");
        }
        return  Objects.equals(Argument.substring(Index).toLowerCase(Locale.ROOT),"true") ||
                Objects.equals(Argument.substring(Index).toLowerCase(Locale.ROOT),"enabled") ||
                Objects.equals(Argument.substring(Index).toLowerCase(Locale.ROOT),"on") ||
                Objects.equals(Argument.substring(Index).toLowerCase(Locale.ROOT),"1");
    }

    /**
     * Used to determine if provided String is one of the accepted ones for turning off a feature.<br>
     * Tries to automatically determine the position of ":". If data of your argument can contain ":" or argument itself has it, please provide the Index for a data search manually.
     * @param Argument String with argument.
     * @throws IllegalArgumentException when Index < 0;
     * @return "True" Boolean when argument has acceptable value.
     */
    private boolean getOffBoolean(String Argument) {
        return getOffBoolean(Argument, Argument.lastIndexOf(":")+1);
    }

    /**
     * Used to determine if provided String is one of the accepted ones for turning off a feature.
     * @param Argument String with argument.
     * @param Index Index of `:` in the argument.
     * @throws IllegalArgumentException when Index < 0;
     * @return "True" Boolean when argument has acceptable value.
     */
    private boolean getOffBoolean(String Argument, int Index) {
        if (Index < 0) {
            throw new IllegalArgumentException("Index can not be below 0!");
        }
        return  Objects.equals(Argument.substring(Index).toLowerCase(Locale.ROOT),"false") ||
                Objects.equals(Argument.substring(Index).toLowerCase(Locale.ROOT),"disabled") ||
                Objects.equals(Argument.substring(Index).toLowerCase(Locale.ROOT),"off") ||
                Objects.equals(Argument.substring(Index).toLowerCase(Locale.ROOT),"0");
    }

    /**
     * Used to determine if argument starts with specified value. Ignores upper case.
     * @param Argument String Argument.
     * @param ArgumentName String with the name of the Argument.
     * @return True if Argument starts with provided name.
     */
    private boolean decodeArgument(String Argument, String ArgumentName) {
        return Argument.toLowerCase(Locale.ROOT).startsWith(ArgumentName.toLowerCase(Locale.ROOT));
    }

    /**
     * Used to automatically decode Path Arguments.
     * @param Argument String Argument.
     * @param ArgumentName String with the name of the Argument.
     * @param Message Name of the Argument to print in the message if Path doesn't exist.
     * @param OriginalValue Original Value to return if Argument is not the one required.
     * @return String with a value of the Argument.
     * @throws FileNotFoundException when specified Path doesn't exist.
     */
    private String decodePathArgument(String Argument, String ArgumentName, String Message, String OriginalValue) throws FileNotFoundException {
        if (decodeArgument(Argument, ArgumentName)) {
            String ArgumentValue = Argument.substring(ArgumentName.indexOf(":"));
            Path ArgumentPath = Path.of(ArgumentValue);
            if (!Files.exists(ArgumentPath)) {
                logger.error("Specified " + Message + " does not exists!");
                logger.error(ArgumentPath.toAbsolutePath().toString());
                throw new FileNotFoundException("Specified " + Message + " does not exists!");
            }
            return ArgumentValue;
        }
        return OriginalValue;
    }

    /**
     * Used to automatically decode Int Arguments.
     * @param Argument String Argument.
     * @param ArgumentName String with the name of the Argument.
     * @param OriginalValue Original Value to return if Argument is not the one required.
     * @return Integer with a value of the Argument.
     */
    private int decodeIntArgument(String Argument, String ArgumentName ,int OriginalValue) {
        if (decodeArgument(Argument,ArgumentName)) {
            int ArgumentValue;
            try {
                ArgumentValue = Integer.parseInt(Argument.substring(ArgumentName.indexOf(":")));
                if (ArgumentValue < 1) {
                    logger.warn("Value below 1 was passed to \"" + ArgumentName + "\" Argument! Defaulting to " + OriginalValue + ".");
                    return OriginalValue;
                }
                return ArgumentValue;
            } catch (IllegalArgumentException e) {
                logger.warn("Non-int value was passed to \"" + ArgumentName + "\" Argument! Defaulting to " + OriginalValue + ".");
            }
        }
        return OriginalValue;
    }

    /**
     * Returns requested data from the arguments.
     * <br><br>Available data types:
     * <ul>
     *  <li>    Mode <br> A mode the program works in, Default: "Pack"    </li>
     *  <li>    Wdir <br> Working Directory of the program, Default: "."  </li>
     *  <li>    Threads <br> Amount of threads allowed to be used for download/verification work, Default: "16"</li>
     *  <li>    DAttempt <br> Amount of attempts a DownloadUtilities#reDownload() will take at re-downloading a mod, Default: "5"</li>
     *  <li>    Logger <br> Determines if Logger is turned on, Default: "True"  </li>
     *  <li>    SizeVer <br> Determines if FileSizeVerification is turned on, Default: "True".</li>
     *  <li>    HashVer <br> Determines if FileHashVerification is turned on, Default: "True".</li>
     *  <li>    Settings <br> Determines if user wants to use config file instead of arguments, Default: "True"</li>
     *  <li>    DefaultSettings <br> Determines if Generated Settings should be created from the Template, Default: "True"</li>
     *  <li>    Experimental <br> Unlocks Experimental features, Default: "False".</li>
     * </ul>
     *
     * @param dataType Requested type of Data.
     * @return String with Requested Data
     * @see ArgumentDecoder#getBooleanData(String) ArgumentDecoder#getBooleanData()<br>Easier Boolean data requests.
     */
    @Deprecated
    public String getData(String dataType) {
        // Note to myself: Next time use Record for this ffs, this is so stupid, yet I'm too lazy to change this now :lul:
        return switch (dataType) {
            case "Mode" -> this.Mode;
            case "Wdir" -> this.WorkingDirectory;
            case "SettingsPath" -> this.SettingsPath;
            case "Threads" -> String.valueOf(this.ThreadCount);
            case "DAttempt" -> String.valueOf(this.DownloadAttempts);
            case "Logger" -> String.valueOf(this.LoggerActive);
            case "SizeVer" -> String.valueOf(this.FileSizeVerification);
            case "HashVer" -> String.valueOf(this.HashVerification);
            case "Settings" -> String.valueOf(this.Settings);
            case "DefaultSettings" -> String.valueOf(this.DefaultSettingsFromTemplate);
            case "Experimental" -> String.valueOf(this.Experimental);
            default -> "";
        };
    }

    /**
     * Returns requested data from the arguments as Boolean.
     * This method only returns data available as Boolean! For non-Boolean data, use the {@link ArgumentDecoder#getData(String)}.
     * <br><br>Available data types:
     * <ul>
     *  <li>    Logger <br> Determines if Logger is turned on, Default: "True"  </li>
     *  <li>    SizeVer <br> Determines if FileSizeVerification is turned on, Default: "True".</li>
     *  <li>    HashVer <br> Determines if FileHashVerification is turned on, Default: "True".</li>
     *  <li>    Settings <br> Determines if user wants to use config file instead of arguments, Default: "True"</li>
     *  <li>    DefaultSettings <br> Determines if Generated Settings should be created from the Template, Default: "True"</li>
     *  <li>    Experimental <br> Unlocks Experimental features, Default: "False".</li>
     * </ul>
     *
     * @param dataType Requested type of Data.
     * @return Boolean of Requested Data.
     * @throws IllegalArgumentException when requesting non-Boolean data.
     * @see ArgumentDecoder#getData(String) ArgumentDecoder#getData() <br> Used for requesting non-Boolean data.
     */
    @Deprecated
    public boolean getBooleanData(String dataType) throws IllegalArgumentException {
        return switch (dataType) {
            case "Mode", "Wdir", "Threads", "DAttempt", "SettingsPath" -> throw new IllegalArgumentException();
            default -> Boolean.parseBoolean(getData(dataType));
        };
    }

    /**
     * Prints entire ARD Configuration to a log file.
     */
    public void printConfiguration(String message) {
        logger.log("---------------------------------------------------------------------");
        logger.log(message);
        logger.log("> Working directory: " + this.WorkingDirectory);
        logger.log("- Full Path: " + Path.of(this.WorkingDirectory).toAbsolutePath());
        logger.log("> Program Mode: " + this.Mode);
        logger.log("> Settings enabled: " + this.Settings);
        logger.log("> Default Settings from the template: " + this.DefaultSettingsFromTemplate);
        logger.log("> Settings Path: " + this.SettingsPath);
        logger.log("- Full Path: " + Path.of(this.SettingsPath).toAbsolutePath());
        logger.log("> Logger enabled: " + this.LoggerActive);
        logger.log("> Stockpiling logs active: " + this.StockPileLogs);
        logger.log("> Amount of logs to keep: " + this.LogStockSize);
        logger.log("> Compressing of logs active: " + this.CompressStockPiledLogs);
        logger.log("> Logs Path: " + this.LogPath);
        logger.log("- Full Path: " + Path.of(this.LogPath).toAbsolutePath());
        logger.log("> Thread count for downloads: " + this.ThreadCount);
        logger.log("> Download attempts for re-downloads: " + this.DownloadAttempts);
        logger.log("> Hash Verification: " + this.HashVerification);
        logger.log("> File Size Verification: " + this.FileSizeVerification);
        logger.log("---------------------------------------------------------------------");
    }

    /**
     * Used to load data to ARD from Settings Manager.
     * @param SettingsData {@link Settings} object with data to load.
     */
    public void loadFromSettings(Settings SettingsData, boolean Print) {
        this.Mode = SettingsData.mode;
        this.WorkingDirectory = SettingsData.workingDirectory;
        this.ThreadCount = SettingsData.threadCount;
        this.DownloadAttempts = SettingsData.downloadAttempts;
        this.LoggerActive = SettingsData.isLoggerActive;
        this.FileSizeVerification = SettingsData.isFileSizeVerificationActive;
        this.HashVerification = SettingsData.isHashVerificationActive;
        if (Print) { printConfiguration("Program Configuration from Settings:");}
    }

    // Just a spam of Get methods. nothing spectacular to see here.
    public String getMode() {return this.Mode;}
    public String getWorkingDir() {return this.WorkingDirectory;}
    public String getSettingsPath() {return this.SettingsPath;}
    public String getLogPath() {return this.LogPath;}
    public int getDownloadAttempts() {return this.DownloadAttempts;}
    public int getThreads() {return this.ThreadCount;}
    public int getLogStockSize() {return this.LogStockSize;}
    public boolean areSettingsEnabled() {return this.Settings;}
    public boolean shouldDefaultSettings() {return this.DefaultSettingsFromTemplate;}
    public boolean isLoggerActive() {return this.LoggerActive;}
    public boolean shouldStockPileLogs() {return this.StockPileLogs;}
    public boolean shouldCompressLogs() {return this.CompressStockPiledLogs;}
    public boolean isFileSizeVerActive() {return this.FileSizeVerification;}
    public boolean isHashVerActive() {return this.HashVerification;}
    public boolean isExperimental() {return this.Experimental;}
}

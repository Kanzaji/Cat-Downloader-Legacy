package com.kanzaji.catdownloaderlegacy.utils;

import com.kanzaji.catdownloaderlegacy.jsons.Settings;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Objects;

public class ArgumentDecoder {
    private static ArgumentDecoder instance;
    private final Logger logger = Logger.getInstance();
    private String WorkingDirectory = "";
    private String SettingsPath = "";
    private String Mode = "instance";
    private int nThreadsCount = 16;
    private int DownloadAttempts = 5;
    private boolean LoggerActive = true;
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
            if (decodeArgument(Argument,"-WorkingDirectory:")) {
                this.WorkingDirectory = Argument.substring(18);
                if (!Files.exists(Path.of(this.WorkingDirectory))) {
                    logger.error("Specified Working Directory does not exists!");
                    logger.error(Path.of(this.WorkingDirectory).toAbsolutePath().toString());
                    throw new FileNotFoundException("Specified Working Directory does not exists!");
                }
            }
            if (decodeArgument(Argument,"-SettingsPath:")) {
                this.SettingsPath = Argument.substring(14);
                if (!Files.exists(Path.of(this.SettingsPath))) {
                    logger.error("Specified Setting Path does not exists!");
                    logger.error(Path.of(this.SettingsPath).toAbsolutePath().toString());
                    throw new FileNotFoundException("Specified Settings file does not exists!");
                }
            }
            if (decodeArgument(Argument,"-Mode:")) {
                this.Mode = Argument.substring(6).toLowerCase(Locale.ROOT);
                if (!validateMode(Mode)) {
                    logger.error("Wrong mode selected!");
                    logger.error("Available modes: Pack // Instance");
                    logger.error("Check my Github Page at https://github.com/Kanzaji/Cat-Downloader-Legacy for more details!");
                    throw new IllegalArgumentException("Incorrect Mode detected!" + this.Mode);
                }
            }
            if (decodeArgument(Argument,"-ThreadCount:")) {
                try {
                    this.nThreadsCount = Integer.parseInt(Argument.substring(13));
                    if (this.nThreadsCount < 1) {
                        logger.warn("Value below 1 was passed to ThreadCount Argument! Defaulting to 16.");
                        this.nThreadsCount = 16;
                    }
                } catch (IllegalArgumentException e) {
                    logger.warn("Non-int value was passed to ThreadCount Argument! Defaulting to 16.");
                    this.nThreadsCount = 16;
                }
            }
            if (decodeArgument(Argument,"-DownloadAttempts:")) {
                try {
                    this.DownloadAttempts = Integer.parseInt(Argument.substring(18));
                    if (this.DownloadAttempts < 1) {
                        logger.warn("Value below 1 was passed to DownloadAttempts Argument! Defaulting to 5.");
                        this.DownloadAttempts = 5;
                    }
                } catch (IllegalArgumentException e) {
                    logger.warn("Non-int value was passed to DownloadAttempts Argument! Defaulting to 5.");
                    this.DownloadAttempts = 5;
                }
            }
            if (decodeArgument(Argument,"-SizeVerification:")) {
                if (getOffBoolean(Argument)) {
                    this.FileSizeVerification = false;
                }
            }
            if (decodeArgument(Argument,"-HashVerification:")) {
                if (getOffBoolean(Argument)) {
                    this.HashVerification = false;
                }
            }
            if (decodeArgument(Argument,"-Logger:")) {
                if (getOffBoolean(Argument)) {
                    this.LoggerActive = false;
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
     * Returns requested data from the arguments.
     * <br><br>Available data types:
     * <ul>
     *  <li>    Mode <br> A mode the program works in, Default: "Pack"    </li>
     *  <li>    Wdir <br> Working Directory of the program, Default: "."  </li>
     *  <li>    Threads <br> Amount of threads allowed to be used for download/verification work, Default: "16"</li>
     *  <li>    DAttempt <br> Amount of attempts a DownloadUtilities#reDownload() will take at re-downloading a mod, Default: "5"</li>
     *  <li>    Logger <br> Determines if Logger is turned on, Default: "True"  </li>
     *  <li>    SizeVer <br> Determines if FileSizeVerification is turned on, Default: "True".</li>
     *  <li>    HashVer <br> Determines if HashVerification is turned on, Default: "True".</li>
     *  <li>    Settings <br> Determines if user wants to use config file instead of arguments, Default: "True"</li>
     *  <li>    DefaultSettings <br> Determines if Generated Settings should be created from the Template, Default: "True"</li>
     *  <li>    Experimental <br> Unlocks Experimental features, Default: "False".</li>
     * </ul>
     *
     * @param dataType Requested type of Data.
     * @return String with Requested Data
     * @see ArgumentDecoder#getBooleanData(String) ArgumentDecoder#getBooleanData()<br>Easier Boolean data requests.
     */
    public String getData(String dataType) {
        // Note to myself: Next time use Record for this ffs, this is so stupid, yet I'm too lazy to change this now :lul:
        return switch (dataType) {
            case "Mode" -> this.Mode;
            case "Wdir" -> this.WorkingDirectory;
            case "SettingsPath" -> this.SettingsPath;
            case "Threads" -> String.valueOf(this.nThreadsCount);
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
     *  <li>    HashVer <br> Determines if HashVerification is turned on, Default: "True".</li>
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
        logger.log("· Working directory: " + this.getData("Wdir"));
        logger.log("- Full Path: " + Path.of(this.getData("Wdir")).toAbsolutePath());
        logger.log("· Program Mode: " + this.getData("Mode"));
        logger.log("· Thread count for downloads: " + this.getData("Threads"));
        logger.log("· Download attempts for re-downloads: " + this.getData("DAttempt"));
        logger.log("· Settings enabled: " + this.getData("Settings"));
        logger.log("· Default Settings from the template: " + this.getData("DefaultSettings"));
        logger.log("· Settings Path: " + this.getData("SettingsPath"));
        logger.log("- Full Path: " + Path.of(this.getData("SettingsPath")).toAbsolutePath());
        logger.log("· Hash Verification: " + this.getData("HashVer"));
        logger.log("· File Size Verification: " + this.getData("SizeVer"));
        logger.log("---------------------------------------------------------------------");
    }

    /**
     * Used to load data to ARD from Settings Manager.
     * @param SettingsData {@link Settings} object with data to load.
     */
    public void loadFromSettings(Settings SettingsData, boolean Print) {
        this.Mode = SettingsData.mode;
        this.WorkingDirectory = SettingsData.workingDirectory;
        this.nThreadsCount = SettingsData.threadCount;
        this.DownloadAttempts = SettingsData.downloadAttempts;
        this.LoggerActive = SettingsData.isLoggerActive;
        this.FileSizeVerification = SettingsData.isFileSizeVerificationActive;
        this.HashVerification = SettingsData.isHashVerificationActive;
        if (Print) { printConfiguration("Program Configuration from Settings:");}
    }
}

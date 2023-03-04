package com.kanzaji.catdownloaderlegacy.utils;

import java.util.Objects;

public class ArgumentDecoder {
    private static ArgumentDecoder instance;
    private final Logger logger = Logger.getInstance();
    private String WorkingDirectory = "";
    private String Mode = "Pack";
    private int nThreadsCount = 16;
    private int DownloadAttempts = 5;
    private boolean LoggerActive = true;
    private boolean FileSizeVerification = true;
    private boolean SumCheckVerification = true;
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
    public void decodeArguments(String[] arguments) throws IllegalArgumentException{
        logger.log("Running with arguments:");
        for (String Argument : arguments) {
            logger.log(Argument);
            if (Argument.startsWith("-WorkingDirectory:")) {
                this.WorkingDirectory = Argument.substring(18);
            }
            if (Argument.startsWith("-Mode:")) {
                this.Mode = Argument.substring(6);
                if (!validateMode(Mode)) {
                    logger.error("Wrong mode selected!");
                    logger.error("Available modes: Pack // Instance");
                    logger.error("Check Github https://github.com/Kanzaji/Cat-Downloader-Legacy for more explanation!");
                    throw new IllegalArgumentException("Incorrect Mode detected!" + this.Mode);
                }
            }
            if (Argument.startsWith("-ThreadCount:")) {
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
            if (Argument.startsWith("-DownloadAttempts:")) {
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
            if (Argument.startsWith("-SizeVerification:")) {
                if (getOffBoolean(Argument)) {
                    this.FileSizeVerification = false;
                }
            }
            if (Argument.startsWith("-SumCheckVerification:")) {
                if (getOffBoolean(Argument)) {
                    this.SumCheckVerification = false;
                }
            }
            if (Argument.startsWith("-Logger:")) {
                if (getOffBoolean(Argument)) {
                    this.LoggerActive = false;
                }
            }
            if (Argument.startsWith("-Experimental:")) {
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
    private boolean validateMode(String Mode) {
        return Objects.equals(Mode, "Pack") || Objects.equals(Mode, "Instance");
    }
    /**
     * Used to determine if provided String is one of the accepted ones for turning on a feature.<br>
     * Tries to automatically determine the position of ":". If data of your argument can contain ":" or argument itself has it, please provide the Index for a data search manually.
     * @param Argument String with argument.
     * @throws IllegalArgumentException when Index < 0;
     * @return "True" Boolean when argument has acceptable value.
     */
    private boolean getOnBoolean(String Argument) {
        return getOnBoolean(Argument, Argument.lastIndexOf(":"));
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
        return  Objects.equals(Argument.substring(Index).toLowerCase(),"true") ||
                Objects.equals(Argument.substring(Index).toLowerCase(),"enabled") ||
                Objects.equals(Argument.substring(Index).toLowerCase(),"on") ||
                Objects.equals(Argument.substring(Index).toLowerCase(),"1");
    }
    /**
     * Used to determine if provided String is one of the accepted ones for turning off a feature.<br>
     * Tries to automatically determine the position of ":". If data of your argument can contain ":" or argument itself has it, please provide the Index for a data search manually.
     * @param Argument String with argument.
     * @throws IllegalArgumentException when Index < 0;
     * @return "True" Boolean when argument has acceptable value.
     */
    private boolean getOffBoolean(String Argument) {
        return getOffBoolean(Argument, Argument.lastIndexOf(":"));
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
        return  Objects.equals(Argument.substring(Index).toLowerCase(),"false") ||
                Objects.equals(Argument.substring(Index).toLowerCase(),"disabled") ||
                Objects.equals(Argument.substring(Index).toLowerCase(),"off") ||
                Objects.equals(Argument.substring(Index).toLowerCase(),"0");
    }
    /**
     * Returns requested data from the arguments. Available data types:
     * <ul>
     *  <li>    Mode | A mode the program works in, Default: "Pack"    </li>
     *  <li>    Wdir | Working Directory of the program, Default: "."  </li>
     *  <li>    Threads | Amount of threads allowed to be used for download/verification work, Default: "16"</li>
     *  <li>    DAttempt | Amount of attempts a DownloadUtilities#reDownload() will take at re-downloading a mod, Default: "5"</li>
     *  <li>    Logger | Determines if Logger is turned on, Default: "True"  </li>
     *  <li>    SizeVer | Determines if FileSizeVerification is turned on, Default: "True".</li>
     *  <li>    SumCheckVer | Determines if SumCheckVerification is turned on, Default: "True".</li>
     * </ul>
     *
     * @param dataType Requested Type of Data.
     * @return String with Requested Data
     */
    public String getData(String dataType) {
        return switch (dataType) {
            case "Mode" -> this.Mode;
            case "Wdir" -> this.WorkingDirectory;
            case "Logger" -> String.valueOf(this.LoggerActive);
            case "Threads" -> String.valueOf(this.nThreadsCount);
            case "SizeVer" -> String.valueOf(this.FileSizeVerification);
            case "SumCheckVer" -> String.valueOf(this.SumCheckVerification);
            case "DAttempt" -> String.valueOf(this.DownloadAttempts);
            default -> "";
        };
    }
}

package com.kanzaji.catdownloaderlegacy.utils;

import java.util.Objects;

public class ArgumentDecoder {
    private static ArgumentDecoder instance;
    private final Logger logger = Logger.getInstance();
    private String WorkingDirectory;
    private String Mode;
    private String LoggerActive = "on";
    private int nThreadsCount = 16;

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
        for (String argument : arguments) {
            logger.log(argument);
            if (argument.startsWith("-WorkingDirectory:")) {
                this.WorkingDirectory = argument.substring(18);
            }
            if (argument.startsWith("-Mode:")) {
                this.Mode = argument.substring(6);
                if (!validateMode(Mode)) {
                    logger.error("Wrong mode selected!");
                    logger.error("Available modes: Pack // Instance");
                    logger.error("Check Github https://github.com/Kanzaji/Cat-Downloader-Legacy for more explanation!");
                    throw new IllegalArgumentException("Incorrect Mode detected!" + this.Mode);
                }
            }
            if (argument.startsWith("-Logger:")) {
                if (Objects.equals(argument.substring(8), "off")) {
                    this.LoggerActive = "off";
                }
            }
            if (argument.startsWith("-ThreadCount:")) {
                try {
                    this.nThreadsCount = Integer.parseInt(argument.substring(13));
                    if (this.nThreadsCount < 1) {
                        logger.warn("Value below 1 was passed to ThreadCount argument! Defaulting to 16.");
                        this.nThreadsCount = 16;
                    }
                } catch (IllegalArgumentException e) {
                    logger.warn("Non-int value was passed to ThreadCount argument! Defaulting to 16.");
                    this.nThreadsCount = 16;
                }
            }
        }
    }
    private boolean validateMode(String Mode) {
        if (
                Objects.equals(Mode, "Pack") ||
                Objects.equals(Mode, "Instance")
            ) {return true;}
        return false;
    }

    /**
     * Returns requested data from the arguments. Available data types:
     * <ul>
     *  <li>    Mode | A mode the program works in, Default: "Pack"    </li>
     *  <li>    Wdir | Working Directory of the program, Default: "."  </li>
     *  <li>    Logger | Determines if Logger is active or not, Default: "on"  </li>
     *  <li>    Threads | Amount of threads allowed to be used for downloads/verification work.</li>
     * </ul>
     *
     * @param dataType Requested Type of Data.
     * @return String with Requested Data
     */
    public String getData(String dataType ) {
        return switch (dataType) {
            case "Mode" -> this.Mode;
            case "WDir" -> this.WorkingDirectory;
            case "Logger" -> this.LoggerActive;
            case "Threads" -> String.valueOf(this.nThreadsCount);
            default -> "";
        };
    }
}

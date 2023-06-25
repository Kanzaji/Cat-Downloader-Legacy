package com.kanzaji.catdownloaderlegacy.loggers;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class LoggerCustom implements ILogger {
    private final Logger logger = Logger.getInstance();
    private String name = "default";
    public LoggerCustom(String name) {
        this.name = name;
    }

    /**
     * Used to initialize Logger. Creates new log file and overrides old one if present
     */
    @Override
    public void init() {
        logger.init();
    }

    /**
     * Used to finish initialization of the Logger. Handles function of Stockpiling of the logs, and moving the log file to a new location.
     * @throws IllegalStateException when reading attributes of the compressed log files is not possible.
     * @throws IOException when IO Exception occurs.
     */
    public void postInit() throws IOException {
        logger.postInit();
    }

    /**
     * Used to get a path to a log file.
     * @return String with absolute path of a log file.
     */
    @Override
    public String getLogPath() {
        return logger.getLogPath();
    }

    /**
     * Used to disable Logger and remove the log file.
     * @throws IOException when log deletion failed.
     */
    @Override
    public void exit() throws IOException {
        logger.exit();
    }

    /**
     * Custom Log method that allows to set level of log, message and attach throwable.
     * Available levels:
     * <ul>
     *     <li>0 | LOG</li>
     *     <li>1 | WARN</li>
     *     <li>2 | ERROR</li>
     *     <li>3 | CRITICAL</li>
     * </ul>
     * @param msg String message to log to a log file.
     * @param type Int between 0 and 2 specifying selected level. Defaults to 0. (Nullable)
     * @param throwable Exception to log. (Nullable)
     */
    @Override
    public void logCustom(String msg, int type, @Nullable Throwable throwable) {
        logger.logCustom("[" + name + "] " + msg, type, throwable);
    }
}

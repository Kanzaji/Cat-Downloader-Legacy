package com.kanzaji.catdownloaderlegacy.loggers;

import org.jetbrains.annotations.Nullable;
import java.io.IOException;

interface ILogger {
    /**
     * Used to initialize Logger. Creates new log file and overrides old one if present.
     * Has to be implemented manually.
     */
    void init();

    /**
     * Used to get a path to a log file.
     * Has to be implemented manually.
     * @return String with absolute path of a log file.
     */
    String getLogPath();

    /**
     * Used to disable Logger and remove log file.
     * Has to be implemented manually.
     * @throws IOException when log deletion failed.
     */
    void exit() throws IOException;


    /**
     * Logs a message to a log file.
     * @param msg String message to log.
     */
    default void log(String msg) {
        this.logType(msg, 0);
    }

    /**
     * Logs a message with level WARN to a log file.
     * @param msg String message to log as WARN.
     */
    default void warn(String msg) {
        this.logType(msg, 1);
    }

    /**
     * Logs a message with level ERROR to a log file.
     * @param msg String message to log as ERROR.
     */
    default void error(String msg) {
        this.logType(msg, 2);
    }

    /**
     * Logs a message with specified level to a log file.<br>
     * Available levels:
     * <ul>
     *     <li>0 | LOG</li>
     *     <li>1 | WARN</li>
     *     <li>2 | ERROR</li>
     * </ul>
     * @param msg String message to log with specified level.
     * @param type Int between 0 and 2 specifying selected level. Out of range defaults to 0.
     */
    default void logType(String msg, int type) {
        this.logCustom(msg, type, null);
    }

    /**
     * Logs a message with ERROR level and Stacktrace of Exception into a log.
     * @param msg String message attached to an Exception.
     * @param throwable Exception to log.
     */
    default void logStackTrace(String msg, Throwable throwable) {
        this.logCustom(msg, 2, throwable);
    }

    /**
     * Custom Log method that allows to set level of log, message and attach throwable.
     * Has to be implemented manually.
     * Available levels:
     * <ul>
     *     <li>0 | LOG</li>
     *     <li>1 | WARN</li>
     *     <li>2 | ERROR</li>
     * </ul>
     * @param msg String message to log to a log file.
     * @param type Int between 0 and 2 specifying selected level. Defaults to 0. (Nullable)
     * @param throwable Exception to log. (Nullable)
     */
    void logCustom(String msg, int type, @Nullable Throwable throwable);
}

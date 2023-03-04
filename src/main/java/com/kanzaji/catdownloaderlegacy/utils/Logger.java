package com.kanzaji.catdownloaderlegacy.utils;

import org.jetbrains.annotations.Nullable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.Files;
import java.io.IOException;

public class Logger {
    private static Logger instance = null;
    private boolean disabled = false;
    Path logFile = Path.of(".", "Cat-Downloader.log");

    /**
     * Used to get an instance of the Logger. Creates new one at first use.
     * @return Reference to an instance of the Logger.
     */
    public static Logger getInstance() {
        if (instance == null) {
            instance = new Logger();
        }
        return instance;
    }

    /**
     * Used to get a path to a log file.
     * @return String with absolute path of a log file.
     */
    public String getLogPath() {
        if (this.logFile == null) {
            return null;
        }
        return this.logFile.toAbsolutePath().toString();
    }

    /**
     * Used to initialize Logger. Creates new log file and overrides old one if present.
     */
    public void init() {
        try {
            if (Files.deleteIfExists(this.logFile)) {
                Files.createFile(this.logFile);
                this.log("Old Log file found! \"" + this.logFile.toAbsolutePath() + "\" file replaced with empty one.");
            } else {
                Files.createFile(this.logFile);
                this.log("\"" + this.logFile.toAbsolutePath() + "\" file created.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.log("Logger Initialization completed.");
    }

    /**
     * Used to disable Logger and remove log file.
     * Logger *can not* be re-activated.
     * @throws IOException when log deletion failed.
     */
    public void exit() throws IOException {
        System.out.println("LOGGER WAS DISABLED. If any errors occur they will not be logged and can be not shown in the console! Use at your own risk.");
        this.disabled = true;
        Files.deleteIfExists(this.logFile);
        this.logFile = null;
    }

    /**
     * Logs a message to a log file.
     * @param msg String message to log.
     */
    public void log(String msg) {
        this.logType(msg, 0);
    }

    /**
     * Logs a message with level WARN to a log file.
     * @param msg String message to log as WARN.
     */
    public void warn(String msg) {
        this.logType(msg, 1);
    }

    /**
     * Logs a message with level ERROR to a log file.
     * @param msg String message to log as ERROR.
     */
    public void error(String msg) {
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
    public void logType(String msg, int type) {
        this.log(msg, type, null);
    }

    /**
     * Logs a message with ERROR level and Stacktrace of Exception into a log.
     * @param msg String message attached to an Exception.
     * @param throwable Exception to log.
     */
    public void logStackTrace(String msg, Throwable throwable) {
        this.logCustom(msg, 2, throwable);
    }

    /**
     * Custom Log method that allows to set level of log, message and attach throwable.
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
    public void logCustom(String msg, int type, @Nullable Throwable throwable) {
        this.log(msg, type, throwable);
    }

    /**
     * Integral Method used to log messages and exceptions to a log file on different levels.
     * Available levels:
     * <ul>
     *     <li>0 | LOG</li>
     *     <li>1 | WARN</li>
     *     <li>2 | ERROR</li>
     * </ul>
     * @param msg String message
     * @param type Int level (Nullable)
     * @param error Exception (Nullable)
     */
    private void log(String msg, int type,@Nullable Throwable error) {
        if (disabled) {
            System.out.println(msg);
            if (error != null) {
                error.printStackTrace();
            }
            return;
        }
        String Type = switch (type) {
            case 1 -> "WARN";
            case 2 -> "ERROR";
            default -> "INFO";
        };

        try {
            Files.writeString(this.logFile, "[" + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS").format(new Date()) + "] [" + Type + "] " + msg + "\n", StandardOpenOption.APPEND);
            if (error != null) {
                StackTraceElement[] stackTraceList = error.getStackTrace();
                StringBuilder stackTrace = new StringBuilder();
                for (StackTraceElement stackTraceElement : stackTraceList) {
                    stackTrace.append("    at ").append(stackTraceElement).append("\n");
                }
                Files.writeString(this.logFile, "[" + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS").format(new Date()) + "] [" + Type + "] " + error + "\n" + stackTrace + "\n", StandardOpenOption.APPEND);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

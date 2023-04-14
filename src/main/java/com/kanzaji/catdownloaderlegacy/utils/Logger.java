package com.kanzaji.catdownloaderlegacy.utils;

import org.jetbrains.annotations.Nullable;

import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.IOException;

public class Logger {
    private static final ArgumentDecoder ARD = ArgumentDecoder.getInstance();
    private static final class InstanceHolder {private static final Logger instance = new Logger();}
    private Logger() {}
    private boolean disabled = false;
    private Path LogFile = Path.of("Cat-Downloader.log");

    /**
     * Used to get an instance of the Logger.
     * @return Reference to an instance of the Logger.
     */
    public static Logger getInstance() {
        return InstanceHolder.instance;
    }

    /**
     * Used to get a path to a log file.
     * @return String with absolute path of a log file.
     */
    public String getLogPath() {
        if (this.LogFile == null) {
            return null;
        }
        return this.LogFile.toAbsolutePath().toString();
    }

    /**
     * Used to initialize Logger. Creates new log file and overrides old one if present.
     */
    public void init() {
        // Gives an option to re-enable the Logger if I want to add this functionality in the future.
        if (disabled) {disabled = false;}
        try {
            if (Files.exists(this.LogFile)) {
                Files.move(this.LogFile, Path.of("Cat-Downloader Archived.log"), StandardCopyOption.REPLACE_EXISTING);
                Files.createFile(this.LogFile);
                this.log("Old Log file found! \"" + this.LogFile.toAbsolutePath() + "\" file has been archived for now.");
            } else {
                Files.createFile(this.LogFile);
                this.log("\"" + this.LogFile.toAbsolutePath() + "\" file created.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.log("Logger Initialization completed.");
    }

    public void postInit() throws IOException {
        this.log("Post-Initialization of Logger started!");
        Path archivedLog = Path.of("Cat-Downloader Archived.log");

        // Move Log files to the log Path if specified.
        if (!FileUtils.getFolder(Path.of(ARD.getLogPath(), ".")).toString().equals(FileUtils.getFolder(this.LogFile).toString())) {
            if (ARD.shouldStockPileLogs()) {
                // Cat-Downloader Archived.log handling.
                if (Files.exists(Path.of(ARD.getLogPath(), "Cat-Downloader Archived.log"))) {
                    this.warn("Found old pre-full-archive log file in specified Path! This might signal a crash in the last post-init phase of the logger!");
                    this.warn("The log file is going to be saved as Unknown.log.gz for future inspection.");
                    String unknownName = FileUtils.rename(Path.of(ARD.getLogPath(), "Cat-Downloader Archived.log"), "unknown.log");
                    FileUtils.compressToGz(Path.of(ARD.getLogPath(), unknownName), true);
                }
                if (Files.exists(archivedLog)) {
                    this.log("Moving archived log to new location...");
                    Files.move(archivedLog, Path.of(ARD.getLogPath(), "Cat-Downloader Archived.log"));
                    archivedLog = Path.of(ARD.getLogPath(), "Cat-Downloader Archived.log");
                    if (ARD.shouldCompressLogs()) {
                        this.log("Moved archived log to new location! Compressing...");
                        FileUtils.compressToGz(archivedLog, DateUtils.getCurrentFullDate(), true);
                    } else {
                        this.log("Moved archived log to new location! Changing the filename...");
                        FileUtils.rename(archivedLog, DateUtils.getCurrentFullDate() + ".log");
                    }
                    this.log("Log has been archived!");
                }

                // Cat-Downloader.log handling.
                if (Files.exists(Path.of(ARD.getLogPath(), "Cat-Downloader.log"))) {
                    this.log("Old log file found! Archiving the log file...");
                    String archivedName = FileUtils.rename(Path.of(ARD.getLogPath(), "Cat-Downloader.log"), "Cat-Downloader Archived.log");
                    if (ARD.shouldCompressLogs()) {
                        FileUtils.compressToGz(Path.of(ARD.getLogPath(), archivedName), DateUtils.getCurrentFullDate(), true);
                    } else {
                        FileUtils.rename(Path.of(ARD.getLogPath(), archivedName), DateUtils.getCurrentFullDate() + ".log");
                    }
                    this.log("Log has been archived!");
                }
            }

            if (Files.exists(this.LogFile)) {
                Path newLogPath = Path.of(ARD.getLogPath(), "Cat-Downloader.log");
                this.log("Moving current log file to new location...");
                if (Files.exists(newLogPath)) {
                    this.error("Found non-archived log in the final destination, what should not happen at this point of the process!");
                    this.error("Archiving the log under unknown_latest.log.gz name for future inspection.");
                    String unknownName = FileUtils.rename(newLogPath, "unknown_latest.log");
                    FileUtils.compressToGz(Path.of(ARD.getLogPath(), unknownName), true);
                }
                Files.move(this.LogFile, newLogPath);
                this.LogFile = newLogPath;
                this.log("Moved current log to the new Location!");
            } else {
                this.error("The log file doesn't exists before even archiving??? Something is horribly wrong...");
            }
        }

        this.log("Post-Initialization of Logger finished!");
    }

    /**
     * Used to disable Logger and remove log file.
     * Logger *can not* be re-activated.
     * @throws IOException when log deletion failed.
     */
    public void exit() throws IOException {
        System.out.println("LOGGER WAS DISABLED. If any errors occur they will not be logged and can be not shown in the console! Use at your own risk.");
        this.disabled = true;
        Files.deleteIfExists(this.LogFile);
        this.LogFile = null;
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
            Files.writeString(this.LogFile, "[" + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS").format(new Date()) + "] [" + Type + "] " + msg + "\n", StandardOpenOption.APPEND);
            if (error != null) {
                StackTraceElement[] stackTraceList = error.getStackTrace();
                StringBuilder stackTrace = new StringBuilder();
                for (StackTraceElement stackTraceElement : stackTraceList) {
                    stackTrace.append("    at ").append(stackTraceElement).append("\n");
                }
                Files.writeString(this.LogFile, "[" + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS").format(new Date()) + "] [" + Type + "] " + error + "\n" + stackTrace + "\n", StandardOpenOption.APPEND);
            }
        } catch (NoSuchFileException e) {
            this.init();
            this.error("Log file seems to had been deleted! Created another copy, but the rest of the log file has been lost.");
            this.error("Catching last message...");
            this.log(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

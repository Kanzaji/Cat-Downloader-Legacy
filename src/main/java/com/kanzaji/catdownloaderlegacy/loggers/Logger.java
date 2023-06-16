package com.kanzaji.catdownloaderlegacy.loggers;

import com.kanzaji.catdownloaderlegacy.ArgumentDecoder;
import com.kanzaji.catdownloaderlegacy.utils.DateUtils;
import com.kanzaji.catdownloaderlegacy.utils.FileUtils;
import org.jetbrains.annotations.Nullable;

import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

public class Logger implements ILogger {
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
        Path logPath = Path.of(ARD.getLogPath());
        Path archivedLog = Path.of("Cat-Downloader Archived.log");
        Path logInLogPath = Path.of(logPath.toString(), "Cat-Downloader.log");
        Path archivedLogInLogPath = Path.of(logPath.toString(), "Cat-Downloader Archived.log");

        // Move Log files to the log Path if specified.
        if (!FileUtils.getFolder(Path.of(logPath.toString(), ".")).toString().equals(FileUtils.getFolder(this.LogFile).toString())) {
            if (Files.notExists(logPath)) {
                this.log("Custom path for logs has been specified, but it doesn't exists! Creating \"" + logPath.toAbsolutePath() + "\".");
                Files.createDirectory(logPath);
            } else {
                this.log("Custom path for logs has been specified: \"" + logPath.toAbsolutePath() + "\".");
            }
            if (ARD.shouldStockPileLogs()) {
                // Cat-Downloader Archived.log handling.
                if (Files.exists(archivedLogInLogPath)) {
                    this.warn("Found old pre-full-archive log file in specified Path! This might signal a crash in the last post-init phase of the logger!");
                    this.warn("The log file is going to be saved as Unknown.log.gz for future inspection.");
                    String unknownName = FileUtils.rename(archivedLogInLogPath, "unknown.log");
                    FileUtils.compressToGz(Path.of(logPath.toString(), unknownName), true);
                }
                if (Files.exists(archivedLog)) {
                    this.log("Found archived log in working directory! Moving archived log to new location...");
                    Files.move(archivedLog, archivedLogInLogPath);
                    archivedLog = archivedLogInLogPath;
                    if (ARD.shouldCompressLogs()) {
                        this.log("Moved archived log to new location! Compressing...");
                        FileUtils.compressToGz(archivedLog, DateUtils.getCurrentFullDate() + ".log", true);
                    } else {
                        this.log("Moved archived log to new location! Changing the filename...");
                        FileUtils.rename(archivedLog, DateUtils.getCurrentFullDate() + ".log");
                    }
                    this.log("Log has been archived!");
                }

                // Cat-Downloader.log handling.
                if (Files.exists(logInLogPath)) {
                    this.log("Old log file found! Archiving the log file...");
                    String archivedName = FileUtils.rename(logInLogPath, "Cat-Downloader Archived.log");
                    Path archivedFile = Path.of(logPath.toString(), archivedName);
                    if (ARD.shouldCompressLogs()) {
                        FileUtils.compressToGz(archivedFile, DateUtils.getCurrentFullDate() + ".log", true);
                    } else {
                        FileUtils.rename(archivedFile, DateUtils.getCurrentFullDate() + ".log");
                    }
                    this.log("Log has been archived!");
                }
            }

            if (Files.exists(this.LogFile)) {
                this.log("Moving currently active log file to new location...");
                if (Files.exists(logInLogPath)) {
                    this.error("Found non-archived log in the final destination, what should not happen at this point of the process!");
                    this.error("Archiving the log under unknown_latest.log.gz name for future inspection.");
                    String unknownName = FileUtils.rename(logInLogPath, "unknown_latest.log");
                    FileUtils.compressToGz(Path.of(logPath.toString(), unknownName), true);
                }
                Files.move(this.LogFile, logInLogPath);
                this.LogFile = logInLogPath;
                this.log("Moved currently active log to the new Location: \"" + this.LogFile.toAbsolutePath() + "\".");
            } else {
                this.error("The log file doesn't exists before even archiving??? Something is horribly wrong...");
            }
        } else {
            this.log("No custom path for Logs has been specified, using working directory for logging!");
            if (Files.exists(archivedLog)) {
                if(ARD.shouldStockPileLogs()) {
                    this.log("Old log file found! Archiving the log file...");
                    if (ARD.shouldCompressLogs()) {
                        FileUtils.compressToGz(archivedLog, DateUtils.getCurrentFullDate() + ".log", true);
                    } else {
                        FileUtils.rename(archivedLog, DateUtils.getCurrentFullDate() + ".log");
                    }
                    this.log("Log has been archived!");
                } else {
                    this.log("Stockpiling of the logs has been disabled! Deleting old log file...");
                    Files.delete(archivedLog);
                    this.log("Old log file has been deleted.");
                }
            }
        }

        if(ARD.shouldStockPileLogs()) {
            this.log("Stockpiling logs is enabled! Stockpile limit is " + ((ARD.getLogStockSize() == 0)? "infinite!": ARD.getLogStockSize()));
            List<Path> archivedLogs = new LinkedList<>();
            try(Stream<Path> directoryList = Files.list(logPath)) {
                directoryList.forEach((File) -> {
                    if(File.getFileName().toString().endsWith(".gz") && File.getFileName().toString().contains(".log")) {
                        archivedLogs.add(File);
                    }
                });
            }

            while (archivedLogs.size() > ARD.getLogStockSize()) {
                this.log("Limit of stockpile has been reached! Deleting the oldest file...");
                AtomicReference<Path> test = new AtomicReference<>();
                archivedLogs.forEach((File) -> {
                    try {
                        BasicFileAttributes currentFile = Files.readAttributes(File, BasicFileAttributes.class);
                        if (test.get() == null) {
                            test.set(File);
                        } else if (currentFile.creationTime().compareTo(Files.readAttributes(test.get(), BasicFileAttributes.class).creationTime()) < 0) {
                            test.set(File);
                        }
                    } catch (Exception e) {
                        this.logStackTrace("Unable to read attributes of file: " + File.toAbsolutePath(), e);
                        throw new IllegalStateException("Unable to read attributes of the file!");
                    }
                });
                archivedLogs.remove(test.get());
                if (Files.deleteIfExists(test.get())) {
                    this.log(test.get().toAbsolutePath() + " has been deleted!");
                } else {
                    this.error(test.get().toAbsolutePath() + " was meant to be deleted, but it's missing! Something is not right... This is not critical error however.");
                }
            }
        }

        this.log("Post-Initialization of Logger finished!");
    }

    /**
     * Used to disable Logger and remove log file.
     * @throws IOException when log deletion failed.
     */
    public void exit() throws IOException {
        System.out.println("LOGGER WAS DISABLED. If any errors occur they will not be logged and can be not shown in the console! Use at your own risk.");
        this.disabled = true;
        Files.deleteIfExists(this.LogFile);
        this.LogFile = null;
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
        if (disabled) {
            System.out.println(msg);
            if (throwable != null) {
                throwable.printStackTrace();
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
            if (throwable != null) {
                StackTraceElement[] stackTraceList = throwable.getStackTrace();
                StringBuilder stackTrace = new StringBuilder();
                for (StackTraceElement stackTraceElement : stackTraceList) {
                    stackTrace.append("    at ").append(stackTraceElement).append("\n");
                }
                Files.writeString(this.LogFile, "[" + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS").format(new Date()) + "] [" + Type + "] " + throwable + "\n" + stackTrace + "\n", StandardOpenOption.APPEND);
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
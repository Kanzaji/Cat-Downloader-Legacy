package com.kanzaji.catdownloader.utils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.kanzaji.catdownloader.SettingsManager;

import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.Files;

public class Logger {
    private static Logger instance = null;

    Path logFile = Path.of(".", "Launcher.log");

    public static Logger getInstance() {
        if (instance == null) {
            instance = new Logger();
        }
        return instance;
    }

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

    public void postInit() {
        this.log("Logger Post-Initialization sequence started.");

        if (SettingsManager.settingsAvailable()) {

            this.log("Settings were initiated, moving log file to \"" + SettingsManager.getSettingsPath() + "\"");
            Path newLogFile = Path.of(SettingsManager.getSettingsPath().toString(), "Launcher.log");

            if (newLogFile.toFile().exists()) {
                this.log("Old Log file found! That file is going to be replaced by the new file generated in initialization.");
            }

            try {
                Files.copy(this.logFile, newLogFile, StandardCopyOption.REPLACE_EXISTING);
                Files.delete(this.logFile);
            } catch (IOException e) {
                this.logStackTrace("Failed to move log file to appdata!", e);
            }

            this.logFile = newLogFile;
            this.log("New log file moved to appdata. Log file present at \"" + this.logFile.toAbsolutePath() + "\"");

            return;
        }

        this.warn("Settings unavailable, log file present at \"" + this.logFile.toAbsolutePath() + "\"");
        this.log("Logger Post-Initialization sequence completed.");
    }

    public void log(String msg) {
        this.logType(msg, 0);
    }

    public void warn(String msg) {
        this.logType(msg, 1);
    }

    public void error(String msg) {
        this.logType(msg, 2);
    }

    public void logType(String msg, int type) {
        this.log(msg, type, null);
    }

    public void logStackTrace(String msg, Throwable throwable) {
        this.logCustom(msg, 2, throwable);
    }

    public void logCustom(String msg, int type, Throwable throwable) {
        this.log(msg, type, throwable);
    }

    private void log(String msg, int type, Throwable error) {
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

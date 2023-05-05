package com.kanzaji.catdownloaderlegacy.loggers;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class LoggerCustom implements ILogger {
    private final Logger logger = Logger.getInstance();
    private String name = "default";
    public LoggerCustom(String name) {
        this.name = name;
    }

    @Override
    public void init() {
        logger.init();
    }
    public void postInit() throws IOException {
        logger.postInit();
    }

    @Override
    public String getLogPath() {
        return logger.getLogPath();
    }

    @Override
    public void exit() throws IOException {
        logger.exit();
    }

    @Override
    public void logCustom(String msg, int type, @Nullable Throwable throwable) {
        logger.logCustom("[" + name + "] " + msg, type, throwable);
    }
}

package com.kanzaji.catdownloaderlegacy.utils;

import com.kanzaji.catdownloaderlegacy.jsons.Settings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.nio.file.Files;
import java.nio.file.Path;

public class SettingsManager {
    private static final Logger logger = Logger.getInstance();
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final ArgumentDecoder ARD = ArgumentDecoder.getInstance();
    private static final Path SettingsFile = Path.of("Cat-Downloader-Legacy Settings.json");
    private static SettingsManager instance = null;

    /**
     * Used to get an instance of the SettingsManager. Creates new one at first use.
     * @return Reference to an instance of the SettingsManager.
     */
    public static SettingsManager getInstance() {
        if (instance == null) {
            instance = new SettingsManager();
        }
        return instance;
    }
    /**
     * Used to initialize SettingsManager.
     * Creates a template of the Settings file, and sets everything up if `DefaultSettingsFromArguments` is true.
     */
    public void init() {
        logger.log("SettingsManager initialization started.");
        if (Files.exists(SettingsFile)) {
            logger.log("Settings file found! Parsing data...");
            //TODO: Parse config data lol
        } else {
            logger.log("No settings file found!");

        }
    }

    public boolean validateSettings() {
        return false;
    }
}

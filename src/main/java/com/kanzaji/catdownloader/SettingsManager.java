package com.kanzaji.catdownloader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.kanzaji.catdownloader.jsons.Settings;
import com.kanzaji.catdownloader.utils.Logger;

import java.io.IOException;
import java.nio.file.*;

public class SettingsManager {
    private static SettingsManager instance = null;
    public boolean isWindows = System.getProperty("os.name").startsWith("Windows");
    public String fsp = System.getProperty("file.separator"); // File Separator
    boolean initSuccessful = false;
    Path LocalDir;
    Path Kanzaji;
    Path CatDownloader;
    Path SettingsFile;

    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    Logger logger = Logger.getInstance();

    public static SettingsManager getInstance() {
        if (instance == null) {
            instance = new SettingsManager();
        }
        return instance;
    }

    public static boolean settingsAvailable() {
        if (instance == null) {
            return false;
        }
        return instance.initSuccessful;
    }

    public static Path getSettingsPath() {
        return instance.CatDownloader.toAbsolutePath();
    }

    public Settings getSettings() throws JsonSyntaxException, IOException {
        if (!initSuccessful) {
            return null;
        }
        return gson.fromJson(Files.readString(this.SettingsFile), Settings.class);
    }

    public boolean validateSettings() {
        return false;
    }

    public void init() {
        logger.log("SettingsManager initialization started.");

        if (!isWindows) {
            logger.log("Settings for your operating system (" + System.getProperty("os.name") + ") are not available yet! Falling back to \"mod download only\" mode.");
            return;
        }

        this.LocalDir = Path.of(System.getProperty("user.home") + fsp + "Appdata" + fsp + "Local");
        this.Kanzaji = Path.of(LocalDir.toAbsolutePath().toString(), "Kanzaji");
        this.CatDownloader = Path.of(Kanzaji.toAbsolutePath().toString(), "CatDownloader");

        if (!this.Kanzaji.toFile().exists()) {
            logger.log("Kanzaji folder in \"" + this.LocalDir.toAbsolutePath() + "\" not found! Creating...");
            try {
                Files.createDirectory(this.Kanzaji);
            } catch (IOException e) {
                logger.logStackTrace("Failed to create Kanzaji folder in Appdata!", e);
                return;
            }
            logger.log("\"" + this.Kanzaji.toAbsolutePath() + "\" created.");
        }

        if (!this.CatDownloader.toFile().exists()) {

            logger.log("\"" + this.CatDownloader.toAbsolutePath() + "\" not found! Creating \"CatDownloader\" Folder and template of settings.");

            try {
                Files.createDirectory(this.CatDownloader);
            } catch (IOException e) {
                logger.logStackTrace("Failed to create CatDownloader folder in Appdata!", e);
                return;
            }

            Settings data = new Settings(); // Creating new Settings Object with default values
            data.launcher = new Settings.Launcher();
            data.launcher.path = Path.of(".").toAbsolutePath().toString();
            data.launcher.type = "Legacy";
            data.cached = false;

            this.SettingsFile = Path.of(this.CatDownloader.toAbsolutePath().toString(), "settings.json");

            try {
                Files.createFile(this.SettingsFile);
                Files.writeString(this.SettingsFile, gson.toJson(data));
            } catch (IOException e) {
                logger.logStackTrace("Failed to create settings file in Appdata!", e);
                return;
            }

            logger.log("Default settings file created.");

        } else {
            logger.log("CatDownloader data folder found!");
        }

        this.initSuccessful = true;
        logger.log("SettingsManager initialization completed.");
    }
}

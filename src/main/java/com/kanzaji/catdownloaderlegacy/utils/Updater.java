package com.kanzaji.catdownloaderlegacy.utils;

import com.kanzaji.catdownloaderlegacy.ArgumentDecoder;
import com.kanzaji.catdownloaderlegacy.loggers.LoggerCustom;

import static com.kanzaji.catdownloaderlegacy.CatDownloader.VERSION;
import static com.kanzaji.catdownloaderlegacy.CatDownloader.REPOSITORY;

import com.google.gson.Gson;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.net.ssl.HttpsURLConnection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * This class holds all Update Checking + Updating related methods.
 */
public class Updater {
    private static final String GithubAPIUrl = "https://api.github.com/repos/" + REPOSITORY.replaceFirst("https://github.com/", "");
    private static final Gson gson = new Gson();
    private static final LoggerCustom logger = new LoggerCustom("Updater");
    private static boolean isUpdated = false;

    /**
     * This method calls GitHub repository and returns {@code true}, if newer version of the app is available, additionally setting up GUI for user choice.
     * @return Boolean depending on the result of update check.
     */
    public static boolean checkUpdates() throws IOException {
        // TODO: Finish Updater!
        if (!ArgumentDecoder.getInstance().isUpdaterActive()) {
            logger.warn("Updater is disabled! Checking for updates is not possible.");
            return false;
        }

        logger.log("Checking for app updates...");
        // Getting the latest version from the GitHub API!
        // This has... weird rate-limit. Might cause issues, but I doubt someone is going to run this app like... over 100 times in an hour. And if so, it's going to update a bit later that's it.
        HttpsURLConnection response = (HttpsURLConnection) new URL(GithubAPIUrl + "/releases/latest").openConnection();
        response.setRequestProperty("Accept", "application/vnd.github+json");
        response.setRequestProperty("X-GitHub-Api-Version", "2022-11-28");

        try (BufferedReader in = new BufferedReader(new InputStreamReader(response.getInputStream(), StandardCharsets.UTF_8))) {
            UpdaterData.releaseData responseData = gson.fromJson(in, UpdaterData.releaseData.class);
            if (Objects.isNull(responseData)) {
                throw new NullPointerException("Null data returned from the API!");
            } else {
                if (Objects.isNull(responseData.tag_name) || Objects.isNull(responseData.html_url) || Objects.isNull(responseData.published_at) || Objects.isNull(responseData.assets)) {
                    throw new NullPointerException("Null data in required fields returned from the API!\n" + responseData);
                }
                if (compareVersions(VERSION, responseData.tag_name)) {
                    logger.log("App is updated! Running version " + VERSION + " when latest version is " + responseData.tag_name);
                    return false;
                } else {
                    logger.warn("New version of the app found! Getting GUI ready for asking a user to update.");
                    logger.warn("Current version: " + VERSION);
                    logger.warn("Latest version: " + responseData.tag_name);
                    UpdaterGUI.startGUI();
                    UpdaterGUI.setUpdateVersion(VERSION, responseData.tag_name);
                    UpdaterGUI.setChangelogText(responseData.body.replaceAll("###", " - ").replaceAll("\\*\\*", ""));
                    UpdaterGUI.setupButtons();
                    return true;
                }
            }
        }
    }

    /**
     * Used to check if Update is finished. If Updater is disabled, returns always true.
     * @return {@link Boolean} with the status of the update. True when finished or Updater is disabled.
     */
    public static boolean isUpdated() {
        return !ArgumentDecoder.getInstance().isUpdaterActive() || isUpdated;
    }

    /**
     * This method is used to compare current version numbers with a dot (".") as a separator.
     * It does not support version numbers with characters.
     * @param currentVersion Current Version to compare.
     * @param latestVersion Latest Version to compare against.
     * @return Boolean {@code true} when current version is the same or higher than latest and when it contains "develop" at the end, otherwise {@code false}.
     * @throws NumberFormatException when version with non-Number character is passed!
     */
    public static boolean compareVersions(String currentVersion, String latestVersion) throws NumberFormatException  {
        return compareVersions(currentVersion, latestVersion, "\\.");
    }

    /**
     * This method is used to compare current version numbers with a given separator.<br>
     * It does not support version numbers with characters.
     * @param currentVersion Current Version to compare.
     * @param latestVersion Latest Version to compare against.
     * @param separator A {@link String} used to separate numbers in passed version schema (Regex).
     * @return Boolean {@code true} when current version is the same or higher than latest and when it contains "develop" at the end, otherwise {@code false}.
     * @throws NumberFormatException when version with non-Number character is passed!
     */
    public static boolean compareVersions(String currentVersion, String latestVersion, String separator) throws NumberFormatException {
        if (currentVersion.toLowerCase().endsWith("develop")) return true;

        List<String> currentVersionMap = new LinkedList<>(Arrays.stream(currentVersion.split(separator)).toList());
        List<String> latestVersionMap = new LinkedList<>(Arrays.stream(latestVersion.split(separator)).toList());
        boolean equal = true;

        for (int i = 0; i < (Math.min(currentVersionMap.size(), latestVersionMap.size())); i++) {
            try {
                int version1 = Integer.parseInt(currentVersionMap.get(i));
                int version2 = Integer.parseInt(latestVersionMap.get(i));
                if (version2 != version1) {
                    equal = false;
                    if (version2 > version1) {
                        return false;
                    }
                }
            } catch (Exception E) {
                throw new NumberFormatException("Non-Number character found in the version schema! (current = " + currentVersion + "; latest = " + latestVersion + ";) Comparison can't continue.");
            }
        }

        // If equal and latest version map size is higher, return false
        // If not equal, return true. (If latest was higher it would return false earlier on)
        // If Equal and current version map size is higher / equal, return true
        // Yes this is here just so I will not mess this up;
        return !equal || currentVersionMap.size() >= latestVersionMap.size();
    }

    /**
     * This class holds all data structures for the {@link Updater}.
     */
    private static class UpdaterData {
        // Why I added a toString() methods here? I have no idea, I was just experimenting with them.
        private static class releaseData {
            @Contract(pure = true)
            @Override
            public @NotNull String toString() {
                return
                    "{" +
                    "\n    \"body\": " + body +
                    "\n    \"html_url\": " + html_url +
                    ",\n    \"tag_name\": " + tag_name +
                    ",\n    \"published_at\": " + published_at  +
                    ",\n    \"assets\": " + Arrays.toString(assets) +
                    "\n}";
            }
            public String body;
            public String html_url;
            public String tag_name;
            public String published_at;
            public Assets[] assets;

            private static class Assets {
                @Contract(pure = true)
                @Override
                public @NotNull String toString() {
                    return "{\n        \"browser_download_url\": " + browser_download_url + ",\n        \"name\": " + name + ",\n        \"size\": " + size + "\n    }";
                }
                public String browser_download_url;
                public String name;
                public int size;

            }
        }
    }
}

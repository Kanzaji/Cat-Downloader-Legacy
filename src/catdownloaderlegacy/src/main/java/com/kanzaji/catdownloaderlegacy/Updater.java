/**************************************************************************************
 * MIT License                                                                        *
 *                                                                                    *
 * Copyright (c) 2023. Kanzaji                                                        *
 *                                                                                    *
 * Permission is hereby granted, free of charge, to any person obtaining a copy       *
 * of this software and associated documentation files (the "Software"), to deal      *
 * in the Software without restriction, including without limitation the rights       *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell          *
 * copies of the Software, and to permit persons to whom the Software is              *
 * furnished to do so, subject to the following conditions:                           *
 *                                                                                    *
 * The above copyright notice and this permission notice shall be included in all     *
 * copies or substantial portions of the Software.                                    *
 *                                                                                    *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR         *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,           *
 * FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE       *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER             *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,      *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE      *
 * SOFTWARE.                                                                          *
 **************************************************************************************/

package com.kanzaji.catdownloaderlegacy;

import com.kanzaji.catdownloaderlegacy.data.Settings;
import com.kanzaji.catdownloaderlegacy.loggers.LoggerCustom;
import com.kanzaji.catdownloaderlegacy.guis.UpdaterGUI;
import com.kanzaji.catdownloaderlegacy.utils.*;

import static com.kanzaji.catdownloaderlegacy.CatDownloader.*;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import com.google.gson.Gson;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import javax.swing.*;


/**
 * This class holds all Update Checking + Updating related methods.
 */
public class Updater {
    private static final String GithubAPIUrl = "https://api.github.com/repos/" + REPOSITORY.replaceFirst("https://github.com/", "");
    private static final Gson gson = new Gson();
    private static final LoggerCustom logger = new LoggerCustom("Updater");
    private static final ArgumentDecoder ARD = ArgumentDecoder.getInstance();
    /**
     * Determines if user has already selected any option from the UpdaterGUI prompt.
     */
    public static boolean actionSelected = false;
    /**
     * Determines if the user has chosen to update the app.
     */
    public static boolean shouldUpdate = false;

    /**
     * This method is used to Check for Updates, and if required, update the app!<br><br>
     * It calls GitHub API for gathering required information about the latest release, and if required, sets up the GUI.
     * @throws java.net.MalformedURLException when GithubAPIUrl is not correct.
     */
    public static void checkForUpdates() throws IOException {
        if (!ARD.isUpdaterActive()) {
            logger.warn("Updater is disabled! Checking for updates is not possible.");
            return;
        }

        if (VERSION.endsWith("DEVELOP")) {
            logger.warn("Running in DEVELOP version (" + VERSION + ") of the app! Disabling Updater for this session.");
            return;
        }

        logger.log("Checking for app updates...");
        // Getting the latest version from the GitHub API!
        // This has... weird rate-limit. Might cause issues, but I doubt someone is going to run this app like... over 100 times in an hour. And if so, it's going to update a bit later, that's it.
        HttpsURLConnection response = (HttpsURLConnection) new URL(GithubAPIUrl + "/releases/latest").openConnection();
        response.setRequestProperty("Accept", "application/vnd.github+json");
        response.setRequestProperty("X-GitHub-Api-Version", "2022-11-28");

        try (BufferedReader in = new BufferedReader(new InputStreamReader(response.getInputStream(), StandardCharsets.UTF_8))) {
            UpdaterData.releaseData responseData = gson.fromJson(in, UpdaterData.releaseData.class);
            if (!Objects.isNull(responseData)) {
                if (Objects.isNull(responseData.tag_name) || Objects.isNull(responseData.html_url) || Objects.isNull(responseData.published_at) || Objects.isNull(responseData.assets)) {
                    throw new NullPointerException("Null data in required fields returned from the API!\n" + responseData);
                }

                if (compareVersions(VERSION, responseData.tag_name)) {
                    logger.log("App is updated! Running version " + VERSION + " when latest version is " + responseData.tag_name);
                } else {
                    logger.warn("New version of the app found! Getting GUI ready for informing the user about the update...");
                    logger.warn("Current version: " + VERSION);
                    logger.warn("Latest version: " + responseData.tag_name);

                    UpdaterGUI.startUpdateGUI();
                    UpdaterGUI.setUpdateVersion(VERSION, responseData.tag_name);
                    UpdaterGUI.setChangelogText(responseData.body.replaceAll("###", " - ").replaceAll("\\*\\*", ""));
                    UpdaterGUI.setupButtons();

                    while (!actionSelected) {
                        //TODO: Should be reworked into wait() notify() in the launcher version.
                        //noinspection BusyWait
                        Thread.sleep(1000);
                    }

                    if (shouldUpdate) installUpdate(responseData);
                    logger.warn("Update has been declined! Going back to the execution of the app.");
                }
            } else {
                throw new NullPointerException("Null data returned from the API!");
            }
        } catch (Exception e) {
            logger.logStackTrace("Exception thrown while trying to fetch update data!", e);
        }
    }

    /**
     * This method is used to install the update of the app.
     * Execution will end after invoking this method!
     */
    @Contract("_ -> fail")
    public static void installUpdate(UpdaterData.releaseData releaseData){
        String appPathString = FileUtils.getParentFolderAsString(APPPATH);
        String updatedAppName = APPPATH.getFileName().toString();
        Path cdlArgumentsFile = Path.of(appPathString, "CDL-Arguments.json");
        Path updatedAppPath;

        logger.log("Installing update!");
        logger.log("App directory: " + APPPATH);
        if (Files.isDirectory(APPPATH)) {
            logger.critical("App is a folder! Is this dev environment? Aborting update!");
            abortUpdate("App file appears to be a directory? Update has been aborted. Do you want to exit the app?");
            return;
        }

        logger.log("App Name: " + updatedAppName);

        if (updatedAppName.equals("Cat-Downloader-Legacy-" + VERSION + ".jar")) {
            updatedAppName = releaseData.assets[0].name;
            logger.log("Updated App Name: " + updatedAppName);
        } else {
            logger.warn("Custom jar name found! Saving the updated app under the same name.");
            updatedAppName += ".new";
        }
        updatedAppPath = Path.of(appPathString, updatedAppName);

        try {
            if (ARGUMENTS.length > 0) {
                logger.log("Current App Arguments:");
                Files.deleteIfExists(cdlArgumentsFile);
                Files.createFile(cdlArgumentsFile);
                for (String argument : ARGUMENTS) {
                    logger.log("  " + argument);
                }
                Files.writeString(cdlArgumentsFile, new Gson().toJson(ARGUMENTS));
                logger.log("Listed arguments have been saved to a file at \"" + cdlArgumentsFile.toAbsolutePath() + "\".");
            } else {
                logger.log("App is running without any arguments.");
            }

            logger.log("Creating a backup of current app, in case update process goes terribly wrong.");
            Files.copy(APPPATH, Path.of(appPathString, APPPATH.getFileName().toString() + ".old"), StandardCopyOption.REPLACE_EXISTING);

            Files.deleteIfExists(updatedAppPath);
            NetworkingUtils.downloadAndVerify(updatedAppPath, releaseData.assets[0].browser_download_url, releaseData.assets[0].size);

            logger.log("Unpacking CDL-Updater sub-app from the archive...");
            Path cdlPath = Path.of(FileUtils.getParentFolderAsString(updatedAppPath), "CDLUpdater.jar");
            Files.copy(FileUtils.getInternalAsset("CDL-Updater-1.0.jar"), cdlPath, StandardCopyOption.REPLACE_EXISTING);

            logger.log("CDL-Updater.jar unpacked. Handing execution to CDLUpdater.jar, closing main app for update process...");
            Runtime.getRuntime().exec(
                "\"" +
                JAVAPATH.toAbsolutePath() +
                "\" -jar \"" +
                cdlPath.toAbsolutePath() +
                "\" -oldApp:\"" +
                APPPATH.toAbsolutePath() +
                "\" -newApp:\"" +
                updatedAppPath.toAbsolutePath() +
                "\" -logPath:\"" +
                logger.getLogPath()
                +"\" -java:\"" +
                JAVAPATH.toAbsolutePath() +
                "\""
            );
            System.exit(10);
        } catch (Exception e) {
            logger.logStackTrace("Exception thrown while updating the app!", e);
            logger.critical(
                "Failed to update the app! " +
                "It is possible current version of the app is corrupted. " +
                "Backup has been made as the .old file. If you can't launch the app again, remove .old extension from the backup and try again."
            );
            RandomUtils.closeTheApp(1);
        }
    }

    /**
     * This method is used to run post-update cleaning routines, like removing additional files and launching the app with the arguments from the previous run.
     * @throws IOException when IO Operation fails.
     */
    public static void updateCleanup() throws IOException {
        String appPathString = FileUtils.getParentFolderAsString(APPPATH);
        logger.log("Post-Update cleanup started. Update log will get deleted if stockpiling of the logs is disabled!");

        if (Files.deleteIfExists(Path.of(appPathString, "CDLUpdater.jar"))) {
            logger.log("Sub-App CDL-Updater has been deleted.");
        } else {
            logger.log("Sub-App CDL-Updater couldn't been found!");
        }

        Path argumentsPath = Path.of(appPathString, "CDL-Arguments.json");
        if (Files.exists(argumentsPath)) {
            logger.log("Found file with last app arguments!");
            String[] parsedArguments = new Gson().fromJson(Files.readString(argumentsPath), String[].class);
            logger.log("Parsed arguments:");
            for (String argument : parsedArguments) {
                logger.log(" " + argument);
            }

            logger.log("Injecting above arguments into the main app argument list.");
            List<String> argumentsList = new LinkedList<>(Arrays.asList(ARGUMENTS));
            argumentsList.addAll(Arrays.asList(parsedArguments));
            ARGUMENTS = argumentsList.toArray(ARGUMENTS);

            logger.log("Deleting the argument file...");
            if (Files.deleteIfExists(argumentsPath)) {
                logger.log("Argument file has been deleted.");
            } else {
                logger.warn("Argument file is missing? Was it deleted right after parsing?");
            }
        } else {
            logger.log("Couldn't find arguments file. App will run without any arguments!");
        }

        if (JOptionPane.showConfirmDialog(
                null,
                "App has been updated to version " + VERSION +"! Do you want to run the updated app?",
                "Cat-Downloader-Legacy // Post-Update",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null
        ) > 0) {
            logger.log("Closing the app...");
            RandomUtils.closeTheApp(0);
        }
        logger.log("Returning to the main execution...");
    }

    /**
     * Used to generate confirmation dialog with specified Message, and exit the app depending on the user choice.
     * @param Message {@link String} with Message to display.
     */
    private static void abortUpdate(String Message) {
        if (JOptionPane.showConfirmDialog(
                null,
                Message,
                "Cat-Downloader-Legacy // Update failed!",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.ERROR_MESSAGE,
                null
        ) < 1) {
            logger.critical("Closing the app...");
            RandomUtils.closeTheApp(1);
        }
        logger.warn("Returning to the main execution...");
    }

    /**
     * This method is used to update Settings File and disable the Updates.
     * @return {@link Boolean} false if Settings aren't initialized or when Exception occurs, otherwise true;
     */
    public static boolean disableUpdates() {
        if (!ARD.areSettingsEnabled() || !SettingsManager.areSettingsInitialized()) return false;
        Settings newSettings = SettingsManager.getSettings();
        newSettings.isUpdaterActive = false;
        try {
            SettingsManager.updateSettings(newSettings);
            return true;
        } catch (Exception e) {
            logger.logStackTrace("Failed to update settings while trying to disable Updater!", e);
            return false;
        }
    }

    /**
     * This method is used to compare current version numbers with a dot (".") as a separator.
     * It does not support version numbers with characters.
     * @param currentVersion Current Version to compare.
     * @param latestVersion Latest Version to compare against.
     * @return Boolean {@code true} when the current version is the same or higher than latest and when it contains "develop" at the end, otherwise {@code false}.
     * @throws NumberFormatException when the version with non-Number character is passed!
     */
    public static boolean compareVersions(@NotNull String currentVersion, String latestVersion) throws NumberFormatException  {
        return compareVersions(currentVersion, latestVersion, "\\.");
    }

    /**
     * This method is used to compare current version numbers with a given separator.<br>
     * It does not support version numbers with characters.
     * @param currentVersion Current Version to compare.
     * @param latestVersion Latest Version to compare against.
     * @param separator A {@link String} used to separate numbers in passed version schema (Regex).
     * @return Boolean {@code true} when the current version is the same or higher than latest and when it contains "develop" at the end, otherwise {@code false}.
     * @throws NumberFormatException when the version with non-Number character is passed!
     */
    public static boolean compareVersions(@NotNull String currentVersion, String latestVersion, String separator) throws NumberFormatException {
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
        // If not equal, return true. (If the latest was higher it would return false earlier on)
        // If Equal and current version map size is higher / equal, return true
        // Yes this is here just so I will not mess this up;
        return !equal || currentVersionMap.size() >= latestVersionMap.size();
    }

    /**
     * This class holds all data structures for the {@link Updater}.
     */
    private static class UpdaterData {
        /**
         * This class is data structure for the data returned by GitHub API.
         */
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

            /**
             * This class is data structure for the assets' fields in {@link releaseData}.
             */
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

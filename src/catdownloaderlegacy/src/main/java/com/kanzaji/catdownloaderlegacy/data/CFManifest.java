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

package com.kanzaji.catdownloaderlegacy.data;

import com.kanzaji.catdownloaderlegacy.loggers.LoggerCustom;
import com.kanzaji.catdownloaderlegacy.Updater;

import org.jetbrains.annotations.ApiStatus;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Class used to represent data structure for Manifest.json file.
 * @see ModFile
 */
public class CFManifest {
    public static LinkedList<String> DataGatheringWarnings = new LinkedList<>();
    public String author;
    public String name;
    public String version;
    public String overrides;
    public minecraft minecraft;
    public ModFile[] files;

    /**
     * Used to hold information and methods related to a single {@link ModFile} object.
     * @see ModFile#getData(CFManifest.minecraft)
     */
    public static class ModFile {
        public boolean error403 = false;
        public boolean error202 = false;

        public Number projectID;
        public Number fileID;
        public String downloadUrl;
        public boolean required;
        public Number fileSize;

        //TODO: Rework this.. again. Also add notes!

        /**
         * Used to gather required data for this object.
         * @param minecraftData {@link minecraft} object from the main Manifest Object.
         * @return {@link ModFile} with data acquired in Data Gathering.
         * @Deprecated This method is meant for a rework in separate class, because of that it is marked as deprecated and should be replaced with updated version whenever possible.
         */
        @ApiStatus.Experimental
        @Deprecated(since = "2.0", forRemoval = true)
        public ModFile getData(minecraft minecraftData) {
            ModFile ModFileData = new ModFile();
            LoggerCustom logger = new LoggerCustom("Manifest");
            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            logger.log("Getting data for project with ID: " + projectID + " with file ID: " + fileID);

            try {
                HttpsURLConnection url;

                if (error403) {
                    url = (HttpsURLConnection) new URL("https://api.cfwidget.com/" + projectID).openConnection();
                    url.setUseCaches(false);
                    url.setRequestProperty("Cache-Control", "no-store");
                } else {
                    url = (HttpsURLConnection) new URL("https://api.cfwidget.com/" + projectID + "?&version=" + fileID).openConnection();
                }

                try (BufferedReader in = new BufferedReader(new InputStreamReader(url.getInputStream(), StandardCharsets.UTF_8))) {
                    data downloadData = gson.fromJson(in, data.class);

                    if (downloadData.download == null || error403) {
                        //TODO: Create ANOTHER Alternate method if downloadData is null.
                        // Appears CFWidget API doesn't support additional files, and just maybe, hidden files can also be accessed by the CF Site.
                        // Plan is simple, trying to connect with the PROJECT URL to CF Site, and extract what I need from the HTML code. If that doesn't work, fallback to the current solution.
                        // OKAY This is not going to happen. CF Server doesn't want to send the data I need, and (at least for this project) I am not going to implement full blown web-browser here.
                        // Note is staying however for future, and because I will be importing this project to the one I am going to actually implement a web browser.

                        logger.warn("No data was received for file id " + fileID + " from project " + projectID + "! Falling back to latest version of the mod for minecraft version requested by the modpack (" + minecraftData.version + ").");

                        for (legacyFile file : downloadData.files) {
                            Set<String> asSet = new HashSet<>(Arrays.asList(file.versions));

                            if (!asSet.contains(minecraftData.version)) continue;

                            if (!asSet.contains((minecraftData.modLoaders[0].id.startsWith("forge")) ? "Forge" : (minecraftData.modLoaders[0].id.startsWith("fabric")) ? "Fabric" : "Quilt")) {
                                if ((
                                    // Forge Checks
                                    minecraftData.modLoaders[0].id.startsWith("forge")) ?
                                    asSet.contains("Fabric") || asSet.contains("Quilt")
                                    // Fabric Checks
                                    : (minecraftData.modLoaders[0].id.startsWith("fabric")) ?
                                    asSet.contains("Forge") || asSet.contains("Quilt")
                                    // Quilt Checks
                                    : asSet.contains("Forge")
                                ) continue;

                                String warning = null;
                                if (minecraftData.modLoaders[0].id.startsWith("quilt") && asSet.contains("Fabric")) {
                                    warning = "Latest version found is for Fabric and is missing Quilt mod loader tag. This might work, however there is no guarantee on that. If the mod causes a crash, this is the reason!" +
                                              "\n     > CurseForge project link: " + ((downloadData.urls.curseforge == null) ? downloadData.urls.project : downloadData.urls.curseforge) +
                                              "\n     > CurseForge file link: " + file.url;
                                } else if (Updater.compareVersions(minecraftData.version, "1.14")){
                                    warning = "Latest version found doesn't have mod loader tag! If " + file.name + " causes a crash, make sure the correct version got installed!" +
                                              "\n     > CurseForge link: " + ((downloadData.urls.curseforge == null) ? downloadData.urls.project : downloadData.urls.curseforge) +
                                              "\n     > CurseForge file link: " + file.url;
                                }

                                if (warning != null) {
                                    logger.warn(warning);
                                    DataGatheringWarnings.add(warning);
                                }
                            }

                            ModFileData.downloadUrl = (
                                    "https://edge.forgecdn.net/files/" +
                                            String.valueOf(file.id).substring(0, 4) +
                                            "/" +
                                            String.valueOf(file.id).substring(4) +
                                            "/" +
                                            file.name
                            ).replaceAll(" ", "%20");

                            ModFileData.fileSize = file.filesize;
                            break;
                        }

                        if (ModFileData.downloadUrl == null) {
                            logger.error("No file for version " + minecraftData.version + " was found in project with id " + projectID + "! Please report this to the pack creator.");
                            return null;
                        }

                        return ModFileData;
                    }

                    if (!Objects.equals(downloadData.download.id, fileID)) {
                        logger.error("Data received from api.cfwidget.com is not correct!");
                        logger.error("\n" + gson.toJson(downloadData));
                        logger.error(fileID.toString());
                        return null;
                    }

                    ModFileData.downloadUrl = (
                            "https://edge.forgecdn.net/files/" +
                                    String.valueOf(downloadData.download.id).substring(0, 4) +
                                    "/" +
                                    String.valueOf(downloadData.download.id).substring(4) +
                                    "/" +
                                    downloadData.download.name
                    ).replaceAll(" ", "%20");

                    ModFileData.fileSize = downloadData.download.filesize;

                } catch (Exception e) {
                    if (!e.getMessage().startsWith("Server returned")) {
                        throw e;
                    }

                    int responseCode = url.getResponseCode();

                    if (Objects.equals(responseCode, 403)) {

                        if (error403 || error202) {
                            logger.error("Attempt of parsing data after Response code 403 for project id: " + projectID + " failed!");
                            DataGatheringWarnings.add(
                                    "403 (Access denied) Error occurred while trying to request data for project " + projectID + "! Mod has to be downloaded manually at this moment." +
                                    "\n     > Site with CurseForge link: https://cfwidget.com/" + projectID + "?&version=" + fileID +
                                    "\n     > Please report it on my github (Link at the end of the log file) if this still happens after waiting few minutes!"
                            );
                            return ModFileData;
                        }

                        logger.error("Response code 403 (Access Denied) returned for project id: " + projectID + " trying to get data for file id: " + fileID + ". Trying to request data without version parameter at the end of the queue.");
                        ModFileData.error403 = true;
                        return ModFileData;
                    }

                    if (
                            Objects.equals(responseCode, 500) ||
                            Objects.equals(responseCode, 202)
                    ) {
                        logger.warn("API returned response code " + responseCode + " (Wait for the request), trying requesting data again at the end of the queue!");
                        ModFileData.error202 = true;
                        if (error403) {
                            error403 = false;
                            return getData(minecraftData);
                        }
                        return ModFileData;
                    } else {
                        logger.error("Unknown response code (" + responseCode + ") returned for project id: " + projectID + " while trying to request data for file id: " + fileID);
                        return null;
                    }
                }
            } catch (Exception e) {
                logger.logStackTrace("Failed to get Data for project with ID " + projectID, e);
                return null;
            }
            return ModFileData;
        }

        /**
         * Used to get a file name for this {@link ModFile} object.
         * @return {@link String} with a file name for this object.
         */
        public String getFileName() {
            return downloadUrl.substring(downloadUrl.lastIndexOf("/")+1).replaceAll("%20", " ");
        }
    }

    public static class minecraft {
        public String version;
        public modLoaders[] modLoaders;
    }

    public static class modLoaders {
        public String id;
        public boolean primary;
    }

    private static class data {
        private urls urls;
        private legacyFile[] files;
        private downloadData download;
    }

    private static class downloadData {
        private String url;
        private Number id;
        private String name;
        private Number filesize;
    }

    private static class urls {
        private String curseforge;
        private String project;
    }

    private static class legacyFile {
        private String url;
        private Number id;
        private String name;
        private Number filesize;
        private String[] versions;
    }
}

package com.kanzaji.catdownloaderlegacy.jsons;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kanzaji.catdownloaderlegacy.SyncManager;
import com.kanzaji.catdownloaderlegacy.loggers.LoggerCustom;
import com.kanzaji.catdownloaderlegacy.utils.Updater;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Manifest {
    public String version;
    public String name;
    public String author;
    public ModFile[] files;
    public minecraft minecraft;

    public static class ModFile {
        public Number projectID;
        public Number fileID;
        public String downloadUrl;
        public Boolean required;
        public Number fileSize;

        //TODO: Rework this.. again :kek: Also add notes! Because this is the only shit I didn't document.

        public ModFile getData(minecraft minecraftData) {
            return getData(minecraftData, false);
        }

        public ModFile getData(minecraft minecraftData, boolean error403) {
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

                    if (downloadData.download == null) {
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
                                    SyncManager.getInstance().DataGatheringWarnings.add(warning);
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
                    //TODO: Finish response code handling!
                    if (!e.getMessage().startsWith("Server returned")) {
                        throw e;
                    }

                    int responseCode = url.getResponseCode();

                    if (Objects.equals(responseCode, 403)) {

                        if (error403) {
                            logger.error("Attempt of parsing data after Response code 403 for project id: " + projectID + " failed!");
                            SyncManager.getInstance().DataGatheringWarnings.add(
                                    "403 (Access denied) Error occurred while trying to request data for project " + projectID + "! Mod has to be downloaded manually at this moment." +
                                    "\n     > Site with CurseForge link: https://cfwidget.com/" + projectID + "?&version=" + fileID +
                                    "\n     > Please report it on my github (Link at the end of the log file) if this still happens after waiting few minutes!"
                            );
                            ModFileData.projectID = projectID;
                            return ModFileData;
                        }

                        logger.error("Response code 403 (Access Denied) returned for project id: " + projectID + " trying to get data for file id: " + fileID + ". Trying to request data without version parameter...");
                        return getData(minecraftData, true);
                    }

                    if (
                            Objects.equals(responseCode, 500) ||
                            Objects.equals(responseCode, 202)
                    ) {
                        logger.warn("API returned response code " + responseCode + ", trying requesting data again in few seconds...");
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

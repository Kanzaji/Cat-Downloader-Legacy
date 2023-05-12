package com.kanzaji.catdownloaderlegacy.jsons;

import com.kanzaji.catdownloaderlegacy.SyncManager;
import com.kanzaji.catdownloaderlegacy.utils.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kanzaji.catdownloaderlegacy.utils.SettingsManager;

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
    public ModFile[] files;
    public String name;
    public minecraft minecraft;

    public static class ModFile {
        public Number projectID;
        public Number fileID;
        public String downloadUrl;
        public Boolean required;
        public Number fileSize;

        //TODO: Rework this.. again :kek:
        public ModFile getData(minecraft minecraftData) {
            ModFile ModFileData = new ModFile();
            Logger logger = Logger.getInstance();
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            logger.log("Getting data for project with ID: " + projectID + " with file ID: " + fileID);

            try {
                URL url = new URL("https://api.cfwidget.com/" + projectID + "?&version=" + fileID);
                try (BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))) {
                    data downloadData = gson.fromJson(in, data.class);

                    if (downloadData.download == null) {
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
                                String warning;
                                if (minecraftData.modLoaders[0].id.startsWith("quilt") && asSet.contains("Fabric")) {
                                    warning = "Latest version found is for Fabric and is missing Quilt mod loader tag. This might work, however there is no guarantee on that. If the mod causes a crash, this is the reason!" +
                                              "\n     > CurseForge project link: " + ((downloadData.urls.curseforge == null) ? downloadData.urls.project : downloadData.urls.curseforge) +
                                              "\n     > CurseForge file link: " + file.url;
                                } else {
                                    warning = "Latest version found doesn't have mod loader tag! If minecraft version is below 1.14, you can probably ignore this warning, if not, and the file causes a crash, make sure the correct mod loader version got installed!" +
                                              "\n     > CurseForge link: " + ((downloadData.urls.curseforge == null) ? downloadData.urls.project : downloadData.urls.curseforge) +
                                              "\n     > CurseForge file link: " + file.url;
                                }
                                logger.warn(warning);
                                SyncManager.getInstance().DataGatheringWarnings.add(warning);
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
                        } else {
                            return ModFileData;
                        }
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

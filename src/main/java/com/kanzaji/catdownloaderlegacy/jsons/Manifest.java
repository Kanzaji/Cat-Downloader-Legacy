package com.kanzaji.catdownloaderlegacy.jsons;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kanzaji.catdownloaderlegacy.utils.Logger;

public class Manifest {
    public String manifestType;
    public String version;
    public Files[] files;
    public Number manifestVersion;
    public String name;
    public String overrides;
    public String author;
    public minecraft minecraft;

    public static class Files {
        public Number projectID;
        public Number fileID;
        public String downloadUrl;
        public Boolean required;
        public Number fileSize;

        public boolean getData(minecraft minecraftData) {

            Logger logger = Logger.getInstance();
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            logger.log("Getting data for project with ID: " + projectID);

            try {
                if (downloadUrl != null) {
                    logger.log("Found downloadURL inside of the Manifest file for project with id: " + projectID);
                    fileSize = -1;
                    return true;
                }
                URL url = new URL("https://api.cfwidget.com/" + projectID + "?&version=" + fileID);
                try (BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))) {
                    data downloadData = gson.fromJson(in, data.class);

                    if (downloadData.download == null) {
                        logger.warn("No data was received for file id " + fileID + " from project " + projectID + "! Falling back to latest version of the mod for minecraft version the modpack is on (" + minecraftData.version + ").");
                        for (legacyFile file : downloadData.files) {
                            Set<String> asSet = new HashSet<>(Arrays.asList(file.versions));
                            if (!asSet.contains(minecraftData.version)) continue;
                            // TODO: Fix this because this one below is pretty useless lmao
                            if (!asSet.contains((minecraftData.modLoaders[0].id.startsWith("forge")) ? "Forge" : (minecraftData.modLoaders[0].id.startsWith("fabric")) ? "Fabric" : "Quilt"))
                                continue;
                            downloadUrl = (
                                    "https://edge.forgecdn.net/files/" +
                                            String.valueOf(file.id).substring(0, 4) +
                                            "/" +
                                            String.valueOf(file.id).substring(4) +
                                            "/" +
                                            file.name
                            ).replaceAll(" ", "%20");

                            fileSize = file.filesize;
                        }
                        if (downloadUrl == null) {
                            logger.error("No file for version " + minecraftData.version + " was found in project with id " + projectID + "! Please report this to the pack creator.");
                            return false;
                        } else {
                            return true;
                        }
                    }

                    if (downloadData.download.id.intValue() != fileID.intValue()) {
                        logger.error("Data received from api.cfwidget.com is not correct!");
                        logger.error("\n" + gson.toJson(downloadData));
                        logger.error(fileID.toString());
                        return false;
                    }

                    downloadUrl = (
                            "https://edge.forgecdn.net/files/" +
                                    String.valueOf(downloadData.download.id).substring(0, 4) +
                                    "/" +
                                    String.valueOf(downloadData.download.id).substring(4) +
                                    "/" +
                                    downloadData.download.name
                    ).replaceAll(" ", "%20");

                    fileSize = downloadData.download.filesize;
                }
            } catch (Exception e) {
                logger.logStackTrace("Failed to get Data for project with ID " + projectID, e);
                return false;
            }
            return true;
        }

        public String getFileName() {
            int cut = downloadUrl.lastIndexOf("/");
            return downloadUrl.substring(cut+1).replaceAll("%20", " ");
        }
    }

    public class minecraft {
        public String version;
        public modLoaders[] modLoaders;
    }

    public static class modLoaders {
        public String id;
        public boolean primary;
    }

    private class data {
        private legacyFile[] files;
        private downloadData download;
    }

    private static class downloadData {
        private Number id;
        private String name;
        private Number filesize;
    }

    private static class legacyFile {
        private Number id;
        private String name;
        private Number filesize;
        private String[] versions;
    }
}
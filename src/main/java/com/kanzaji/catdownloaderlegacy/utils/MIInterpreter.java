package com.kanzaji.catdownloaderlegacy.utils;

import com.kanzaji.catdownloaderlegacy.jsons.Manifest;
import com.kanzaji.catdownloaderlegacy.jsons.MinecraftInstance;

import com.google.gson.Gson;

public class MIInterpreter {
    private static final Logger logger = Logger.getInstance();
    public static Manifest decode(MinecraftInstance MinecraftInstanceFile) throws UnsupportedOperationException {
        Gson gson = new Gson();
        Manifest manifest = new Manifest();
        logger.log("Translating MinecraftInstance into Manifest compatible object...");
        try {
            manifest.version = "";
            manifest.name = MinecraftInstanceFile.name;
            manifest.minecraft = gson.fromJson("{\"version\":\"" + MinecraftInstanceFile.baseModLoader.minecraftVersion + "\",\"modLoaders\": [{\"id\":\"" + MinecraftInstanceFile.baseModLoader.name + "\"}]}", Manifest.minecraft.class);
            int index = 0;
            manifest.files = new Manifest.ModFile[MinecraftInstanceFile.installedAddons.length];
            for (MinecraftInstance.installedAddons File : MinecraftInstanceFile.installedAddons) {
                Manifest.ModFile mf = new Manifest.ModFile();
                mf.projectID = File.addonID;
                mf.fileID = File.installedFile.id;
                mf.fileSize = File.installedFile.fileLength;
                mf.downloadUrl = File.installedFile.downloadUrl;
                mf.required = true;
                manifest.files[index] = mf;
                index += 1;
            }
        } catch (Exception e) {
            logger.logStackTrace("Interpretation of MinecraftInstance.json failed!", e);
            throw new UnsupportedOperationException();
        }
        logger.log("Translation successful");
        return manifest;
    }
}

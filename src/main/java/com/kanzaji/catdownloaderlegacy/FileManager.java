package com.kanzaji.catdownloaderlegacy;

import com.kanzaji.catdownloaderlegacy.jsons.Manifest;
import com.kanzaji.catdownloaderlegacy.utils.Logger;

import java.util.List;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;

//TODO: Finish this! Idk what I just started exactly XD But Yea, its pretty late I have to get some sleep.
public class FileManager {
    private static final Logger logger = Logger.getInstance();
    private static FileManager instance;
    private ExecutorService executor;
    private Path ModFolderPath;
    private Manifest ManifestData;
    private List<String> ModFileNames;

    public static FileManager getInstance() {
        if (instance == null) {
            logger.log("Starting new instance of FileManager!");
            instance = new FileManager();
        }
        return instance;
    }

    public void passData(Path modFolderPath, Manifest manifest) {
        this.ModFolderPath = modFolderPath;
        this.ManifestData = manifest;
    }

    public void startDownload() throws NullPointerException {
        if (ModFolderPath == null || ManifestData == null ) {
            if (ModFolderPath == null) {
                throw new NullPointerException("ModFolderPath is null!");
            }
            if (ManifestData == null) {
                throw new NullPointerException("ManifestData is null!");
            }
        }

        logger.log("Started downloading process!");
        System.out.println("Started downloading mods...");


    }
}

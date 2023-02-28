package com.kanzaji.catdownloaderlegacy;

import com.kanzaji.catdownloaderlegacy.jsons.Manifest;
import com.kanzaji.catdownloaderlegacy.utils.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.util.LinkedList;
import java.util.List;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.stream.Stream;

//TODO: Finish this! Idk what I just started exactly XD But Yea, its pretty late I have to get some sleep.
public class FileManager {
    private static final Logger logger = Logger.getInstance();
    private static FileManager instance;
    private ExecutorService executor;
    private Path ModFolderPath;
    private Manifest ManifestData;
    private int RemovedCount;
    private List<String> ModFileNames = new LinkedList<>();

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

    public void startSync() throws NullPointerException, IOException {
        if (ModFolderPath == null || ManifestData == null ) {
            if (ModFolderPath == null) {
                throw new NullPointerException("ModFolderPath is null!");
            }
            if (ManifestData == null) {
                throw new NullPointerException("ManifestData is null!");
            }
        }

        logger.log("Started syncing process!");
        System.out.println("Started syncing process!");

        for (Manifest.Files mod: ManifestData.files) {
            String FileName = mod.getFileName();
            Path ModFile = Path.of(ModFolderPath.toString(), FileName);
            ModFileNames.add(FileName);
            // I know this is slower, but it checks few times if the file exists, and that was an issue with original
            if (Files.notExists(ModFile, LinkOption.NOFOLLOW_LINKS)) {
                logger.log("Downloading " + FileName + "...");
                //TODO: Download mods here:tm:
                // Downloader will have SumCheck verification and file size verification in it.
            } else {
                //TODO: SumCheck with URL and File on the drive.
                // FileSize Verification
            }
        }

        logger.log("Checking Mods folder for removed mods...");
        try (Stream<Path> pathStream = Files.list(ModFolderPath)) {
            pathStream.forEach(File -> {
                if (!ModFileNames.contains(File.getFileName().toString())) {
                    logger.log("Found removed mod " + File.getFileName() + "! Deleting...");
                    try {
                        Files.delete(File);
                        logger.log("Deleted " + File.getFileName() + ".");
                        RemovedCount += 1;
                    } catch (IOException e) {
                        logger.logStackTrace("Failed deleting " + File.getFileName() + "!", e);
                    }
                }
            });
        }
        if (RemovedCount > 0) {
            logger.log("Removed " + RemovedCount + " mods!");
        } else {
            logger.log("No mods were removed.");
        }

        logger.log("Finished syncing profile!");
    }
}

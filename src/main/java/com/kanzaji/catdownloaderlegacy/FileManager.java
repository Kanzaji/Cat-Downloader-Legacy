package com.kanzaji.catdownloaderlegacy;

import com.kanzaji.catdownloaderlegacy.jsons.Manifest;
import com.kanzaji.catdownloaderlegacy.utils.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

//TODO: Finish this! Idk what I just started exactly XD But Yea, its pretty late I have to get some sleep.
public class FileManager {
    private static final Logger logger = Logger.getInstance();
    private static FileManager instance;
    private ExecutorService executor;
    private Path ModFolderPath;
    private Manifest ManifestData;
    private int RemovedCount = 0;
    private int RemovalFailed = 0;
    private int DownloadFailed = 0;
    private final List<String> ModFileNames = new LinkedList<>();

    public static FileManager getInstance() {
        if (instance == null) {
            logger.log("Starting new instance of FileManager!");
            instance = new FileManager();
        }
        return instance;
    }

    public void passData(Path modFolderPath, Manifest manifest, int nThreadsCount) {
        this.ModFolderPath = modFolderPath;
        this.ManifestData = manifest;
        this.executor = Executors.newFixedThreadPool(nThreadsCount);
    }

    public void startSync() throws NullPointerException, IOException, InterruptedException, RuntimeException{
        if (this.ModFolderPath == null || this.ManifestData == null || this.executor == null) {
            if (this.ModFolderPath == null) {
                throw new NullPointerException("ModFolderPath is null!");
            }
            if (this.ManifestData == null) {
                throw new NullPointerException("ManifestData is null!");
            }
            throw new NullPointerException("Executor is null! passData seems to have failed?");
        }

        logger.log("Started syncing process!");
        System.out.println("Started syncing process!");

        for (Manifest.Files mod: this.ManifestData.files) {
            String FileName = mod.getFileName();
            Path ModFile = Path.of(this.ModFolderPath.toString(), FileName);
            this.ModFileNames.add(FileName);
            // I know this is slower, but it checks few times if the file exists, and that was an issue with original
            if (Files.notExists(ModFile, LinkOption.NOFOLLOW_LINKS)) {
                logger.log(FileName + " not found! Added to download queue.");
                //TODO: Download mods here:tm:
                // Downloader will have SumCheck verification and file size verification in it.
                // For now, using old downloader just to test removal part!
                this.executor.submit(DownloadManager.download(ModFile, mod.downloadUrl));
            } else {
                logger.log("Found mod " + FileName + " on the drive! Verifying install...");
                //TODO: SumCheck with URL and File on the drive.
                // FileSize Verification
                logger.log("Verification of " + FileName + " was successful!");
            }
        }

        logger.log("Checking Mods folder for removed mods...");
        try (Stream<Path> pathStream = Files.list(this.ModFolderPath)) {
            pathStream.forEach(File -> {
                if (!this.ModFileNames.contains(File.getFileName().toString())) {
                    logger.log("Found removed mod " + File.getFileName() + "! Deleting...");
                    try {
                        Files.delete(File);
                        logger.log("Deleted " + File.getFileName() + ".");
                        RemovedCount += 1;
                    } catch (IOException e) {
                        logger.logStackTrace("Failed deleting " + File.getFileName() + "!", e);
                        RemovalFailed += 1;
                    }
                }
            });
        }
        if (this.RemovedCount > 0) {
            logger.log("Removed " + this.RemovedCount + " mods!");
        } else {
            logger.log("No mods were removed.");
        }

        this.executor.shutdown();
        if (!this.executor.awaitTermination(1, TimeUnit.DAYS)) {
            logger.error("Downloading takes over a day! This for sure isn't right???");
            System.out.println("Downloads interrupted due to taking over a day! This for sure isn't right???");
            throw new RuntimeException("Downloads are taking over a day! Something is horribly wrong.");
        }

        if (this.RemovalFailed > 0 || this.DownloadFailed > 0) {
            logger.error("Errors were found while doing synchronisation of the profile!");
            if (this.RemovalFailed > 0) {
                logger.error("Failed removals: " + this.RemovalFailed);
            } else {
                logger.error("No removal errors occurred.");
            }
            if (this.DownloadFailed > 0) {
                logger.error("Download errors: " + this.DownloadFailed);
            } else {
                logger.error("No download errors occurred.");
            }
        } else {
            logger.log("Finished syncing profile!");
        }
    }
}

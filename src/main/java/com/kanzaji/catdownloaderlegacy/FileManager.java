package com.kanzaji.catdownloaderlegacy;

import com.kanzaji.catdownloaderlegacy.jsons.Manifest;
import com.kanzaji.catdownloaderlegacy.utils.ArgumentDecoder;
import com.kanzaji.catdownloaderlegacy.utils.DownloadUtilities;
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

//TODO: Add Documentation, Log stuff, Comments and finish this.
public class FileManager {
    private static final Logger logger = Logger.getInstance();
    private static FileManager instance;
    private ExecutorService executor;
    private Path ModFolderPath;
    private Manifest ManifestData;
    private int RemovedCount = 0;
    private int RemovalFailed = 0;
    public static int DownloadFailed = 0;
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
                //TODO: This seems to be launching before executor.shutdown??
                this.executor.submit(() -> {
                    DownloadUtilities.download(ModFile, mod.downloadUrl, FileName);
                    try {
                        if (!verifyFile(ModFile, mod.fileSize)) {
                            DownloadUtilities.reDownload(ModFile, mod.downloadUrl, FileName, mod.fileSize);
                        }
                    } catch (IOException e) {
                        logger.logStackTrace("IO Operation failed while redownloading " + FileName + " !", e);
                    }
                });
            } else {
                logger.log("Found mod " + FileName + " on the drive! Verifying install...");
                if (!verifyFileSize(ModFile, mod.fileSize)) {
                    logger.warn("Mod " + FileName + " seems to be corrupted! Added to redownload queue.");
                    executor.submit(() -> {
                        try {
                            DownloadUtilities.reDownload(ModFile, mod.downloadUrl, FileName, mod.fileSize);
                        } catch (IOException e) {
                            logger.logStackTrace("IO Operation failed while redownloading " + FileName + " !", e);
                        }
                    });
                }
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

        if (this.RemovalFailed > 0 || DownloadFailed > 0) {
            logger.error("Errors were found while doing synchronisation of the profile!");
            if (this.RemovalFailed > 0) {
                logger.error("Failed removals: " + this.RemovalFailed);
            } else {
                logger.error("No removal errors occurred.");
            }
            if (DownloadFailed > 0) {
                logger.error("Download errors: " + DownloadFailed);
            } else {
                logger.error("No download errors occurred.");
            }
        } else {
            logger.log("Finished syncing profile!");
        }
    }

    public static boolean verifyFile(Path File, int Size) throws IOException {
        return verifyFileSize(File, Size);
    }
    public static boolean verifyFile(Path File, Number Size) throws IOException {
        return verifyFileSize(File, Size);
    }
    public static boolean verifyFileSize(Path File, Number Size) throws IOException {
        return verifyFileSize(File, Size.intValue());
    }
    public static boolean verifyFileSize(Path File, int Size) throws IOException {
        if (!Boolean.getBoolean(ArgumentDecoder.getInstance().getData("SizeVer"))) {
            return true;
        };
        return Files.size(File) == Size;
    }
}

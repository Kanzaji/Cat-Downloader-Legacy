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

public class FileManager {
    private static final Logger logger = Logger.getInstance();
    private static FileManager instance;
    private ExecutorService downloadExecutor;
    private ExecutorService verificationExecutor;
    private Path ModFolderPath;
    private Manifest ManifestData;
    private int RemovedCount = 0;
    private int RemovalFailed = 0;
    private int DownloadFailed = 0;
    private int DownloadQueue = 0;
    private int ReDownloadQueue = 0;
    private int VerificationQueue = 0;
    private int DownloadSuccess = 0;
    private final List<Runnable> DownloadQueueL = new LinkedList<>();
    private final List<Runnable> VerificationQueueL = new LinkedList<>();
    private final List<String> ModFileNames = new LinkedList<>();

    /**
     * Used to get an instance of the FileManager. Creates new one at first use.
     * @return Reference to an instance of the FileManager.
     */
    public static FileManager getInstance() {
        if (instance == null) {
            logger.log("Starting new instance of FileManager!");
            instance = new FileManager();
        }
        return instance;
    }

    /**
     * Used to pass required Data to FileManager instance.
     * @param modFolderPath Path to mods folder.
     * @param manifest Manifest Data
     * @param nThreadsCount Amount of Threads for downloader to use.
     */
    public void passData(Path modFolderPath, Manifest manifest, int nThreadsCount) {
        this.ModFolderPath = modFolderPath;
        this.ManifestData = manifest;
        this.downloadExecutor = Executors.newFixedThreadPool(nThreadsCount);
        this.verificationExecutor = Executors.newFixedThreadPool(nThreadsCount);
    }

    /**
     * Used to start process of Syncing a Profile. It uses a data passed by FileManager#passData to a FileManager instance.
     * @throws NullPointerException when Path to Mod Folder, Manifest Data or Executor is null.
     * @throws IOException when IO Operation fails.
     * @throws InterruptedException when Executor is interrupted.
     * @throws RuntimeException when downloading process takes over a day.
     */
    public void runSync() throws NullPointerException, IOException, InterruptedException, RuntimeException{
        long StartingTime = System.currentTimeMillis();
        if (this.ModFolderPath == null || this.ManifestData == null || this.downloadExecutor == null || this.verificationExecutor == null) {
            if (this.ModFolderPath == null) {
                throw new NullPointerException("ModFolderPath is null!");
            }
            if (this.ManifestData == null) {
                throw new NullPointerException("ManifestData is null!");
            }
            throw new NullPointerException("Executor is null! passData seems to have failed?");
        }

        //TODO: Add a list of downloaded/removed mods/files and corrupted ones.

        logger.log("Started syncing process.");
        logger.log("Checking for already existing files, and getting download queue ready...");
        System.out.println("Started syncing process!");
        System.out.println("---------------------------------------------------------------------");
        System.out.println("Looking for already installed mods...");

        for (Manifest.Files mod: this.ManifestData.files) {
            String FileName = mod.getFileName();
            Path ModFile = Path.of(this.ModFolderPath.toString(), FileName);

            this.ModFileNames.add(FileName);
            // I know this is slower, but it checks few times if the file exists, and that was an issue with original
            if (Files.notExists(ModFile, LinkOption.NOFOLLOW_LINKS)) {
                logger.log(FileName + " not found! Added to download queue.");
                DownloadQueue += 1;
                this.DownloadQueueL.add(() -> {
                    DownloadUtilities.download(ModFile, mod.downloadUrl, FileName);
                    try {
                        logger.log("Verifying " + FileName + " after download...");
                        if (!verifyFile(ModFile, mod.fileSize, mod.downloadUrl)) {
                            logger.warn("Mod " + FileName + " appears to have not downloaded correctly! Re-downloading...");
                            if(DownloadUtilities.reDownload(ModFile, mod.downloadUrl, FileName, mod.fileSize)) {
                                logger.log("Re-download of " + FileName + " was successful!");
                                this.DownloadSuccess += 1;
                            } else {
                                //TODO: Replace "5" with value from argument
                                logger.error("Re-download of " + FileName + " after 5 attempts failed!");
                                this.DownloadFailed += 1;
                            }
                        } else {
                            logger.log("Verification of " + FileName + " was successful!");
                            this.DownloadSuccess += 1;
                        }
                    } catch (IOException e) {
                        logger.logStackTrace("IO Operation failed while re-downloading " + FileName + " !", e);
                        this.DownloadFailed += 1;
                    }
                });
            } else {
                logger.log("Found mod " + FileName + " on the drive! Added to Verification queue.");
                this.VerificationQueueL.add(() -> {
                    logger.log("Verifying " + FileName + " ...");
                    try {
                        if (!verifyFile(ModFile, mod.fileSize, mod.downloadUrl)) {
                            logger.warn("Mod " + FileName + " seems to be corrupted! Added to re-download queue.");
                            ReDownloadQueue += 1;
                            this.DownloadQueueL.add(() -> {
                                logger.log("Re-downloading " + FileName + " ...");
                                try {
                                    if(DownloadUtilities.reDownload(ModFile, mod.downloadUrl, FileName, mod.fileSize)) {
                                        logger.log("Re-download of " + FileName + " was successful!");
                                        this.DownloadSuccess += 1;
                                    } else {
                                        //TODO: Replace "5" with value from argument
                                        logger.error("Re-download of " + FileName + " after 5 attempts failed!");
                                        this.DownloadFailed += 1;
                                    }
                                } catch (IOException e) {
                                    logger.logStackTrace("IO Operation failed while re-downloading " + FileName + " !", e);
                                    this.DownloadFailed += 1;
                                }
                            });
                        } else {
                            logger.log("Verification of " + FileName + " was successful!");
                        }
                    } catch (Exception e) {
                        logger.logStackTrace("Verification of " + FileName + " failed with exception!", e);
                    }
                });
            }
        }

        logger.log("Starting verification of existing files...");
        for (Runnable Task: this.VerificationQueueL) { this.verificationExecutor.submit(Task); }
        this.verificationExecutor.shutdown();
        if (!this.verificationExecutor.awaitTermination(1, TimeUnit.DAYS)) {
            logger.error("Verification takes over a day! This for sure isn't right???");
            System.out.println("Verification interrupted due to taking over a day! This for sure isn't right???");
            throw new RuntimeException("Verification is taking over a day! Something is horribly wrong.");
        }

        logger.log("Checking for existing files done.");
        System.out.println("Verification of existing files finished!");
        if (DownloadQueue > 0) {
            logger.log("Mods designated to download: " + DownloadQueue);
            System.out.println("> Mods designated to download: " + DownloadQueue);
        } else {
            logger.log("No mods designated to download!");
            System.out.println("> No mods designated to download!");
        }
        if (ReDownloadQueue > 0) {
            logger.warn("Found corrupted mods! Mods designated to re-download: " + ReDownloadQueue);
            System.out.println("> Found corrupted mods! Mods designated to re-download: " + ReDownloadQueue);
        } else {
            logger.log("No corrupted mods found!");
            System.out.println("> No corrupted mods found!");
        }

        logger.log("Checking Mods folder for removed mods...");
        System.out.println("---------------------------------------------------------------------");
        System.out.println("Checking for removed mods...");
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

        logger.log("Check for removed mods done.");
        System.out.println("Checking for removed mods finished!");
        if (this.RemovedCount > 0) {
            logger.log("Removed " + this.RemovedCount + " mods!");
            System.out.println("> Removed " + this.RemovedCount + " mods!");
        } else {
            logger.log("No mods were removed.");
            System.out.println("> No mods were removed!");
        }

        System.out.println("---------------------------------------------------------------------");

        if (DownloadQueue > 0 || ReDownloadQueue > 0) {
            logger.log("Starting download process...");
            System.out.println("Starting download process... This may take a while.");
        } else {
            logger.log("Download queue is empty.");
            System.out.println("Download queue is empty!");
        }

        for (Runnable Task: this.DownloadQueueL) { this.downloadExecutor.submit(Task); }
        this.downloadExecutor.shutdown();
        if (!this.downloadExecutor.awaitTermination(1, TimeUnit.DAYS)) {
            logger.error("Downloading takes over a day! This for sure isn't right???");
            System.out.println("Downloads interrupted due to taking over a day! This for sure isn't right???");
            throw new RuntimeException("Downloads are taking over a day! Something is horribly wrong.");
        }

        //TODO: Rework this message
        if (DownloadQueue > 0 || ReDownloadQueue > 0) {
            logger.log("Finished downloading process.");
            System.out.println("Finished downloading process, downloaded " + DownloadSuccess + " mods!");
        }

        System.out.println("---------------------------------------------------------------------");

        if (this.RemovalFailed > 0 || DownloadFailed > 0) {
            logger.error("Errors were found while doing synchronisation of the profile!");
            System.out.println("Errors were found while doing synchronisation of the profile!");
            if (this.RemovalFailed > 0) {
                logger.error("Failed removals: " + this.RemovalFailed);
                System.out.println("Failed removals: " + this.RemovalFailed);
            } else {
                logger.error("No removal errors occurred.");
            }
            if (DownloadFailed > 0) {
                logger.error("Download errors: " + DownloadFailed);
                System.out.println("Download errors: " + DownloadFailed);
            } else {
                logger.error("No download errors occurred.");
            }
            System.out.println("For more details, check log file at " + logger.getLogPath());
        } else {
            logger.log("Finished syncing profile!");
            System.out.println("Finished syncing profile!");
        }
        System.out.println("(Took " + (float) (System.currentTimeMillis() - StartingTime) / 1000F + "s)");
        logger.log("Sync took " + (float) (System.currentTimeMillis() - StartingTime) / 1000F + "s)");
    }

    /**
     * Used to verify integrity of the file with use of Length and SumCheck verification!
     * @param File Path to a file designated for verification.
     * @param Size Expected file length.
     * @param URL DownloadURL for SumCheck verification.
     * @return Boolean with the result of the verification.
     * @throws IOException when IO Operation fails.
     */
    public static boolean verifyFile(Path File, Number Size, String URL) throws IOException {
        if (Files.notExists(File)) {
            logger.error("File for mod " + File.getFileName() + " doesn't exists??");
            return false;
        }
        return verifyFileSize(File, Size);
    }

    /**
     * File size verification, can be disabled with an argument!
     * @param File Path to a file designated for verification.
     * @param Size Expected file length.
     * @return Boolean with the result of the verification.
     * @throws IOException when IO Operation fails.
     */
    private static boolean verifyFileSize(Path File, Number Size) throws IOException {
        return verifyFileSize(File, Size.intValue());
    }
    /**
     * File size verification, can be disabled with an argument!
     * @param File Path to a file designated for verification.
     * @param Size Expected file length.
     * @return Boolean with the result of the verification.
     * @throws IOException when IO Operation fails.
     */
    private static boolean verifyFileSize(Path File, int Size) throws IOException {
        if (Boolean.getBoolean(ArgumentDecoder.getInstance().getData("SizeVer"))) {
            return true;
        }
        return Files.size(File) == Size;
    }
}

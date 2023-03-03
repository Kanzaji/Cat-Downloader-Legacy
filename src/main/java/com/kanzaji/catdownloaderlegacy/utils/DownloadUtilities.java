package com.kanzaji.catdownloaderlegacy.utils;

import com.kanzaji.catdownloaderlegacy.FileManager;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class DownloadUtilities {
    private static final Logger logger = Logger.getInstance();

    /**
     * Used to download a file from URL without any verification. For that, use FileManager#verifyFile.
     * @param ModFile Path for the download.
     * @param DownloadUrl URL to a file.
     * @param FileName Name of the file.
     */
    public static void download(final Path ModFile, final String DownloadUrl, @Nullable String FileName) {
        try {
            if (FileName == null || !ModFile.endsWith(FileName)) {
                logger.warn("Mod with name " + FileName + " doesn't have matching Path and FileName! New FileName: " + ModFile.getFileName().toString());
                FileName = ModFile.getFileName().toString();
            }

            logger.log("Started downloading " + FileName + " ...");

            long StartTime = System.currentTimeMillis();
            URL DownloadURL = new URL(DownloadUrl);
            OutputStream OutputFile = Files.newOutputStream(ModFile, StandardOpenOption.CREATE_NEW);
            URLConnection MainConnection = DownloadURL.openConnection();
            InputStream InputData = MainConnection.getInputStream();

            byte[] Buffer = new byte[4096];
            int read;

            while((read = InputData.read(Buffer)) > 0)
                OutputFile.write(Buffer, 0, read);

            OutputFile.close();
            InputData.close();

            float ElapsedTime = (float) (System.currentTimeMillis() - StartTime) / 1000F;
            logger.log(String.format("Finished downloading %s (Took %.2fs)", FileName, ElapsedTime));
        } catch(Exception e) {
            logger.logStackTrace("Failed to download " + FileName, e);
        }
    }

    /**
     * Used to automatically delete, download and verify a file. The amount of attempts can be defined in the arguments (Default: 5)
     * @param modFile Path to a file to re-download.
     * @param downloadUrl DownloadURl of a file.
     * @param fileName A name of the file.
     * @param fileSize Expected length of the file.
     * @return Boolean with the result of re-download.
     * @throws IOException when IO operation fails.
     */
    public static boolean reDownload(Path modFile, String downloadUrl, String fileName, Number fileSize) throws IOException {
        //TODO: Add an argument for amount of retries a program will attempt while download fails.
        for (int i = 0; i < 5; i++) {
            if (Files.deleteIfExists(modFile)) {
                logger.log("Deleted corrupted " + fileName + ". Re-download attempt: " + i);
            }
            download(modFile, downloadUrl, fileName);
            if (FileManager.verifyFile(modFile, fileSize, downloadUrl)) {
                return true;
            }
        }
        return false;
    }
}

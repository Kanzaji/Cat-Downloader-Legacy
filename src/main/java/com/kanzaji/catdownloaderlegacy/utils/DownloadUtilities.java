package com.kanzaji.catdownloaderlegacy.utils;

import com.kanzaji.catdownloaderlegacy.FileManager;

import javax.xml.transform.sax.SAXTransformerFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

//TODO: Add documentation and log stuff
public class DownloadUtilities {
    private static final Logger logger = Logger.getInstance();
    public static void download(final Path ModFile, final String DownloadUrl, final String FileName) {
        try {
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
            FileManager.DownloadFailed += 1;
        }
    }

    public static void reDownload(Path modFile, String downloadUrl, String fileName, Number fileSize) throws IOException {
        //TODO: Add an argument for amount of retries a program will attempt while download fails.
        for (int i = 0; i < 5; i++) {
            if (Files.deleteIfExists(modFile)) {
                logger.log("Deleted corrupted " + fileName);
            }
            download(modFile, downloadUrl, fileName);
            if (FileManager.verifyFile(modFile, fileSize)) {
                break;
            }
        }
    }
}

package com.kanzaji.catdownloaderlegacy.utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.MissingResourceException;
import java.util.zip.GZIPOutputStream;

public class FileUtils {
    private static final Logger logger = Logger.getInstance();
    public static InputStream getInternalFile(String url) {
        InputStream file = FileUtils.class.getResourceAsStream("/assets/" + url);
        if (file == null) {
            throw new MissingResourceException("One of the integral assets is missing!\nThis may signal corrupted app. Please consider reinstalling the program.\nMissing asset: " + url + "\nError StackTrace:", url, url);
        }
        return file;
    }

    public static void compressToGz(Path File, boolean DeleteOriginal, boolean Override) throws IOException {
        if (Files.isDirectory(File)) {
            throw new IllegalArgumentException("Tried to compress directory with use of GZ!");
        } else if (Files.notExists(File)) {
            throw new FileNotFoundException("Tried compressing \"" + File.toAbsolutePath() + "\" but it doesn't exists!");
        }

        logger.log("Compressing file \"" + File.toAbsolutePath() + "\"...");
        Path gzFile = Path.of(FileUtils.getFolder(File).toString(), File.getFileName() + ".gz");

        if (Files.exists(gzFile)) {
            logger.warn("Found already compressed file with the same name!");
            if (Override) {
                logger.warn("Deleting compressed file...");
                Files.delete(gzFile);
                logger.warn("Compressed file \"" + gzFile.toAbsolutePath() + "\" has been deleted!");
            } else {
                logger.warn("Adding numeric suffix to the file name...");
                String gzFileName = gzFile.getFileName().toString();
                String gzNewFileName = gzFileName.substring(0,gzFileName.lastIndexOf("."));
                int suffix = 1;
                while (Files.exists(Path.of(gzNewFileName + " (" + suffix + ").gz"))) {
                    suffix++;
                }
                Path newGzFile = Path.of(gzNewFileName + " (" + suffix + ").gz");
                Files.move(gzFile, newGzFile);
                logger.warn("New file name: " + newGzFile.getFileName());
            }
        }

        Files.copy(File, gzFile);

        try (GZIPOutputStream gzOutput = new GZIPOutputStream(Files.newOutputStream(gzFile))) {
            Files.copy(File, gzOutput);
            if (DeleteOriginal) {
                logger.log("Compression done! Deleting original file...");
                Files.delete(File);
                logger.log("File \"" + File.getFileName() + "\" has been deleted.");
            } else {
                logger.log("Compression done!");
            }
        }
    }

    public static void compressToGz(Path File) throws IOException {
        compressToGz(File, false, false);
    }

    public static void compressToGz(Path File, boolean DeleteOriginal) throws IOException {
        compressToGz(File, DeleteOriginal, false);
    }

    public static Path getFolder(Path File) {
        return File.toAbsolutePath().getParent();
    }
}

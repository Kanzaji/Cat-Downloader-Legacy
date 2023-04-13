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

    public static Path getFolder(Path File) {return File.toAbsolutePath().getParent();}

    public static String rename(Path File, String Name) throws IOException {
        if (Files.exists(Path.of(getFolder(File).toString(), Name))) {
            logger.warn("Found existing file with name: \"" + Name + "\"! Adding numeric suffix to the file name...");
            String newName;
            String fileExtension = null;
            if (Name.lastIndexOf(".") == -1) {
                newName = Name;
            } else {
                newName = Name.substring(0, Name.lastIndexOf("."));
                fileExtension = Name.substring(Name.lastIndexOf("."));
            }
            long suffix = 1;
            while (Files.exists(Path.of(newName + " (" + suffix + ")" + ((fileExtension != null)? fileExtension:"")))) {
                suffix++;
            }
            Path newFile = Path.of(getFolder(File).toString(),newName + " (" + suffix + ")" + ((fileExtension != null)? fileExtension:""));
            Files.move(File, newFile);
            logger.warn("New file name: \"" + newFile.getFileName() + "\"");
            return newFile.getFileName().toString();
        } else {
            Files.move(File, Path.of(getFolder(File).toString(),Name));
            logger.log("Renamed file \"" + File.getFileName() + "\" to \"" + Name + "\".");
            return Name;
        }
    }

    // Okay I have actually no idea how I can set the file name in the gz file itself... Soo I'm going to just rename the final file afterwards if the FileName is provided.
    public static void compressToGz(Path File, String FileName, boolean DeleteOriginal, boolean Override) throws IOException {
        if (Files.isDirectory(File)) {
            throw new IllegalArgumentException("Tried to compress directory with use of GZ!");
        } else if (Files.notExists(File)) {
            throw new FileNotFoundException("Tried compressing \"" + File.toAbsolutePath() + "\" but it doesn't exists!");
        }

        Path gzFile = Path.of(FileUtils.getFolder(File).toString(), File.getFileName() + ".gz");
        Path customFile = Path.of(FileUtils.getFolder(File).toString(),FileName + ".gz");
        boolean gzFileExists = Files.exists(gzFile);
        boolean fileNameExists = Files.exists(customFile);

        logger.log("Compressing file \"" + File.toAbsolutePath() + "\"...");

        if (gzFileExists || fileNameExists) {
            logger.warn("Found already compressed file with the same name!");
            if (Override) {
                logger.warn("Deleting compressed file...");
                Files.delete((fileNameExists)? customFile: gzFile);
                logger.warn("Compressed file \"" + ((fileNameExists)? customFile: gzFile).toAbsolutePath() + "\" has been deleted!");
            } else {
                logger.warn("Adding numeric suffix to the file name...");
                String gzFileName = ((fileNameExists)? customFile: gzFile).getFileName().toString();
                String gzNewFileName = gzFileName.substring(0,gzFileName.lastIndexOf("."));
                long suffix = 1;
                while (Files.exists(Path.of(gzNewFileName + " (" + suffix + ").gz"))) {
                    suffix++;
                }
                Path newGzFile = Path.of(gzNewFileName + " (" + suffix + ").gz");
                Files.move((fileNameExists)? customFile: gzFile, newGzFile);
                logger.warn("New file name: " + newGzFile.getFileName());
            }
        }

        Files.copy(File, gzFile);

        try (GZIPOutputStream gzOutput = new GZIPOutputStream(Files.newOutputStream(gzFile))) {
            Files.copy(File, gzOutput);
            if (FileName != null) {Files.move(gzFile, customFile);}
            if (DeleteOriginal) {
                logger.log("Compression done! Deleting original file...");
                Files.delete(File);
                logger.log("File \"" + File.getFileName() + "\" has been deleted.");
            } else {
                logger.log("Compression done!");
            }
        }
    }

    public static void compressToGz(Path File) throws IOException { compressToGz(File, null, false, false);}
    public static void compressToGz(Path File, boolean DeleteOriginal) throws IOException {compressToGz(File, null, DeleteOriginal, false);}
    public static void compressToGz(Path File, boolean DeleteOriginal, boolean Override) throws IOException {compressToGz(File, null, DeleteOriginal, Override);}
    public static void compressToGz(Path File, String FileName) throws IOException {compressToGz(File, FileName, false, false);}
    public static void compressToGz(Path File, String FileName, boolean DeleteOriginal) throws IOException {compressToGz(File, FileName, DeleteOriginal, false);}
}

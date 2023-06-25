package com.kanzaji.catdownloaderlegacy.utils;

import com.kanzaji.catdownloaderlegacy.loggers.LoggerCustom;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.zip.GZIPOutputStream;

/**
 * {@link FileUtils} holds Utility methods for interacting with Files! <br>
 * @see FileUtils#getInternalFile(String)
 * @see FileUtils#compressToGz(Path)
 * @see FileUtils#getFileName(Path)
 */
public class FileUtils {
    private static final LoggerCustom logger = new LoggerCustom("FileUtils");

    /**
     * Used to get an {@link InputStream} for an internal resource. Starts from the `root` directory of the jar.
     * @param Path {@link String} Path to the file, starting from `root`
     * @return Not Null {@link InputStream} with internal resource.
     * @throws MissingResourceException when internal resource couldn't be found.
     */
    public static @NotNull InputStream getInternalFile(@NotNull String Path) throws MissingResourceException {
        InputStream file = FileUtils.class.getResourceAsStream(Path);
        if (file == null) {
            throw new MissingResourceException(
                "\n----------------------------------------------------------------------------------------------------\n" +
                "    One of the integral assets is missing!\n" +
                "    This may signal corrupted app. Please consider reinstalling the program.\n" +
                "    Missing asset: \"/assets/" + Path + "\"" +
                "\n----------------------------------------------------------------------------------------------------\n" +
                "StackTrace:",
            Path, Path);
        }
        return file;
    }

    /**
     * Used to get an {@link InputStream} for an internal resource. Starts from the `assets` directory of the jar.
     * @param Path {@link String} Path to the file, starting from `root`
     * @return Not Null {@link InputStream} with internal resource.
     * @throws MissingResourceException when internal resource couldn't be found.
     */
    public static @NotNull InputStream getInternalAsset(@NotNull String Path) throws MissingResourceException {
        return getInternalFile("/assets/" + Path);
    }

    /**
     * Used to rename specified file to a specified name. Adds numeric Suffix to the file name if a file with the same name exists.
     * @param File Not Null {@link Path} to a file to rename.
     * @param Name Not Null {@link String} with new name for a File.
     * @return Not Null {@link String} with new name for a File (Includes added Suffix)
     * @throws IOException when IO Exception occurs.
     */
    public static @NotNull String rename(@NotNull Path File, @NotNull String Name) throws IOException {
        if (Files.exists(Path.of(getFolderAsString(File), Name))) {
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
            while (Files.exists(Path.of(getFolderAsString(File),newName + " (" + suffix + ")" + ((fileExtension != null)? fileExtension:"")))) {
                suffix++;
            }

            Path newFile = Path.of(getFolderAsString(File),newName + " (" + suffix + ")" + ((fileExtension != null)? fileExtension:""));
            Files.move(File, newFile);

            logger.warn("New file name: \"" + newFile.getFileName() + "\"");
            logger.log("Renamed file \"" + File.toAbsolutePath().getFileName() + "\" to \"" + newFile.getFileName() + "\".");

            return newFile.getFileName().toString();
        } else {
            Files.move(File, Path.of(getFolderAsString(File),Name));
            logger.log("Renamed file \"" + File.toAbsolutePath().getFileName() + "\" to \"" + Name + "\".");
            return Name;
        }
    }

    /**
     * Used to compress specified File to a Gz archive.
     * @param File Not Null {@link Path} to a file specified for compression.
     * @param FileName Nullable {@link String} with name for an archive.
     * @param DeleteOriginal {@link Boolean} if uncompressed file has to be deleted.
     * @param Override {@link Boolean} if should delete an archive with the same name, if found.
     * @throws IllegalArgumentException when File is a directory.
     * @throws FileNotFoundException when File specified for compression doesn't exist.
     * @throws IOException when IO Exception occurs.
     */
    public static void compressToGz(@NotNull Path File, @Nullable String FileName, boolean DeleteOriginal, boolean Override) throws IllegalArgumentException, FileNotFoundException, IOException {
        // Couldn't find out how to change the file name in the GZ Archive, or is it even possible.
        // Current solution -> Rename final archive to the custom name specified.
        if (Files.isDirectory(File)) {
            throw new IllegalArgumentException("Tried to compress directory with use of GZ!");
        } else if (Files.notExists(File)) {
            throw new FileNotFoundException("Tried compressing \"" + File.toAbsolutePath() + "\" but it doesn't exists!");
        }

        Path gzFile = Path.of(getFolderAsString(File), File.getFileName() + ".gz");
        Path customFile = Path.of(getFolderAsString(File),FileName + ".gz");
        boolean gzFileExists = Files.exists(gzFile);
        boolean fileNameExists = false;

        if (FileName != null) {
            logger.log("Custom file name for archive specified! Archive will be saved under name: \"" + FileName + ".gz\"");
            fileNameExists = Files.exists(customFile);
        }

        logger.log("Compressing file \"" + File.toAbsolutePath() + "\"...");

        //TODO: Fix this fully, still doesn't completely work >.>
        if (gzFileExists || fileNameExists) {
            logger.warn("Found already compressed file with the same name!");
            if (Override) {
                logger.warn("Deleting compressed file...");
                Files.delete((fileNameExists)? customFile: gzFile);
                logger.warn("Compressed file \"" + ((fileNameExists)? customFile: gzFile).toAbsolutePath() + "\" has been deleted!");
            } else {
                logger.warn("Adding numeric suffix to the file name...");
                String gzNewFileName = getFileName((fileNameExists)? customFile: gzFile).substring(0,(getFileName((fileNameExists)? customFile: gzFile)).lastIndexOf("."));
                long suffix = 1;
                while (Files.exists(Path.of(getFolderAsString(File), gzNewFileName + " (" + suffix + ").gz"))) {
                    suffix++;
                }
                gzFile = Path.of(getFolderAsString(File), gzNewFileName + " (" + suffix + ").gz");
                logger.warn("New file name: " + gzFile.getFileName());
            }
        }

        Files.copy(File, gzFile);

        try (GZIPOutputStream gzOutput = new GZIPOutputStream(Files.newOutputStream(gzFile))) {
            Files.copy(File, gzOutput);
            if (FileName != null) {
                Files.move(gzFile, customFile);
            }
            if (DeleteOriginal) {
                logger.log("Compression done! Deleting original file...");
                Files.delete(File);
                logger.log("File \"" + File.toAbsolutePath().getFileName() + "\" has been deleted.");
            } else {
                logger.log("Compression done!");
            }
        }
    }

    /**
     * Used to compress specified File to a Gz archive.
     * @param File Not Null {@link Path} to a file specified for compression.
     * @throws IllegalArgumentException when File is a directory.
     * @throws FileNotFoundException when File specified for compression doesn't exist.
     * @throws IOException when IO Exception occurs.
     */
    public static void compressToGz(@NotNull Path File) throws IllegalArgumentException, FileNotFoundException, IOException { compressToGz(File, null, false, false);}

    /**
     * Used to compress specified File to a Gz archive.
     * @param File Not Null {@link Path} to a file specified for compression.
     * @param DeleteOriginal {@link Boolean} if uncompressed file has to be deleted.
     * @throws IllegalArgumentException when File is a directory.
     * @throws FileNotFoundException when File specified for compression doesn't exist.
     * @throws IOException when IO Exception occurs.
     */
    public static void compressToGz(@NotNull Path File, boolean DeleteOriginal) throws IllegalArgumentException, FileNotFoundException, IOException { compressToGz(File, null, DeleteOriginal, false);}

    /**
     * Used to compress specified File to a Gz archive.
     * @param File Not Null {@link Path} to a file specified for compression.
     * @param DeleteOriginal {@link Boolean} if uncompressed file has to be deleted.
     * @param Override {@link Boolean} if should delete an archive with the same name, if found.
     * @throws IllegalArgumentException when File is a directory.
     * @throws FileNotFoundException when File specified for compression doesn't exist.
     * @throws IOException when IO Exception occurs.
     */
    public static void compressToGz(@NotNull Path File, boolean DeleteOriginal, boolean Override) throws IllegalArgumentException, FileNotFoundException, IOException { compressToGz(File, null, DeleteOriginal, Override);}

    /**
     * Used to compress specified File to a Gz archive.
     * @param File Not Null {@link Path} to a file specified for compression.
     * @param FileName Nullable {@link String} with name for an archive.
     * @throws IllegalArgumentException when File is a directory.
     * @throws FileNotFoundException when File specified for compression doesn't exist.
     * @throws IOException when IO Exception occurs.
     */
    public static void compressToGz(@NotNull Path File, @Nullable String FileName) throws IllegalArgumentException, FileNotFoundException, IOException { compressToGz(File, FileName, false, false);}

    /**
     * Used to compress specified File to a Gz archive.
     * @param File Not Null {@link Path} to a file specified for compression.
     * @param FileName Nullable {@link String} with name for an archive.
     * @param DeleteOriginal {@link Boolean} if uncompressed file has to be deleted.
     * @throws IllegalArgumentException when File is a directory.
     * @throws FileNotFoundException when File specified for compression doesn't exist.
     * @throws IOException when IO Exception occurs.
     */
    public static void compressToGz(@NotNull Path File, @Nullable String FileName, boolean DeleteOriginal) throws IllegalArgumentException, FileNotFoundException, IOException { compressToGz(File, FileName, DeleteOriginal, false);}

    /**
     * Used to get parent folder for specified {@link Path}.
     * @param File Not Null {@link Path} to get a parent of.
     * @return Not Null {@link Path} with the parent of the directory, or File if there is no Parent.
     */
    public static @NotNull Path getFolder(@NotNull Path File) {return (Objects.isNull(File.toAbsolutePath().getParent())? File: File.toAbsolutePath().getParent());}

    /**
     * Used to get parent folder for specified {@link Path} as a String.
     * @param File Not Null {@link Path} to get a parent of.
     * @return Not Null {@link String} with the parent of the directory, or File if there is no Parent.
     */
    public static @NotNull String getFolderAsString(@NotNull Path File) {return getFolder(File).toString();}

    /**
     * Used to get a name of the file.
     * @param File Not Null {@link Path} to get a name of.
     * @return Not Null {@link String} with the name of the file.
     */
    public static @NotNull String getFileName(@NotNull Path File) {return File.getFileName().toString();}
}

/**************************************************************************************
 * MIT License                                                                        *
 *                                                                                    *
 * Copyright (c) 2023. Kanzaji                                                        *
 *                                                                                    *
 * Permission is hereby granted, free of charge, to any person obtaining a copy       *
 * of this software and associated documentation files (the "Software"), to deal      *
 * in the Software without restriction, including without limitation the rights       *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell          *
 * copies of the Software, and to permit persons to whom the Software is              *
 * furnished to do so, subject to the following conditions:                           *
 *                                                                                    *
 * The above copyright notice and this permission notice shall be included in all     *
 * copies or substantial portions of the Software.                                    *
 *                                                                                    *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR         *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,           *
 * FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE       *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER             *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,      *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE      *
 * SOFTWARE.                                                                          *
 **************************************************************************************/

package com.kanzaji.catdownloaderlegacy.utils;

import com.kanzaji.catdownloaderlegacy.loggers.LoggerCustom;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.rmi.UnexpectedException;
import java.util.*;
import java.util.stream.Stream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * This class holds utility methods related to interacting with files.
 * @see FileUtils#getInternalFile(String)
 * @see FileUtils#compressToGz(Path)
 * @see FileUtils#getFileName(Path)
 */
public class FileUtils {
    private static final LoggerCustom logger = new LoggerCustom("File Utilities");

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
                "    Missing asset: \"" + Path + "\"" +
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
     * This method creates all requires directories for the specified path to exist.
     * @param path Path to create.
     * @throws UnexpectedException when an Exception occurs.
     * @apiNote Be aware, this method can't check if a specified path is to a file or directory!
     */
    public static void createRequiredPath(@NotNull Path path) throws UnexpectedException {
        Objects.requireNonNull(path);

        String msgPath = path.toAbsolutePath().toString();
        try {
            if (Files.exists(path)) return;

            List<Path> directoryPath = new LinkedList<>();
            while (Files.notExists(path)) {
                directoryPath.add(path);
                path = getParentFolder(path);
            }

            for (int i = directoryPath.size() - 1; i >= 0; i--) {
                Files.createDirectory(directoryPath.get(i));
            }

            logger.log("Path \"" + msgPath + "\" has been created!");
        } catch (Exception e) {
            throw new UnexpectedException("Exception caught while creating path \"" + msgPath + "\"", e);
        }
    }

    /**
     * This method is used to move a file or directory to a specified destination.
     * @param FileOrFolder File or Folder to move.
     * @param Destination Destination folder to move.
     * @param override Boolean determining override rules for already existing files.
     * @throws IOException when IO Exception occurs.
     * @throws IllegalArgumentException when Destination is not a Directory.
     */
    public static void move(Path FileOrFolder, Path Destination, boolean override) throws IOException, IllegalArgumentException {
        Objects.requireNonNull(FileOrFolder);
        Objects.requireNonNull(Destination);

        if (!Files.isDirectory(Destination.toAbsolutePath())) throw new IllegalArgumentException("Destination is not a directory! \"" + Destination + "\"");

        Path finalPath = Path.of(Destination.toString(), FileOrFolder.getFileName().toString());

        HashSet<Exception> exceptionsHashSet = new HashSet<>();
        if (Files.isDirectory(FileOrFolder)) {
            createRequiredPath(finalPath);
            Stream<Path> dirListing = Files.list(FileOrFolder);

            dirListing.forEach((File) -> {
                try {
                    move(File, finalPath, override);
                } catch (IOException e) {
                    exceptionsHashSet.add(new IOException(File.toAbsolutePath().toString(), e));
                }
            });
            dirListing.close();

        } else {
            logger.log("Moving file \"" + FileOrFolder + "\" to the folder \"" + Destination + "\"");
            if (override) {
                Files.move(FileOrFolder, finalPath, StandardCopyOption.REPLACE_EXISTING);
            } else {
                if (Files.exists(finalPath)) {
                    logger.warn("File \"" + finalPath + "\" already exists!");
                } else {
                    Files.move(FileOrFolder, finalPath);
                }
            }
        }

        if (exceptionsHashSet.size() > 0) {
            IOException ioe = new IOException("IO Exception occurred while deleting the folder" + FileOrFolder.toAbsolutePath());
            exceptionsHashSet.forEach(ioe::addSuppressed);
            throw ioe;
        }
    }

    /**
     * This method is used to automatically delete a file or a folder.
     * @param FileOrFolder Path to a file or a folder designated to deletion.
     * @throws NullPointerException when the argument is null.
     * @throws IOException when IO Exception occurs.
     */
    public static void delete(@NotNull Path FileOrFolder) throws IOException, NullPointerException {
        Objects.requireNonNull(FileOrFolder);

        if (Files.notExists(FileOrFolder)) {
            logger.warn("Tried to delete already not existent file!");
            logger.warn("Path: \"" + FileOrFolder.toAbsolutePath() + "\".");
            return;
        }

        if (!Files.isDirectory(FileOrFolder)) {
            Files.deleteIfExists(FileOrFolder);
            logger.log("File \"" + FileOrFolder + "\" has been deleted.");
            return;
        }

        HashSet<Exception> exceptionsHashSet = new HashSet<>();
        try (Stream<Path> directoryListing = Files.list(FileOrFolder)) {
            directoryListing.forEach((File) -> {
                try {
                    delete(File);
                } catch (IOException e) {
                    exceptionsHashSet.add(new IOException(File.toAbsolutePath().toString(), e));
                }
            });
        }

        Files.deleteIfExists(FileOrFolder);

        if (exceptionsHashSet.size() > 0) {
            IOException ioe = new IOException("IO Exception occurred while deleting the folder" + FileOrFolder.toAbsolutePath());
            exceptionsHashSet.forEach(ioe::addSuppressed);
            throw ioe;
        } else {
            logger.log("Directory \"" + FileOrFolder + "\" has been deleted.");
        }
    }

    /**
     * Used to rename a specified file to a specified name.
     * Adds numeric Suffix to the file name if a file with the same name exists.
     * @param File Not Null {@link Path} to a file to rename.
     * @param Name Not Null {@link String} with new name for a File.
     * @return Not Null {@link String} with new name for a File (Includes added Suffix)
     * @throws IOException when IO Exception occurs.
     */
    public static @NotNull String rename(@NotNull Path File, @NotNull String Name) throws IOException {
        if (Files.exists(Path.of(getParentFolderAsString(File), Name))) {
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
            while (Files.exists(Path.of(getParentFolderAsString(File),newName + " (" + suffix + ")" + ((fileExtension != null)? fileExtension:"")))) {
                suffix++;
            }

            Path newFile = Path.of(getParentFolderAsString(File),newName + " (" + suffix + ")" + ((fileExtension != null)? fileExtension:""));
            Files.move(File, newFile);

            logger.warn("New file name: \"" + newFile.getFileName() + "\"");
            logger.log("Renamed file \"" + File.toAbsolutePath().getFileName() + "\" to \"" + newFile.getFileName() + "\".");

            return newFile.getFileName().toString();
        } else {
            Files.move(File, Path.of(getParentFolderAsString(File),Name));
            logger.log("Renamed file \"" + File.toAbsolutePath().getFileName() + "\" to \"" + Name + "\".");
            return Name;
        }
    }

    /**
     * This method is used to unzip a ZIP archive. Other types are not supported.
     * @param zipFilePath Path to the zip file.
     * @param destinationPath Path to the destination. (@Nullable)
     * @param shouldDeleteZipFile Determines if uncompressed zip file should be deleted.
     * @throws IOException when IO Exception occurs.
     */
    public static void unzip(Path zipFilePath, @Nullable Path destinationPath, boolean shouldDeleteZipFile) throws IOException {
        Objects.requireNonNull(zipFilePath);

        logger.log("Unzipping of the archive \"" + zipFilePath.toAbsolutePath() + "\" has been requested.");

        if (Objects.isNull(destinationPath)) {
            String zipFileName = zipFilePath.getFileName().toString();
            destinationPath = Path.of(getParentFolderAsString(zipFilePath),zipFileName.substring(0, zipFileName.lastIndexOf(".")-1));
        }

        if (!Files.exists(destinationPath)) {
            Files.createDirectory(destinationPath);
        } else if (!Files.isDirectory(destinationPath)) {
            throw new IllegalStateException("Destination for the zip archive \"" + zipFilePath.toAbsolutePath() + "\" is a file!");
        }

        logger.log("Destination: \"" + destinationPath.toAbsolutePath() + "\"");

        long dirs = 0;
        long files = 0;
        try (ZipFile zipFile = new ZipFile(zipFilePath.toFile())) {
            logger.log("Archive contains " + zipFile.stream().toList().size() + " entries.");
            Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
            while (zipEntries.hasMoreElements()) {
                ZipEntry zipEntry = zipEntries.nextElement();
                Path dirOrFile = Path.of(destinationPath.toString(), zipEntry.getName());
                if (zipEntry.isDirectory()) {
                    Files.createDirectory(dirOrFile);
                    logger.log("Directory \"" + dirOrFile + "\" has been created.");
                    dirs++;
                } else {
                    Files.copy(zipFile.getInputStream(zipEntry), dirOrFile);
                    logger.log("File \"" + dirOrFile + "\" has been created.");
                    files++;
                }
            }
        }

        logger.log("Archive \"" + zipFilePath.toAbsolutePath() + "\" has been successfully uncompressed.");
        logger.log(files + " files have been created.");
        logger.log(dirs + " directories have been created");
        if (shouldDeleteZipFile) delete(zipFilePath);
    }

    /**
     * Used to compress a specified File to a Gz archive.
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

        Path gzFile;
        if (FileName != null) {
            logger.log("Custom file name for archive specified! Archive will be saved under name: \"" + FileName + ".gz\"");
            gzFile = Path.of(getParentFolderAsString(File),FileName + ".gz");
        } else {
            gzFile = Path.of(getParentFolderAsString(File), File.getFileName() + ".gz");
        }

        boolean gzFileExists = Files.exists(gzFile);

        logger.log("Compressing file \"" + File.toAbsolutePath() + "\"...");

        if (gzFileExists) {
            logger.warn("Found already compressed file with the same name!");
            if (Override) {
                logger.warn("Deleting compressed file...");
                delete(gzFile);
                logger.warn("Compressed file \"" + gzFile.toAbsolutePath() + "\" has been deleted!");
            } else {
                logger.warn("Adding numeric suffix to the file name...");
                String gzNewFileName = getFileName(gzFile).substring(0,(getFileName(gzFile)).lastIndexOf("."));
                String extension = "";
                if (gzNewFileName.lastIndexOf(".") > -1) {
                    extension = gzNewFileName.substring(gzNewFileName.lastIndexOf("."));
                    gzNewFileName = gzNewFileName.substring(0, gzNewFileName.lastIndexOf("."));
                }
                long suffix = 1;
                while (Files.exists(Path.of(getParentFolderAsString(File), gzNewFileName + " (" + suffix + ")" + extension + ".gz"))) {
                    suffix++;
                }
                gzFile = Path.of(getParentFolderAsString(File), gzNewFileName + " (" + suffix + ")" + extension + ".gz");
                logger.warn("New file name: " + gzFile.getFileName());
            }
        }

        Files.copy(File, gzFile);

        try (GZIPOutputStream gzOutput = new GZIPOutputStream(Files.newOutputStream(gzFile))) {
            Files.copy(File, gzOutput);
            if (DeleteOriginal) {
                logger.log("Compression done! Deleting original file...");
                delete(File);
                logger.log("File \"" + File.toAbsolutePath().getFileName() + "\" has been deleted.");
            } else {
                logger.log("Compression done!");
            }
        }
    }

    /**
     * Used to compress a specified File to a Gz archive.
     * @param File Not Null {@link Path} to a file specified for compression.
     * @throws IllegalArgumentException when File is a directory.
     * @throws FileNotFoundException when File specified for compression doesn't exist.
     * @throws IOException when IO Exception occurs.
     */
    public static void compressToGz(@NotNull Path File) throws IllegalArgumentException, FileNotFoundException, IOException { compressToGz(File, null, false, false);}

    /**
     * Used to compress a specified File to a Gz archive.
     * @param File Not Null {@link Path} to a file specified for compression.
     * @param DeleteOriginal {@link Boolean} if uncompressed file has to be deleted.
     * @throws IllegalArgumentException when File is a directory.
     * @throws FileNotFoundException when File specified for compression doesn't exist.
     * @throws IOException when IO Exception occurs.
     */
    public static void compressToGz(@NotNull Path File, boolean DeleteOriginal) throws IllegalArgumentException, FileNotFoundException, IOException { compressToGz(File, null, DeleteOriginal, false);}

    /**
     * Used to compress a specified File to a Gz archive.
     * @param File Not Null {@link Path} to a file specified for compression.
     * @param DeleteOriginal {@link Boolean} if uncompressed file has to be deleted.
     * @param Override {@link Boolean} if should delete an archive with the same name, if found.
     * @throws IllegalArgumentException when File is a directory.
     * @throws FileNotFoundException when File specified for compression doesn't exist.
     * @throws IOException when IO Exception occurs.
     */
    public static void compressToGz(@NotNull Path File, boolean DeleteOriginal, boolean Override) throws IllegalArgumentException, FileNotFoundException, IOException { compressToGz(File, null, DeleteOriginal, Override);}

    /**
     * Used to compress a specified File to a Gz archive.
     * @param File Not Null {@link Path} to a file specified for compression.
     * @param FileName Nullable {@link String} with name for an archive.
     * @throws IllegalArgumentException when File is a directory.
     * @throws FileNotFoundException when File specified for compression doesn't exist.
     * @throws IOException when IO Exception occurs.
     */
    public static void compressToGz(@NotNull Path File, @Nullable String FileName) throws IllegalArgumentException, FileNotFoundException, IOException { compressToGz(File, FileName, false, false);}

    /**
     * Used to compress a specified File to a Gz archive.
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
    public static @NotNull Path getParentFolder(@NotNull Path File) {return (Objects.isNull(File.toAbsolutePath().getParent())? File: File.toAbsolutePath().getParent());}

    /**
     * Used to get parent folder for specified {@link Path} as a String.
     * @param File Not Null {@link Path} to get a parent of.
     * @return Not Null {@link String} with the parent of the directory, or File if there is no Parent.
     */
    public static @NotNull String getParentFolderAsString(@NotNull Path File) {return getParentFolder(File).toString();}

    /**
     * Used to get a name of the file.
     * @param File Not Null {@link Path} to get a name of.
     * @return Not Null {@link String} with the name of the file.
     */
    public static @NotNull String getFileName(@NotNull Path File) {return File.getFileName().toString();}

    /**
     * This method is used to unzip a ZIP archive. Other types are not supported.
     * @param zipFilePath Path to the zip file.
     * @param shouldDeleteZipFile Determines if uncompressed zip file should be deleted.
     * @throws IOException when IO Exception occurs.
     */
    public static void unzip(Path zipFilePath, boolean shouldDeleteZipFile) throws IOException {unzip(zipFilePath, null, shouldDeleteZipFile);}

    /**
     * This method is used to unzip a ZIP archive. Other types are not supported.
     * @param zipFilePath Path to the zip file.
     * @param destinationPath Path to the destination. (@Nullable)
     * @throws IOException when IO Exception occurs.
     */
    public static void unzip(Path zipFilePath, @Nullable Path destinationPath) throws IOException {unzip(zipFilePath, destinationPath, false);}
    /**
     * This method is used to unzip a ZIP archive. Other types are not supported. The resulting files and directories will be created in the folder with the same name as the zip file, in the same directory.
     * @param zipFilePath Path to the zip file.
     * @throws IOException when IO Exception occurs.
     */
    public static void unzip(Path zipFilePath) throws IOException {unzip(zipFilePath, null, false);}

    /**
     * This method is used to move a file or directory to a specified destination.
     * @param FileOrFolder File or Folder to move.
     * @param Destination Destination folder to move.
     * @throws IOException when IO Exception occurs.
     * @throws IllegalArgumentException when Destination is not a Directory.
     */
    public static void move(Path FileOrFolder, Path Destination) throws IOException, IllegalArgumentException { move(FileOrFolder, Destination, false); }

    /**
     * This method creates all requires directories for the specified path to exist.
     * @param path Path to create.
     * @throws UnexpectedException when an Exception occurs.
     * @apiNote Be aware, this method is meant to be used with files, and will get a parent folder of specified Path.
     */
    public static void createRequiredPathToAFile(@NotNull Path path) throws UnexpectedException {createRequiredPath(path.getParent());}
}

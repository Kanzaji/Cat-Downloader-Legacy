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

import com.kanzaji.catdownloaderlegacy.ArgumentDecoder;
import com.kanzaji.catdownloaderlegacy.loggers.LoggerCustom;
import static com.kanzaji.catdownloaderlegacy.utils.FileVerUtils.verifyFile;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

/**
 * This class holds utility methods related to Networking.
 * @see NetworkingUtils#download(Path, String, String)
 * @see NetworkingUtils#reDownload(Path, Number, String, String)
 * @see NetworkingUtils#downloadAndVerify(Path, String, int, String)
 */
public class NetworkingUtils {
    private static final LoggerCustom logger = new LoggerCustom("Network Utilities");

    /**
     * This method is used to check the connection to the specified URL.
     * @param url URL to check connection with.
     * @return True if connection is successful, false otherwise.
     */
    public static boolean checkConnection(final String url) {
        try {
            new URL(url).openConnection().connect();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Used to download a file from a URL.
     * @param File Path for the download.
     * @param DownloadUrl URL to a file.
     * @param FileName Name of the file.
     * @apiNote This method does not verify downloaded files. For that purpose, use {@link NetworkingUtils#downloadAndVerify(Path, String, int, String)}
     */
    public static void download(Path File, final String DownloadUrl, @Nullable String FileName) {
        try {
            if (Objects.isNull(FileName)) {
                FileName = File.getFileName().toString();
            } else {
                if (Files.isDirectory(File)){
                    File = Path.of(File.toString(), FileName);
                } else {
                    File = Path.of(FileUtils.getParentFolderAsString(File), FileName);
                }
            }

            if (Files.exists(File)) {
                logger.warn("Found already file with the same name as the download! Renaming existing file...");
                logger.warn("File has been renamed to: " + FileUtils.rename(File, FileName));
            }

            logger.log("Started downloading " + FileName + " ...");
            if (Files.notExists(FileUtils.getParentFolder(File))) FileUtils.createRequiredPathToAFile(File);

            long StartTime = System.currentTimeMillis();
            URL DownloadURL = new URL(DownloadUrl);
            OutputStream OutputFile = Files.newOutputStream(File, StandardOpenOption.CREATE_NEW);
            URLConnection MainConnection = DownloadURL.openConnection();
            InputStream InputData = MainConnection.getInputStream();

            byte[] Buffer = new byte[4096];
            int read;

            while((read = InputData.read(Buffer)) > 0)
                OutputFile.write(Buffer, 0, read);

            OutputFile.close();
            InputData.close();

            float ElapsedTime = (float) (System.currentTimeMillis() - StartTime) / 1000F;
            logger.log("Finished downloading " + FileName + " (Took " + ElapsedTime + "s)");
        } catch(Exception e) {
            if (Objects.equals(e.getClass(), UnknownHostException.class)) {
                logger.critical("Couldn't find specified host (" + e.getMessage() + ") for the download of \"" + File + "\"!");
            } else {
                logger.logStackTrace("Failed to download \"" + File + "\" with an exception!", e);
            }
        }
    }

    /**
     * Used to download a file from a URL.
     * @param File Path for the download.
     * @param DownloadUrl URL to a file.
     * @apiNote This method does not verify downloaded files. For that purpose, use {@link NetworkingUtils#downloadAndVerify(Path, String, int)}
     */
    public static void download(Path File, final String DownloadUrl) {
        download(File, DownloadUrl, null);
    }

    /**
     * Used to automatically download, verify, and if verification fails, re-download specified file.
     * @param File Destination of the downloaded file.
     * @param DownloadURL String with URL to the file.
     * @param FileSize Expected FileSize.
     * @param FileName @Nullable String with the name for the downloaded file.
     * @throws IOException when IO Operation fails.
     * @throws NoSuchAlgorithmException when Hash Verification complains about Algorithm.
     * @throws InterruptedException when Thread is interrupted.
     */
    public static boolean downloadAndVerify(Path File, final String DownloadURL, final int FileSize, @Nullable String FileName, String Hash, String Algorithm)
        throws IOException, NoSuchAlgorithmException, InterruptedException
    {
        Objects.requireNonNull(File);
        Objects.requireNonNull(DownloadURL);

        download(File, DownloadURL, FileName);

        if (Objects.isNull(FileName)) {
            FileName = File.getFileName().toString();
        }

        logger.log("Verifying " + FileName + " after download...");
        if ((Objects.isNull(Hash) || Objects.isNull(Algorithm))?
                !FileVerUtils.verifyFile(File, FileSize, DownloadURL):
                !FileVerUtils.verifyFile(File, FileSize, Hash, Algorithm)
        ) {
            logger.error("Verification of the " + FileName + " failed! Trying to re-download the file...");
            if(NetworkingUtils.reDownload(File, FileSize, DownloadURL, FileName, Hash, Algorithm)) {
                logger.log("Re-download of " + FileName + " was successful!");
            } else {
                logger.critical("Re-download of " + FileName + " after " + ArgumentDecoder.getInstance().getDownloadAttempts() + " attempts failed!");
                return false;
            }
        } else {
            logger.log("Verification of the file \"" + FileName + "\" was successful.");
        }
        return true;
    }

    /**
     * Used to automatically download, verify, and if verification fails, re-download specified file.
     * @param File Destination of the downloaded file.
     * @param DownloadURL String with URL to the file.
     * @param FileSize Expected FileSize.
     * @param FileName @Nullable String with the name for the downloaded file.
     * @throws IOException when IO Operation fails.
     * @throws NoSuchAlgorithmException when Hash Verification complains about Algorithm.
     * @throws InterruptedException when Thread is interrupted.
     */
    public static boolean downloadAndVerify(Path File, final String DownloadURL, final int FileSize, @Nullable String FileName)
            throws IOException, NoSuchAlgorithmException, InterruptedException
    {
        return downloadAndVerify(File, DownloadURL, FileSize, FileName, null, null);
    }

    /**
     * Used to automatically download, verify, and if verification fails, re-download specified file.
     * @param File Destination of the downloaded file.
     * @param DownloadURL String with URL to the file.
     * @param FileSize Expected FileSize.
     * @throws IOException when IO Operation fails.
     * @throws NoSuchAlgorithmException when Hash Verification complains about Algorithm.
     * @throws InterruptedException when Thread is interrupted.
     */
    public static boolean downloadAndVerify(Path File, final String DownloadURL, final int FileSize) throws IOException, NoSuchAlgorithmException, InterruptedException {
        return downloadAndVerify(File, DownloadURL, FileSize, null, null, null);
    }

    /**
     * Used to automatically delete, re-download and verify a file. Each attempt waits 500ms x attempt before requesting data from the server.
     * @param file Path to a file to re-download.
     * @param downloadUrl DownloadURl of a file.
     * @param fileName A name of the file.
     * @param fileSize Expected length of the file.
     * @param Hash Hash for the file verification. If null, Hash will be calculated from the DownloadURL
     * @param Algorithm Algorithm for the specified Hash value.
     * @return Boolean with the result of re-download.
     * @throws IOException when IO operation fails.
     * @throws NoSuchAlgorithmException when Hash Verification complains about algorithm.
     * @throws InterruptedException when thread is interrupted.
     * @apiNote The number of attempts for re-downloading a file is defined in the arguments (Default: 5)
     */
    public static boolean reDownload(Path file, Number fileSize, String downloadUrl, @Nullable String fileName, @Nullable String Hash, @Nullable String Algorithm)
        throws IOException, NoSuchAlgorithmException, InterruptedException
    {
        boolean hashVerification = Objects.nonNull(Hash) && Objects.nonNull(Algorithm);
        if (Objects.isNull(fileName)) fileName = file.getFileName().toString();

        for (int i = 0; i < ArgumentDecoder.getInstance().getDownloadAttempts(); i++) {
            // Waiting a while, in case server has some small issue and requires a bit of time, Each attempt increases the time to wait.
            //noinspection BusyWait
            Thread.sleep(2500L * i);

            if (Files.deleteIfExists(file)) {
                logger.log("Deleted corrupted " + fileName + ". Re-download attempt: " + (i+1));
            }

            download(file, downloadUrl, fileName);
            if (hashVerification) {
                if (verifyFile(file, fileSize, Hash, Algorithm)) {
                    return true;
                }
            } else {
                if (verifyFile(file, fileSize, downloadUrl)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Used to automatically delete, re-download and verify a file. Each attempt waits 500ms x attempt before requesting data from the server.
     * @param file Path to a file to re-download.
     * @param downloadUrl DownloadURl of a file.
     * @param fileName A name of the file.
     * @param fileSize Expected length of the file.
     * @return Boolean with the result of re-download.
     * @throws IOException when IO operation fails.
     * @throws NoSuchAlgorithmException when Hash Verification complains about algorithm.
     * @throws InterruptedException when thread is interrupted.
     * @apiNote The number of attempts for re-downloading a file is defined in the arguments (Default: 5)
     */
    public static boolean reDownload(Path file, Number fileSize, String downloadUrl, @Nullable String fileName) throws IOException, NoSuchAlgorithmException, InterruptedException {
        return reDownload(file, fileSize, downloadUrl, fileName, null, null);
    }

    /**
     * Used to automatically delete, re-download and verify a file. Each attempt waits 500ms x attempt before requesting data from the server.
     * @param file Path to a file to re-download.
     * @param downloadUrl DownloadURl of a file.
     * @param fileSize Expected length of the file.
     * @return Boolean with the result of re-download.
     * @throws IOException when IO operation fails.
     * @throws NoSuchAlgorithmException when Hash Verification complains about algorithm.
     * @throws InterruptedException when thread is interrupted.
     * @apiNote The number of attempts for re-downloading a file is defined in the arguments (Default: 5)
     */
    public static boolean reDownload(Path file, Number fileSize, String downloadUrl) throws IOException, NoSuchAlgorithmException, InterruptedException {
        return reDownload(file, fileSize, downloadUrl, null);
    }
}

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

import static com.kanzaji.catdownloaderlegacy.utils.FileVerUtils.verifyFile;

import com.kanzaji.catdownloaderlegacy.ArgumentDecoder;
import com.kanzaji.catdownloaderlegacy.loggers.LoggerCustom;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

public class DownloadUtils {
    private static final LoggerCustom logger = new LoggerCustom("Download Utilities");

    //TODO: Rework this
    /**
     * Used to download a file from a URL.
     * @param File Path for the download.
     * @param DownloadUrl URL to a file.
     * @param FileName Name of the file.
     * @apiNote This method does not verify downloaded files. For that purpose, use {@link FileVerUtils#verifyFile(Path, Number, String)}
     */
    public static void download(Path File, final String DownloadUrl, @Nullable String FileName) {
        try {
            if (FileName == null) {
                FileName = File.getFileName().toString();
            } else {
                File = Path.of(FileUtils.getFolderAsString(File), FileName);
            }

            logger.log("Started downloading " + FileName + " ...");

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
            logger.log(String.format("Finished downloading %s (Took %.2fs)", FileName, ElapsedTime));
        } catch(Exception e) {
            logger.logStackTrace("Failed to download " + FileName, e);
        }
    }

    /**
     * Used to download a file from a URL.
     * @param File Path for the download.
     * @param DownloadUrl URL to a file.
     * @apiNote This method does not verify downloaded files. For that purpose, use {@link FileVerUtils#verifyFile(Path, Number, String)}
     */
    public static void download(Path File, final String DownloadUrl) {
        download(File, DownloadUrl, null);
    }

    /**
     * Used to automatically delete, download and verify a file.
     * @param file Path to a file to re-download.
     * @param downloadUrl DownloadURl of a file.
     * @param fileName A name of the file.
     * @param fileSize Expected length of the file.
     * @return Boolean with the result of re-download.
     * @throws IOException when IO operation fails.
     * @throws NoSuchAlgorithmException when Hash Verification complains about algorithm.
     * @apiNote The amount of attempts for re-downloading a file can be defined in the arguments (Default: 5)
     */
    public static boolean reDownload(Path file, String downloadUrl, String fileName, Number fileSize) throws IOException, NoSuchAlgorithmException {
        if (Objects.isNull(fileName)) {
            fileName = file.getFileName().toString();
        }
        for (int i = 0; i < ArgumentDecoder.getInstance().getDownloadAttempts(); i++) {
            if (Files.deleteIfExists(file)) {
                logger.log("Deleted corrupted " + fileName + ". Re-download attempt: " + (i+1));
            }
            download(file, downloadUrl, fileName);
            if (verifyFile(file, fileSize, downloadUrl)) {
                return true;
            }
        }
        return false;
    }
}

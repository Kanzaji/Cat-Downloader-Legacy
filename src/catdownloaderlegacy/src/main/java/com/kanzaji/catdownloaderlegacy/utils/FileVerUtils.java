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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * This class holds all File Verification methods.
 */
public class FileVerUtils {
    private static final LoggerCustom logger = new LoggerCustom("File Verification Utilities");

    /**
     * Used to verify integrity of the file with use of {@link FileVerUtils#verifyFileSize(Path, int)} and {@link FileVerUtils#verifyHash(Path, String)}.
     * @param File {@link Path} to a file designated for verification.
     * @param Size {@link Number} with Expected file length.
     * @param URL {@link String} DownloadURL for Hash verification.
     * @return {@link Boolean} with the result of the verification.
     * @throws IOException when IO Operation fails.
     */
    public static boolean verifyFile(Path File, Number Size, String URL) throws IOException, NoSuchAlgorithmException {
        if (Files.notExists(File)) {
            logger.error("File for mod " + File.getFileName() + " doesn't exists??");
            return false;
        }
        return verifyFileSize(File, Size) && verifyHash(File, URL);
    }

    /**
     * File size verification, can be disabled with an argument!
     * @param File {@link Path} to a file designated for verification.
     * @param Size {@link Number} with Expected file length..
     * @return {@link Boolean} with the result of the verification.
     * @throws IOException when IO Operation fails.
     */
    public static boolean verifyFileSize(Path File, Number Size) throws IOException {
        return verifyFileSize(File, Size.intValue());
    }

    /**
     * File size verification, can be disabled with an argument!
     * @param File {@link Path} to a file designated for verification.
     * @param Size {@link Number} with Expected file length.
     * @return {@link Boolean} with the result of the verification.
     * @throws IOException when IO Operation fails.
     */
    public static boolean verifyFileSize(Path File, int Size) throws IOException {
        if (!ArgumentDecoder.getInstance().isFileSizeVerActive()) {
            return true;
        }
        return Files.size(File) == Size;
    }

    /**
     * Used to verify a file using Hash calculations (SHA-256).
     * @param File {@link Path} to a file designated for verification.
     * @param DownloadURL {@link String} DownloadURL to a source file.
     * @return {@link Boolean} with the result of the verification.
     * @throws IOException when IO Operation fails.
     * @throws NoSuchAlgorithmException when Hash Verification complains about Algorithm for some reason.
     */
    public static boolean verifyHash(Path File, String DownloadURL) throws IOException, NoSuchAlgorithmException {
        if (!ArgumentDecoder.getInstance().isHashVerActive()) {
            return true;
        }
        return Arrays.equals(getHash(File), getHash(DownloadURL));
    }

    /**
     * Used to get a Hash (SHA-256) from an URL.
     * @param DownloadURL {@link String} DownloadURL to a source file.
     * @return {@link Byte} array with the result of the Hash calculations
     * @throws IOException when IO operation fails.
     * @throws NoSuchAlgorithmException when Hash Verification complains about Algorithm.
     */
    public static byte[] getHash(String DownloadURL) throws IOException, NoSuchAlgorithmException {
        return getHash(DownloadURL, null, "SHA-256");
    }

    /**
     * Used to get a Hash (SHA-256) from a file.
     * @param FilePath {@link Path} to a file to calculate Hash from.
     * @return {@link Byte} array with the result of the Hash calculations
     * @throws IOException when IO operation fails.
     * @throws NoSuchAlgorithmException when Hash Verification complains about Algorithm for some reason.
     */
    public static byte[] getHash(Path FilePath) throws IOException, NoSuchAlgorithmException {
        return getHash(null, FilePath, "SHA-256");
    }

    /**
     * Used to get a Hash from a file or URL.
     * @param DownloadURL {@link String} URL for data stream to calculate Hash from.
     * @param FilePath {@link Path} to a file to calculate Hash from.
     * @param Algorithm {@link String} Algorithm to use for Calculations.
     * @return {@link Byte} array with the result of the Hash calculations
     * @throws IOException when IO operation fails.
     * @throws NoSuchAlgorithmException when Hash Verification complains about Algorithm.
     */
    private static byte[] getHash(String DownloadURL, Path FilePath, String Algorithm) throws IOException, NoSuchAlgorithmException {
        // Logger commands are commented out because they are making a lot of mess in the log itself, and they aren't that important
        if (DownloadURL == null && FilePath == null) {
            throw new NullPointerException("Both arguments for Hash verification are null!");
        }
//        long StartTime = System.currentTimeMillis();
        InputStream InputData;
        MessageDigest MD = MessageDigest.getInstance(Algorithm);

        if (DownloadURL == null) {
//            logger.log("Getting Hash for file " + FilePath.getFileName());
            InputData = Files.newInputStream(FilePath);
        } else {
//            logger.log("Getting Hash for file from URL " + DownloadURL);
            InputData = new URL(DownloadURL).openStream();
        }

        byte[] Buffer = new byte[4096];
        int read;

        while((read = InputData.read(Buffer)) > 0)
            MD.update(Buffer, 0, read);

        InputData.close();
//        logger.log(
//                String.format("Finished getting Hash for %s (Took %.2fs)",
//                (DownloadURL == null)? FilePath.getFileName(): DownloadUrl,
//                (float) (System.currentTimeMillis() - StartTime) / 1000F
//                ));
        return MD.digest();
    }
}

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
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

/**
 * This class holds utility methods related to verification of the files.
 * @see FileVerUtils#verifyFile(Path, Number, String)
 */
public class FileVerUtils {
    private static final LoggerCustom logger = new LoggerCustom("File Verification Utilities");

    /**
     * Used to verify integrity of the file with use of {@link FileVerUtils#verifyFileSize(Path, int)} and {@link FileVerUtils#verifyHash(Path, String, String)}.
     * @param File {@link Path} to a file designated for verification.
     * @param Size {@link Number} with Expected file length.
     * @param Hash {@link String} with Expected Hash value.
     * @param Algorithm {@link String} with Algorithm of the Hash passed to the method.
     * @return {@link Boolean} with the result of the verification.
     * @throws IOException when IO Operation fails.
     * @throws NoSuchAlgorithmException when Algorithm is invalid or not supported.
     */
    public static boolean verifyFile(Path File, Number Size, String Hash, String Algorithm) throws IOException, NoSuchAlgorithmException {
        if (Files.notExists(File)) {
            logger.error("File for mod " + File.getFileName() + " doesn't exists??");
            return false;
        }
        return verifyFileSize(File, Size) && verifyHash(File, Hash, Algorithm);
    }

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
     * File size verification. Can be disabled with an argument!
     * @param File {@link Path} to a file designated for verification.
     * @param Size {@link Number} with Expected file length.
     * @return {@link Boolean} with the result of the verification.
     * @throws IOException when IO Operation fails.
     */
    public static boolean verifyFileSize(Path File, @NotNull Number Size) throws IOException {
        return verifyFileSize(File, Size.intValue());
    }

    /**
     * File size verification. Can be disabled with an argument!
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
     * Used to verify a file using Hash calculations with specified algorithm, comparing to passed Hash value.
     * @param File {@link Path} to a file to calculate Hash from.
     * @param Algorithm {@link String} Algorithm to use for Calculations.
     * @return {@link Byte} array with the result of the Hash calculations
     * @throws IOException when IO operation fails.
     * @throws NoSuchAlgorithmException when Hash Verification complains about Algorithm.
     */
    public static boolean verifyHash(Path File, String Hash, String Algorithm) throws IOException, NoSuchAlgorithmException {
        if (!ArgumentDecoder.getInstance().isHashVerActive()) {
            return true;
        }
        return Objects.equals(getHash(File, Algorithm), Hash);
    }

    /**
     * Used to verify a file using Hash calculations (SHA-256) with the resource from the network.
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
        return Objects.equals(getHash(File), getHash(DownloadURL));
    }

    /**
     * Used to get a Hash (SHA-256) from an URL.
     * @param DownloadURL {@link String} DownloadURL to a source file.
     * @return {@link Byte} array with the result of the Hash calculations
     * @throws IOException when IO operation fails.
     * @throws NoSuchAlgorithmException when Hash Verification complains about Algorithm.
     */
    public static @NotNull String getHash(String DownloadURL) throws IOException, NoSuchAlgorithmException {
        return getHash(DownloadURL, null, "SHA-256");
    }

    /**
     * Used to get a Hash (SHA-256) from a file.
     * @param FilePath {@link Path} to a file to calculate Hash from.
     * @return {@link Byte} array with the result of the Hash calculations
     * @throws IOException when IO operation fails.
     * @throws NoSuchAlgorithmException when Hash Verification complains about Algorithm for some reason.
     */
    public static @NotNull String getHash(Path FilePath) throws IOException, NoSuchAlgorithmException {
        return getHash(null, FilePath, "SHA-256");
    }

    /**
     * Used to get a Hash from a file or URL.
     * @param DownloadURL {@link String} URL for data stream to calculate Hash from.
     * @param Algorithm {@link String} Algorithm to use for Calculations.
     * @return {@link Byte} array with the result of the Hash calculations
     * @throws IOException when IO operation fails.
     * @throws NoSuchAlgorithmException when Hash Verification complains about Algorithm.
     */
    public static @NotNull String getHash(String DownloadURL, String Algorithm) throws IOException, NoSuchAlgorithmException {
        return getHash(DownloadURL, null, Algorithm);
    }

    /**
     * Used to get a Hash from a file or URL.
     * @param FilePath {@link Path} to a file to calculate Hash from.
     * @param Algorithm {@link String} Algorithm to use for Calculations.
     * @return {@link Byte} array with the result of the Hash calculations
     * @throws IOException when IO operation fails.
     * @throws NoSuchAlgorithmException when Hash Verification complains about Algorithm.
     */
    public static @NotNull String getHash(Path FilePath, String Algorithm) throws IOException, NoSuchAlgorithmException {
        return getHash(null, FilePath, Algorithm);
    }

    /**
     * Used to get a Hash from a file or URL. Either DownloadURL or FilePath should be null.
     * @param DownloadURL {@link String} URL for data stream to calculate Hash from.
     * @param FilePath {@link Path} to a file to calculate Hash from.
     * @param Algorithm {@link String} Algorithm to use for Calculations.
     * @return {@link Byte} array with the result of the Hash calculations
     * @throws IOException when IO operation fails.
     * @throws NoSuchAlgorithmException when Hash Verification complains about Algorithm.
     * @throws NullPointerException when both arguments are null.
     * @apiNote Non-Fatal exception will be thrown and logged when both DownloadURL and FilePath are not-null.
     */
    private static @NotNull String getHash(String DownloadURL, Path FilePath, String Algorithm) throws IOException, NoSuchAlgorithmException {
        Objects.requireNonNull(Algorithm);

        if (Objects.isNull(DownloadURL) && Objects.isNull(FilePath)) {
            throw new NullPointerException("Both arguments for Hash verification are null!");
        } else if (Objects.nonNull(DownloadURL) && Objects.nonNull(FilePath)) {
            try {
                throw new IllegalArgumentException("Both arguments for Hash verification are populated!");
            } catch (IllegalArgumentException e) {
                logger.logStackTrace("Invalid implementation of getHash() detected! Stacktrace below to trace problematic implementation. DownloadURL will be prioritized.", e);
            }
        }

        InputStream InputData;
        MessageDigest MD = MessageDigest.getInstance(Algorithm);

        if (Objects.isNull(DownloadURL)) {
            if (Files.notExists(FilePath)) throw new NoSuchFileException("Specified File to use for calculating hash value (" + Algorithm +") doesn't exists!");
            InputData = Files.newInputStream(FilePath);
        } else {
            InputData = new URL(DownloadURL).openStream();
        }

        byte[] Buffer = new byte[4096];
        int read;

        while((read = InputData.read(Buffer)) > 0)
            MD.update(Buffer, 0, read);

        InputData.close();

        StringBuilder hash = new StringBuilder(new BigInteger(1, MD.digest()).toString(16));
        while (hash.length() < MD.getDigestLength()*2) {
            hash.insert(0, "0");
        }
        return hash.toString();
    }
}

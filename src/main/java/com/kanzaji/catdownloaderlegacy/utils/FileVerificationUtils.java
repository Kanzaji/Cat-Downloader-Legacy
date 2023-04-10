package com.kanzaji.catdownloaderlegacy.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class FileVerificationUtils {
    private static final Logger logger = Logger.getInstance();

    /**
     * Used to verify integrity of the file with use of Length and Hash verification!
     * @param File Path to a file designated for verification.
     * @param Size Expected file length.
     * @param URL DownloadURL for Hash verification.
     * @return Boolean with the result of the verification.
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
     * @param File Path to a file designated for verification.
     * @param Size Expected file length.
     * @return Boolean with the result of the verification.
     * @throws IOException when IO Operation fails.
     */
    public static boolean verifyFileSize(Path File, Number Size) throws IOException {
        return verifyFileSize(File, Size.intValue());
    }

    /**
     * File size verification, can be disabled with an argument!
     * @param File Path to a file designated for verification.
     * @param Size Expected file length.
     * @return Boolean with the result of the verification.
     * @throws IOException when IO Operation fails.
     */
    public static boolean verifyFileSize(Path File, int Size) throws IOException {
        if (!ArgumentDecoder.getInstance().getBooleanData("SizeVer")) {
            return true;
        }
        return Files.size(File) == Size;
    }

    /**
     * Used to verify a file using Hash calculations (SHA-256).
     * @param File Path to a file designated for verification.
     * @param DownloadURL URL to a source of the file.
     * @return Boolean with the result of the verification.
     * @throws IOException when IO operation fails.
     * @throws NoSuchAlgorithmException when Hash Verification complains about Algorithm for some reason.
     */
    public static boolean verifyHash(Path File, String DownloadURL) throws IOException, NoSuchAlgorithmException {
        if (!ArgumentDecoder.getInstance().getBooleanData("HashVer")) {
            return true;
        }
        return Arrays.equals(getHash(File), getHash(DownloadURL));
    }

    /**
     * Used to get a Hash (SHA-256) from a URL.
     * @param DownloadURL URL for data stream to calculate Hash from.
     * @return Byte array with the result of the Hash calculations
     * @throws IOException when IO operation fails.
     * @throws NoSuchAlgorithmException when Hash Verification complains about Algorithm for some reason.
     */
    public static byte[] getHash(String DownloadURL) throws IOException, NoSuchAlgorithmException {
        return getHash(DownloadURL, null, "SHA-256");
    }

    /**
     * Used to get a Hash (SHA-256) from a file.
     * @param FilePath Path to a file to calculate Hash from.
     * @return Byte array with the result of the Hash calculations
     * @throws IOException when IO operation fails.
     * @throws NoSuchAlgorithmException when Hash Verification complains about Algorithm for some reason.
     */
    public static byte[] getHash(Path FilePath) throws IOException, NoSuchAlgorithmException {
        return getHash(null, FilePath, "SHA-256");
    }

    /**
     * Used to get a Hash from a file or URL.
     * @param DownloadURL URL for data stream to calculate Hash from.
     * @param FilePath Path to a file to calculate Hash from.
     * @param Algorithm Algorithm to use for Calculations.
     * @return Byte array with the result of the Hash calculations
     * @throws IOException when IO operation fails.
     * @throws NoSuchAlgorithmException when Hash Verification complains about Algorithm.
     */
    private static byte[] getHash(String DownloadURL, Path FilePath, String Algorithm) throws IOException, NoSuchAlgorithmException {
        // Logger commands are commented out because they are making a lot of mess in the log itself, and they aren't that important tbh.
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

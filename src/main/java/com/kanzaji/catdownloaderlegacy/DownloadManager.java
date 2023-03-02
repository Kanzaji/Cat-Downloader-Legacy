package com.kanzaji.catdownloaderlegacy;

import com.kanzaji.catdownloaderlegacy.utils.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class DownloadManager {
    private static final Logger logger = Logger.getInstance();
    public static Runnable download(final Path target, final String downloadUrl) {
        return () -> {
            String name = target.getFileName().toString();

            try {
                logger.log("Downloading " + name);
                long time = System.currentTimeMillis();

                URL url = new URL(downloadUrl);
                OutputStream out = java.nio.file.Files.newOutputStream(target, StandardOpenOption.CREATE_NEW);

                URLConnection connection = url.openConnection();
                InputStream in = connection.getInputStream();

                byte[] buf = new byte[4096];
                int read;

                while((read = in.read(buf)) > 0)
                    out.write(buf, 0, read);

                out.close();
                in.close();

                float secs = (float) (System.currentTimeMillis() - time) / 1000F;
                logger.log(String.format("Finished downloading %s (Took %.2fs)", name, secs));
            } catch(Exception e) {
                System.out.println("Failed to download " + name);
                logger.logStackTrace("Failed to download " + name, e);
            }

            try {
                if (java.nio.file.Files.size(target) == 0) {
                    logger.warn("Probably failed to download " + name +" // File appears to be empty!");
                    logger.warn("Trying to download " + name +" again...");
                    if (java.nio.file.Files.deleteIfExists(target))	download(target, downloadUrl);
                }
            } catch (IOException e) {
                logger.logStackTrace("Failed to delete file " + name, e);
            }
        };
    }
}

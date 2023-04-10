package com.kanzaji.catdownloaderlegacy.utils;

import java.io.InputStream;
import java.util.MissingResourceException;

public class AssetsUtils {
    public static InputStream getFile(String url) {
        InputStream file = AssetsUtils.class.getResourceAsStream("/assets/" + url);
        if (file == null) {
            throw new MissingResourceException("One of the integral assets is missing!\nThis may signal corrupted app. Please consider reinstalling the program.\nMissing asset: " + url + "\nError StackTrace:", url, url);
        }
        return file;
    }
}

package com.kanzaji.catdownloaderlegacy.utils;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static com.kanzaji.catdownloaderlegacy.CatDownloader.VERSION;

/**
 * This class holds most (if not all) Update Checking + Updating related methods.
 */
public class Updater {

    /**
     * This method calls GitHub repository and returns true, if newer version of the app is available.
     * @return Boolean depending on the result of update check.
     */
    public static boolean checkUpdates() {
        // TODO: Finish Updater Checker
        return false;
    }

    /**
     * This method is used to compare current version numbers with a dot (".") as a separator.
     * It does not support version numbers with characters.
     * @param currentVersion Current Version to compare.
     * @param latestVersion Latest Version to compare against.
     * @return Boolean True when current version is the same or higher than latest, otherwise false.
     */
    public static boolean compareVersions(String currentVersion, String latestVersion) throws NumberFormatException  {
        return compareVersions(currentVersion, latestVersion, "\\.");
    }

    /**
     * This method is used to compare current version numbers with a given separator.
     * It does not support version numbers with characters.
     * @param currentVersion Current Version to compare.
     * @param latestVersion Latest Version to compare against.
     * @param separator A String used to separate numbers in passed version schema (Regex).
     * @return Boolean True when current version is the same or higher than latest, otherwise false.
     */
    public static boolean compareVersions(String currentVersion, String latestVersion, String separator) throws NumberFormatException {
        List<String> currentVersionMap = new LinkedList<>(Arrays.stream(currentVersion.split(separator)).toList());
        List<String> latestVersionMap = new LinkedList<>(Arrays.stream(latestVersion.split(separator)).toList());
        boolean equal = true;

        for (int i = 0; i < (Math.min(currentVersionMap.size(), latestVersionMap.size())); i++) {
            try {
                int version1 = Integer.parseInt(currentVersionMap.get(i));
                int version2 = Integer.parseInt(latestVersionMap.get(i));
                if (version2 != version1) {
                    equal = false;
                    if (version2 > version1) {
                        return false;
                    }
                }
            } catch (Exception E) {
                throw new NumberFormatException("Non-Int character found in the version schema! Comparison can't continue.");
            }
        }

        // If equal and latest version map size is higher, return false
        // If not equal, return true. (If latest was higher it would return false earlier on)
        // If Equal and current version map size is higher / equal, return true
        // Yes this is here just so I will not mess this up lol;
        return !equal || currentVersionMap.size() >= latestVersionMap.size();
    }
}

package com.kanzaji.catdownloaderlegacy.jsons;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Object used to represent JSON Structure of configuration file.
 * @see com.kanzaji.catdownloaderlegacy.utils.SettingsManager
 */
public class Settings {
    /**
     * This static contains all Setting Keys from the Settings File.
     */
    public static String[] SettingsKeys = {
            "mode",
            "workingDirectory",
            "logDirectory",
            "threadCount",
            "downloadAttempts",
            "logStockpileSize",
            "isLoggerActive",
            "shouldStockpileLogs",
            "shouldCompressLogFiles",
            "isFileSizeVerificationActive",
            "isHashVerificationActive",
            "modBlacklist"
    };

    public String mode;
    public String workingDirectory;
    public String logDirectory;
    public int threadCount;
    public int downloadAttempts;
    public int logStockpileSize;
    public boolean isLoggerActive;
    public boolean shouldStockpileLogs;
    public boolean shouldCompressLogFiles;
    public boolean isFileSizeVerificationActive;
    public boolean isHashVerificationActive;
    public BlackList<String> modBlacklist;
    public boolean experimental;

    public static class BlackList<E> extends LinkedList<E> {
        @Override
        public String toString() {
            Iterator<E> it = iterator();
            if (! it.hasNext())
                return "[]";

            StringBuilder sb = new StringBuilder();
            sb.append('[');
            for (;;) {
                E e = it.next();
                sb.append(e == this ? "(this Collection)" : "\"" + e + "\"");
                if (! it.hasNext())
                    return sb.append(']').toString();
                sb.append(',').append(' ');
            }
        }
    }
}

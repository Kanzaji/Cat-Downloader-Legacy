package com.kanzaji.catdownloaderlegacy.jsons;

/**
 * Object used to represent JSON Structure of configuration file.
 * @see com.kanzaji.catdownloaderlegacy.utils.SettingsManager
 */
public class Settings {
    public String mode;
    public String workingDirectory;
    public String logDirectory;
    public int threadCount;
    public int downloadAttempts;
    public int logStockPileSize;
    public boolean isLoggerActive;
    public boolean shouldStockPileLogs;
    public boolean shouldCompressLogFiles;
    public boolean isFileSizeVerificationActive;
    public boolean isHashVerificationActive;
    public String[] modBlacklist;
    public boolean experimental;
}

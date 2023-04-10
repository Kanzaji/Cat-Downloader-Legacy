package com.kanzaji.catdownloaderlegacy.jsons;

/**
 * Object used to represent JSON Structure of configuration file.
 * @see com.kanzaji.catdownloaderlegacy.utils.SettingsManager
 */
public class Settings {
    /**
     * Does this work?
      */
    public String mode;
    public String workingDirectory;
    public int threadCount;
    public int downloadAttempts;
    public boolean isLoggerActive;
    public boolean isFileSizeVerificationActive;
    public boolean isSumCheckVerificationActive;
    public String[] modBlacklist;
}

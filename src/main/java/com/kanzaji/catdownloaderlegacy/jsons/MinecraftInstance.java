package com.kanzaji.catdownloaderlegacy.jsons;

public class MinecraftInstance {

    public baseModLoader baseModLoader;
    public String name;
    public installedAddons[] installedAddons;

    public static class installedAddons {
        public Number addonID;
        public AddonFile installedFile;
    }
    public static class AddonFile {
        public Number id;
        public Number fileLenght;
        public String downloadUrl;
    }
    public static class baseModLoader {
        public String name;
        public String minecraftVersion;
    }
}

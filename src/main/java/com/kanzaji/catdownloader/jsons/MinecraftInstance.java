package com.kanzaji.catdownloader.jsons;

public class MinecraftInstance {

    public baseModLoader baseModLoader;
    public String name;
    public installedAddons[] installedAddons;

    public static class installedAddons {
        public AddonFile installedFile;
    }
    public static class AddonFile {
        public String downloadUrl;
    }
    public static class baseModLoader {
        public String name;
        public String minecraftVersion;
    }
}

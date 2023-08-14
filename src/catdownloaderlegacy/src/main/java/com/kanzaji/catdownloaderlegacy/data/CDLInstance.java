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

package com.kanzaji.catdownloaderlegacy.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kanzaji.catdownloaderlegacy.loggers.LoggerCustom;
import com.kanzaji.catdownloaderlegacy.temp.OldDataGathering;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.rmi.UnexpectedException;
import java.util.Objects;
import java.util.UnknownFormatConversionException;

/**
 * This class holds data for CDLPack format, and additional methods for transforming other formats (CurseForge Instance / Pack, Modrinth mrpack) to this format.
 * @apiNote This class will be extended in the future with more fields in the Launcher version of the app.
 */
public class CDLInstance {
    private static final LoggerCustom logger = new LoggerCustom("CDLInstance Utilities");
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    public static final String formatVersion = "1.0";

    /**
     * This method is used to fill up data in CDLInstance Object with information from passed CFManifest object.
     * @param CFPackData CFManifest to import data from.
     * @return Itself, for easier use after importing.
     * @throws UnexpectedException when Exception occurs in the translating code.
     * @apiNote This method DOES NOT return Hashes used for verification of the downloads. Filling up missing hashes is required to do at the download process.
     * @see OldDataGathering#startDataGathering() Click this for the old solution of data gathering.
     */
    @ApiStatus.Experimental
    public CDLInstance importCFPack(@NotNull CFManifest CFPackData) throws UnexpectedException  {
        logger.log("Translating CF Manifest into CDLInstance format...");
        Objects.requireNonNull(CFPackData);
        try {
            this.instanceName = CFPackData.name;
            this.modpackData = new ModpackData();
            this.modpackData.name  = CFPackData.name;
            this.modpackData.version = CFPackData.version;
            this.modpackData.author  = CFPackData.author;
            this.modpackData.overrides  = CFPackData.overrides;
            this.minecraftData = new MinecraftData();
            this.minecraftData.version = CFPackData.minecraft.version;
            this.modLoaderData = new ModLoader();
            String modLoaderName = CFPackData.minecraft.modLoaders[0].id;
            this.modLoaderData.modLoader = (
                modLoaderName.contains("forge")?    "forge":
                modLoaderName.contains("fabric")?   "fabric":
                modLoaderName.contains("quilt")?    "quilt":
                //TODO: Trying to future-proof here. I have no idea what CF will use for this... thing.
                modLoaderName.contains("neo-forge")? "neo-forge":
                "unknown"
            );

            //TODO: Get more manifest files, to see the schema for fabric / quilt mod loaders.
            this.modLoaderData.version = modLoaderName.substring(modLoaderName.indexOf("-")+1);
/*
            switch (this.modLoaderData.modLoader) {
                case "forge" -> this.modLoaderData.version  = modLoaderName.substring(modLoaderName.indexOf("-"));
                case "fabric" -> this.modLoaderData.version  =
                case "quilt" -> this.modLoaderData.version  =
                case "neo-forge" -> this.modLoaderData.version  =
                default -> this.modLoaderData.version = null;
            }
*/
            this.files = new ModFile[CFPackData.files.length];
            for (int i = 0; i < CFPackData.files.length; i++) {
                CFManifest.ModFile mod = CFPackData.files[i];
                //TODO: Create own getData() method. Replace use of Deprecated method.
                // Additionally make use of multithreading and experimental option.
                mod = mod.getData(CFPackData.minecraft);
                this.files[i] = new ModFile(mod.getFileName(), mod.downloadUrl, mod.fileSize.intValue());
            }
        } catch (Exception e) {
            logger.logStackTrace("Interpretation of CF Manifest failed!", e);
            logger.critical(gson.toJson(CFPackData, CFManifest.class));
            throw new UnexpectedException("Exception thrown while translating CF Manifest object!");
        }
        logger.log("Translation successful.");
        return this;
    }

    /**
     * This method is used to fill up data in CDLInstance Object with information from passed CFMinecraftInstance object.
     * @param CFInstanceData CFMinecraftInstance to import data from.
     * @return Itself, for easier use after importing.
     * @throws UnexpectedException when Exception occurs in the translating code.
     * @apiNote This method DOES NOT return Hashes used for verification of the downloads. Filling up missing hashes is required to do at the download process.
     */
    @ApiStatus.Experimental
    public CDLInstance importCFInstance(@NotNull CFMinecraftInstance CFInstanceData) throws UnexpectedException {
        logger.log("Translating CF MinecraftInstance into CDLInstance format...");
        Objects.requireNonNull(CFInstanceData);
        try {
            this.instanceName = CFInstanceData.name;
            this.minecraftData = new MinecraftData();
            this.minecraftData.version = CFInstanceData.baseModLoader.minecraftVersion;
            this.modpackData = new ModpackData();
            if (Objects.nonNull(CFInstanceData.manifest)) {
                this.modpackData.version = CFInstanceData.manifest.version;
                this.modpackData.author  = CFInstanceData.manifest.author;
                this.modpackData.overrides  = CFInstanceData.manifest.overrides;
                this.modpackData.name  = CFInstanceData.manifest.name;
            } else {
                this.modpackData.overrides = "overrides";
                this.modpackData.name = CFInstanceData.name;
            }
            this.modLoaderData = new ModLoader();
            String modLoaderName = CFInstanceData.baseModLoader.name;
            this.modLoaderData.modLoader = (
                modLoaderName.contains("forge")?    "forge":
                modLoaderName.contains("fabric")?   "fabric":
                modLoaderName.contains("quilt")?    "quilt":
                //TODO: Trying to future-proof here. I have no idea what CF will use for this... thing.
                modLoaderName.contains("neo-forge")? "neo-forge":
                "unknown"
            );

            switch (this.modLoaderData.modLoader) {
                case "forge" -> this.modLoaderData.version  = CFInstanceData.baseModLoader.forgeVersion;
                case "fabric" -> this.modLoaderData.version  = CFInstanceData.baseModLoader.fabricVersion;
                case "quilt" -> this.modLoaderData.version  = CFInstanceData.baseModLoader.quiltVersion;
                case "neo-forge" -> this.modLoaderData.version  = CFInstanceData.baseModLoader.neoForgeVersion;
                default -> this.modLoaderData.version = null;
            }

            this.files = new ModFile[CFInstanceData.installedAddons.length];
            for (int i = 0; i < CFInstanceData.installedAddons.length; i++) {
                CFMinecraftInstance.AddonFile addon = CFInstanceData.installedAddons[i].installedFile;
                this.files[i] = new ModFile(addon.fileName, addon.downloadUrl, addon.fileLength.intValue());
            }
        } catch (Exception e) {
            logger.logStackTrace("Interpretation of CF MinecraftInstance failed!", e);
            logger.critical(gson.toJson(CFInstanceData, CFMinecraftInstance.class));
            throw new UnexpectedException("Exception thrown while translating CF MinecraftInstance object!");
        }
        logger.log("Translation successful.");
        return this;
    }

    /**
     * This method is used to fill up data in CDLInstance object with information from passed MRIndex Object.
     * @param MRIndexData MRIndex to import data from.
     * @return Itself, for easier use after importing.
     * @throws UnknownFormatConversionException when MRIndex passed is in un-supported version.
     * @throws UnexpectedException when Exception occurs in the translating code.
     */
    public CDLInstance importModrinthPack(@NotNull MRIndex MRIndexData) throws UnknownFormatConversionException, UnexpectedException {
        logger.log("Translating Modrinth Index into CDLInstance format...");

        Objects.requireNonNull(MRIndexData);
        if (MRIndexData.formatVersion.intValue() != 1) {
            throw new UnknownFormatConversionException("MRIndex specified is in newer than supported version! Please report that to the github repository.");
        }

        if (!Objects.equals(MRIndexData.game, "minecraft")) {
            throw new UnknownFormatConversionException("MRIndex specified is for a different game!");
        }

        try {
            this.instanceName = MRIndexData.name;
            this.minecraftData = new MinecraftData();
            this.minecraftData.version = MRIndexData.dependencies.minecraft;
            this.modpackData = new ModpackData();
            this.modpackData.version = MRIndexData.versionId;
            this.modpackData.overrides  = "overrides";
            this.modpackData.summary = MRIndexData.summary;
            this.modpackData.name  = MRIndexData.name;
            this.modLoaderData = new ModLoader();

            if (Objects.nonNull(MRIndexData.dependencies.fabric)) {
                this.modLoaderData.version = MRIndexData.dependencies.fabric;
                this.modLoaderData.modLoader = "fabric";
            } else if (Objects.nonNull(MRIndexData.dependencies.forge)) {
                this.modLoaderData.version = MRIndexData.dependencies.forge;
                this.modLoaderData.modLoader = "forge";
            } else if (Objects.nonNull(MRIndexData.dependencies.quilt)) {
                this.modLoaderData.version = MRIndexData.dependencies.quilt;
                this.modLoaderData.modLoader = "quilt";
            } else if (Objects.nonNull(MRIndexData.dependencies.neoforge)) {
                // TODO: Future proof! Yes this is only here so I know where to change stuff.
                this.modLoaderData.version = MRIndexData.dependencies.neoforge;
                this.modLoaderData.modLoader = "neo-forge";
            } else {
                this.modLoaderData.version = null;
                this.modLoaderData.modLoader = "unknown";
            }

            this.files = new ModFile[MRIndexData.files.length];
            for (int i = 0; i < MRIndexData.files.length; i++) {
                MRIndex.MRModFile mod = MRIndexData.files[i];
                //TODO: Create proper "Server / client" separation in the launcher version.
                if (Objects.nonNull(mod.env) && Objects.equals(mod.env.client, "unsupported")) continue;
                this.files[i] = new ModFile(
                    Path.of(mod.path).getFileName().toString(),
                    mod.downloads[0],
                    mod.fileSize.intValue(),
                    mod.hashes,
                    mod.path
                );
            }
        } catch (Exception e) {
            logger.logStackTrace("Interpretation of Modrinth Index failed!", e);
            logger.critical(gson.toJson(MRIndexData, MRIndex.class));
            throw new UnexpectedException("Exception thrown while translating Modrinth Index object!");
        }
        logger.log("Translation successful.");
        return this;
    }

    // Data Fields
    public String instanceName;
    public ModpackData modpackData;
    public MinecraftData minecraftData;
    public ModLoader modLoaderData;
    public ModFile[] files;

    public static class ModpackData {
        public String version;
        public String author;
        public String overrides;
        public String name;
        public String summary;
    }
    public static class MinecraftData {
        public String version;
    }

    public static class ModLoader {
        public String modLoader;
        public String version;
    }

    public static class ModFile {
        public String fileName;
        public String downloadURL;
        public String path;
        public int fileLength;
        public Hashes hashes;
        public static class Hashes {
            public String sha1;
            public String sha256;
            public String sha512;
        }
        public ModFile() {}
        public ModFile(String fileName, String downloadURL,int fileLength) {
            this.fileName = fileName;
            this.downloadURL = downloadURL;
            this.fileLength = fileLength;
        }
        public ModFile(String fileName, String downloadURL,int fileLength, Hashes hashes) {
            this.fileName = fileName;
            this.downloadURL = downloadURL;
            this.fileLength = fileLength;
            this.hashes = hashes;
        }
        public ModFile(String fileName, String downloadURL,int fileLength, Hashes hashes, String path) {
            this.fileName = fileName;
            this.downloadURL = downloadURL;
            this.fileLength = fileLength;
            this.hashes = hashes;
            this.path = path;
        }
    }
}

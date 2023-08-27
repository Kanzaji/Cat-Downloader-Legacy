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

import com.kanzaji.catdownloaderlegacy.ArgumentDecoder;
import com.kanzaji.catdownloaderlegacy.loggers.LoggerCustom;
import com.kanzaji.catdownloaderlegacy.utils.FileUtils;
import com.kanzaji.catdownloaderlegacy.utils.FileVerUtils;
import com.kanzaji.catdownloaderlegacy.utils.NetworkingUtils;
import com.kanzaji.catdownloaderlegacy.utils.RandomUtils;
import static com.kanzaji.catdownloaderlegacy.CatDownloader.WORKPATH;
import static com.kanzaji.catdownloaderlegacy.guis.MRSecurityCheckGUI.modrinthSecurityCheckFail;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.nio.file.Path;
import java.rmi.UnexpectedException;
import java.util.Arrays;
import java.util.Objects;
import java.util.UnknownFormatConversionException;
import java.util.concurrent.Callable;

/**
 * This class holds data for CDLPack format, and additional methods for transforming other formats (CurseForge Instance / Pack, Modrinth mrpack) to this format.
 * @apiNote This class will be extended in the future with more fields in the Launcher version of the app.
 */
@SuppressWarnings("unused")
public class CDLInstance {
    private static final LoggerCustom logger = new LoggerCustom("CDLInstance Utilities");
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    public static final String formatVersion = "1.0";

    /**
     * This method is used to get Verification task for the file under specified index.
     * @param modFile Index to the modFile in the Files Array.
     * @return Callable to execute with verification routine for specified file.
     * <h3>Returns:</h3>
     * <ul>
     * <li><b>1</b> if file not found.</li>
     * <li><b>-1</b> if corrupted.</li>
     * <li><b>0</b> if file was verified successfully.</li>
     * </ul>
     */
    public Callable<Integer[]> getVerificationTask(int modFile) {
        ModFile mod = this.files[modFile];
        if (Objects.isNull(mod.path)) {
            mod.path = "mods/" + mod.fileName;
        }
        return new Callable<>() {

            /**
             * Computes a result, or throws an exception if unable to do so.
             *
             * @return computed result
             * @throws Exception if unable to compute a result
             */
            @Override
            public Integer[] call() throws Exception {
                try {
                    Path modPath = Path.of(WORKPATH.toString(), mod.path);
                    if (Files.notExists(modPath)) {
                        return new Integer[]{modFile, 1};
                    }

                    boolean corrupted;
                    if (Objects.isNull(mod.hashes) || !mod.hashes.isPopulated()) {
                        corrupted = !FileVerUtils.verifyFile(modPath, mod.fileLength, mod.downloadURL);
                    } else if (Objects.nonNull(mod.hashes.sha512)) {
                        corrupted = !FileVerUtils.verifyFile(modPath, mod.fileLength, mod.hashes.sha512, "SHA-512");
                    } else if (Objects.nonNull(mod.hashes.sha256)) {
                        corrupted = !FileVerUtils.verifyFile(modPath, mod.fileLength, mod.hashes.sha256, "SHA-256");
                    } else {
                        corrupted = !FileVerUtils.verifyFile(modPath, mod.fileLength, mod.hashes.sha1, "SHA-1");
                    }

                    if (corrupted) return new Integer[]{modFile, -1};

                    return new Integer[]{modFile, 0};
                } catch (Exception e) {
                    throw new UnexpectedException(String.valueOf(modFile), new UnexpectedException("Exception was thrown while verifying a file \"" + mod.path + "\"!", e));
                }
            }
        };
    }

    /**
     * This method is used to get Download task for the file under specified index.
     * @param modFile Index to the modFile in the Files Array.
     * @return Callable to execute with download routine for specified file.
     * <h3>Returns:</h3>
     * <ul>
     * <li><b>-1</b> if download process didn't succeed.</li>
     * <li><b>0</b> if file was downloaded successfully. </li>
     * </ul>
     */
    public Callable<Integer[]> getDownloadTask(int modFile) {
        ModFile mod = this.files[modFile];
        if (Objects.isNull(mod.path)) {
            mod.path = "mods/" + mod.fileName;
        }
        return new Callable<>() {

            /**
             * Computes a result, or throws an exception if unable to do so.
             *
             * @return computed result
             * @throws Exception if unable to compute a result
             */
            @Override
            public Integer[] call() throws Exception {
                try {
                    Path modPath = Path.of(WORKPATH.toString(), mod.path);
                    if (Files.exists(modPath)) {
                        FileUtils.delete(modPath);
                    }

                    boolean successful;
                    if (Objects.isNull(mod.hashes) || !mod.hashes.isPopulated()) {
                        successful = NetworkingUtils.downloadAndVerify(modPath, mod.downloadURL, mod.fileLength, mod.fileName);
                    } else if (Objects.nonNull(mod.hashes.sha512)) {
                        successful = NetworkingUtils.downloadAndVerify(modPath, mod.downloadURL, mod.fileLength, mod.fileName, mod.hashes.sha512, "SHA-512");
                    } else if (Objects.nonNull(mod.hashes.sha256)) {
                        successful = NetworkingUtils.downloadAndVerify(modPath, mod.downloadURL, mod.fileLength, mod.fileName, mod.hashes.sha256, "SHA-256");
                    } else {
                        successful = NetworkingUtils.downloadAndVerify(modPath, mod.downloadURL, mod.fileLength, mod.fileName, mod.hashes.sha1, "SHA-1");
                    }

                    if (successful) return new Integer[]{modFile, 0};

                    return new Integer[]{modFile, -1};
                } catch (Exception e) {
                    throw new UnexpectedException(String.valueOf(modFile), new UnexpectedException("Exception was thrown while downloading a file \"" + mod.path + "\"!", e));
                }
            }
        };
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

                if (Objects.nonNull(mod.env) && Objects.equals(mod.env.client, "unsupported")) {
                    logger.log("Found server-side only mod! Skipping " + mod.path + " in the interpretation process...");
                    continue;
                }

                if (
                    mod.path.contains("..") ||
                    mod.path.startsWith("\\") ||
                    mod.path.startsWith("/") ||
                    mod.path.matches("[A-Z]:[/\\\\].*")
                ) modrinthSecurityCheckFail(mod, MRIndexData);

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

    /**
     * This method is used to fill up data in CDLInstance Object with information from passed CFManifest object.
     * @param CFPackData CFManifest to import data from.
     * @param shouldGatherData Determines if the mods present in the CFManifestObject should be parsed at the stage of importing.
     * @return Itself, for easier use after importing.
     * @throws UnexpectedException when Exception occurs in the translating code.
     * @apiNote This method, when {@code shouldGatherData} is false, generates mod files in specific schema:<ul>
     * <li> {@code fileName} -> {@code CF-PACK_MOD}</li>
     * <li> {@code downloadURL} -> FileID of the mod</li>
     * <li> {@code fileSize} -> ProjectID of the mod</li>
     * </ul>
     * In a scenario where {@code shouldGatherData} is {@code true}, this method DOES NOT return Hashes used for verification of the downloads. Filling up missing hashes is required to do at the download process.
     */
    @ApiStatus.Experimental
    public CDLInstance importCFPack(@NotNull CFManifest CFPackData, boolean shouldGatherData) throws UnexpectedException  {
        logger.log("Translating CF Manifest into CDLInstance format...");
        logger.print("Warning! CF-Pack importing is still experimental! Use with caution.", 1);
        if (shouldGatherData) logger.print("Gathering data about mods present in the modpack... (This will take a while)");

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
            // Fabric confirmed, Quilt is not possible due to literally no mod-packs for it on cf.
            this.modLoaderData.version = modLoaderName.substring(modLoaderName.indexOf("-")+1);

            this.files = new ModFile[CFPackData.files.length];
            for (int i = 0; i < CFPackData.files.length; i++) {
                CFManifest.CFModFile mod = CFPackData.files[i];
                this.files[i] = new ModFile("CF-PACK_MOD", Integer.toString(mod.fileID), mod.projectID);
                if (shouldGatherData) this.gatherCFModInformation(i);
            }

            if (shouldGatherData) logger.print("Finished gathering data about mods, got required data for " + this.clearCFModFiles() + " out of " + RandomUtils.intGrammar(CFPackData.files.length, " mod.", " mods.", true));
            System.out.println("---------------------------------------------------------------------");
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
     * This method is used to gather information for CF-PACK_MOD returned from {@link CDLInstance#importCFPack(CFManifest, boolean)} when data gathering was not enabled.
     * @param index Index to a mod file to gather information about.
     * @apiNote If the method fails to gather information, the mod is left at the original state. Look for CF-PACK_MOD at the filename to get information if the gathering was successful.
     */
    public void gatherCFModInformation(int index) {
        //TODO: Create own getData() method. Replace use of Deprecated method.
        // Additionally make use of multithreading and experimental option.
        // - Mods without data support
        // - Mods without any data statistics
        // - Required data present for x out of y for CF Pack
        ModFile mod = this.files[index];
        if (!ArgumentDecoder.getInstance().isPackMode() || !Objects.equals(mod.fileName, "CF-PACK_MOD")) return;

        CFManifest.minecraft CFminecraft = new CFManifest.minecraft();
        CFminecraft.version = this.minecraftData.version;
        CFminecraft.modLoaders = new CFManifest.modLoaders[] {
            new CFManifest.modLoaders(this.modLoaderData.modLoader, true)
        };

        CFManifest.CFModFile CFmod = new CFManifest.CFModFile(mod.fileLength, Integer.parseInt(mod.downloadURL)).getData(CFminecraft);
        if (Objects.isNull(CFmod) || Objects.isNull(CFmod.downloadUrl)) return;
        this.files[index] = CFmod.toCDLModFile();
    }

    /**
     * This method removes all CFModFiles from the file list of this object, overriding current array with a fresh one.
     * @return Integer with amount of mods removed.
     */
    public int clearCFModFiles() {
        int originalLength = this.files.length;
        this.files = (ModFile[]) Arrays.stream(this.files).dropWhile(mod -> Objects.equals(mod.fileName, "CF-PACK_MOD")).toArray();
        return originalLength - this.files.length;
    }

    /**
     * This method is used to fill up data in CDLInstance Object with information from passed CFManifest object. It automatically tries to gather information about mods present in the manifest object.
     * @param CFPackData CFManifest to import data from.
     * @return Itself, for easier use after importing.
     * @throws UnexpectedException when Exception occurs in the translating code.
     * @apiNote This method DOES NOT return Hashes used for verification of the downloads. Filling up missing hashes is required to do at the download process.
     */
    @ApiStatus.Experimental
    public CDLInstance importCFPack(@NotNull CFManifest CFPackData) throws UnexpectedException { return this.importCFPack(CFPackData, true);}

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

            /**
             * This method is used to check if any of the hashes are populated.
             * @return true if any of the hashes are populated, otherwise false.
             */
            public boolean isPopulated() {
                return Objects.nonNull(sha1) ||
                        Objects.nonNull(sha256) ||
                        Objects.nonNull(sha512);
            }

            @Override
            public String toString() {
                return gson.toJson(this);
            }
        }

        @Override
        public String toString() {
            return gson.toJson(this);
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

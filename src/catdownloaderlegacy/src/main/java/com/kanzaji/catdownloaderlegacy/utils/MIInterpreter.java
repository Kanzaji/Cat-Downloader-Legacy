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

package com.kanzaji.catdownloaderlegacy.utils;

import com.kanzaji.catdownloaderlegacy.data.CFManifest;
import com.kanzaji.catdownloaderlegacy.data.CFMinecraftInstance;
import com.kanzaji.catdownloaderlegacy.loggers.LoggerCustom;

import org.jetbrains.annotations.NotNull;

import com.google.gson.Gson;

/**
 * This class holds methods related to decoding CurseForge MinecraftInstance file to Manifest-compatible file.
 */
public class MIInterpreter {
    private static final LoggerCustom logger = new LoggerCustom("CF MI Interpreter");

    /**
     * Used to decode {@link CFMinecraftInstance} object into {@link CFManifest} object.
     * @param CFMinecraftInstanceFile MinecraftInstance object to decode.
     * @return Manifest object with decoded information from passed MinecraftInstance.
     * @throws RuntimeException when Translation fails.
     * @deprecated This method is deprecated, as CDLinstance will be used in the future. Translation to the Manifest format from MI format is not useful anymore.
     */
    @Deprecated(since = "2.0.1", forRemoval = true)
    public static @NotNull CFManifest decode(CFMinecraftInstance CFMinecraftInstanceFile) throws RuntimeException {
        Gson gson = new Gson();
        CFManifest CFManifest = new CFManifest();
        logger.log("Translating MinecraftInstance into Manifest compatible object...");
        try {
            CFManifest.version = "";
            CFManifest.name = CFMinecraftInstanceFile.name;
            CFManifest.minecraft = gson.fromJson("{\"version\":\"" + CFMinecraftInstanceFile.baseModLoader.minecraftVersion + "\",\"modLoaders\": [{\"id\":\"" + CFMinecraftInstanceFile.baseModLoader.name + "\"}]}", CFManifest.minecraft.class);
            int index = 0;
            CFManifest.files = new CFManifest.ModFile[CFMinecraftInstanceFile.installedAddons.length];
            for (CFMinecraftInstance.installedAddons File : CFMinecraftInstanceFile.installedAddons) {
                CFManifest.ModFile mf = new CFManifest.ModFile();
                mf.projectID = File.addonID;
                mf.fileID = File.installedFile.id;
                mf.fileSize = File.installedFile.fileLength;
                mf.downloadUrl = File.installedFile.downloadUrl;
                mf.required = true;
                CFManifest.files[index] = mf;
                index += 1;
            }
        } catch (Exception e) {
            logger.logStackTrace("Interpretation of MinecraftInstance.json failed!", e);
            throw new RuntimeException("Exception thrown while translating MinecraftInstance object!");
        }
        logger.log("Translation successful");
        return CFManifest;
    }
}

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

import com.kanzaji.catdownloaderlegacy.jsons.Manifest;
import com.kanzaji.catdownloaderlegacy.jsons.MinecraftInstance;

import com.google.gson.Gson;
import com.kanzaji.catdownloaderlegacy.loggers.LoggerCustom;

public class MIInterpreter {
    private static final LoggerCustom logger = new LoggerCustom("CF MI Interpreter");
    public static Manifest decode(MinecraftInstance MinecraftInstanceFile) throws UnsupportedOperationException {
        Gson gson = new Gson();
        Manifest manifest = new Manifest();
        logger.log("Translating MinecraftInstance into Manifest compatible object...");
        try {
            manifest.version = "";
            manifest.name = MinecraftInstanceFile.name;
            manifest.minecraft = gson.fromJson("{\"version\":\"" + MinecraftInstanceFile.baseModLoader.minecraftVersion + "\",\"modLoaders\": [{\"id\":\"" + MinecraftInstanceFile.baseModLoader.name + "\"}]}", Manifest.minecraft.class);
            int index = 0;
            manifest.files = new Manifest.ModFile[MinecraftInstanceFile.installedAddons.length];
            for (MinecraftInstance.installedAddons File : MinecraftInstanceFile.installedAddons) {
                Manifest.ModFile mf = new Manifest.ModFile();
                mf.projectID = File.addonID;
                mf.fileID = File.installedFile.id;
                mf.fileSize = File.installedFile.fileLength;
                mf.downloadUrl = File.installedFile.downloadUrl;
                mf.required = true;
                manifest.files[index] = mf;
                index += 1;
            }
        } catch (Exception e) {
            logger.logStackTrace("Interpretation of MinecraftInstance.json failed!", e);
            throw new UnsupportedOperationException();
        }
        logger.log("Translation successful");
        return manifest;
    }
}

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

/**
 * Class used to represent Data Structure for MinecraftInstance.json file.
 */
@SuppressWarnings("unused")
public class CFMinecraftInstance {

    public baseModLoader baseModLoader;
    public String name;
    public CFManifest manifest;
    public installedAddons[] installedAddons;

    public static class installedAddons {
        public Number addonID;
        public AddonFile installedFile;
    }
    public static class AddonFile {
        public Number id;
        public Number fileLength;
        public String downloadUrl;
        public String fileName;
    }
    public static class baseModLoader {
        public String name;
        public String minecraftVersion;
        public String forgeVersion;
        //TODO: Verify CF Uses those names for different modLoaders.
        public String fabricVersion;
        public String quiltVersion;
        // TODO: Change this to anything that CF will use for "NeoForge" thing.
        public String neoForgeVersion;
    }
}

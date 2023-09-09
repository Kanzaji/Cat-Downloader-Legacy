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
import com.google.gson.annotations.SerializedName;

/**
 * This class holds the data schema for Modrinth.index.json file from the mrpack archives.
 */
@SuppressWarnings("unused")
public class MRIndex {
    public Number formatVersion;
    public String game;
    public String versionId;
    public String name;
    /**
     * Optional field.
     */
    public String summary;
    public MRDependencies dependencies;
    public MRModFile[] files;

    public static class MRModFile {
        private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
        @Override
        public String toString() {
            return gson.toJson(this);
        }
        public Number fileSize;
        public String path;
        public String[] downloads;
        public CDLInstance.Hashes hashes;
        public env env;
        /**
         * Optional field.
         */
        public static class env {
            public static String[] acceptedValues = {
                "required","optional","unsupported"
            };
            public String client;
            public String server;
        }
    }
    public static class MRDependencies {
        public String minecraft;
        @SerializedName("fabric-loader")
        public String fabric;
        @SerializedName("quilt-loader")
        public String quilt;
        @SerializedName("forge")
        public String forge;
        // TODO: Future proofing!
        @SerializedName("neo-forge")
        public String neoforge;
    }
}

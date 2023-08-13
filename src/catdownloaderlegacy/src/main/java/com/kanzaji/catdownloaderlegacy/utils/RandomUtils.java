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

import com.kanzaji.catdownloaderlegacy.loggers.LoggerCustom;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * This class holds random utility methods that aren't worth their own class.
 * @see RandomUtils#checkIfJsonObject(String) 
 * @see RandomUtils#closeTheApp(int)
 */
public class RandomUtils {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().setLenient().create();
    private static final LoggerCustom logger = new LoggerCustom("Random Utilities");

    /**
     * Used to check if provided String is JSON Object.
     * @param JSONObject {@link String} with JSON Object.
     * @return {@link Boolean} {@code true} if String is JSON Object, {@code false} otherwise.
     */
    public static boolean checkIfJsonObject(String JSONObject) {
        try {
            // Ignoring return value of gson#fromJson here is intended!.
            // When String isn't an JsonObject, it will throw an exception.
            gson.fromJson(JSONObject, Object.class);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Used to close the app, and print to the log file GitHub information.
     */
    public static void closeTheApp(int exitCode) {
        if (logger.isInitialized()) {
            logger.log("Cat-Downloader Legacy is created and maintained by Kanzaji! Find the source code and issue tracker here:");
            logger.log("https://github.com/Kanzaji/Cat-Downloader-Legacy");
        }
        System.exit(exitCode);
    }
}

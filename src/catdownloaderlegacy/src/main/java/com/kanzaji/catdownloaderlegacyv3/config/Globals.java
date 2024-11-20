/**************************************************************************************
 * MIT License                                                                        *
 *                                                                                    *
 * Copyright (c) 2024. Kanzaji                                                        *
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

package com.kanzaji.catdownloaderlegacyv3.config;

import com.kanzaji.catdownloaderlegacy.CatDownloader;
import com.kanzaji.catdownloaderlegacyv3.guis.GUIUtils;
import com.kanzaji.catdownloaderlegacyv3.services.Logger;
import com.kanzaji.catdownloaderlegacyv3.services.interfaces.ILogger;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Globals {
    private static final ILogger logger = Logger.get("Initial Configuration");
    // Global variables
    public static final String VERSION = "3.0.0-DEVELOP";
    @SuppressWarnings("ConstantConditions")
    public static final boolean DEVELOP = VERSION.endsWith("DEVELOP");
    @SuppressWarnings("ConstantConditions")
    public static final boolean SNAPSHOT = VERSION.endsWith("SNAPSHOT");
    public static final String REPOSITORY = "https://github.com/Kanzaji/Cat-Downloader-Legacy";
    public static final String NAME = "Cat Downloader Legacy";
    /**
     * Arguments passed to the app.
     */
    public static final List<String> ARGUMENTS = new ArrayList<>();
    /**
     * Path to the java environment running the app.
     */
    public static Path JAVAPATH = null;
    /**
     * Path to the .jar containing the app.
     */
    public static Path APPPATH = null;

    public static void setup() {
        GUIUtils.setLookAndFeel();

        try {
            APPPATH = Path.of(CatDownloader.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath().replaceFirst("/", ""));
            logger.info("App Path: " + APPPATH.toAbsolutePath());
        } catch (Exception e) {
            logger.logStackTrace("Failed to get App directory!", e);
        }

        try {
            JAVAPATH = Path.of(ProcessHandle.current().info().command().orElseThrow());
            logger.info("Java Path: " + JAVAPATH.toAbsolutePath());
        } catch (Exception e) {
            logger.logStackTrace("Failed to get Java directory!", e);
        }

        // Logo! Or more, a banner, we don't have a logo :P
        System.out.println("---------------------------------------------------------------------");
        System.out.printf ("     %s %s%n", NAME, VERSION);
        System.out.println("     Created by: Kanzaji");
        System.out.println("---------------------------------------------------------------------");
    }
}

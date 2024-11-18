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

package com.kanzaji.catdownloaderlegacyv3;

import com.google.gson.Gson;
import com.kanzaji.catdownloaderlegacyv3.services.Logger;
import com.kanzaji.catdownloaderlegacyv3.services.ServiceManager;
import com.kanzaji.catdownloaderlegacyv3.services.Services;
import com.kanzaji.catdownloaderlegacyv3.services.interfaces.ILogger;

import javax.swing.*;
import java.nio.file.*;
import java.util.*;


/**
 * Main class holding Global variables for the app and containing the main method.
 * @see CatDownloader#main(String[])
 */
public final class CatDownloader {
    // Launch fresh instances of required utilities.
    private static final Gson gson = new Gson();
    private static final ILogger logger = Logger.get("Main");

    // Global variables
    public static final String VERSION = "3.0.0-DEVELOP";
    @SuppressWarnings("ConstantConditions")
    public static final boolean DEVELOP = VERSION.endsWith("DEVELOP");
    @SuppressWarnings("ConstantConditions")
    public static final boolean SNAPSHOT = VERSION.endsWith("SNAPSHOT");
    public static final List<String> ARGUMENTS = new ArrayList<>();
    public static final String REPOSITORY = "https://github.com/Kanzaji/Cat-Downloader-Legacy";
    public static final String NAME = "Cat Downloader Legacy";

    /**
     * Path to the java environment running the app.
     */
    public static Path JAVAPATH = null;

    /**
     * Path to the .jar containing the app.
     */
    public static Path APPPATH = null;

    /**
     * Path to the working directory.
     */
    public static Path WORKPATH = null;

    /**
     * Main method of the app.
     * @param args String[] arguments for the app.
     */
    public static void main(String[] args) {
        long startingTime = System.currentTimeMillis();
        ARGUMENTS.addAll(Arrays.stream(args).toList());

        try {
            logger.info("%s version %s".formatted(NAME, VERSION));
            Services.registerServices();

            ServiceManager.runPreInit();
            ServiceManager.runInit();
            ServiceManager.runPostInit();

            // For some reason, the console object is null when running this from IntelliJ.
            // Some features, like password hiding, will not be available in DEV environment because of that.
            // @see https://youtrack.jetbrains.com/issue/IDEA-18814/IDEA-doesnt-work-with-System.console
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                logger.logStackTrace("Look And Feel not available! Going back to default.", e);
            }

            ServiceManager.runExit();
        } catch (Throwable e) {
            if (!logger.isInitialized()) Logger.getInstance().crashInit();

            try {
                logger.logStackTrace(NAME + " crashed!", e);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            System.out.println(NAME + " crashed! Exception: \"" + e.getMessage() + "\"! For more details, check the log file at: \n" + logger.getLogPath());
            ServiceManager.runCrash();
        }
    }
}
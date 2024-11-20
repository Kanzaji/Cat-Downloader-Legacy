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
import com.kanzaji.catdownloaderlegacyv3.config.Globals;
import com.kanzaji.catdownloaderlegacyv3.services.Logger;
import com.kanzaji.catdownloaderlegacyv3.services.ServiceManager;
import com.kanzaji.catdownloaderlegacyv3.services.Services;
import com.kanzaji.catdownloaderlegacyv3.services.interfaces.ILogger;

import java.util.*;


/**
 * Main class holding Global variables for the app and containing the main method.
 * @see CatDownloader#main(String[])
 */
public final class CatDownloader {
    // Launch fresh instances of required utilities.
    private static final Gson gson = new Gson();
    private static final ILogger logger = Logger.get("Main");

    /**
     * Main method of the app.
     * @param args String[] arguments for the app.
     */
    public static void main(String[] args) {
        // For some reason, the console object is null when running from IntelliJ.
        // Some features, like password hiding, will not be available in DEV environment because of that.
        // @see https://youtrack.jetbrains.com/issue/IDEA-18814/IDEA-doesnt-work-with-System.console
        // Note: This isn't that important in CDL, but worth remembering when trying to work with ANSI.
        long startingTime = System.currentTimeMillis();
        Globals.ARGUMENTS.addAll(Arrays.stream(args).toList());

        try {
            logger.info("%s version %s".formatted(Globals.NAME, Globals.VERSION));
            Services.registerServices();

            ServiceManager.runPreInit();
            ServiceManager.runInit();
            ServiceManager.runPostInit();

            ServiceManager.runExit();
        } catch (Throwable e) {
            if (!logger.isInitialized()) Logger.getInstance().crashInit();

            try {
                logger.logStackTrace(Globals.NAME + " crashed!", e);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            System.out.println(Globals.NAME + " crashed! Exception: \"" + e.getMessage() + "\"! For more details, check the log file at: \n" + logger.getLogPath());
            ServiceManager.runCrash();
        }
    }
}
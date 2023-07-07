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

package com.kanzaji.cdlupdater;

import com.kanzaji.cdlupdater.loggers.LoggerCustom;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Objects;

public class CDLUpdater {
    private static final LoggerCustom logger = new LoggerCustom("CDLUpdater Sub App");
    private static Path oldApp = null;
    private static Path newApp = null;
    private static Path log = null;
    private static String java = null;
    public static void main(String[] arguments) {
        for (String fullArgument : arguments) {
            String[] splitArgument = fullArgument.split(":", 2);
            String argument = splitArgument[0].startsWith("-") ?
                    splitArgument[0].toLowerCase(Locale.ROOT).replaceFirst("-", "") :
                    splitArgument[0].toLowerCase(Locale.ROOT);
            String value = splitArgument[1];

            if (value.endsWith("/")) value = value.substring(0, value.lastIndexOf("/"));

            Path pathOfValue = null;
            try {pathOfValue = Path.of(value);} catch (Exception ignored){}

            switch (argument) {
                case "logpath" -> log = pathOfValue;
                case "oldapp" -> oldApp = pathOfValue;
                case "newapp" -> newApp = pathOfValue;
                case "java" -> java = value;
                default -> {
                    return;
                }
            }
        }

        if (Objects.nonNull(log)) {
            logger.init(log);
        } else {
            logger.init();
        }

        try {
            Thread.sleep(250);
            if (Objects.isNull(oldApp) || Objects.isNull(newApp) || Objects.isNull(java)) throw new NullPointerException("At least one of required arguments is null!");

            logger.log("Old App Path: " + oldApp.toAbsolutePath());
            logger.log("New App Path: " + newApp.toAbsolutePath());

            if (newApp.getFileName().toString().endsWith(".new")) {
                logger.log("Replacing current app with new version...");
                Files.move(newApp, oldApp, StandardCopyOption.REPLACE_EXISTING);
                newApp = oldApp;
            } else {
                logger.log("Deleting current app, as new version has been downloaded...");
                Files.deleteIfExists(oldApp);
            }

            logger.log("App has been updated successfully! Looking for the backup...");
            if (Files.deleteIfExists(Path.of(oldApp.toAbsolutePath() + ".old"))) {
                logger.log("Old backup has been deleted!");
            } else {
                logger.log("Backup not found.");
            }

            logger.log("Finished updating routine.");
            logger.log("Handing execution to main app for post-update cleanup. This log will be archived!");

            Runtime.getRuntime().exec(
                "\"" + java + "\" -jar \"" + newApp.toAbsolutePath() + "\" -PostUpdateRoutine "
            );

            System.exit(0);
        } catch (Error | Exception e) {
            logger.logStackTrace("Exception thrown while executing main app code!", e);
            System.exit(1);
        }
    }
}

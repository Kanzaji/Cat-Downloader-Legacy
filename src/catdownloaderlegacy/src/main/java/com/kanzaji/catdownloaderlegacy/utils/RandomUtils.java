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

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
            // Ignoring the return value of gson#fromJson here is intended!.
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

    /**
     * This method is used to choose between two Strings, depending on if the int value is 1. Appends the value to the start of the String if requested.
     * @param Value Value to check if is 1.
     * @param String1 String to return when value is 1.
     * @param String2 String to return when value is not 1.
     * @param AppendValue if true, Value is appended at the beginning of the Strings.
     * @return {@code String1} when Value is 1, Otherwise {@code String2};
     */
    public static String intGrammar(int Value, String String1, String String2, boolean AppendValue) {
        if (AppendValue) {
            String1 = Value + String1;
            String2 = Value + String2;
        }
        return (Objects.equals(Value, 1))? String1: String2;
    }

    /**
     * This method is used to shut down and wait a specified amount of time for passed ExecutorService.
     * @param executor ExecutorService to shut down.
     * @param time Amount of time to wait for the Executor.
     * @param timeUnit TimeUnit to use with specified time.
     * @param msg Message to log at the Critical level.
     * @throws InterruptedException if executor is interrupted while waiting.
     * @throws TimeoutException when specified Timeout passes.
     */
    public static void waitForExecutor(@NotNull ExecutorService executor, int time, TimeUnit timeUnit, String msg)
    throws InterruptedException, TimeoutException {
        Objects.requireNonNull(executor);
        executor.shutdown();
        if (!executor.awaitTermination(time, timeUnit)) {
            logger.critical(msg);
            throw new TimeoutException(msg);
        }
    }

    /**
     * This method is used to remove common part from the Strings passed as arguments.
     * @param StringToEdit String to remove common part from.
     * @param StringToCompareAgainst String to compare against StringToEdit.
     * @return Substring of StringToEdit with removed common part between the two.
     * @apiNote This method returns common part from the beginning.
     */
    public static @NotNull String removeCommonPart(@NotNull String StringToEdit, @NotNull String StringToCompareAgainst) {
        if (StringToEdit.equals(StringToCompareAgainst)) return "";
        if (StringToEdit.startsWith(StringToCompareAgainst)) return StringToEdit.substring(StringToCompareAgainst.length());

        char[] STE = StringToEdit.toCharArray();
        char[] STC = StringToCompareAgainst.toCharArray();
        int i;
        for (i = 0; i < STE.length; i++) {
            if (!Objects.equals(STE[i], STC[i])) break;
        }
        return StringToEdit.substring(i);
    }

    public static void runGC() {
        runGC(true);
    }

    public static void runGCL() {
        runGC(false);
    }

    public static void runGC(boolean skipLog) {
        if (skipLog) {
            System.gc();
            return;
        }

        Runtime run = Runtime.getRuntime();
        long memory = run.totalMemory() - run.freeMemory();
        System.gc();
        logger.log(("GC Manual Trigger! " + (float)(memory - run.totalMemory() + run.freeMemory())/(1024L*1024L)) + " MegaBytes were cleared up.");
    }
}

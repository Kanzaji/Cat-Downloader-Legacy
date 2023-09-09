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

import java.util.Iterator;
import java.util.LinkedList;

/**
 * Class used to represent JSON Structure of configuration file.
 * @see com.kanzaji.catdownloaderlegacy.utils.SettingsManager
 */
public class Settings {
    /**
     * This static contains all Setting Keys from the Settings File.
     */
    public static final String[] SettingsKeys = {
            "mode",
            "workingDirectory",
            "logDirectory",
            "threadCount",
            "downloadAttempts",
            "logStockpileSize",
            "isLoggerActive",
            "shouldStockpileLogs",
            "shouldCompressLogFiles",
            "isUpdaterActive",
            "isFileSizeVerificationActive",
            "isHashVerificationActive",
            "modBlackList",
            "dataCache",
            "dataCacheDirectory"
    };

    public String mode;
    public String workingDirectory;
    public String logDirectory;
    public String dataCacheDirectory;
    public int threadCount;
    public int downloadAttempts;
    public int logStockpileSize;
    public boolean dataCache;
    public boolean isLoggerActive;
    public boolean shouldStockpileLogs;
    public boolean shouldCompressLogFiles;
    public boolean isUpdaterActive;
    public boolean isFileSizeVerificationActive;
    public boolean isHashVerificationActive;
    public BlackList<String> modBlackList;
    public boolean experimental;

    public static class BlackList<E> extends LinkedList<E> {
        @Override
        public String toString() {
            Iterator<E> it = iterator();
            if (! it.hasNext())
                return "[]";

            StringBuilder sb = new StringBuilder();
            sb.append('[');
            for (;;) {
                E e = it.next();
                sb.append(e == this ? "(this Collection)" : "\"" + e + "\"");
                if (! it.hasNext())
                    return sb.append(']').toString();
                sb.append(',').append(' ');
            }
        }
    }
}

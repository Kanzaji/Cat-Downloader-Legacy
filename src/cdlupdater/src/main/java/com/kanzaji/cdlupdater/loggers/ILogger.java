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

package com.kanzaji.cdlupdater.loggers;

import java.io.IOException;

/**
 * @apiNote Imported from com.kanzaji.catdownloaderlegacy
 */
interface ILogger {
    /**
     * Used to initialize Logger. Creates new log file and overrides old one if present.
     * It Has to be implemented manually.
     */
    void init();

    /**
     * Used to get a path to a log file.
     * Has to be implemented manually.
     * @return {@link String} with absolute path of a log file.
     */
    String getLogPath();

    /**
     * Used to disable Logger and remove log file.
     * Has to be implemented manually.
     */
    void exit();

    /**
     * Logs a message to a log file.
     * @param msg {@link String} message to log.
     */
    default void log(String msg) {
        this.logType(msg, 0);
    }

    /**
     * Logs a message with level WARN to a log file.
     * @param msg {@link String} message to log as WARN.
     */
    default void warn(String msg) {
        this.logType(msg, 1);
    }

    /**
     * Logs a message with level ERROR to a log file.
     * @param msg {@link String} message to log as ERROR.
     */
    default void error(String msg) {
        this.logType(msg, 2);
    }

    /**
     * Logs a message with level CRITICAL to a log file.
     * @param msg {@link String} message to log as CRITICAL.
     */
    default void critical(String msg) { this.logType(msg, 3);}

    /**
     * Logs a message with level INFO to a log file, additionally printing the message to the console.
     * @param msg {@link String} message to log with INFO level.
     */
    default void print(String msg) {
        this.logType(msg, 0);
        System.out.println(msg);
    }

    /**
     * Logs a message with specified level to a log file, additionally printing the message to the console.<br>
     * Available levels:
     * <ul>
     *     <li>0 | LOG</li>
     *     <li>1 | WARN</li>
     *     <li>2 | ERROR</li>
     *     <li>3 | CRITICAL</li>
     * </ul>
     * @param msg {@link String} message to log with specified level.
     * @param type {@link Integer} between 0 and 3 specifying selected level. Out of range defaults to 0.
     */
    default void print(String msg, int type) {
        this.logType(msg, type);
        System.out.println(msg);
    }

    /**
     * Logs a message with specified level to a log file.<br>
     * Available levels:
     * <ul>
     *     <li>0 | LOG</li>
     *     <li>1 | WARN</li>
     *     <li>2 | ERROR</li>
     *     <li>3 | CRITICAL</li>
     * </ul>
     * @param msg {@link String} message to log with specified level.
     * @param type {@link Integer} between 0 and 3 specifying selected level. Out of range defaults to 0.
     */
    default void logType(String msg, int type) {
        this.logCustom(msg, type, null);
    }

    /**
     * Logs a message with ERROR level and Stacktrace of Exception into a log.
     * @param msg String message attached to an Exception.
     * @param throwable Exception to log.
     */
    default void logStackTrace(String msg, Throwable throwable) {
        this.logCustom(msg, 3, throwable);
    }

    /**
     * Custom Log method that allows to set level of log, message and attach throwable. Has to be implemented manually.<br>
     * Available levels:
     * <ul>
     *     <li>0 | LOG</li>
     *     <li>1 | WARN</li>
     *     <li>2 | ERROR</li>
     *     <li>3 | CRITICAL</li>
     * </ul>
     * @param msg {@link String} message to log to a log file.
     * @param type Nullable {@link Integer} between 0 and 3 specifying selected level. Defaults to 0.
     * @param throwable Nullable {@link Throwable} to log.
     */
    void logCustom(String msg, int type, Throwable throwable);
}

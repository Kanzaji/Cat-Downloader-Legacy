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

import java.nio.file.Path;
import java.util.Objects;

/**
 * This class is used to create custom logger services, by wrapping around the single instance of the main {@link Logger} service.
 * @apiNote Imported from com.kanzaji.catdownloaderlegacy
 */
public class LoggerCustom implements ILogger {
    private final Logger logger = Logger.getInstance();
    private String name = "default";
    public LoggerCustom(String name) {
        if (Objects.nonNull(name)) this.name = name;
    }

    /**
     * Used to initialize Logger. Uses old log file if present.
     */
    public void init(Path logPath) {
        logger.init(logPath);
    }

    /**
     * Used to initialize Logger. Uses old log file if present.
     */
    @Override
    public void init() {
        logger.init(null);
    }

    /**
     * Used to get a path to a log file.
     * @return String with the absolute path of a log file.
     */
    @Override
    public String getLogPath() {
        return logger.getLogPath();
    }

    /**
     * Used to disable Logger and remove the log file.
     */
    @Override
    public void exit() {
        logger.exit();
    }

    /**
     * Custom Log method that allows to set level of log, message and attach throwable.
     * Available levels:
     * <ul>
     *     <li>0 | LOG</li>
     *     <li>1 | WARN</li>
     *     <li>2 | ERROR</li>
     *     <li>3 | CRITICAL</li>
     * </ul>
     * @param msg String message to log to a log file.
     * @param type Int between 0 and 2 specifying selected level. Defaults to 0. (Nullable)
     * @param throwable Exception to log. (Nullable)
     */
    @Override
    public void logCustom(String msg, int type, Throwable throwable) {
        logger.logCustom("[" + name + "] " + msg, type, throwable);
    }
}

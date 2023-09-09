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

import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

/**
 * @apiNote Imported from com.kanzaji.catdownloaderlegacy. Modified for use in this sub-app.
 */
public class Logger implements ILogger {

    private static final class InstanceHolder {private static final Logger instance = new Logger();}
    private Logger() {}
    private boolean crashed = false;
    private Path LogFile = Path.of("Cat-Downloader.log");

    /**
     * Used to get an instance of the Logger.
     * @return Reference to an instance of the Logger.
     */
    public static Logger getInstance() {
        return InstanceHolder.instance;
    }

    /**
     * Used to get a path to a log file.
     * @return String with the absolute path of a log file.
     */
    public String getLogPath() {
        if (this.LogFile == null) {
            return null;
        }
        return this.LogFile.toAbsolutePath().toString();
    }

    /**
     * Used to initialize Logger. Uses old log file if present.
     */
    public void init(Path logPath) {
        try {
            if (Objects.nonNull(logPath)) {
                if (!Files.isDirectory(logPath)) logPath = logPath.getParent();
                this.LogFile = Path.of(logPath.toAbsolutePath().toString(), this.LogFile.getFileName().toString());
            }
            if (!Files.exists(this.LogFile)) {
                Files.createFile(this.LogFile);
                this.log("\"" + this.LogFile.toAbsolutePath() + "\" file created.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.log(
            """
            
            ------------------------------------------------------------------------------------
                   Initialization of Logger service for sub-app CDLUpdater completed.
            ------------------------------------------------------------------------------------
            """
        );
    }

    /**
     * Used to initialize Logger. Uses old log file if present.
     */
    @Override
    public void init() {
        this.init(null);
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
    public void logCustom(String msg, int type, Throwable throwable) {
        String Type = switch (type) {
            case 1 -> "WARN";
            case 2 -> "ERROR";
            case 3 -> "CRITICAL";
            default -> "INFO";
        };

        try {
            Files.writeString(this.LogFile, "[" + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS").format(new Date()) + "] [" + Type + "] " + msg + "\n", StandardOpenOption.APPEND);
            if (throwable != null) {
                StackTraceElement[] stackTraceList = throwable.getStackTrace();
                StringBuilder stackTrace = new StringBuilder();
                for (StackTraceElement stackTraceElement : stackTraceList) {
                    stackTrace.append("    at ").append(stackTraceElement).append("\n");
                }
                Files.writeString(this.LogFile, "[" + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS").format(new Date()) + "] [" + Type + "] " + throwable + "\n" + stackTrace + "\n", StandardOpenOption.APPEND);
            }
        } catch (NoSuchFileException e) {
            if (this.crashed) {
                e.printStackTrace();
                System.exit(1);
            }
            crashed = true;
            this.init();
            this.error("Log file seems to had been deleted! Created another copy, but the rest of the log file has been lost.");
            this.error("Catching last message...");
            this.log(msg);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Used to disable Logger and remove log file.
     * Has to be implemented manually.
     *
     * @apiNote This method is not implemented in this implementation of the {@link ILogger}. Calling it will do nothing.
     */
    @Override
    public void exit() {
    }

}

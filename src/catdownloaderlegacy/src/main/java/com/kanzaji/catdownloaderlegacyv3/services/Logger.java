/**************************************************************************************
 * MIT License                                                                        *
 *                                                                                    *
 * Copyright (c) 2023-2024. Kanzaji                                                   *
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

package com.kanzaji.catdownloaderlegacyv3.services;

import com.kanzaji.catdownloaderlegacyv3.utils.DateUtils;
import com.kanzaji.catdownloaderlegacyv3.utils.FileUtils;
import com.kanzaji.catdownloaderlegacyv3.CatDownloader;
import com.kanzaji.catdownloaderlegacyv3.config.Configuration;
import com.kanzaji.catdownloaderlegacyv3.services.interfaces.ILogger;
import com.kanzaji.catdownloaderlegacyv3.services.interfaces.IService;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Stream;

/**
 * This class is the main instance of the Logger Service. It handles creation, stockpiling and logging to log files.
 * @apiNote This class is currently under a rework, this might cause issues and unexpected behavior until rework is complete.
 */
public class Logger implements ILogger, IService {
    private static final class InstanceHolder {private static final Logger instance = new Logger();}
    private Logger() {}
    private boolean crashed = false;
    private boolean initialized = false;
    private Path logFile = Path.of(CatDownloader.NAME + ".log");
    private final List<PreInitMessage> preInitMessages = new LinkedList<>();

    /**
     * Used to get an instance of the Logger.
     * @return Reference to an instance of the Logger.
     * @apiNote This is meant only for situations before ServiceManager is available,
     * please use {@link ServiceManager#get(String)} with {@link Services#LOGGER} instead.
     */
    public static Logger getInstance() {
        return InstanceHolder.instance;
    }

    /**
     * Used to get new ILogger instance wrapping this logger with custom name.
     * @param name name to use in the logs.
     * @return new ILogger implementation with name before all logged messages.
     */
    @Contract(value = "_ -> new", pure = true)
    public static @NotNull ILogger get(String name) {
        return new ILogger() {
            private final Logger logger = Logger.getInstance();
            @Override
            public String getLogPath() {
                return logger.getLogPath();
            }

            @Override
            public boolean isInitialized() {
                return logger.isInitialized();
            }

            @Override
            public void logCustom(String msg, int type, @Nullable Throwable throwable) {
                logger.logCustom("[" + name + "] " + msg, type, throwable);
            }
        };
    }

    /**
     * Used to get lists of implemented phases.
     * Only phases mentioned in a returned list are executed, even if implemented.
     * @return List with implemented initialization phases.
     */
    @Override
    public List<ServiceManager.State> getPhases() {
        return List.of(ServiceManager.State.PRE_INIT, ServiceManager.State.POST_INIT);
    }

    /**
     * Used to get the name of the Service for Service Manager.
     * @return String with Service name.
     */
    @Override
    public String getName() {
        return "Logger Service";
    }

    /**
     * Used to get a path to a log file.
     * @return String with the absolute path of a log file.
     */
    public String getLogPath() {
        if (this.logFile == null) return null;
        return this.logFile.toAbsolutePath().toString();
    }

    /**
     * Used as a last resort to try to initialize the logger. If it fails, it prints everything to the console.
     * @see Logger#preInit() for any other scenario for logger initialization.
     */
    public void crashInit() {
        try {
            this.preInit();
            this.warn("Logger was initialized with use of CRASH initialization!");
        } catch (Throwable ex) {
            this.initialized = true;
            if (!Files.exists(this.logFile)) {
                try {
                    Files.createFile(this.logFile);
                    this.warn("Successfully created new log file.");
                } catch (Exception e) {
                    this.error("Failed creating log file! Printing entire pre-init messages to the console.");
                    crashed = true;
                }
            } else {
                this.warn("USING OLD LOG FILE DUE TO APPLICATION CRASH.");
            }
            logPreInitMessages();
        }
    }

    private void logPreInitMessages() {
        if (!preInitMessages.isEmpty()) preInitMessages.forEach(msg -> this.logCustom(msg.msg, msg.type, msg.ex));
    }

    /**
     * Used to initialize Logger. Creates new log file and overrides old one if present.
     */
    @Override
    public void preInit() throws Throwable {
        try {
            if (Files.exists(this.logFile)) {
                Files.move(this.logFile, Path.of(this.logFile.getFileName().toString().replace(".log", ".archived.log")), StandardCopyOption.REPLACE_EXISTING);
                this.info("Old Log file found! \"" + this.logFile.toAbsolutePath() + "\" file has been archived for now.");
            }
            Files.createFile(this.logFile);
            this.info("\"" + this.logFile.toAbsolutePath() + "\" file created.");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        this.info("Logger Initialization completed.");
        this.initialized = true;

        logPreInitMessages();
    }

    /**
     * Used to finish initialization of the Logger.
     * Handles the Stockpiling function of the logs, and moving the log file to a new location.
     * @throws IllegalStateException when reading attributes of the compressed log files is not possible.
     * @throws IOException when IO Exception occurs.
     */
    @Override
    public void postInit() throws Throwable {
        // If logger is disabled, we skip the entire Post Init.
        if (!Configuration.LoggerActive.get()) {
            this.exit();
            return;
        }

        var newDir = Configuration.LogsDir.get();
        if (Files.notExists(newDir)) {
            this.info("Creating Logs directory at: " + newDir.toAbsolutePath());
            Files.createDirectories(newDir);
        }
        archiveLogs();
        checkLogLimit();
        moveToLogsFolder();
    }

    /**
     * Used to get boolean with the state of initialization of the Logger.
     *
     * @return {@link Boolean} true if logger has been initialized successfully, false otherwise.
     * @apiNote Has to be implemented manually.
     */
    @Override
    public boolean isInitialized() {
        return this.initialized;
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
     * @param type Int between 0 and 3 specifying selected level. Defaults to 0.
     * @param throwable Exception to log. (Nullable)
     */
    public void logCustom(String msg, int type, @Nullable Throwable throwable) {
        if (Objects.nonNull(msg) && !msg.startsWith("[")) msg = "[Logger] " + msg;
        if (!isInitialized()) {
            this.preInitMessages.add(new PreInitMessage(msg, type, throwable));
            return;
        }

        String Type = switch (type) {
            case 1 -> "WARN";
            case 2 -> "ERROR";
            case 3 -> "CRITICAL";
            default -> "INFO";
        };

        if (crashed) {
            System.out.println("[" + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS").format(new Date()) + "] [" + Type + "] " + msg);
            if (throwable != null) throwable.printStackTrace();
            return;
        }

        try {
            Files.writeString(this.logFile, "[" + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS").format(new Date()) + "] [" + Type + "] " + msg + "\n", StandardOpenOption.APPEND);
            if (throwable != null) {
                StackTraceElement[] stackTraceList = throwable.getStackTrace();
                StringBuilder stackTrace = new StringBuilder();

                for (StackTraceElement stackTraceElement : stackTraceList) stackTrace.append("\tat ").append(stackTraceElement).append("\n");

                Files.writeString(
                    this.logFile,
                    "[" + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS").format(new Date()) + "] [" + Type + "] " +
                    throwable + "\n" + stackTrace, StandardOpenOption.APPEND
                );

                if (Objects.nonNull(throwable.getCause())) this.logStackTrace("Caused By:", throwable.getCause());
                for (Throwable throwable1 : throwable.getSuppressed()) this.logStackTrace("Contains suppressed exception:", throwable1);
            }
        } catch (NoSuchFileException e) {
            try {
                this.preInit();
            } catch (Throwable ex) {
                this.crashInit();
                if (crashed) {
                    this.error("Failed generating new log file! Application will now exit.");
                    throw new RuntimeException("Failed generating log file!");
                }
            }
            this.error("Log file seems to had been deleted! Created another copy, but the rest of the log file has been lost.");
            this.error("Catching last message...");
            this.info(msg);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * Used to disable Logger and remove the log file.
     */
    public void exit() {
        // Force logger into the "crashed" state, so it will print messages to the standard output instead.
        this.crashed = true;

        // Log all previous messages to the standard output.
        try {
            Files.readAllLines(this.logFile).forEach(System.out::println);
        } catch (IOException e) {
            this.logStackTrace("Failed reading the current log file!", e);
        }

        // Delete the current log file
        try {
            Files.deleteIfExists(this.logFile);
        } catch (IOException e) {
            this.logStackTrace("Failed reading the current log file!", e);
        }

        this.logFile = null;
        // Warn the user about disabled logger.
        this.error("Logger was disabled! Entire logger output was redirected to the standard output. Please re-enable the logger for submitting bug reports or feature requests.");
    }

    /**
     * Used to hold log entries before initialization of the logger and creation of the log file.
     * @param msg Message content
     * @param type Message Type
     * @param ex Throwable, if any.
     */
    private record PreInitMessage(String msg, int type, Throwable ex) {}

    private void archiveLogs() throws IOException {
        var newDir = Configuration.LogsDir.get();
        var archived = Path.of(this.logFile.getFileName().toString().replace(".log", ".archived.log"));
        var archivedInDir = newDir.resolve(this.logFile.getFileName());
        if (Files.notExists(archivedInDir) && Files.notExists(archived)) return;

        if (!Configuration.LogStockSize.shouldStockpile()) {
            this.info("Stockpiling disabled. Deleting archived log.");
            FileUtils.delete(archivedInDir);
            FileUtils.delete(archived);
            return;
        }

        if (Configuration.CompressStockpiledLogs.get()) {
            FileUtils.move(archived, newDir, true);
            FileUtils.compressToGz(archivedInDir, DateUtils.getCurrentFullDate() + ".log", true);
            FileUtils.compressToGz(newDir.resolve(archived.getFileName()), DateUtils.getCurrentFullDate() + ".log", true);
        } else {
            FileUtils.move(archived, newDir, true);
            FileUtils.rename(archivedInDir, DateUtils.getCurrentFullDate() + ".log");
            FileUtils.rename(newDir.resolve(archived.getFileName()), DateUtils.getCurrentFullDate() + ".log");
        }
    }

    private void checkLogLimit() throws IOException {
        var newDir = Configuration.LogsDir.get();
        var logStock = Configuration.LogStockSize;
        if (!logStock.shouldStockpile()) return;

        if (logStock.isInfinite()) {
            this.info("Stockpiling of the logs is enabled without the limit!");
            return;
        }

        this.info("Stockpiling of the logs is enabled, with set limit of %d.".formatted(logStock.get()));
        List<Path> archivedLogs = new LinkedList<>();
        try(Stream<Path> directoryList = Files.list(newDir)) {
            directoryList.forEach((File) -> {
                String fileName = File.getFileName().toString();
                if(fileName.contains(".log") && !fileName.equals(this.logFile.getFileName().toString())) {
                    archivedLogs.add(File);
                }
            });
        }

        if (archivedLogs.size() > logStock.get()) {
            this.info("Limit of log stockpile has been reached (Currently found %d log files). Deleting oldest files...".formatted(archivedLogs.size()));

            archivedLogs.sort((e1, e2) -> {
                try { return Files
                    .readAttributes(e1, BasicFileAttributes.class)
                    .creationTime()
                    .compareTo(Files
                        .readAttributes(e2, BasicFileAttributes.class)
                        .creationTime()
                    );
                } catch (Exception e) {
                    this.logStackTrace("Unable to read attributes of file: " + e1.toAbsolutePath(), e);
                    return 0;
                }
            });

            while (archivedLogs.size() > logStock.get()) {
                try {
                    if (Files.deleteIfExists(archivedLogs.get(0))) {
                        this.info(archivedLogs.get(0).toAbsolutePath() + " has been deleted!");
                    }
                } catch (Exception e) {
                    this.logStackTrace("Failed to delete the log file " + archivedLogs.get(0).toAbsolutePath(), e);
                }
                archivedLogs.remove(archivedLogs.get(0));
            }
        }
    }

    private void moveToLogsFolder() throws IOException {
        var newDir = Configuration.LogsDir.get();
        var newLog = newDir.resolve(this.logFile.getFileName());
        if (FileUtils.getParentFolder(this.logFile) == FileUtils.getParentFolder(newLog)) {
            this.info("Logs directory set to the root. No need to transfer the log file.");
            return;
        }

        this.info("Moving current log file to new location...");
        if (Files.exists(newLog))
            this.warn("Previous non-archived log found! The log will be overridden.");
        Files.move(this.logFile, newLog, StandardCopyOption.REPLACE_EXISTING);
        this.logFile = newLog;
    }
}

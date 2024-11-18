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

import com.kanzaji.catdownloaderlegacyv3.config.enums.Mode;
import com.kanzaji.catdownloaderlegacyv3.services.configuration.*;
import com.kanzaji.catdownloaderlegacyv3.services.configuration.types.EnumType;
import com.kanzaji.catdownloaderlegacyv3.services.configuration.types.IntegerType;
import com.kanzaji.catdownloaderlegacyv3.services.configuration.types.BooleanType;
import com.kanzaji.catdownloaderlegacyv3.services.configuration.types.PathType;
import com.kanzaji.catdownloaderlegacyv3.utils.interfaces.ThrowingFunction;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Configuration class is used to hold the references to Configuration Keys, registered in {@link Configuration#register(ConfigurationService)} method.
 */
public class Configuration {
    // Enums
    public static ConfigurationKey<Mode> AppMode;

    // Booleans
    public static ConfigurationKey<Boolean> FileSizeVerification;

    public static ConfigurationKey<Boolean> HashVerification;

    public static ConfigurationKey<Boolean> UpdaterActive;

    public static ConfigurationKey<Boolean> CacheActive;

    public static ConfigurationKey<Boolean> LoggerActive;

    public static ConfigurationKey<Boolean> CompressStockpiledLogs;

    public static ConfigurationKey<Boolean> BypassNetworkCheck;

    @Argument
    public static ConfigurationKey<Boolean> Settings;

    @Argument
    public static ConfigurationKey<Boolean> DefaultSettingsFromTemplate;

    @Argument
    public static ConfigurationKey<Boolean> Experimental;

    // Integers
    public static ConfigurationKey<Integer> ThreadCount;

    public static ConfigurationKey<Integer> DownloadRetries;

    public static LogStockSizeKey LogStockSize;

    // Paths
    public static ConfigurationKey<Path> WorkingDir;

    @Argument
    public static ConfigurationKey<Path> SettingsDir;

    public static ConfigurationKey<Path> LogsDir;

    public static ConfigurationKey<Path> CacheDir;

    public static void register(ConfigurationService service) {
        AppMode = registerKey(service,
            "App-Mode",
            () -> Mode.AUTOMATIC,
            EnumType.getParser(Mode::valueOf, Arrays.stream(Mode.values()).map(Enum::name).toList()),
            "Mode",
            "Sets mode of the application. Can be: CF-Pack // CF-Instance // Modrinth // Automatic"
        );

        WorkingDir = registerKey(service,
            "Working-Directory",
            PathType.defaultPath("."),
            PathType.PARSER,
            PathType.MUST_EXIST_VALIDATOR,
            PathType.SERIALIZER,
            "WorkingDirectory",
            """
                Determines the working directory of the app.
                Working directory is the Minecraft instance directory with the manifest file.
                If set to a relative path, it's resolved from the execution directory."""
        );

        SettingsDir = registerArg(service,
        "Settings-Dir",
            PathType.defaultPath("."),
            PathType.PARSER,
            "SettingsDir",
            "Determines the directory where Configuration Files will be stored."
        );

        LogsDir = registerKey(service,
        "Logs-Dir",
            PathType.defaultPath("logs/cdl"),
            PathType.PARSER,
            null,
            PathType.SERIALIZER,
            "LogsDir",
            "Determines the directory where Log Files will be stored."
        );

        CacheDir = registerKey(service,
        "Cache-Dir",
            PathType.defaultPath("logs/cdl"),
            PathType.PARSER,
            null,
            PathType.SERIALIZER,
            "CacheDir",
            "Determines the directory where Cache file will be stored."
        );

        ThreadCount = registerKey(service,
            "Thread-Count",
            IntegerType.defaultSupplier(4),
            IntegerType.PARSER,
            IntegerType.inRangeValidator(1, 128),
            "ThreadCount",
            "Determines the amount of concurrent download and verification threads launched by the app."
        );

        DownloadRetries = registerKey(service,
            "Download-Retries",
            IntegerType.defaultSupplier(5),
            IntegerType.PARSER,
            IntegerType.inRangeValidator(1, 256),
            "DownloadRetries",
            "Determines the number of download attempts when after-download validation fails."
        );

        LogStockSize = new LogStockSizeKey();
        service.registerKey(LogStockSize);

        FileSizeVerification = registerKey(service,
            "File-Size-Verification",
            BooleanType.DEFAULT_TRUE,
            BooleanType.PARSER,
            "FileSizeVerification",
            "Determines if File Size Verification should be enabled."
        );
        HashVerification = registerKey(service,
            "Hash-Verification",
            BooleanType.DEFAULT_TRUE,
            BooleanType.PARSER,
            "HashVerification",
            "Determines if Hash Verification should be enabled."
        );
        UpdaterActive = registerKey(service,
            "Updater-Active",
            BooleanType.DEFAULT_TRUE,
            BooleanType.PARSER,
            "UpdaterActive",
            "Determines if the Update Checker of the application should be enabled."
        );
        CacheActive = registerKey(service,
            "Cache-Active",
            BooleanType.DEFAULT_TRUE,
            BooleanType.PARSER,
            "CacheActive",
            "Determines if the app should generate a cached version of the mod data. In very rare cases this might cause corruption detection to fail, when cache itself is corrupted."
        );
        LoggerActive = registerKey(service,
            "Logger-Active",
            BooleanType.DEFAULT_TRUE,
            BooleanType.PARSER,
            "LoggerActive",
            "Determines if the application should generate logs."
        );
        CompressStockpiledLogs = registerKey(service,
            "Compress-Stockpiled-Logs",
            BooleanType.DEFAULT_TRUE,
            BooleanType.PARSER,
            "CompressStockpiledLogs",
            "Determines if the stored logs should be compressed."
        );
        BypassNetworkCheck = registerKey(service,
            "Bypass-Network-Check",
            BooleanType.DEFAULT_FALSE,
            BooleanType.PARSER,
            "BypassNetworkCheck",
            "Determines if the Network check should be skipped."
        );
        // Args
        Settings = registerArg(service,
            "Settings",
            BooleanType.DEFAULT_TRUE,
            BooleanType.PARSER,
            "Settings",
            "Determines if Settings file should be used."
        );
        DefaultSettingsFromTemplate = registerArg(service,
            "Default-Settings-From-Template",
            BooleanType.DEFAULT_FALSE,
            BooleanType.PARSER,
            "DefaultSettingsFromTemplate",
            "Determines if default settings should be generated from the arguments."
        );
        Experimental = registerArg(service,
            "Experimental",
            BooleanType.DEFAULT_FALSE,
            BooleanType.PARSER,
            "Experimental",
            "Enables experimental features."
        );
    }

    private static <T> @NotNull ConfigurationKey<T> registerKey(
        @NotNull ConfigurationService service,
        String name,
        Supplier<Object> dValue,
        ThrowingFunction<Object, Object> parser,
        Function<Object, ValidationResult> validator,
        Function<Object, String> serializer,
        String argument,
        String desc
    ) {
        var key = new ConfigurationKey<T>(name, dValue, new Codec(parser, validator, serializer), argument, desc);
        service.registerKey(key);
        return key;
    }

    private static <T> @NotNull ConfigurationKey<T> registerKey(
        @NotNull ConfigurationService service,
        String name,
        Supplier<Object> dValue,
        ThrowingFunction<Object, Object> parser,
        Function<Object, ValidationResult> validator,
        String argument,
        String desc
    ) {
        var key = new ConfigurationKey<T>(name, dValue, new Codec(parser, validator, null), argument, desc);
        service.registerKey(key);
        return key;
    }

    private static <T> @NotNull ConfigurationKey<T> registerKey(
        @NotNull ConfigurationService service,
        String name,
        Supplier<Object> dValue,
        ThrowingFunction<Object, Object> parser,
        String argument,
        String desc
    ) {
        var key = new ConfigurationKey<T>(name, dValue, new Codec(parser, null, null), argument, desc);
        service.registerKey(key);
        return key;
    }

    private static <T> @NotNull ArgumentKey<T> registerArg(
        @NotNull ConfigurationService service,
        String name,
        Supplier<Object> dValue,
        ThrowingFunction<Object, Object> parser,
        String argument,
        String desc
    ) {
        var key = new ArgumentKey<T>(name, dValue, new Codec(parser, null, null), argument, desc);
        service.registerKey(key);
        return key;
    }
}

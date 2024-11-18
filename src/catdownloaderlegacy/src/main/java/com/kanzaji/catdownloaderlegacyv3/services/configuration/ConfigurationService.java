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

package com.kanzaji.catdownloaderlegacyv3.services.configuration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.kanzaji.catdownloaderlegacyv3.CatDownloader;
import com.kanzaji.catdownloaderlegacyv3.services.ServiceManager;
import com.kanzaji.catdownloaderlegacyv3.services.ServiceManager.State;
import com.kanzaji.catdownloaderlegacyv3.services.Services;
import com.kanzaji.catdownloaderlegacyv3.services.Logger;
import com.kanzaji.catdownloaderlegacyv3.services.interfaces.ILogger;
import com.kanzaji.catdownloaderlegacyv3.services.interfaces.IService;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class ConfigurationService implements IService {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final Logger mainLogger = Services.getLogger();
    private final ILogger logger;
    private final String name;
    public final Path configFile;
    private final Map<String, ConfigurationKey<?>> keys = new LinkedHashMap<>();
    private final Map<String, String> arguments = new HashMap<>();
    private boolean initialized = false;

    /**
     * @param name Name of the service.
     * @param configFile Path to the configuration file.
     */
    public ConfigurationService(String name, Path configFile) {
        this.name = name;
        this.configFile = configFile;
        this.logger = Logger.get(name);
        ConfigurationConflictService.addService(this);
    }

    /**
     * Used to create ConfigurationService without configuration file.
     * @param name Name of the service.
     */
    public ConfigurationService(String name) {
        this(name, null);
    }

    public void registerKey(@NotNull ConfigurationKey<?> key) {
        if (ServiceManager.getStatus() != State.NOT_INIT) {
            throw new IllegalStateException("Registration of new Keys has to be done before PRE_INIT!");
        }
        if (keys.containsKey(Objects.requireNonNull(key, "Can't register null key!").getName())) {
            throw new IllegalArgumentException("A key is already registered under specified name!");
        }
        keys.put(key.getName(), key);
        if (key.getArg() != null) arguments.put(key.getArg().toLowerCase(Locale.ROOT), key.getName());
        logger.info("Registered configuration key under name: " + key.getName());
    }

    /**
     * Returns if ConfigurationService finished its initialization.
     * @return True if after INIT, otherwise false.
     */
    public boolean initialized() {
        return this.initialized;
    }

    /**
     * Returns description of the key.
     * @param key Name of the key to get description for.
     * @return The Description of the key. Can be null.
     */
    public String getDesc(String key) {
        return keys.get(Objects.requireNonNull(key)).getDesc();
    }

    /**
     * Used to get a map of registered configuration keys.
     * @return A copy of the map used to hold registered keys.
     */
    public Map<String, ConfigurationKey<?>> getKeys() {
        return new LinkedHashMap<>(this.keys);
    }

    /**
     * @return The name of the service.
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Used to get the value of a specified key.
     * @param key Name of the key.
     * @return Value of the key.
     * @apiNote It is easier and more convenient to use {@link ConfigurationKey#get()} call directly.
     */
    public Object getValue(@NotNull String key) {
        if (!keys.containsKey(Objects.requireNonNull(key))) throw new IllegalArgumentException("Key with name: " + key + " not found!");
        return keys.get(key).get();
    }

    /**
     * Used to set the value of a specified key.
     * @param key Name of the key.
     * @param value Value to set the key to.
     * @return Set value.
     */
    public Object setValue(@NotNull String key, @NotNull Object value) {
        if (!this.initialized) throw new IllegalStateException(this.getName() + " is not yet initialized! Can't get config values before INIT.");
        if (!keys.containsKey(Objects.requireNonNull(key))) throw new IllegalArgumentException("Key with name: " + key + " not found!");
        return keys.get(key).setValue(Objects.requireNonNull(value, "Value can't be null!"));
    }

    /**
     * @return True if Service has Configuration file, otherwise false.
     */
    public boolean hasConfig() {
        return Objects.nonNull(this.configFile);
    }

    /**
     * Used to get lists of implemented phases.
     * Only phases mentioned in a returned list are executed, even if implemented.
     * @return List with implemented initialization phases.
     */
    @Override
    public List<State> getPhases() {
        return List.of(State.PRE_INIT);
    }

    @Override
    public void preInit() throws IOException {
        checkDefaults();
        if (hasConfig() && !Files.exists(configFile)) generateConfig(configFile);
        parseConfig();
        parseArguments();
        logger.info("PRE_INIT Finished.");
        this.initialized = true;
    }

    /**
     * Private method used to check default values of the keys, and if they haven't changed during initialization.
     */
    private void checkDefaults() {
        logger.info("Gathering default values of the keys...");
        keys.forEach((name, value) -> {
            if (value.getValue() != value.getDefault()) {
                logger.warn("Default value CHANGED for key: " + name + "!");
                value.setValue(value.getDefault());
            }
        });
    }

    /**
     * Private method used to parse arguments and patch parsed values from the configuration file.
     */
    private void parseArguments() {
        Map<String, String> args = new HashMap<>();
        String currentArg = null;
        for (String arg : CatDownloader.ARGUMENTS) {
            // Skip all values until we find the first argument.
            if (arg.startsWith("-")) {
                if (arg.contains(":")) {
                    currentArg = arg.substring(0, arg.indexOf(":"));
                    if (!arg.endsWith(":"))
                        args.putIfAbsent(currentArg, arg.substring(arg.indexOf(":")+1).strip());
                }
            }
            // We skip all arguments that don't have a value specified. Those are handled by the default value generators of specified configuration keys.
            else if (currentArg != null)
                args.putIfAbsent(currentArg, arg);
        }

        args.forEach((arg, value) -> {
            var keyName = arguments.get(arg.toLowerCase(Locale.ROOT).replace("-", ""));
            if (keyName == null) {
                logger.warn("Skipping unknown argument: " + arg);
                return;
            }
            var key = keys.get(keyName);
            if (key == null) throw new IllegalStateException("Argument \"%s\" points to an invalid key \"%s\"!".formatted(arg, keyName));
            try {
                key.parseAndSet(value);
                logger.info("Patched configuration key \"%s\" with argument value \"%s\".".formatted(keyName, value));
            } catch (Throwable ex) {
                logger.logStackTrace("Failed parsing value for argument \"%s\"! Problematic value: \"%s\".".formatted(arg, value), ex);
            }
        });
    }

    /**
     * Private method used to parse and correct the configuration file.
     */
    private void parseConfig() throws IOException {
        if (hasConfig() && !Files.exists(configFile)) throw new IllegalStateException("Configuration File is missing, even tho default should be generated! Something isn't right.");

        if (!hasConfig()) {
            logger.info("No configuration file for this Configuration Service.");
            return;
        }

        logger.info("Loading configuration file...");
        Map<String, Object> config = new LinkedHashMap<>();
        AtomicBoolean configRegeneration = new AtomicBoolean(false);
        try {
            config.putAll(gson.fromJson(Files.readString(configFile), new TypeToken<Map<String, Object>>(){}.getType()));
        } catch (Exception e) {
            logger.logStackTrace("Failed parsing configuration file! The file is going to be regenerated with defaults...", e);
            configRegeneration.set(true);
        }

        keys.forEach((name, key) -> {
            if (key.isArgumentOnly()) return;
            if (!config.containsKey(name)) {
                logger.warn("Missing key from configuration file! " + name + " (" + key.getValueClass().getName() + ")");
                configRegeneration.set(true);
                return;
            }

            Object value = config.get(name);
            config.remove(name);

            var validation = key.verify(value);
            if (!validation.result()) {
                logger.error("Key \"%s\" failed validation. Message: %s".formatted(name, validation.message()));
                logger.error("Illegal value for key: " + name + "! Value: " + value);
                configRegeneration.set(true);
                return;
            }

            try {
                key.parseAndSet(value);
            } catch (Throwable e) {
                logger.logStackTrace("Exception while parsing value for key: " + name, e);
                configRegeneration.set(true);
            }
        });

        if (!config.keySet().isEmpty()) {
            logger.warn("Additional keys found in the configuration file!");
            configRegeneration.set(true);
            config.forEach((name, value) -> logger.warn("- " + name + " -> " + value));
        }

        if (configRegeneration.get()) {
            logger.warn("Config \"" + configFile.toAbsolutePath() + "\" appears to be incorrect. Correcting...");
            Files.deleteIfExists(configFile);
            this.generateConfig(configFile);
        }

        logger.info("Configuration file loaded.");
    }

    /**
     * Used to generate configuration file from the configuration service data.
     * @param path Path to generate configuration file at.
     * @throws IOException When IO Exception occurs.
     */
    private void generateConfig(Path path) throws IOException {
        path = path.toAbsolutePath();
        if (Files.exists(path)) throw new FileAlreadyExistsException("Specified location already exists!");
        logger.info("Generating configuration file...");

        Map<String, Object> config = new LinkedHashMap<>();
        keys.forEach((name, key) -> {
            if (key.isArgumentOnly()) return;
            config.put(name, key.serialize());
        });
        String[] configJsonArray = gson.toJson(config).split("\n");

        int index = 0;
        StringBuilder configJson = new StringBuilder();
        List<String> keyNames = keys.entrySet().stream()
            .filter(it -> !it.getValue().isArgumentOnly())
            .map(Map.Entry::getKey)
            .toList();

        for (String entry : configJsonArray) {
            ConfigurationKey<?> key = keys.get(keyNames.get(index));
            if (entry.strip().startsWith("\"" + key.getName() + "\":")) {
                if (index+1 < keyNames.size()) index++;
                if (key.getDesc() != null) configJson.append("  // ").append(key.getDesc().replaceAll("\n", "\n  // ")).append("\n");
                if (key.getArg() != null) configJson.append("  // Argument representation: -").append(key.getArg()).append("\n");
            }
            configJson.append(entry).append("\n\n");
        }

        // Removes additional \n on the start and the end of the Configuration json. Only cosmetic
        configJson.replace(0, 3, "{\n").replace(configJson.length()-4, configJson.length(), "}");

        Files.createDirectories(path.getParent());
        Files.createFile(path);
        Files.writeString(path, configJson.toString());
        logger.info("Saved configuration file to: \"" + path + "\".");
    }
}

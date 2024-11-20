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

package com.kanzaji.catdownloaderlegacyv3.services.configuration;

import com.kanzaji.catdownloaderlegacyv3.services.ServiceManager;
import com.kanzaji.catdownloaderlegacyv3.services.Services;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Configuration key is used to register new configuration keys to any of the {@link ConfigurationService}s.
 */
public class ConfigurationKey<T> {
    private final String registry;
    private final String argument;
    private final String desc;
    private final Class<?> valClass;
    private final Supplier<Object> defaultValue;
    private final Codec codec;
    private final boolean argumentOnly;
    private Object value;

    /**
     * Constructor of the ConfigurationKey.
     * @param registryName Required. Used to represent the configuration key in the registry. Used as name in the Configuration file.
     * @param defaultValue Required non-native. Sets the class of this Configuration Key.
     * @param codec Not Required. Provides Codec-Related methods for the Configuration Service for the type stored in this key.
     * @param argument Not Required. Used to add possibility to change value with use of the application arguments.
     * @param description Not Required. Used to add description in the configuration file of that key.
     * @param argumentOnly Determines if this ConfigurationKey should not be included in the configuration file.
     */
    @SuppressWarnings("unchecked")
    public ConfigurationKey(
        @NotNull String registryName,
        @NotNull Supplier<Object> defaultValue,
        @Nullable Codec codec,
        @Nullable String argument,
        @Nullable String description,
        boolean argumentOnly
    ) {
        this.argumentOnly = argumentOnly;
        this.registry = registryName;
        this.defaultValue = defaultValue;
        this.value = defaultValue.get();
        this.valClass = value.getClass();
        this.codec = codec == null? new Codec(): codec;
        this.desc = description;
        this.argument = argument;

        if (!valClass.isInstance(value)) throw new IllegalArgumentException("Default value is primitive or something really bad happened! Configuration key: " + this.getName());
        try {
            // This is stupid, but for easy access to the values in IDE the stored Class is specified. This is just the sanity check.
            this.value = (T) value;
        } catch (Throwable e) {
            throw new IllegalStateException("Either invalid value or type provided for key " + this.getName());
        }
    }

    /**
     * Constructor of the ConfigurationKey.
     * @param registryName Required. Used to represent the configuration key in the registry. Used as name in the Configuration file.
     * @param defaultValue Required non-native. Sets the class of this Configuration Key.
     * @param codec Not Required. Provides Codec-Related methods for the Configuration Service for the type stored in this key.
     * @param argument Not Required. Used to add possibility to change value with use of the application arguments.
     * @param description Not Required. Used to add description in the configuration file of that key.
     */
    public ConfigurationKey(
        @NotNull String registryName,
        @NotNull Supplier<Object> defaultValue,
        @Nullable Codec codec,
        @Nullable String argument,
        @Nullable String description
    ) {
        this(registryName, defaultValue, codec, argument, description, false);
    }

    /**
     * Constructor of the ConfigurationKey.
     * @param registryName Required. Used to represent the configuration key in the registry. Used as name in the Configuration file.
     * @param defaultValue Required non-native. Sets the class of this Configuration Key.
     * @param codec Not Required. Provides Codec-Related methods for the Configuration Service for the type stored in this key.
     * @param description Not Required. Used to add description in the configuration file of that key.
     */
    public ConfigurationKey(
        @NotNull String registryName,
        @NotNull Supplier<Object> defaultValue,
        @Nullable Codec codec,
        @Nullable String description
    ) {
        this(registryName, defaultValue, codec, null, description, false);
    }

    /**
     * Constructor of the ConfigurationKey.
     * @param registryName Required. Used to represent the configuration key in the registry. Used as name in the Configuration file.
     * @param defaultValue Required non-native. Sets the class of this Configuration Key.
     * @param argument Not Required. Used to add possibility to change value with use of the application arguments.
     * @param description Not Required. Used to add description in the configuration file of that key.
     */
    public ConfigurationKey(
        @NotNull String registryName,
        @NotNull Supplier<Object> defaultValue,
        @Nullable String argument,
        @Nullable String description
    ) {
        this(registryName, defaultValue, null, argument, description, false);
    }

    /**
     * Constructor of the ConfigurationKey.
     * @param registryName Required. Used to represent the configuration key in the registry. Used as name in the Configuration file.
     * @param defaultValue Required non-native. Sets the class of this Configuration Key.
     * @param description Not Required. Used to add description in the configuration file of that key.
     */
    public ConfigurationKey(
        @NotNull String registryName,
        @NotNull Supplier<Object> defaultValue,
        @Nullable String description
    ) {
        this(registryName, defaultValue, null, null, description, false);
    }

    /**
     * Constructor of the ConfigurationKey.
     * @param registryName Required. Used to represent the configuration key in the registry. Used as name in the Configuration file.
     * @param defaultValue Required non-native. Sets the class of this Configuration Key.
     * @param codec Not Required. Provides Codec-Related methods for the Configuration Service for the type stored in this key.
     */
    public ConfigurationKey(
        @NotNull String registryName,
        @NotNull Supplier<Object> defaultValue,
        @Nullable Codec codec
    ) {
        this(registryName, defaultValue, codec, null, null, false);
    }

    /**
     * Constructor of the ConfigurationKey.
     * @param registryName Used to represent the configuration key in the registry. Used as name in the Configuration file.
     * @param defaultValue Required non-native. Sets the class of this Configuration Key.
     */
    public ConfigurationKey(@NotNull String registryName, @NotNull Supplier<Object> defaultValue) {
        this(registryName, defaultValue, null, null, null, false);
    }

    /**
     * @return The registry name of this Configuration key.
     */
    public String getName() {
        return registry;
    }

    /**
     * @return The argument representation of this Configuration key. Can be null.
     */
    public String getArg() {
        return argument;
    }

    /**
     * @return The Description of the Configuration key. Can be null.
     */
    public String getDesc() {
        return desc;
    }

    /**
     * @return If true, this ConfigurationKey is argument only and won't be included in the configuration file.
     */
    public boolean isArgumentOnly() {
        return argumentOnly;
    }

    /**
     * Return value of this configuration key.
     * @return Object with the value of this key.
     * @throws IllegalStateException when called before PRE-INIT of services is finished.
     */
    @SuppressWarnings("unchecked")
    public T get() {
        ConfigurationService config = Services.CONFIG.get();
        if (!config.initialized()) throw new IllegalStateException(config.getName() + " is not yet initialized! Configuration values are available after PRE-INIT.");
        if (!valClass.isInstance(value) && Objects.nonNull(value))
            throw new IllegalStateException("Object of different class stored in key %s! Expected: %s, Stored: %s".formatted(getName(), valClass, value.getClass()));
        return (T) value;
    }

    /**
     * Protected method to get the stored value without initialization checks. Should not be used outside the Configuration package.
     * @return Object with the value of this key.
     */
    protected Object getValue() {
        return value;
    }

    /**
     * Used to generate default value of this configuration key.
     * @return Object returned by default value supplier.
     */
    public Object getDefault() {
        return this.defaultValue.get();
    }

    /**
     * @return Class of the value stored in this key.
     */
    public Class<?> getValueClass() {
        return this.valClass;
    }

    /**
     * Used to verify if passed value is acceptable for this key. Returns true if verifier is null.
     * @param value Object to verify.
     * @return True if object passes verification or verifier is null.
     */
    public ValidationResult verify(Object value) {
        return this.codec.hasValidator()? this.codec.validate(value): new ValidationResult();
    }

    /**
     * Used to parse data from Strings. If Parser is not present, returns original String.
     * @param value String with data to parse.
     * @return Result of the parser, or passed String if Parser not present.
     */
    public Object parse(Object value) throws Throwable {
        return this.codec.hasParser()? this.codec.parse(value): value;
    }

    /**
     * Used to serialize stored Data to String.
     * @return String representation of stored data if Codec is present, otherwise original object for GSON to serialize.
     */
    public Object serialize() {
        return this.codec.hasSerializer()? codec.serialize(this.value): this.value;
    }

    /**
     * Used to set the value of this configuration key. Requires to be the same Class as the default value.
     * @param value Object of the same class as the default value.
     * @return Set value.
     * @throws IllegalArgumentException if Class of the passed object is different from the default object.
     */
    public Object setValue(Object value) {
        if (!valClass.isInstance(value) && Objects.nonNull(value)) {
            throw new IllegalArgumentException("Can't change class of configuration key %s! Expected: %s, got: %s".formatted(getName(), valClass, value.getClass()));
        }
        this.value = value;
        return getValue();
    }

    /**
     * Used to parse and set the value from String. Returns set value.
     * @param value String with data to parse and set.
     * @return Set value.
     */
    public Object parseAndSet(Object value) throws Throwable {
        return this.setValue(this.parse(value));
    }
}

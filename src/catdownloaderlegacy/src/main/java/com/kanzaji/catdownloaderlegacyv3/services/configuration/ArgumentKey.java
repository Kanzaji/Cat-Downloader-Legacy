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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/**
 * ArgumentKey is a version of ArgumentKey, which is marked as "Argument Only", meaning it won't be included in generation and parsing of the Configuration file.
 * @param <T> Type of the stored object.
 */
public class ArgumentKey<T> extends ConfigurationKey<T> {
    /**
     * Constructor of the ArgumentKey.
     * @param registryName Required. Used to represent the configuration key in the registry. Used as name in the Configuration file.
     * @param defaultValue Required non-native. Sets the class of this Configuration Key.
     * @param codec Not Required. Provides Codec-Related methods for the Configuration Service for the type stored in this key.
     * @param argument Not Required. Used to add possibility to change value with use of the application arguments.
     * @param description Not Required. Used to add description in the configuration file of that key.
     */
    public ArgumentKey(
        @NotNull String registryName,
        @NotNull Supplier<Object> defaultValue,
        @Nullable Codec codec,
        @Nullable String argument,
        @Nullable String description
    ) {
        super(registryName, defaultValue, codec, argument, description, true);
    }

    /**
     * Constructor of the ArgumentKey.
     * @param registryName Required. Used to represent the configuration key in the registry. Used as name in the Configuration file.
     * @param defaultValue Required non-native. Sets the class of this Configuration Key.
     * @param codec Not Required. Provides Codec-Related methods for the Configuration Service for the type stored in this key.
     * @param description Not Required. Used to add description in the configuration file of that key.
     */
    public ArgumentKey(
        @NotNull String registryName,
        @NotNull Supplier<Object> defaultValue,
        @Nullable Codec codec,
        @Nullable String description
    ) {
        super(registryName, defaultValue, codec, null, description, true);
    }

    /**
     * Constructor of the ArgumentKey.
     * @param registryName Required. Used to represent the configuration key in the registry. Used as name in the Configuration file.
     * @param defaultValue Required non-native. Sets the class of this Configuration Key.
     * @param argument Not Required. Used to add possibility to change value with use of the application arguments.
     * @param description Not Required. Used to add description in the configuration file of that key.
     */
    public ArgumentKey(
        @NotNull String registryName,
        @NotNull Supplier<Object> defaultValue,
        @Nullable String argument,
        @Nullable String description
    ) {
        super(registryName, defaultValue, null, argument, description, true);
    }

    /**
     * Constructor of the ArgumentKey.
     * @param registryName Required. Used to represent the configuration key in the registry. Used as name in the Configuration file.
     * @param defaultValue Required non-native. Sets the class of this Configuration Key.
     * @param description Not Required. Used to add description in the configuration file of that key.
     */
    public ArgumentKey(
        @NotNull String registryName,
        @NotNull Supplier<Object> defaultValue,
        @Nullable String description
    ) {
        super(registryName, defaultValue, null, null, description, true);
    }

    /**
     * Constructor of the ArgumentKey.
     * @param registryName Required. Used to represent the configuration key in the registry. Used as name in the Configuration file.
     * @param defaultValue Required non-native. Sets the class of this Configuration Key.
     * @param codec Not Required. Provides Codec-Related methods for the Configuration Service for the type stored in this key.
     */
    public ArgumentKey(
        @NotNull String registryName,
        @NotNull Supplier<Object> defaultValue,
        @Nullable Codec codec
    ) {
        super(registryName, defaultValue, codec, null, null, true);
    }

    /**
     * Constructor of the ArgumentKey.
     * @param registryName Used to represent the configuration key in the registry. Used as name in the Configuration file.
     * @param defaultValue Required non-native. Sets the class of this Configuration Key.
     */
    public ArgumentKey(@NotNull String registryName, @NotNull Supplier<Object> defaultValue) {
        super(registryName, defaultValue, null, null, null, true);
    }
}

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

package com.kanzaji.catdownloaderlegacyv3.services.configuration.types;

import com.kanzaji.catdownloaderlegacyv3.utils.interfaces.ThrowingFunction;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;

public class EnumType {
    private EnumType() {}

    /**
     * EnumParser is used to automatically parse Enum values from Strings in the configuration file.<br><br>
     * It converts the values to uppercase, and replaces <code>-</code> with <code>_</code>,
     * resulting in naming convention of <code>ENUM_ENTRY_ONE</code>, resulting in "Enum-Entry-One" (Case Insensitive).
     */
    public static @NotNull ThrowingFunction<Object, Object> getParser(Function<String, Enum<?>> parser, List<String> acceptableValues) {
        Objects.requireNonNull(parser, "Can't provide empty method!");
        var values = List.copyOf(Objects.requireNonNull(acceptableValues, "Acceptable values can't be null!"));
        return (o) -> {
            try {
                if (o instanceof String s)
                    return parser.apply(s.toUpperCase(Locale.ROOT).replaceAll("-", "_"));
            } catch (Throwable e) {
                if (e instanceof IllegalArgumentException)
                    throw new IllegalArgumentException("Invalid value for this Enum! Acceptable values are: " + String.join(", ", values));
                throw e;
            }
            throw new IllegalArgumentException("Invalid class for Enum! Expected String, got: " + o.getClass());
        };
    }
}

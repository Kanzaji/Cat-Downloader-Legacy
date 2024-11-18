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

import com.kanzaji.catdownloaderlegacyv3.utils.interfaces.ThrowingFunction;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Function;

public class Codec {
    @Nullable
    private final ThrowingFunction<Object, Object> parser;
    @Nullable
    private final Function<Object, ValidationResult> validator;
    @Nullable
    private final Function<Object, String> serializer;

    /**
     * Constructor of the Codec with all fields empty.
     */
    public Codec() {
        this.parser = null;
        this.validator = null;
        this.serializer = null;
    }

    /**
     * Constructor of the Codec. None of the fields are mandatory.
     * @param parser Required for objects not supported by the GSON Library. Turns the parsed String into the Java Object.
     * @param serializer Required for objects not supported by the GSON Library. Turns the stored object into String.
     * @param validator Used to verify if parsed value is acceptable.
     */
    public Codec(
        @Nullable ThrowingFunction<Object, Object> parser,
        @Nullable Function<Object, ValidationResult> validator,
        @Nullable Function<Object, String> serializer
    ) {
        this.parser = parser;
        this.validator = validator;
        this.serializer = serializer;
    }

    public boolean hasParser() {
        return Objects.nonNull(parser);
    }

    public boolean hasValidator() {
        return Objects.nonNull(validator);
    }

    public boolean hasSerializer() {
        return Objects.nonNull(serializer);
    }

    public Object parse(Object o) throws Throwable {
        if (!hasParser()) return null;
        return Objects.requireNonNull(parser).apply(o);
    }

    public ValidationResult validate(Object o) {
        if (!hasValidator()) return null;
        return Objects.requireNonNull(validator).apply(o);
    }

    public String serialize(Object o) {
        if (!hasSerializer()) return null;
        return Objects.requireNonNull(serializer).apply(o);
    }
}

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

import java.util.function.Supplier;

public class BooleanType {
    private BooleanType() {}
    public static final Supplier<Object> DEFAULT_FALSE = () -> false;
    public static final Supplier<Object> DEFAULT_TRUE = () -> true;
    public static final ThrowingFunction<Object, Object> PARSER = (s) -> {
        if (s instanceof String) {
            // Blank string on default is counted as false, what might be good in some cases, but in this case, it only causes issues.
            if (((String) s).isBlank()) throw new IllegalArgumentException("Blank string is not acceptable as a boolean value!");
            return Boolean.parseBoolean((String) s);
        }
        return (Boolean) s;
    };
}

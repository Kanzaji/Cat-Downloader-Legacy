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

import com.kanzaji.catdownloaderlegacyv3.services.configuration.ValidationResult;
import com.kanzaji.catdownloaderlegacyv3.utils.interfaces.ThrowingFunction;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;
import java.util.function.Supplier;

public class PathType {
    private PathType() {}

    public static final ThrowingFunction<Object, Object> PARSER = (s) ->  {
        if (s instanceof String) return Path.of((String) s);
        return (Path) s;
    };

    public static final Function<Object, String> SERIALIZER = (o) -> {
        if (o instanceof Path) return ((Path) o).toString();
        return (String) o;
    };

    public static final Function<Object, ValidationResult> MUST_EXIST_VALIDATOR = (s) -> {
        try {
            Path path = (Path) PARSER.apply(s);
            if (Files.exists(path)) return new ValidationResult(true, "");
            return new ValidationResult(false, "Provided path (\"%s\") doesn't exist!".formatted(path.toAbsolutePath()));
        } catch (Throwable e) {
            return new ValidationResult(false, "Exception caught while parsing value. " + e);
        }
    };

    @Contract(pure = true)
    public static @NotNull Supplier<Object> defaultPath(String init) {
        var path = Path.of(init);
        return () -> path;
    }
}

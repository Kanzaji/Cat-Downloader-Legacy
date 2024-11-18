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

import com.kanzaji.catdownloaderlegacyv3.services.configuration.Codec;
import com.kanzaji.catdownloaderlegacyv3.services.configuration.ConfigurationKey;
import com.kanzaji.catdownloaderlegacyv3.services.configuration.types.IntegerType;

public class LogStockSizeKey extends ConfigurationKey<Integer> {
    public LogStockSizeKey() {
        super(
            "Log-Stockpile-Limit",
            IntegerType.defaultSupplier(10),
            new Codec((value) -> {
                if (value instanceof Boolean) return ((Boolean) value)? 0: -1;
                if (value instanceof Integer || value instanceof Double) return IntegerType.PARSER.apply(value);
                throw new IllegalArgumentException("Illegal value type for this key! Can only be Boolean or Integer.");
            }, null, null),
            "LogStockpileLimit",
            """
            Determines the maximum number of logs stored by the launcher.
            Accepts:
            - True or 0 -> No limit.
            - False or < 0 -> Only the latest log.
            - Number -> Sets custom limit."""
        );
    }

    public boolean shouldStockpile() {
        return this.get() > -1;
    }

    public boolean isInfinite() {
        return this.get() == 0;
    }
}

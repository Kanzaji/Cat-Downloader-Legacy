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

package com.kanzaji.catdownloaderlegacyv3.services;

import com.kanzaji.catdownloaderlegacyv3.config.Configuration;
import com.kanzaji.catdownloaderlegacyv3.services.configuration.ConfigurationService;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public enum Services {
    // Services enum, for easier access to services when needed.
    LOGGER(Logger.getInstance().getName()),
    CONFIG("Main Configuration Service");

    // Fields used by ServiceManager to get services.
    public final String name;
    Services(String name) {
        this.name = name;
    }

    // Utility

    /**
     * Used to get instance of the logger from ServiceManager.
     * @return Instance of the main logger service.
     */
    public static @NotNull Logger getLogger() {
        return ServiceManager.get(LOGGER);
    }

    // Other methods related to services

    /**
     * Used to register services to ServiceManager.
     * @apiNote Even tho it's possible to register new service anywhere,
     * and the ENUM isn't required, for consistency reasons,
     * new services should be registered here with added ENUM entry.
     */
    public static void registerServices() {
        ServiceManager.registerService(Logger.getInstance());

        ConfigurationService mainCFG = new ConfigurationService("Main Configuration Service", Path.of("Cat-Downloader-Legacy-Config.json5"));
        ServiceManager.registerService(mainCFG);

        Configuration.register(mainCFG);
    }
}

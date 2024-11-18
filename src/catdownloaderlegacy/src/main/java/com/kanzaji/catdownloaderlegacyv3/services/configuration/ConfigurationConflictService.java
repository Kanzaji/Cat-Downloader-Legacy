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

import com.kanzaji.catdownloaderlegacyv3.services.Logger;
import com.kanzaji.catdownloaderlegacyv3.services.ServiceManager;
import com.kanzaji.catdownloaderlegacyv3.services.Services;
import com.kanzaji.catdownloaderlegacyv3.services.interfaces.ILogger;
import com.kanzaji.catdownloaderlegacyv3.services.interfaces.IService;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * ConfigurationConflictService is used to prevent conflicts between different Configuration Services, and prevent issues with accidental overlapping configuration keys.
 */
public class ConfigurationConflictService implements IService {
    private static final ILogger logger;

    static {
        Services.getLogger();
        logger = Logger.get("Configuration Conflict Prevention Service");
    }

    private static final List<ConfigurationService> CFGServices = new ArrayList<>();

    @Override
    public String getName() {
        return "Configuration Conflict Prevention Service";
    }

    protected static void addService(@NotNull ConfigurationService service) {
        CFGServices.add(Objects.requireNonNull(service));
    }

    /**
     * Used to get lists of implemented phases.
     * Only phases mentioned in a returned list are executed, even if implemented.
     *
     * @return List with implemented initialization phases.
     */
    @Override
    public List<ServiceManager.State> getPhases() {
        return List.of(ServiceManager.State.POST_INIT);
    }

    /**
     * Used for PostInit phase of the service.
     *
     */
    @Override
    public void postInit() {
        //TODO: Do check for conflicts between services
        logger.info("Scanning Configuration services for potential conflicts...");
        CFGServices.forEach(service -> {

        });
    }
}

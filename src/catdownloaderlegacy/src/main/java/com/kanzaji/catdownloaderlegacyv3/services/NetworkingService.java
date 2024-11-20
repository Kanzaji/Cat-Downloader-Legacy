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
import com.kanzaji.catdownloaderlegacyv3.services.enums.State;
import com.kanzaji.catdownloaderlegacyv3.services.interfaces.ILogger;
import com.kanzaji.catdownloaderlegacyv3.services.interfaces.IService;
import com.kanzaji.catdownloaderlegacyv3.utils.NetworkingUtils;

import java.util.List;

public class NetworkingService implements IService {
    private static final ILogger logger = Logger.get("Networking Service");

    @Override
    public String getName() {
        return "Networking Service";
    }

    @Override
    public List<State> getPhases() {
        return List.of(State.INIT);
    }

    @Override
    public void init() throws Throwable {
        if (Configuration.BypassNetworkCheck.get()) {
            logger.warn("Network Check bypass active! Skipping checking networking connection, stuff might go horribly wrong!");
            return;
        }

        logger.info("Checking network connection...");

        long StartingTime = System.currentTimeMillis();
        if (NetworkingUtils.checkConnection("https://github.com/")) {
            float CurrentTime = (float) (System.currentTimeMillis() - StartingTime) / 1000F;
            logger.info("Network connection checked! Time to verify network: " + CurrentTime + " seconds.");
            if (CurrentTime > 2) {
                logger.print("It appears you have slow network connection! This might or might not cause issues with Verification or Download steps. Use with caution.", 1);
            }
        } else {
            logger.critical("No network connection! This app can not run properly without access to the internet.");
            System.out.println("It appears you are running this app without access to the internet. This app requires internet connection to function properly.");
            System.out.println("If you have network connection, and the Check host is unavailable (github.com), run the app with -BypassNetworkCheck argument!");
            throw new IllegalStateException("Network connection check failed!");
        }
    }
}

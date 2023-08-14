/**************************************************************************************
 * MIT License                                                                        *
 *                                                                                    *
 * Copyright (c) 2023. Kanzaji                                                        *
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

package com.kanzaji.catdownloaderlegacy.temp;

import com.kanzaji.catdownloaderlegacy.ArgumentDecoder;
import com.kanzaji.catdownloaderlegacy.data.CFManifest;
import com.kanzaji.catdownloaderlegacy.loggers.LoggerCustom;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.kanzaji.catdownloaderlegacy.CatDownloader.dataGatheringFails;

public class OldDataGathering {
    // Temporary fields to make IDE happy
    private static final ArgumentDecoder ARD = ArgumentDecoder.getInstance();
    private static final LoggerCustom logger = new LoggerCustom(null);
    private static final CFManifest CFManifestData = new CFManifest();

    /**
     * This is just an archived method, waiting for a rework into CDLInstance Interperter for CFPack manifest. DO NOT USE IT ANYWHERE.
     * @throws InterruptedException
     */
    @SuppressWarnings(value = "all")
    @Deprecated(since = "2.0", forRemoval = true)
    public static void startDataGathering() throws InterruptedException {
        if (true) return;
        if (ARD.isPackMode()) {
            logger.print("Gathering Data about mods... This may take a while.");
            ExecutorService Executor, FailExecutor;
            if (ARD.isExperimental()) {
                logger.warn("Experimental mode for CurseForge support turned on! This may cause unexpected behaviour and issues with data gathering process.");
                logger.warn("Use at your own risk! Try to not over-use it.");
                Executor = Executors.newFixedThreadPool(ARD.getThreads());
                FailExecutor = Executors.newFixedThreadPool(ARD.getThreads()/4);
            } else {
                // If without Experimental there are issues with Data Gathering process, this is to blame.
                Executor = Executors.newFixedThreadPool(2);
                FailExecutor = Executors.newFixedThreadPool(1);
            }

            int Index = 0;
            for (CFManifest.ModFile mod : CFManifestData.files) {
                int finalIndex = Index;
                Executor.submit(() -> {
                    CFManifestData.files[finalIndex] = mod.getData(CFManifestData.minecraft);
                    if (CFManifestData.files[finalIndex] != null && (CFManifestData.files[finalIndex].error202 || CFManifestData.files[finalIndex].error403)) {
                        mod.error403 = CFManifestData.files[finalIndex].error403;
                        mod.error202 = CFManifestData.files[finalIndex].error202;
                        dataGatheringFails.add(() -> CFManifestData.files[finalIndex] = mod.getData(CFManifestData.minecraft));
                    }
                });
                Index += 1;
            }

            Executor.shutdown();
            if (!Executor.awaitTermination(1, TimeUnit.DAYS)) {
                logger.print("Data gathering takes over a day! This for sure isn't right???",3);
                throw new RuntimeException("Data gathering is taking over a day! Something is horribly wrong.");
            }

            if (dataGatheringFails.size() > 0) {
                logger.warn("Data gathering errors present! Trying to re-run unsuccessful data requests. Errors present: " + dataGatheringFails.size());
                dataGatheringFails.forEach(FailExecutor::submit);
            }

            FailExecutor.shutdown();
            if (!FailExecutor.awaitTermination(1, TimeUnit.DAYS)) {
                logger.print("Data gathering takes over a day! This for sure isn't right???",3);
                throw new RuntimeException("Data gathering is taking over a day! Something is horribly wrong.");
            }

            logger.print("Finished gathering data!");
        }
    }
}

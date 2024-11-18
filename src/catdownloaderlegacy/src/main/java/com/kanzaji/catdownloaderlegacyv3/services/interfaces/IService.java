/**************************************************************************************
 * MIT License                                                                        *
 *                                                                                    *
 * Copyright (c) 2023-2024. Kanzaji                                                   *
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

package com.kanzaji.catdownloaderlegacyv3.services.interfaces;

import com.kanzaji.catdownloaderlegacyv3.services.ServiceManager;

import java.util.List;

public interface IService {
    String getName();

    /**
     * Used to get lists of implemented phases.
     * Only phases mentioned in a returned list are executed, even if implemented.
     * @return List with implemented initialization phases.
     */
    List<ServiceManager.State> getPhases();
    /**
     * Used for PreInit phase of the service.
     * @throws Throwable Possible exception from the PreInit phase.
     */
    default void preInit() throws Throwable {}
    /**
     * Used for Init phase of the service.
     * @throws Throwable Possible exception from the Init phase.
     */
    default void init() throws Throwable {}
    /**
     * Used for PostInit phase of the service.
     * @throws Throwable Possible exception from the PostInit phase.
     */
    default void postInit() throws Throwable {}

    /**
     * Used for handling the exit off the application if required.
     * @throws Throwable Possible exception from the Exit phase.
     */
    default void exit() throws Throwable {}

    /**
     * Used for handling the unexpected exit (crash) off the application if required.
     * @throws Throwable Possible exception from the Exit phase.
     */
    default void crash() throws Throwable {}
}

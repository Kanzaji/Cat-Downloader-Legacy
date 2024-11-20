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

package com.kanzaji.catdownloaderlegacyv3.services;

import com.kanzaji.catdownloaderlegacyv3.services.enums.State;
import com.kanzaji.catdownloaderlegacyv3.services.interfaces.ILogger;
import com.kanzaji.catdownloaderlegacyv3.services.interfaces.IService;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * <h1>ServiceManager</h1>
 * ServiceManager is a class used to hold and manage Services.
 * All services have 3 phases of initialization, Exiting phase and Crash phase. None of those are required to be implemented.
 * <br>
 * <h3>Order of Operations:</h3>
 * <li><b>PRE_INIT</b> - Instantly after the app is launched, should be used for only crucial initialization that needs to be done as fast as possible.</li>
 * <li><b>INIT</b> - Launched soon after PRE_INIT. Some crucial code might get executed between those two phases.</li>
 * <li><b>POST_INIT</b> - Should be used for stuff required to do after initialization of the app.</li>
 * <li><b>EXIT</b> - Launched on exit of the app.</li>
 * <li><b>CRASH</b> - Launched on the crash of the app, if any special handling in services is required.</li>
 */
public class ServiceManager {
    private static State status = State.NOT_INIT;
    private static final Map<String, IService> services = new HashMap<>();
    private static final Map<State, List<Runnable>> subscribers = new HashMap<>();
    private static final ILogger logger = Logger.get("Service Manager");
    @Contract(pure = true)
    private ServiceManager() {}

    /**
     * @return True if after NOT_INIT phase, after which registration of new services is not possible.
     */
    public static boolean afterRegistration() {
        return status != State.NOT_INIT;
    }

    /**
     * Used to get IService with a given name.
     * @throws NoSuchElementException if service under provided name doesn't exist.
     * @param name Name of the Service.
     * @return Requested IService object with specified name.
     * @apiNote Cast to original class is required to use the service.
     */
    public static @NotNull IService get(@NotNull String name) {
        if (!services.containsKey(name)) throw new NoSuchElementException("Service under name: " + name + " was not found!");
        return services.get(name);
    }

    /**
     * Used to get current status of Service Initialization.
     * @return Status of the ServiceManager.
     */
    public static State getStatus() {
        return status;
    }

    /**
     * Used to register the Service to Service Manager.
     * @throws IllegalStateException when the execution state is different from {@link State#NOT_INIT} or Service with specified name already is registered.
     * @param service Object implementing IService to register.
     */
    public static void registerService(IService service) {
        if (afterRegistration()) throw new IllegalStateException("You can't register new Services after initialization started!");
        if (Objects.isNull(service)) throw new NullPointerException("You can't register NULL Service!");
        if (services.containsKey(service.getName())) throw new IllegalStateException("You can't register already registered service %s!".formatted(service.getName()));

        services.put(service.getName(), service);
        logger.info("Registered service: " + service.getName());
    }

    /**
     * Used to subscribe to specified execution phase.
     * @param phase Phase to subscribe to
     * @param runnable Runnable to execute.
     * @apiNote Take a note that event subscribers will be executed after services in non-deterministic order.
     * Services are a recommended way to execute in a given phase. See {@link ServiceManager#registerService(IService)}.
     */
    public static void subscribe(State phase, Runnable runnable) {
        if (afterRegistration()) throw new IllegalStateException("You can't subscribe to the execution phases after initialization started!");
        if (phase == State.NOT_INIT)
            throw new IllegalStateException("NOT_INIT is not a valid execution phase!");
        subscribers.computeIfAbsent(phase, it -> new ArrayList<>()).add(Objects.requireNonNull(runnable, "Subscriber can't be null!"));
    }

    public static void runPreInit() {
        runStage(State.PRE_INIT, State.NOT_INIT);
    }

    public static void runInit() {
        runStage(State.INIT, State.PRE_INIT);
    }

    public static void runPostInit() {
        runStage(State.POST_INIT, State.INIT);
    }

    public static void runExit() {
        runStage(State.EXIT, null);
    }

    public static void runCrash() {
        runStage(State.CRASH, null, true);
    }

    private static void runStage(@NotNull State stage, @Nullable State requiredStage) {
        runStage(stage, requiredStage, false);
    }

    private static void runStage(@NotNull State stage, @Nullable State requiredStage, boolean ignoreExceptions) {
        if (Objects.nonNull(requiredStage) && status != requiredStage) throw new IllegalStateException("Can't run " + stage + " of services when status is " + status);
        status = stage;
        services.forEach((name, service) -> {
            if (!service.getPhases().contains(stage)) return;
            try {
                logger.info("Running phase \"" + stage + "\" of service: " + name);
                switch (stage) {
                    case PRE_INIT -> service.preInit();
                    case INIT -> service.init();
                    case POST_INIT -> service.postInit();
                    case EXIT -> service.exit();
                    case CRASH -> service.crash();
                    default -> throw new IllegalArgumentException("NOT_INIT can't be passed as Stage argument!");
                }
            } catch (Throwable e) {
                if (ignoreExceptions) {
                    logger.logStackTrace("Failed " + stage + " of service: " + name + "! This exception is being ignored, but it might cause issues later on!", e);
                } else {
                    throw new RuntimeException("Failed " + stage + " of service: " + name + "! Service Initialization exceptions are considered FATAL.", e);
                }
            }
        });
        logger.info(stage + " phase of Services finished.");

        if (subscribers.getOrDefault(stage, List.of()).isEmpty()) return;
        var subs = subscribers.get(stage);
        logger.info("Executing subscribers (%d) for execution phase \"%s\"...".formatted(subs.size(), stage));
        subs.forEach(run -> {
            try {
                run.run();
            } catch (Throwable ex) {
                if (ignoreExceptions) {
                    logger.logStackTrace("Failed execution of phase \"%s\" subscriber!".formatted(stage), ex);
                } else {
                    throw new RuntimeException("Failed execution of phase \"%s\" subscriber!".formatted(stage), ex);
                }
            }
        });
        logger.info("Finished execution of phase subscribers.");
    }
}

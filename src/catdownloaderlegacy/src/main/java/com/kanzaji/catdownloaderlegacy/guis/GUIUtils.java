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

package com.kanzaji.catdownloaderlegacy.guis;

import com.kanzaji.catdownloaderlegacy.loggers.LoggerCustom;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

/**
 * This class holds utility methods related to GUI.
 */
public class GUIUtils {
    private static final LoggerCustom logger = new LoggerCustom("GUI Utilities");

    /**
     * This method is used to set LookAndFeel of GUI's to the system one.
     */
    public static void setLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            logger.logStackTrace("Look And Feel not available! Going back to default.", e);
        }
    }

    private static int gWidth = 1;
    private static int gHeight = 1;
    /**
     * This method is used to update static variables for the Width and Height of the user display.
     */
    public static void updateResolutionInformation() {
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        gWidth = gd.getDisplayMode().getWidth();
        gHeight = gd.getDisplayMode().getHeight();
    }
    @Contract(pure = true)
    public static int getScreenWidth() {
        return gWidth;
    }
    @Contract(pure = true)
    public static int getScreenHeight() {
        return gHeight;
    }
    @Contract(value = " -> new", pure = true)
    public static @NotNull Dimension getScreenDimension() {
        return new Dimension(gWidth, gHeight);
    }
}

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

import com.kanzaji.catdownloaderlegacy.data.MRIndex;
import com.kanzaji.catdownloaderlegacy.loggers.LoggerCustom;
import com.kanzaji.catdownloaderlegacy.utils.RandomUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class MRSecurityCheckGUI {
    private static final LoggerCustom logger = new LoggerCustom("Modrinth Security Check");

    /**
     * This method is used to prompt the user about possibly malicious intent of the pack they are trying to install. Closes the app with 401 exit code.
     * @param mod Mod file that tripped the security check.
     * @param MRIndexData Modrinth Index Data of the pack that contained illegal mod file.
     */
    @Contract(value = "_,_ -> fail")
    public static void modrinthSecurityCheckFail(MRIndex.@NotNull MRModFile mod, @NotNull MRIndex MRIndexData) {
        //TODO: Create proper looking alert for the launcher version.
        logger.critical("Illegal path found! This pack might contain non-safe mods or files. Aborting installation due to security reasons!");

        String msg = """
                Illegal path has been found in the pack you are trying to install!
                This might signal that the pack or one of the mods has malicious intent, like installing malicious software, stealing sensitive data or doing harm to your PC!
                Installation process has been stopped for safety reasons.
                Below has been printed problematic file. If it doesn't look like an app error, please ask about details the moderation of the site the pack was acquired from.""";
        System.out.println(msg);

        logger.print(mod.toString(), 3);
        logger.print("Pack that was meant to be installed: " + MRIndexData.name + " " + MRIndexData.versionId);

        Container panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

        JLabel jl = new JLabel("Major security issue has been found in the pack you are trying to install!");
        jl.setFont(new Font(Font.DIALOG, Font.BOLD, 22));
        jl.setForeground(Color.RED);
        jl.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        panel.add(jl);

        jl = new JLabel("Installation process has been stopped for safety reasons.");
        jl.setFont(new Font(Font.DIALOG, Font.BOLD, 19));
        jl.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        panel.add(jl);
        panel.add(new JLabel(" "));

        Font textFont = new Font(Font.DIALOG, Font.PLAIN, 16);

        jl = new JLabel("This might signal that the pack or one of the mods has malicious intent, like installing malicious software, stealing sensitive data or doing harm to your PC!");
        jl.setFont(textFont);
        jl.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        panel.add(jl);

        jl = new JLabel("The problematic file that contains illegal path has been printed to the console and the log file, and should be inspected immediately.");
        jl.setFont(textFont);
        jl.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        panel.add(jl);

        jl = new JLabel("If it doesn't look like an app error, please ask about details the moderation of the site the pack was acquired from.");
        jl.setFont(textFont);
        jl.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        panel.add(jl);
        JOptionPane.showMessageDialog(null, panel, "CatDownloader Legacy - SECURITY ALERT", JOptionPane.PLAIN_MESSAGE);

        RandomUtils.closeTheApp(401);
    }
}

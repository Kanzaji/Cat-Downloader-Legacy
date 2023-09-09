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

import com.kanzaji.catdownloaderlegacy.ArgumentDecoder;
import com.kanzaji.catdownloaderlegacy.loggers.LoggerCustom;
import com.kanzaji.catdownloaderlegacy.Updater;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Path;
import java.util.Objects;

/**
 * This class holds all methods for starting and managing GUI for the {@link Updater}.
 * @see Updater
 * @see UpdaterGUI#startUpdateGUI()
 */
public class UpdaterGUI {
    private static final LoggerCustom logger = new LoggerCustom("Updater GUI");
    private static final JFrame MainFrame = new JFrame("Cat-Downloader Legacy Updater");
    private static JTextArea ChangelogText = null;
    private static JLabel UpdateText = null;
    private static JButton AskButton = null;
    private static JButton RemindButton = null;
    private static JButton UpdateButton = null;

    /**
     * This method setups GUI for the update screen.
     */
    public static void startUpdateGUI() {
        GUIUtils.updateResolutionInformation();
        int gWidth = GUIUtils.getScreenWidth();
        int gHeight = GUIUtils.getScreenHeight();

        logger.log("Starting Update GUI...");
        logger.log("Current resolution: " + gWidth + "x" + gHeight);

        Container panel = MainFrame.getContentPane();
        panel.setLayout(null);

        // Title
        JLabel title = new JLabel("New version of the Cat Downloader Legacy is available!");
        title.setBounds(0, gHeight/100, gWidth/2, (int) (gHeight/21.6));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setFont(new Font(Font.DIALOG, Font.BOLD, (int) (gHeight/43.2)));
        panel.add(title);

        // Version
        UpdateText = new JLabel("Current Version x.x.x ==> Latest Version y.y.y");
        UpdateText.setBounds(0, gHeight/25, gWidth/2, gHeight/27);
        UpdateText.setHorizontalAlignment(SwingConstants.CENTER);
        UpdateText.setFont(new Font(Font.DIALOG, Font.ITALIC, (int) (gHeight/67.5)));
        panel.add(UpdateText);

        // Changelog title
        JLabel changelog = new JLabel("Changelog");
        changelog.setBounds(gWidth/4 - (int) (gWidth/3.84)/2, gHeight/16, (int) (gWidth/3.84), gHeight/27);
        changelog.setHorizontalAlignment(SwingConstants.CENTER);
        changelog.setFont(new Font(Font.DIALOG, Font.BOLD, (int) (gHeight/67.5)));
        panel.add(changelog);

        // Changelog text
        ChangelogText = new JTextArea();
        ChangelogText.setText("Nothing here sadly :C");
        ChangelogText.setEditable(false);
        ChangelogText.setLineWrap(true);
        ChangelogText.setFont(new Font(Font.DIALOG, Font.PLAIN, gHeight/72));

        // Scrollable changelog
        JScrollPane scrollableTextArea = new JScrollPane(ChangelogText);
        scrollableTextArea.setBounds(gWidth/10, gHeight/10, gWidth/2 - gWidth/5, gHeight/2 - gHeight/5 - gHeight/50);
        panel.add(scrollableTextArea);

        // Buttons!
        // Don't ask again
        AskButton = new JButton("Don't ask again.");
        AskButton.setBounds(gWidth/4 - (int) (gWidth/9.6*1.5), gHeight/2 - gHeight/10, (int) (gWidth/9.6), gHeight/27);
        AskButton.setHorizontalAlignment(SwingConstants.CENTER);
        AskButton.setFont(new Font(Font.DIALOG, Font.ITALIC, gHeight/90));
        AskButton.setToolTipText("Click on this button to disable the Update checker! Check the start of the log file to see how to re-enable it.");
        panel.add(AskButton);

        // Remind me later
        RemindButton = new JButton("Remind me later.");
        RemindButton.setBounds(gWidth/4 - (int) (gWidth/9.6)/2, gHeight/2 - gHeight/10, (int) (gWidth/9.6), gHeight/27);
        RemindButton.setHorizontalAlignment(SwingConstants.CENTER);
        RemindButton.setFont(new Font(Font.DIALOG, Font.ITALIC, gHeight/90));
        RemindButton.setToolTipText("Click on this button to update the app Later. This prompt will not be shown in the next hour or until WorkingDirectory gets changed.");
        panel.add(RemindButton);

        // Update now!
        UpdateButton = new JButton("Update!");
        UpdateButton.setBounds(gWidth/4 + (int) (gWidth/9.6)/2, gHeight/2 - gHeight/10, (int) (gWidth/9.6), gHeight/27);
        UpdateButton.setHorizontalAlignment(SwingConstants.CENTER);
        UpdateButton.setFont(new Font(Font.DIALOG, Font.ITALIC, gHeight/90));
        UpdateButton.setToolTipText("Click on this button to update the app");
        panel.add(UpdateButton);

        logger.log("Main Frame ready! Making it visible.");
        panel.setBackground(new Color(0xffffffff, true));
        MainFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        MainFrame.setSize(gWidth/2, gHeight/2);
        MainFrame.setResizable(false);
        MainFrame.setIconImage(null);
        MainFrame.setLocationRelativeTo(null);
        MainFrame.setVisible(true);
    }

    /**
     * This method sets changelog text!
     * @param text Text to be displayed in the changelog box.
     * @throws NullPointerException when a Changelog object is null.
     */
    public static void setChangelogText(String text) {
        if (Objects.isNull(ChangelogText)) throw new NullPointerException("ChangelogText is null! Was GUI not started?");
        logger.log("Setting changelog text to: \"" + text + "\"");
        ChangelogText.setText(text);
    }

    /**
     * This method sets versions in the "Current to Latest" text.
     * @param currentVersion Current App Version.
     * @param latestVersion Latest App Version.
     * @throws NullPointerException if a Changelog object is null.
     */
    public static void setUpdateVersion(String currentVersion, String latestVersion) {
        if (Objects.isNull(UpdateText)) throw new NullPointerException("ChangelogText is null! Was GUI not started?");
        logger.log("Setting update text to: \"Current Version " + currentVersion + " ==> Latest Version " + latestVersion + "\"");
        UpdateText.setText("Current Version " + currentVersion + " ==> Latest Version " + latestVersion);
    }

    /**
     * This method setups the buttons in the Update screen.
     * @throws NullPointerException when Buttons objects are null.
     */
    public static void setupButtons() {
        if (Objects.isNull(AskButton) || Objects.isNull(RemindButton) || Objects.isNull(UpdateButton)) throw new NullPointerException("Buttons are null! Was GUI not started?");

        AskButton.addActionListener(actionEvent -> {
            disableButtons();
            MainFrame.setVisible(false);
            //TODO: Change this out for a custom dialog in the launcher version. Good enough for Legacy version
            if (Updater.disableUpdates()) {
                JOptionPane.showOptionDialog(
                        null,
                        "Updates have been permanently disabled!\n If you want to re-enable them, go to your settings file (" +
                                Path.of(ArgumentDecoder.getInstance().getSettingsPath()).toAbsolutePath() +
                                ") and change property \"Updater\" from false to true!",
                        "Cat-Downloader Legacy",
                        JOptionPane.DEFAULT_OPTION,
                        JOptionPane.INFORMATION_MESSAGE,
                        null,
                        null,
                        null
                );
            } else {
                JOptionPane.showOptionDialog(
                        null,
                        "For this to work, you have to use settings!\nTo manually disable Updates, use the argument \"-Updater:disable\" while launching the app.",
                        "Cat-Downloader Legacy",
                        JOptionPane.DEFAULT_OPTION,
                        JOptionPane.WARNING_MESSAGE,
                        null,
                        null,
                        null
                );
            }
            Updater.actionSelected = true;
        });

        RemindButton.addActionListener(actionEvent -> {
            disableButtons();
            Updater.actionSelected = true;
            MainFrame.setVisible(false);
        });

        UpdateButton.addActionListener(actionEvent -> {
            disableButtons();
            Updater.shouldUpdate = true;
            Updater.actionSelected = true;
            MainFrame.setVisible(false);
        });
    }

    private static void disableButtons() {
        if (Objects.isNull(AskButton) || Objects.isNull(RemindButton) || Objects.isNull(UpdateButton)) throw new NullPointerException("Buttons are null! Was GUI not started?");
        RemindButton.setEnabled(false);
        UpdateButton.setEnabled(false);
        AskButton.setEnabled(false);
    }
}

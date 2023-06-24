package com.kanzaji.catdownloaderlegacy.utils;

import com.kanzaji.catdownloaderlegacy.loggers.LoggerCustom;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Objects;

public class UpdaterGUI {
    private static final LoggerCustom logger = new LoggerCustom("GUI");
    private static JTextArea ChangelogText = null;
    private static JLabel UpdateText = null;
    private static JButton AskButton = null;
    private static JButton RemindButton = null;
    private static JButton UpdateButton = null;

    /**
     * This method setups GUI for the update screen.
     */
    public static void startGUI() {

        // Getting the screen resolution, so the app can scale itself with the resolution the user uses
        logger.log("Starting GUI...");
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        int gWidth = gd.getDisplayMode().getWidth();
        int gHeight = gd.getDisplayMode().getHeight();

        logger.log("Current resolution: " + gWidth + "x" + gHeight);

        JFrame frame = new JFrame("Cat-Downloader Legacy Updater");

        Container panel = frame.getContentPane();
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

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            logger.logStackTrace("Look And Feel not available! Going back to default.", e);
        }

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

        // Adding stuff to the frame
        logger.log("Frame ready! Making it visible.");
        frame.getContentPane().setBackground(new Color(0xffffffff, true));
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(gWidth/2, gHeight/2);
        frame.setResizable(false);
        frame.setIconImage(null);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    /**
     * This method sets changelog text!
     * @param text Text to be displayed in the changelog box.
     */
    public static void setChangelogText(String text) {
        if (Objects.isNull(ChangelogText)) {
            throw new NullPointerException("ChangelogText is null! Was GUI not started?");
        }
        logger.log("Setting changelog text to: \"" + text + "\"");
        ChangelogText.setText(text);
    }

    /**
     * This method sets versions in the "Current to Latest" text.
     * @param currentVersion Current App Version.
     * @param latestVersion Latest App Version.
     */
    public static void setUpdateVersion(String currentVersion, String latestVersion) {
        if (Objects.isNull(UpdateText)) {
            throw new NullPointerException("ChangelogText is null! Was GUI not started?");
        }
        logger.log("Setting update text to: \"Current Version " + currentVersion + " ==> Latest Version " + latestVersion + "\"");
        UpdateText.setText("Current Version " + currentVersion + " ==> Latest Version " + latestVersion);
    }

    /**
     * This method setups the buttons in the Update screen.
     */
    public static void setupButtons() {
        // TODO: Finish setupButtons() method.
        AskButton.addActionListener(actionEvent -> {

        });

        RemindButton.addActionListener(actionEvent -> {

        });

        UpdateButton.addActionListener(actionEvent -> {

        });
    }
}
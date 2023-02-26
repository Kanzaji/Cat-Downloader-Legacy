package com.kanzaji.catdownloader;

import javax.swing.*;

import com.kanzaji.catdownloader.utils.FileReader;

import java.awt.*;
import javax.imageio.ImageIO;

import java.io.IOException;
import java.io.InputStream;

public class GUI {

    public static void startGUI() {

        // Getting the screen resolution, so the app can scale itself with the resolution the user uses
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        int gWidth = gd.getDisplayMode().getWidth();
        int gHeight = gd.getDisplayMode().getHeight();

        JFrame frame = new JFrame("Cat-Downloader " + CatDownloader.VERSION);

        // Actual Content of the window!
        // Number in JLayeredPane.add(thing, int) is layer -> Higher number means higher
        JButton button;
        JLayeredPane panel = new JLayeredPane();
        GridBagConstraints c = new GridBagConstraints();
        panel.setLayout(new GridBagLayout());

        button = new JButton("Reload");
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 3;
        c.gridy = 3;
        c.gridwidth = 2;
        c.gridheight = 1;
        panel.add(button, c, 10);

        button = new JButton("TEST 2");
        c = resetGBC(c);
        c.gridx = 5;
        c.gridy = 5;
        panel.add(button, c, 11);

        c = resetGBC(c);

        try {
            InputStream file = FileReader.getFile("Assets/test.png");
            Image myPicture = ImageIO.read(file).getScaledInstance(gWidth/2, gHeight/2, Image.SCALE_SMOOTH); // This distorts the image a ton. But like... this is going to be a white background so.
            JLabel picLabel = new JLabel(new ImageIcon(myPicture));
            picLabel.setSize(new Dimension(gWidth/2, gHeight/2));
            c.anchor = GridBagConstraints.FIRST_LINE_START;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridx = 0;
            c.gridy = 0;
            c.gridwidth = 16;
            c.gridheight = 9;
            panel.add(picLabel, c, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Adding stuff to the frame or whatever
        frame.getContentPane().add(panel);

        frame.getContentPane().setBackground(new Color(0, 0, 0, 255));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(gWidth/2, gHeight/2);
        frame.setResizable(false);
        frame.setVisible(true);
        frame.setIconImage(null);
    }

    private static GridBagConstraints resetGBC(GridBagConstraints GBC) {

        GBC.ipadx = 0;
        GBC.ipady = 0;
        GBC.fill = 0;
        GBC.weightx = 0;
        GBC.weighty = 0;
        GBC.gridx = 0;
        GBC.gridy = 0;
        GBC.gridwidth = 0;
        GBC.gridheight = 0;
        GBC.insets = new Insets(0,0,0,0);
        GBC.anchor = GridBagConstraints.CENTER;

        return GBC;

    }
}

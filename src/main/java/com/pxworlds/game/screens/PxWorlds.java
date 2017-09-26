package com.pxworlds.game.screens;

import com.pxworlds.Bootstrap;
import com.pxworlds.configuration.screen.ScreenConfiguration;
import com.pxworlds.configuration.screen.ScreenResolutions;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class PxWorlds extends JFrame {

    public PxWorlds(String title, int width, int height, boolean fullscreen) {

        super(title);

        System.out.println("width: " + width + ", height: " + height + ", fullscreen: " + fullscreen);

        setLayout(new BorderLayout());
        add(new GamePanel(width, height, ScreenResolutions.getResolution(width, height).getScale()), BorderLayout.CENTER);
        pack();
        setSize(width, height);
        setResizable(false);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);


        if (fullscreen) {
            setExtendedState(JFrame.MAXIMIZED_BOTH);
            setUndecorated(true);
        }

        setVisible(true);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
                Dimension           size                = e.getComponent().getSize();
                ScreenConfiguration screenConfiguration = Bootstrap.getInstance().getConfigurationStorage().getScreenConfiguration().setLastHeight(((int) size.getHeight())).setLastWidth(((int) size.getWidth()));
                Bootstrap.getInstance().getConfigurationStorage().setScreenConfiguration(screenConfiguration);
            }
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                Bootstrap.getInstance().getConfigurationStorage().saveAllConfigurations();
            }
        });

    }


}

package com.pxworlds.screens;

import javax.swing.*;

public class PxWorlds extends JFrame {

    private String TITLE;
    private int WIDTH;
    private int HEIGHT;
    private boolean FULLSCREEN;

    public PxWorlds(String title, int width, int height, boolean fullscreen) {

        super(title);

        this.TITLE = title;
        this.WIDTH = width;
        this.HEIGHT = height;
        this.FULLSCREEN = fullscreen;

        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        if(fullscreen) {
            setExtendedState(JFrame.MAXIMIZED_BOTH);
            setUndecorated(true);
        }

        setVisible(true);

    }

}

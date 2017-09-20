package com.pxworlds;

import com.pxworlds.screens.PxWorlds;

import java.awt.*;

public class Main {

    private static final String  TITLE      = "PxWorlds - Let's Adventure!";
    private static       int     WIDTH      = 1920;
    private static       int     HEIGHT     = 1080;
    private static final boolean FULLSCREEN = true;

    public static void main(String[] args) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int       width      = (int) screenSize.getWidth();
        int       height     = (int) screenSize.getHeight();
        WIDTH = width;
        HEIGHT = height;

        new PxWorlds(TITLE, WIDTH, HEIGHT, FULLSCREEN);
    }

}

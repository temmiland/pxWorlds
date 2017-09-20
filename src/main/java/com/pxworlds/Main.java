package com.pxworlds;

import com.pxworlds.screens.pxWorlds;

public class Main {

    private static final String TITLE = "pxWorlds - Let's Adventure!";
    private static final int WIDTH = 1920;
    private static final int HEIGHT = 1080;
    private static final boolean FULLSCREEN = true;

    public static void main(String[] args){
        new pxWorlds(TITLE, WIDTH, HEIGHT, FULLSCREEN);
    }

}

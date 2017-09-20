package com.pxworlds;

import com.pxworlds.screens.PxWorlds;

public class Main {

    private static final String TITLE = "PxWorlds - Let's Adventure!";
    private static final int WIDTH = 1920;
    private static final int HEIGHT = 1080;
    private static final boolean FULLSCREEN = true;

    public static void main(String[] args){
        new PxWorlds(TITLE, WIDTH, HEIGHT, FULLSCREEN);
    }

}

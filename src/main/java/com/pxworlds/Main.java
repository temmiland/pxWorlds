package com.pxworlds;


import com.pxworlds.game.pxWorlds;

public class Main {

    private static final String applicationName = "pxWorlds - Let's adventure!";
    private static final int applicationWidth = 1280;
    private static final int applicationHeight = 720;

    public static void main(String[] args) {
        SharedLibraryLoader.load();
        new pxWorlds(applicationWidth, applicationHeight, applicationName).start();
    }

}
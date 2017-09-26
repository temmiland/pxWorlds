package com.pxworlds.configuration.screen;

/**
 * Created by tompi on 23.09.2017.
 */
public enum ScreenTypes {


    WINDOWED("Fenstermodus", 0),
    WINDOWED_FULLSCREEN("Fenstermodus ohne Rahmen", 1),
    FULLSCREEN("Vollbild", 2);

    private String displayName = "";
    private int screenType = 0;

    ScreenTypes(String displayName, int screenType) {
        this.displayName = displayName;
        this.screenType = screenType;
    }



}
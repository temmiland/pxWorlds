package com.pxworlds.configuration.screen;

/**
 * Created by temmiland on 23.09.2017.
 */
public enum ScreenTypes {

    /** Windowed screen type. */
    WINDOWED("Fenstermodus", 0),
    /** Windowed fullscreen screen type. */
    WINDOWED_FULLSCREEN("Fenstermodus ohne Rahmen", 1),
    /** Fullscreen screen type. */
    FULLSCREEN("Vollbild", 2);

    /** The display name of the screen type. */
    private String displayName = "";
    /** The integer value representing the screen type. */
    private int screenType = 0;

    ScreenTypes(String newDisplayName, int newScreenType) {
        this.displayName = newDisplayName;
        this.screenType = newScreenType;
    }



}

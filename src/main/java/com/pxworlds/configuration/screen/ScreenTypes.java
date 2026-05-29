package com.pxworlds.configuration.screen;

/**
 * Created by temmiland on 23.09.2017.
 */
public enum ScreenTypes {

    /** Fenstermodus. */
    WINDOWED("Fenstermodus", 0),
    /** Fenstermodus ohne Rahmen. */
    WINDOWED_FULLSCREEN("Fenstermodus ohne Rahmen", 1),
    /** Vollbild. */
    FULLSCREEN("Vollbild", 2);

    /** Anzeigename des Bildschirmtyps. */
    private String displayName = "";
    /** Ganzzahlige Repräsentation des Bildschirmtyps. */
    private int screenType = 0;

    ScreenTypes(String newDisplayName, int newScreenType) {
        this.displayName = newDisplayName;
        this.screenType = newScreenType;
    }



}

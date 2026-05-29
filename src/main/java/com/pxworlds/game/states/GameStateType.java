package com.pxworlds.game.states;

public enum GameStateType {
    /** Lade-/Splash-Screen. */
    BRAND(0),
    /** Hauptmenü. */
    MENU(1),
    /** Spiel-Level (aktive Welt). */
    LEVEL(2),
    /** Weltenauswahl. */
    WORLD_SELECT(3),
    /** Neue-Welt-Erstellung. */
    NEW_WORLD(4);

    /** Numerischer Index dieses Zustands. */
    private final int index;

    GameStateType(final int newIndex) {
        this.index = newIndex;
    }

    public int getIndex() {
        return index;
    }
}

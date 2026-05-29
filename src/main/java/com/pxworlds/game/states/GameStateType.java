package com.pxworlds.game.states;

public enum GameStateType {
    BRAND(0),
    MENU(1),
    LEVEL(2);

    private final int index;

    GameStateType(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }
}

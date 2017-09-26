package com.pxworlds.game.tilemap;


public enum ButtonType {

    START("Play"),
    QUIT("Quit");

    private String displayName = "";

    ButtonType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return this.displayName;
    }

}

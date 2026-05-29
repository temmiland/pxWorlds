package com.pxworlds.configuration.screen;

import com.pxworlds.configuration.Configuration;

public class ScreenConfiguration extends Configuration {

    /** Standard-Fensterbreite beim ersten Start. */
    private static final int DEFAULT_WIDTH = 1280;
    /** Standard-Fensterhöhe beim ersten Start. */
    private static final int DEFAULT_HEIGHT = 720;
    /** Standard-Bildwiederholrate. */
    private static final int DEFAULT_FRAME_RATE = 60;

    /** Ob die Anwendung im Vollbildmodus läuft. */
    private boolean fullscreen = false;
    /** Letzte gespeicherte Fensterbreite. */
    private int     lastWidth  = DEFAULT_WIDTH;
    /** Letzte gespeicherte Fensterhöhe. */
    private int     lastHeight = DEFAULT_HEIGHT;
    /** Ziel-Bildwiederholrate. */
    private int     frameRate  = DEFAULT_FRAME_RATE;

    /** No-Arg-Konstruktor — wird von Gson zur Deserialisierung und bei der Erstanlage genutzt. */
    public ScreenConfiguration() {
    }

    public boolean isFullscreen() {
        return fullscreen;
    }

    public ScreenConfiguration setFullscreen(boolean newFullscreen) {
        this.fullscreen = newFullscreen;
        return this;
    }

    public int getLastWidth() {
        return lastWidth;
    }

    public ScreenConfiguration setLastWidth(int newLastWidth) {
        this.lastWidth = newLastWidth;
        return this;
    }

    public int getLastHeight() {
        return lastHeight;
    }

    public ScreenConfiguration setLastHeight(int newLastHeight) {
        this.lastHeight = newLastHeight;
        return this;
    }

    public int getFrameRate() {
        return frameRate;
    }

    public ScreenConfiguration setFrameRate(int newFrameRate) {
        this.frameRate = newFrameRate;
        return this;
    }

    @Override
    public String toString() {
        return "ScreenConfiguration{" +
                "fullscreen=" + fullscreen +
                ", lastWidth=" + lastWidth +
                ", lastHeight=" + lastHeight +
                ", frameRate=" + frameRate +
                '}';
    }

}

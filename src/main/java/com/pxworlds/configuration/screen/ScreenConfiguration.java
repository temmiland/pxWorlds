package com.pxworlds.configuration.screen;

import com.pxworlds.configuration.Configuration;

public class ScreenConfiguration extends Configuration {

    /** Default window width used on first start. */
    private static final int DEFAULT_WIDTH = 1280;
    /** Default window height used on first start. */
    private static final int DEFAULT_HEIGHT = 720;
    /** The default frame rate. */
    private static final int DEFAULT_FRAME_RATE = 60;

    /** Whether the application is in fullscreen mode. */
    private boolean fullscreen = false;
    /** The last width of the screen. */
    private int     lastWidth  = DEFAULT_WIDTH;
    /** The last height of the screen. */
    private int     lastHeight = DEFAULT_HEIGHT;
    /** The frame rate of the application. */
    private int     frameRate  = DEFAULT_FRAME_RATE;

    /** No-arg constructor used by Gson for deserialization and first-time creation. */
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

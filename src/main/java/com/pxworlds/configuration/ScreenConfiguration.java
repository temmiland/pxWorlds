package com.pxworlds.configuration;

public class ScreenConfiguration extends Configuration {

    private boolean fullscreen = false;
    private int     lastWidth  = 0;
    private int     lastHeight = 0;

    public ScreenConfiguration() {
    }

    public boolean isFullscreen() {
        return fullscreen;
    }

    public ScreenConfiguration setFullscreen(boolean fullscreen) {
        this.fullscreen = fullscreen;
        return this;
    }

    public int getLastWidth() {
        return lastWidth;
    }

    public ScreenConfiguration setLastWidth(int lastWidth) {
        this.lastWidth = lastWidth;
        return this;
    }

    public int getLastHeight() {
        return lastHeight;
    }

    public ScreenConfiguration setLastHeight(int lastHeight) {
        this.lastHeight = lastHeight;
        return this;
    }
}

package com.pxworlds.configuration.screen;

import com.pxworlds.Bootstrap;
import com.pxworlds.configuration.Configuration;

public class ScreenConfiguration extends Configuration {

    private boolean fullscreen = false;
    private int     lastWidth  = ((int) Bootstrap.getInstance().getDimension().getWidth());
    private int     lastHeight = ((int) Bootstrap.getInstance().getDimension().getHeight());

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

    @Override
    public String toString() {
        return "ScreenConfiguration{" +
                "fullscreen=" + fullscreen +
                ", lastWidth=" + lastWidth +
                ", lastHeight=" + lastHeight +
                '}';
    }
}
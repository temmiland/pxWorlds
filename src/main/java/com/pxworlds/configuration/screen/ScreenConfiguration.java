package com.pxworlds.configuration.screen;

import com.pxworlds.Bootstrap;
import com.pxworlds.configuration.Configuration;

public class ScreenConfiguration extends Configuration {

    /** Whether the application is in fullscreen mode. */
    private boolean fullscreen = false;
    /** The last width of the screen. */
    private int     lastWidth  = ((int) Bootstrap.getInstance().getDimension().getWidth());
    /** The last height of the screen. */
    private int     lastHeight = ((int) Bootstrap.getInstance().getDimension().getHeight());
    /** The default frame rate. */
    private static final int DEFAULT_FRAME_RATE = 60;
    /** The frame rate of the application. */
    private int     frameRate = DEFAULT_FRAME_RATE;

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

    public int getFrameRate() {
        return frameRate;
    }

    public ScreenConfiguration setFrameRate(int frameRate) {
        this.frameRate = frameRate;
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

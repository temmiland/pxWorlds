package com.pxworlds.configuration.screen;

/**
 * Created by temmiland on 23.09.2017.
 */
public enum ScreenResolutions {

    /** 4096x2160 resolution. */
    R_4096_2160("4096x2160", 4096, 2160, 0),
    /** 3840x2160 resolution. */
    R_3840_2160("3840x2160", 3840, 2160, 0),
    /** 2560x1440 resolution. */
    R_2560_1600("2560x1440", 2560, 1440, 0),
    /** 2048x1536 resolution. */
    R_2048_1536("2048x1536", 2048, 1536, 0),
    /** 1920x1440 resolution. */
    R_1920_1440("1920x1440", 1920, 1440, 0),
    /** 1920x1200 resolution. */
    R_1920_1200("1920x1200", 1920, 1200, 0),
    /** 1920x1080 resolution. */
    R_1920_1080("1920x1080", 1920, 1080, 0),
    /** 1680x1050 resolution. */
    R_1680_1050("1680x1050", 1680, 1050, 0),
    /** 1600x1200 resolution. */
    R_1600_1200("1600x1200", 1600, 1200, 2),
    /** 1600x1024 resolution. */
    R_1600_1024("1600x1024", 1600, 1024, 0),
    /** 1600x900 resolution. */
    R_1600_900("1600x900", 1600, 900, 0),
    /** 1440x900 resolution. */
    R_1440_900("1440x900", 1440, 900, 0),
    /** 1366x768 resolution. */
    R_1366_768("1366x768", 1366, 768, 0),
    /** 1360x768 resolution. */
    R_1360_768("1360x768", 1360, 768, 0),
    /** 1280x1024 resolution. */
    R_1280_1024("1280x1024", 1280, 1024, 0),
    /** 1280x960 resolution. */
    R_1280_960("1280x960", 1280, 960, 0),
    /** 1280x800 resolution. */
    R_1280_800("1280x800", 1280, 800, 0),
    /** 1280x768 resolution. */
    R_1280_768("1280x768", 1280, 768, 0),
    /** 1280x720 resolution. */
    R_1280_720("1280x720", 1280, 720, 0),
    /** 1176x664 resolution. */
    R_1176_664("1176x664", 1176, 664, 0),
    /** 1152x864 resolution. */
    R_1152_864("1152x864", 1152, 864, 0),
    /** 1024x768 resolution. */
    R_1024_768("1024x768", 1024, 768, 0),
    /** 800x600 resolution. */
    R_800_600("800x600", 800, 600, 1);

    /** The display name of the resolution. */
    private String displayName = "";
    /** The width of the resolution. */
    private int width = 0;
    /** The height of the resolution. */
    private int height = 0;
    /** The scale factor for the resolution. */
    private double scale = 1;

    ScreenResolutions(String newDisplayName, int newWidth, int newHeight, double newScale) {
        this.displayName = newDisplayName;
        this.width = newWidth;
        this.height = newHeight;
        this.scale = newScale;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public double getScale() {
        return scale;
    }

    public static ScreenResolutions getResolution(int width, int height) {

        for(ScreenResolutions sr : ScreenResolutions.values()) {
            if(width == sr.getWidth()) {
                if(height == sr.getHeight()) {
                    return sr;
                }
            }
        }
        return null;
    }

}

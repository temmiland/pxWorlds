package com.pxworlds;

import com.pxworlds.util.OSHelperUtil;

import java.io.File;

public class Constants {

    /** The title of the application. */
    public static final String TITLE                 = "pxWorlds - Let's Adventure!";
    /** The name of the program directory. */
    public static final String PROGRAM_NAME          = ".pxworlds";
    /** The home directory path. */
    public static final String HOME_DIR              = OSHelperUtil.getHomeDirectory() + File.separator + PROGRAM_NAME + File.separator;
    /** The configuration directory path. */
    public static final String CONFIG_DIRECTORY_PATH = HOME_DIR + "config" + File.separator;
    /** The worlds directory path. */
    public static final String WORLDS_DIRECTORY_PATH = HOME_DIR + "worlds" + File.separator;
    /** The web root URL. */
    public static final String WEB_ROOT = "https://<API_URL>/pxworlds/";
    /** The web API root URL. */
    public static final String WEB_API_ROOT = WEB_ROOT + "api/";

    // --- Game Loop ---
    /** Maximum number of update ticks processed per frame.
     *  Caps accumulated time to prevent the spiral-of-death on lag spikes. */
    public static final int MAX_UPDATES_PER_FRAME = 10;

    // --- World / Tiles ---
    /** Size of one tile in world-space units.
     *  Used for tile rendering (TileRenderer) and entity/AABB positioning (World).
     *  Changing this value scales the entire world consistently. */
    public static final float TILE_SIZE = 2.0f;
}

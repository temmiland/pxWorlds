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
}

package com.pxworlds;

import com.pxworlds.util.OSHelperUtil;

import java.io.File;

public class Constants {

    public static final String TITLE                 = "pxWorlds - Let's Adventure!";
    public static final String PROGRAM_NAME          = ".pxworlds";
    public static final String HOME_DIR              = OSHelperUtil.getHomeDirectory() + File.separator + PROGRAM_NAME + File.separator;
    public static final String CONFIG_DIRECTORY_PATH = HOME_DIR + "config" + File.separator;
    public static final String WORLDS_DIRECTORY_PATH = HOME_DIR + "worlds" + File.separator;
    public static final String WEB_ROOT = "https://<API_URL>/pxworlds/";
    public static final String WEB_API_ROOT = WEB_ROOT + "api/";
}
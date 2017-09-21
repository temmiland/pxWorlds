package com.pxworlds;

import com.pxworlds.util.OSHelperUtil;

import java.io.File;

public class Constants {

    public static final String TITLE                 = "PxWorlds - Let's Adventure!";
    public static final String PROGRAM_NAME          = ".pxworlds";
    public static final String HOME_DIR              = OSHelperUtil.getHomeDirectory() + File.separator + PROGRAM_NAME + File.separator;
    public static final String CONFIG_DIRECTORY_PATH = HOME_DIR + "config" + File.separator;

}

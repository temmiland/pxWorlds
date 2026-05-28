package com.pxworlds.worlds;

import com.pxworlds.Constants;

import java.io.File;

public class WorldManager {

    public void init() {
        createWorldDirectoryIfNotExists();
    }

    private void createWorldDirectoryIfNotExists() {
        final File file = new File(Constants.WORLDS_DIRECTORY_PATH);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

}

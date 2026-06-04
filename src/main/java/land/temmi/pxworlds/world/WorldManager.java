package land.temmi.pxworlds.world;

import land.temmi.pxworlds.PxWorlds;

import java.io.File;

/**
 * Manages world-level infrastructure, such as ensuring the worlds directory
 * exists before the game attempts to read or write world files.
 */
public class WorldManager {

    /** Ensures all world infrastructure is ready. Must be called at game startup. */
    public void init() {
        createWorldDirectoryIfNotExists();
    }

    /** Creates the worlds directory if it does not yet exist. */
    private void createWorldDirectoryIfNotExists() {
        final File file = new File(PxWorlds.WORLD_DIRECTORY);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

}

package temmiland.pxworlds.game.world;

import temmiland.pxworlds.game.PxWorlds;

import java.io.File;

public class WorldManager {

    public void init() {
        createWorldDirectoryIfNotExists();
    }

    private void createWorldDirectoryIfNotExists() {
        final File file = new File(PxWorlds.WORLD_DIRECTORY);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

}

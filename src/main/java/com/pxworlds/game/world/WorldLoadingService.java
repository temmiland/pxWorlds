package com.pxworlds.game.world;

import com.pxworlds.Constants;
import com.pxworlds.game.rendering.Camera;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Handles loading and saving of {@link World} instances from/to {@code .pxwl} files.
 *
 * <p>Load priority:
 * <ol>
 *   <li>User file in {@link Constants#WORLDS_DIRECTORY_PATH} — contains last position.</li>
 *   <li>Classpath resource under {@code /levels/} — initial bundled level.</li>
 * </ol>
 */
public final class WorldLoadingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorldLoadingService.class);

    private WorldLoadingService() {
    }

    /**
     * Loads a world by name and spawns the player at the saved or spawn position.
     *
     * @param worldName   level name without extension (e.g. {@code "test_level"})
     * @param camera      camera to position at the player spawn
     * @param useLastPos  {@code true} to restore the last player position (LevelState);
     *                    {@code false} to always use the spawn tile (MenuState background)
     * @param context     class used for classpath resource lookup
     * @return loaded, fully initialised world
     * @throws RuntimeException if the level cannot be found or parsed
     */
    public static World load(final String worldName, final Camera camera,
                             final boolean useLastPos, final Class<?> context) {
        final File userFile = new File(Constants.WORLDS_DIRECTORY_PATH, worldName + ".pxwl");
        try {
            final InputStream stream = userFile.exists()
                    ? new FileInputStream(userFile)
                    : context.getResourceAsStream("/levels/" + worldName + ".pxwl");
            if (stream == null) {
                throw new IOException("Kein .pxwl-Stream verfügbar für '" + worldName + "'");
            }
            try (InputStream s = stream) {
                final World world = WorldIO.load(s);
                world.spawnPlayer(camera, useLastPos);
                LOGGER.info("Level '{}' geladen (Lastpos={}, SpawnTile={}/{})",
                        worldName, world.hasLastPos(),
                        world.getSpawnTileX(), world.getSpawnTileY());
                return world;
            }
        } catch (IOException e) {
            throw new RuntimeException("Kann Level '" + worldName + "' nicht laden", e);
        }
    }

    /**
     * Saves the world to {@link Constants#WORLDS_DIRECTORY_PATH} as {@code <worldName>.pxwl},
     * capturing the current player position beforehand.
     *
     * <p>Does nothing if {@code world} is {@code null}.</p>
     *
     * @param world     the world to save; may be {@code null}
     * @param worldName level name without extension
     */
    public static void save(final World world, final String worldName) {
        if (world == null) {
            return;
        }
        final File dir = new File(Constants.WORLDS_DIRECTORY_PATH);
        dir.mkdirs();
        final File file = new File(dir, worldName + ".pxwl");
        world.capturePlayerPosition();
        try (FileOutputStream fos = new FileOutputStream(file)) {
            WorldIO.save(world, fos);
            LOGGER.info("Welt gespeichert: {}", file.getAbsolutePath());
        } catch (IOException e) {
            LOGGER.error("Fehler beim Speichern der Welt '{}'", worldName, e);
        }
    }
}

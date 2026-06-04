package land.temmi.pxworlds.world;

import land.temmi.pxworlds.PxWorlds;
import land.temmi.rollercoaster.graphics.Camera;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Loads and saves {@link World} instances from/to {@code .pxwl} files.
 *
 * <p>Load order:
 * <ol>
 *   <li>User file in {@link PxWorlds#WORLD_DIRECTORY} — contains the last position.</li>
 *   <li>Classpath resource under {@code /levels/} — initial bundled level.</li>
 * </ol>
 */
public final class WorldLoadingService {

    /** Logger of this class. */
    private static final Logger LOGGER = LoggerFactory.getLogger(WorldLoadingService.class);

    private WorldLoadingService() {
    }

    /**
     * Loads a world by name and places the player at the saved or spawn position.
     *
     * @param worldName   level name without file extension (e.g. {@code "test_level"})
     * @param camera      camera that is positioned to the player spawn
     * @param useLastPos  {@code true} to restore the last player position (LevelState);
     *                    {@code false} to always use the spawn tile (menu background)
     * @param context     class for classpath resource access
     * @return the loaded, fully initialized world
     * @throws RuntimeException if the level cannot be found or parsed
     */
    public static World load(final String worldName, final Camera camera,
                             final boolean useLastPos, final Class<?> context) {
        final File userFile = new File(PxWorlds.WORLD_DIRECTORY, worldName + ".pxwl");
        try {
            final InputStream stream;
            if (userFile.exists()) {
                stream = new FileInputStream(userFile);
            } else {
                stream = context.getResourceAsStream("/levels/" + worldName + ".pxwl");
            }
            if (stream == null) {
                throw new IOException("No .pxwl stream available for '" + worldName + "'");
            }
            try (InputStream s = stream) {
                final World world = WorldReader.load(s);
                world.spawnPlayer(camera, useLastPos);
                LOGGER.info("Level '{}' loaded (lastPos={}, spawnTile={}/{})",
                        worldName, world.hasLastPos(),
                        world.getSpawnTileX(), world.getSpawnTileY());
                return world;
            }
        } catch (IOException e) {
            throw new RuntimeException("Cannot load level '" + worldName + "'", e);
        }
    }

    /** Name of the bundled template for new worlds. */
    private static final String TEMPLATE_WORLD = "test_level";
    /** Default width of a new fallback world in tiles. */
    private static final int DEFAULT_WORLD_WIDTH = 64;
    /** Default height of a new fallback world in tiles. */
    private static final int DEFAULT_WORLD_HEIGHT = 20;
    /** Default scale of a new fallback world. */
    private static final int DEFAULT_WORLD_SCALE = 16;
    /** Default spawn X for the fallback world. */
    private static final int DEFAULT_SPAWN_X = 32;
    /** Default spawn Y for the fallback world. */
    private static final int DEFAULT_SPAWN_Y = 10;

    /**
     * Creates a new world as a copy of the bundled template ({@value #TEMPLATE_WORLD}).
     *
     * <p>The template is loaded from the classpath and saved under the new name.
     * The spawn point and tiles of the template are preserved. If the template
     * is missing, an empty 64×20 world is created as a fallback.</p>
     *
     * @param worldName world name without file extension
     */
    public static void createNew(final String worldName) {
        try (InputStream s = WorldLoadingService.class
                .getResourceAsStream("/levels/" + TEMPLATE_WORLD + ".pxwl")) {
            if (s != null) {
                final World world = WorldReader.load(s);
                save(world, worldName);
                LOGGER.info("Created new world '{}' from template '{}'.", worldName, TEMPLATE_WORLD);
                return;
            }
        } catch (IOException e) {
            LOGGER.warn("Could not load template '{}', creating empty world: {}",
                    TEMPLATE_WORLD, e.getMessage());
        }
        // Fallback: empty world with spawn in the center
        final World fallback = new World(DEFAULT_WORLD_WIDTH, DEFAULT_WORLD_HEIGHT, DEFAULT_WORLD_SCALE);
        fallback.setSpawn(DEFAULT_SPAWN_X, DEFAULT_SPAWN_Y);
        save(fallback, worldName);
        LOGGER.info("Created new empty world '{}' (fallback).", worldName);
    }

    /**
     * Saves the world to {@link PxWorlds#WORLD_DIRECTORY} as {@code <worldName>.pxwl},
     * first capturing the current player position.
     *
     * <p>Has no effect if {@code world} is {@code null}.</p>
     *
     * @param world     the world to save; may be {@code null}
     * @param worldName level name without file extension
     */
    public static void save(final World world, final String worldName) {
        if (world == null) {
            return;
        }
        final File dir = new File(PxWorlds.WORLD_DIRECTORY);
        dir.mkdirs();
        final File file = new File(dir, worldName + ".pxwl");
        world.capturePlayerPosition();
        try (FileOutputStream fos = new FileOutputStream(file)) {
            WorldWriter.save(world, fos);
            LOGGER.info("World saved: {}", file.getAbsolutePath());
        } catch (IOException e) {
            LOGGER.error("Failed to save world '{}'", worldName, e);
        }
    }
}

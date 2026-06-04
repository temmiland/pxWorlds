package land.temmi.pxworlds.world.migration;

import land.temmi.pxworlds.PxWorlds;
import land.temmi.pxworlds.world.World;
import land.temmi.pxworlds.world.WorldReader;
import land.temmi.pxworlds.world.WorldWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Migrates worlds of format versions 1–3 to version 4.
 *
 * <p>The migration loads the world via {@link WorldReader} (without spawning a
 * player) and saves it again — {@link WorldWriter#save} automatically writes the
 * current game version string and timestamp. The spawn point and last position
 * from the original file are preserved.</p>
 */
public final class MigrationV3ToV4 implements WorldMigration {

    /** Logger of this class. */
    private static final Logger LOGGER = LoggerFactory.getLogger(MigrationV3ToV4.class);

    /** Maximum format version that this migration reads. */
    private static final int MAX_SUPPORTED_INPUT_VERSION = 3;

    @Override
    public boolean supports(final int formatVersion) {
        return formatVersion >= 1 && formatVersion <= MAX_SUPPORTED_INPUT_VERSION;
    }

    @Override
    public void apply(final String worldName) {
        final File file = new File(PxWorlds.WORLD_DIRECTORY, worldName + ".pxwl");
        LOGGER.info("Migrating '{}' from v1-v3 to v4 …", worldName);

        final World world;
        try (FileInputStream fis = new FileInputStream(file)) {
            world = WorldReader.load(fis);
        } catch (IOException e) {
            throw new RuntimeException("Cannot load world '" + worldName + "' for migration", e);
        }

        // No capturePlayerPosition() — no player entity in the world,
        // so the loaded lastPos (from v3) is preserved unchanged.
        try (FileOutputStream fos = new FileOutputStream(file)) {
            WorldWriter.save(world, fos);
        } catch (IOException e) {
            throw new RuntimeException("Cannot save world '" + worldName + "' after migration", e);
        }
        LOGGER.info("Migration '{}' completed.", worldName);
    }
}

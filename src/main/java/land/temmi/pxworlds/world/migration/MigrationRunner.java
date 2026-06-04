package land.temmi.pxworlds.world.migration;

import land.temmi.pxworlds.PxWorlds;
import land.temmi.pxworlds.world.WorldMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

/**
 * Runs the appropriate {@link WorldMigration} for a given {@link WorldMeta}.
 *
 * <p>Known migrations (in order):
 * <ol>
 *   <li>{@link MigrationV3ToV4} — migrates format versions 1–3 to version 4</li>
 * </ol>
 * </p>
 */
public final class MigrationRunner {

    /** Logger of this class. */
    private static final Logger LOGGER = LoggerFactory.getLogger(MigrationRunner.class);

    /** All known migrations in order. */
    private static final List<WorldMigration> MIGRATIONS = List.of(
            new MigrationV3ToV4()
    );

    private MigrationRunner() { }

    /**
     * Runs the first matching migration for the given world.
     *
     * <p>Before the migration a backup of the original file is automatically
     * created as {@code <worldname>_backup.pxwl}. An existing backup file is
     * overwritten. If the backup fails, the migration still proceeds.</p>
     *
     * @param meta metadata of the world to migrate
     * @throws IllegalStateException if no migration was found for the format version
     */
    public static void run(final WorldMeta meta) {
        if (!meta.needsMigration()) {
            return;
        }
        for (final WorldMigration migration : MIGRATIONS) {
            if (migration.supports(meta.formatVersion())) {
                backup(meta.name());
                migration.apply(meta.name());
                return;
            }
        }
        LOGGER.error("No migration for format version {} (world '{}')",
                meta.formatVersion(), meta.name());
        throw new IllegalStateException(
                "No migration for format version " + meta.formatVersion());
    }

    /**
     * Copies {@code <worldname>.pxwl} to {@code <basename>_backup.pxwl}.
     *
     * <p>For worlds whose name already ends with {@code _backup} no backup is
     * created — they are themselves already a backup. An existing backup file is
     * overwritten.</p>
     *
     * @param worldName name of the world to back up (without file extension)
     */
    private static void backup(final String worldName) {
        if (worldName.endsWith("_backup")) {
            LOGGER.info("No backup created for backup world '{}'.", worldName);
            return;
        }
        final String backupName = worldName + "_backup";
        final File source = new File(PxWorlds.WORLD_DIRECTORY, worldName + ".pxwl");
        final File target = new File(PxWorlds.WORLD_DIRECTORY, backupName + ".pxwl");
        try {
            Files.copy(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
            LOGGER.info("Backup created: {}", target.getName());
        } catch (IOException e) {
            LOGGER.warn("Backup for '{}' failed, continuing migration: {}",
                    worldName, e.getMessage());
        }
    }
}

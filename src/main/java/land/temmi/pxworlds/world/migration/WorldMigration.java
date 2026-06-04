package land.temmi.pxworlds.world.migration;

/**
 * Strategy for migrating a world file from an older format version to the
 * current version.
 *
 * <p>Implementations load the world, patch missing fields and save it in the
 * current format. The {@link MigrationRunner} selects the appropriate
 * implementation based on the source version.</p>
 */
public interface WorldMigration {

    /**
     * Returns whether this migration handles the given source version.
     *
     * @param formatVersion format version of the file to migrate
     * @return {@code true} if this implementation can migrate the version
     */
    boolean supports(int formatVersion);

    /**
     * Migrates the world file with the given name.
     * Loads, patches and overwrites the file in the new format.
     *
     * @param worldName world name without {@code .pxwl} extension
     * @throws RuntimeException on I/O error or invalid format
     */
    void apply(String worldName);
}

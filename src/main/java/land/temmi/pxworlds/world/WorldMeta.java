package land.temmi.pxworlds.world;

/**
 * Lightweight metadata of a world — read from the GZIP header without fully
 * loading the tile data (see {@link WorldMetaReader}).
 *
 * @param name            world name (file name without {@code .pxwl})
 * @param formatVersion   format version of the file (1–{@link WorldFormat#FORMAT_VERSION})
 * @param gameVersion     game version at the time of the last save;
 *                        {@code null} for worlds before v4
 * @param lastPlayed      timestamp of the last save (Unix ms);
 *                        falls back to the file mtime for worlds before v4
 * @param needsMigration  {@code true} if {@code formatVersion < FORMAT_VERSION}
 */
public record WorldMeta(
        String  name,
        int     formatVersion,
        String  gameVersion,
        long    lastPlayed,
        boolean needsMigration
) { }

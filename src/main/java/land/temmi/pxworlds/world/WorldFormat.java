package land.temmi.pxworlds.world;

/**
 * Constants that define the {@code .pxwl} binary format.
 *
 * <h3>Format specification (version 4)</h3>
 * <pre>
 *  Offset        Length  Type     Content
 *  ────────────  ──────  ───────  ──────────────────────────────────────────────
 *             0       4  byte[]   Magic "PXWL"
 *             4       1  byte     Format version (4)
 *             5       2  short    World width in tiles
 *             7       2  short    World height in tiles
 *             9       2  short    Scale (pixel multiplier)
 *            11   2+n    String   Game version (Java-UTF: 2-byte length + UTF-8 bytes)
 *         11+2+n      8  long     Timestamp of the last save (Unix ms)
 *  19+n       W × H  byte[]   Tile ids, row by row (y=0..H-1, x=0..W-1)
 *                                  0xFF (= byte -1) → air / no tile
 *                                  0x00..0xFE      → registered tile id
 *  …              2  short    Spawn tile X (−1 = no spawn set)
 *  …              2  short    Spawn tile Y (−1 = no spawn set)
 *  …              4  float    Last player X position (NaN = not set)
 *  …              4  float    Last player Y position (NaN = not set)
 * </pre>
 *
 * <h3>Backward compatibility</h3>
 * <ul>
 *   <li><b>Version 1</b> — tiles only; spawn and last position remain unset.</li>
 *   <li><b>Version 2</b> — tiles + spawn shorts; last position remains unset.</li>
 *   <li><b>Version 3</b> — tiles + spawn + last position.</li>
 *   <li><b>Version 4</b> — like v3, plus game version string and last-played timestamp
 *       (between dimensions and tiles) → header-only reader possible.</li>
 * </ul>
 *
 * <p>The entire content is GZIP-compressed.</p>
 */
public final class WorldFormat {

    /** Magic bytes for format identification. */
    public static final byte[] MAGIC = {'P', 'X', 'W', 'L'};
    /** Current format version. Increment on incompatible changes. */
    public static final byte FORMAT_VERSION = 4;
    /** Byte value representing air / no tile. */
    static final byte AIR = (byte) -1;

    private WorldFormat() { }
}

package land.temmi.pxworlds.world;

import land.temmi.pxworlds.tiles.Tile;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;

/**
 * Loads a {@link World} from a GZIP-compressed {@code .pxwl} binary stream.
 *
 * <p>Older versions are loaded with backward compatibility — missing fields
 * remain at their default value ({@link World#NO_SPAWN}, {@link Float#NaN}).</p>
 *
 * <p>See {@link WorldFormat} for the format specification.</p>
 */
public final class WorldReader {

    /** Oldest still-readable format version. */
    private static final byte MIN_SUPPORTED_VERSION = 1;
    /** Minimum version with spawn point data. */
    private static final byte MIN_VERSION_SPAWN = 2;
    /** Minimum version with saved player position. */
    private static final byte MIN_VERSION_LAST_POS = 3;
    /** Short value for "no spawn set". */
    private static final short NO_SPAWN = (short) World.NO_SPAWN;

    private WorldReader() { }

    /**
     * Reads a world from the given stream.
     *
     * @param in source stream
     * @return the loaded world
     * @throws IOException on I/O error or invalid format
     */
    public static World load(final InputStream in) throws IOException {
        try (GZIPInputStream gzip = new GZIPInputStream(in);
             DataInputStream data = new DataInputStream(gzip)) {

            final byte[] magic = new byte[WorldFormat.MAGIC.length];
            data.readFully(magic);
            if (!Arrays.equals(magic, WorldFormat.MAGIC)) {
                throw new IOException("Invalid world file: magic bytes do not match.");
            }
            final byte version = data.readByte();
            if (version < MIN_SUPPORTED_VERSION || version > WorldFormat.FORMAT_VERSION) {
                throw new IOException(
                        "Unsupported format version: " + version
                        + " (supported: " + MIN_SUPPORTED_VERSION
                        + "-" + WorldFormat.FORMAT_VERSION + ")");
            }

            final int width  = data.readShort();
            final int height = data.readShort();
            final int scale  = data.readShort();

            if (version >= WorldFormat.FORMAT_VERSION) {
                data.readUTF();  // gameVersion
                data.readLong(); // lastPlayed
            }

            final World world = new World(width, height, scale);

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    final byte id = data.readByte();
                    if (id == WorldFormat.AIR) {
                        continue;
                    }
                    final int idx = Byte.toUnsignedInt(id);
                    if (idx < Tile.getTiles().length && Tile.getTiles()[idx] != null) {
                        world.setTile(Tile.getTiles()[idx], x, y);
                    }
                }
            }

            if (version >= MIN_VERSION_SPAWN) {
                final int spawnX = data.readShort();
                final int spawnY = data.readShort();
                if (spawnX != NO_SPAWN && spawnY != NO_SPAWN) {
                    world.setSpawn(spawnX, spawnY);
                }
            }

            if (version >= MIN_VERSION_LAST_POS) {
                final float lastX = data.readFloat();
                final float lastY = data.readFloat();
                if (!Float.isNaN(lastX) && !Float.isNaN(lastY)) {
                    world.setLastPos(lastX, lastY);
                }
            }

            return world;
        }
    }
}

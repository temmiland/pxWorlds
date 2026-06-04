package land.temmi.pxworlds.world;

import land.temmi.pxworlds.Version;
import land.temmi.pxworlds.tiles.Tile;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Saves a {@link World} to a GZIP-compressed {@code .pxwl} binary stream.
 *
 * <p>Before calling, {@link World#capturePlayerPosition()} should be invoked
 * so that the current player position is stored in the world.</p>
 *
 * <p>See {@link WorldFormat} for the format specification.</p>
 */
public final class WorldWriter {

    private WorldWriter() { }

    /**
     * Writes the world to the given stream.
     *
     * <p>The stream is not closed after writing — the caller is responsible for
     * lifecycle management.</p>
     *
     * @param world the world to save
     * @param out   target stream (wrapped by GZIPOutputStream)
     * @throws IOException on I/O error
     */
    public static void save(final World world, final OutputStream out) throws IOException {
        try (GZIPOutputStream gzip = new GZIPOutputStream(out);
             DataOutputStream data = new DataOutputStream(gzip)) {

            data.write(WorldFormat.MAGIC);
            data.writeByte(WorldFormat.FORMAT_VERSION);
            data.writeShort(world.getWidth());
            data.writeShort(world.getHeight());
            data.writeShort(world.getScale());

            data.writeUTF(Version.GAME);
            data.writeLong(System.currentTimeMillis());

            for (int y = 0; y < world.getHeight(); y++) {
                for (int x = 0; x < world.getWidth(); x++) {
                    final Tile tile = world.getTile(x, y);
                    if (tile != null) {
                        data.writeByte(tile.getId());
                    } else {
                        data.writeByte(WorldFormat.AIR);
                    }
                }
            }

            data.writeShort(world.getSpawnTileX());
            data.writeShort(world.getSpawnTileY());

            data.writeFloat(world.getLastSavedPosX());
            data.writeFloat(world.getLastSavedPosY());
        }
    }
}

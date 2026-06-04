package land.temmi.pxworlds.world;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.zip.GZIPInputStream;

/**
 * Reads only the header of a {@code .pxwl} file (without tile data) and returns
 * a {@link WorldMeta} object.
 *
 * <p>For format versions &lt; 4, {@code gameVersion = null} and
 * {@code lastPlayed = file.lastModified()} are used as fallbacks.</p>
 */
public final class WorldMetaReader {

    /** Logger of this class. */
    private static final Logger LOGGER = LoggerFactory.getLogger(WorldMetaReader.class);

    private WorldMetaReader() { }

    /**
     * Reads metadata from a {@code .pxwl} file.
     *
     * @param file the world file
     * @return {@link WorldMeta}, or {@link Optional#empty()} on read error or invalid format
     */
    public static Optional<WorldMeta> read(final File file) {
        try (GZIPInputStream gzip = new GZIPInputStream(new FileInputStream(file));
             DataInputStream data = new DataInputStream(gzip)) {

            final byte[] magic = new byte[WorldFormat.MAGIC.length];
            data.readFully(magic);
            if (!Arrays.equals(magic, WorldFormat.MAGIC)) {
                LOGGER.warn("Invalid magic bytes in {}", file.getName());
                return Optional.empty();
            }

            final int version = Byte.toUnsignedInt(data.readByte());
            if (version < 1 || version > WorldFormat.FORMAT_VERSION) {
                LOGGER.warn("Unsupported format version {} in {}", version, file.getName());
                return Optional.empty();
            }

            data.readShort(); // width  — not needed for metadata
            data.readShort(); // height — not needed for metadata
            data.readShort(); // scale  — not needed for metadata

            final String gameVersion;
            final long lastPlayed;
            if (version >= WorldFormat.FORMAT_VERSION) {
                gameVersion = data.readUTF();
                lastPlayed  = data.readLong();
            } else {
                gameVersion = null;
                lastPlayed  = file.lastModified();
            }

            final String name = stripExtension(file.getName());
            final boolean needsMigration = version < WorldFormat.FORMAT_VERSION;
            return Optional.of(new WorldMeta(name, version, gameVersion, lastPlayed, needsMigration));

        } catch (IOException e) {
            LOGGER.warn("Cannot read metadata of '{}': {}", file.getName(), e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Strips the file extension from a filename.
     *
     * @param filename the file name (e.g. {@code "world.pxwl"})
     * @return the name without extension, or the unchanged name if there is no dot
     */
    private static String stripExtension(final String filename) {
        final int dot = filename.lastIndexOf('.');
        final String result;
        if (dot > 0) {
            result = filename.substring(0, dot);
        } else {
            result = filename;
        }
        return result;
    }
}

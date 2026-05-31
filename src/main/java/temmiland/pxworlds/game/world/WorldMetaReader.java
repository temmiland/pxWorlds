package temmiland.pxworlds.game.world;

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
 * Liest nur den Header einer {@code .pxwl}-Datei (ohne Tile-Daten)
 * und gibt ein {@link WorldMeta}-Objekt zurück.
 *
 * <p>Für Format-Versionen &lt; 4 werden {@code gameVersion = null} und
 * {@code lastPlayed = file.lastModified()} als Fallback verwendet.</p>
 */
public final class WorldMetaReader {

    /** Logger dieser Klasse. */
    private static final Logger LOGGER = LoggerFactory.getLogger(WorldMetaReader.class);

    private WorldMetaReader() { }

    /**
     * Liest Metadaten aus einer {@code .pxwl}-Datei.
     *
     * @param file die Weltdatei
     * @return {@link WorldMeta} oder {@link Optional#empty()} bei Lesefehler oder ungültigem Format
     */
    public static Optional<WorldMeta> read(final File file) {
        try (GZIPInputStream gzip = new GZIPInputStream(new FileInputStream(file));
             DataInputStream data = new DataInputStream(gzip)) {

            final byte[] magic = new byte[WorldIO.MAGIC.length];
            data.readFully(magic);
            if (!Arrays.equals(magic, WorldIO.MAGIC)) {
                LOGGER.warn("Ungültige Magic-Bytes in {}", file.getName());
                return Optional.empty();
            }

            final int version = Byte.toUnsignedInt(data.readByte());
            if (version < 1 || version > WorldIO.FORMAT_VERSION) {
                LOGGER.warn("Nicht unterstützte Format-Version {} in {}", version, file.getName());
                return Optional.empty();
            }

            data.readShort(); // width  – für Metadaten nicht benötigt
            data.readShort(); // height – für Metadaten nicht benötigt
            data.readShort(); // scale  – für Metadaten nicht benötigt

            final String gameVersion;
            final long lastPlayed;
            if (version >= WorldIO.FORMAT_VERSION) {
                gameVersion = data.readUTF();
                lastPlayed  = data.readLong();
            } else {
                gameVersion = null;
                lastPlayed  = file.lastModified();
            }

            final String name = stripExtension(file.getName());
            final boolean needsMigration = version < WorldIO.FORMAT_VERSION;
            return Optional.of(new WorldMeta(name, version, gameVersion, lastPlayed, needsMigration));

        } catch (IOException e) {
            LOGGER.warn("Kann Metadaten von '{}' nicht lesen: {}", file.getName(), e.getMessage());
            return Optional.empty();
        }
    }

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

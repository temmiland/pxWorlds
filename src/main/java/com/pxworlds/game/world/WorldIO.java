package com.pxworlds.game.world;

import com.pxworlds.game.rendering.tiles.Tile;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Serialisiert und deserialisiert Welten in einem kompakten GZIP-Binärformat.
 *
 * <h3>Format-Spezifikation (Version 3)</h3>
 * <pre>
 *  Offset        Länge  Typ      Inhalt
 *  ────────────  ─────  ───────  ──────────────────────────────────────────────
 *             0      4  byte[]   Magic "PXWL"
 *             4      1  byte     Format-Version (3)
 *             5      2  short    Weltbreite in Tiles
 *             7      2  short    Welthöhe in Tiles
 *             9      2  short    Scale (Pixel-Multiplikator)
 *            11  W × H  byte[]   Tile-IDs, Zeilenweise (y=0..H-1, x=0..W-1)
 *                                  0xFF (= byte -1) → Luft / kein Tile
 *                                  0x00..0xFE      → registrierte Tile-ID
 *  11 + W×H       2  short    Spawn-Tile-X (−1 = kein Spawn gesetzt)
 *  13 + W×H       2  short    Spawn-Tile-Y (−1 = kein Spawn gesetzt)
 *  15 + W×H       4  float    letzte Spieler-X-Position (NaN = nicht gesetzt)
 *  19 + W×H       4  float    letzte Spieler-Y-Position (NaN = nicht gesetzt)
 * </pre>
 *
 * <h3>Rückwärtskompatibilität</h3>
 * <ul>
 *   <li><b>Version 1</b> — nur Tiles; Spawn und Letztposition bleiben ungesetzt.</li>
 *   <li><b>Version 2</b> — Tiles + Spawn-Shorts; Letztposition bleibt ungesetzt.</li>
 *   <li><b>Version 3</b> — Tiles + Spawn + Letztposition (aktuelles Format).</li>
 * </ul>
 *
 * <p>Der gesamte Inhalt ist GZIP-komprimiert.</p>
 *
 * <h3>Hinweise für Future-Proofing</h3>
 * <ul>
 *   <li>Tile-IDs müssen zwischen Speichern und Laden identisch registriert sein.</li>
 *   <li>Weitere Entity-Typen: Version auf 4 erhöhen und eine Entity-Sektion anhängen.</li>
 * </ul>
 */
public final class WorldIO {

    /** Magic bytes zur Formatidentifikation. */
    private static final byte[] MAGIC = {'P', 'X', 'W', 'L'};
    /** Aktuelle Format-Version. Erhöhen bei inkompatiblen Änderungen. */
    private static final byte FORMAT_VERSION = 3;
    /** Älteste noch lesbare Format-Version. */
    private static final byte MIN_SUPPORTED_VERSION = 1;
    /** Byte-Wert der für Luft / kein Tile steht. */
    private static final byte AIR = (byte) -1;
    /** Short-Wert für „kein Spawn gesetzt". */
    private static final short NO_SPAWN = (short) World.NO_SPAWN;

    /** Statische Utility-Klasse — nicht instanziierbar. */
    private WorldIO() { }

    // -------------------------------------------------------------------------

    /**
     * Speichert Tile-Daten, Spawn-Punkt und Letztposition der Welt.
     *
     * <p>Vor dem Aufruf sollte {@link World#capturePlayerPosition()} aufgerufen
     * werden, damit die aktuelle Spielerposition in der Welt hinterlegt ist.</p>
     *
     * <p>Der Stream wird nach dem Schreiben nicht geschlossen — der Aufrufer
     * ist für das Lifecycle-Management verantwortlich.</p>
     *
     * @param world  die zu speichernde Welt
     * @param out    Ziel-Stream (wird von GZIPOutputStream gewrapped)
     * @throws IOException bei I/O-Fehler
     */
    public static void save(final World world, final OutputStream out) throws IOException {
        try (GZIPOutputStream gzip = new GZIPOutputStream(out);
             DataOutputStream data = new DataOutputStream(gzip)) {

            data.write(MAGIC);
            data.writeByte(FORMAT_VERSION);
            data.writeShort(world.getWidth());
            data.writeShort(world.getHeight());
            data.writeShort(world.getScale());

            for (int y = 0; y < world.getHeight(); y++) {
                for (int x = 0; x < world.getWidth(); x++) {
                    final Tile tile = world.getTile(x, y);
                    data.writeByte(tile != null ? tile.getId() : AIR);
                }
            }

            // Version 2: Spawn-Punkt
            data.writeShort(world.getSpawnTileX());
            data.writeShort(world.getSpawnTileY());

            // Version 3: Letztposition (Float.NaN = nicht gesetzt)
            data.writeFloat(world.getLastSavedPosX());
            data.writeFloat(world.getLastSavedPosY());
        }
    }

    /**
     * Lädt eine Welt aus einem GZIP-komprimierten Stream.
     *
     * <p>Ältere Versionen werden rückwärtskompatibel geladen — fehlende Felder
     * bleiben auf ihrem Standardwert ({@link World#NO_SPAWN}, {@link Float#NaN}).</p>
     *
     * @param in  Quell-Stream
     * @return    geladene Welt
     * @throws IOException bei I/O-Fehler oder ungültigem Format
     */
    public static World load(final InputStream in) throws IOException {
        try (GZIPInputStream gzip = new GZIPInputStream(in);
             DataInputStream data = new DataInputStream(gzip)) {

            // Header validieren
            final byte[] magic = new byte[MAGIC.length];
            data.readFully(magic);
            if (!Arrays.equals(magic, MAGIC)) {
                throw new IOException(
                        "Ungültige Welt-Datei: Magic-Bytes stimmen nicht überein.");
            }
            final byte version = data.readByte();
            if (version < MIN_SUPPORTED_VERSION || version > FORMAT_VERSION) {
                throw new IOException(
                        "Nicht unterstützte Format-Version: " + version
                        + " (unterstützt: " + MIN_SUPPORTED_VERSION
                        + "–" + FORMAT_VERSION + ")");
            }

            final int width  = data.readShort();
            final int height = data.readShort();
            final int scale  = data.readShort();

            final World world = new World(width, height, scale);

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    final byte id = data.readByte();
                    if (id == AIR) {
                        continue;
                    }
                    final int idx = Byte.toUnsignedInt(id);
                    if (idx < Tile.getTiles().length && Tile.getTiles()[idx] != null) {
                        world.setTile(Tile.getTiles()[idx], x, y);
                    }
                }
            }

            // Version 2+: Spawn-Punkt
            if (version >= 2) {
                final int spawnX = data.readShort();
                final int spawnY = data.readShort();
                if (spawnX != NO_SPAWN && spawnY != NO_SPAWN) {
                    world.setSpawn(spawnX, spawnY);
                }
            }

            // Version 3+: Letztposition
            if (version >= 3) {
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

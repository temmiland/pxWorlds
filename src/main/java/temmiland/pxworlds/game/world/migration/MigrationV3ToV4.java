package temmiland.pxworlds.game.world.migration;

import temmiland.pxworlds.Constants;
import temmiland.pxworlds.game.PxWorlds;
import temmiland.pxworlds.game.world.World;
import temmiland.pxworlds.game.world.WorldIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Migriert Welten der Format-Versionen 1–3 auf Version 4.
 *
 * <p>Die Migration lädt die Welt via {@link WorldIO} (ohne Spieler-Spawn)
 * und speichert sie direkt neu – {@link WorldIO#save} schreibt dabei
 * automatisch {@link Constants#VERSION} und den aktuellen Zeitstempel.
 * Spawn-Punkt und Letztposition aus der Originaldatei bleiben erhalten.</p>
 */
public final class MigrationV3ToV4 implements WorldMigration {

    /** Logger dieser Klasse. */
    private static final Logger LOGGER = LoggerFactory.getLogger(MigrationV3ToV4.class);

    /** Maximale Format-Version die diese Migration einliest. */
    private static final int MAX_SUPPORTED_INPUT_VERSION = 3;

    @Override
    public boolean supports(final int formatVersion) {
        return formatVersion >= 1 && formatVersion <= MAX_SUPPORTED_INPUT_VERSION;
    }

    @Override
    public void apply(final String worldName) {
        final File file = new File(PxWorlds.WORLD_DIRECTORY, worldName + ".pxwl");
        LOGGER.info("Migriere '{}' von v1-v3 → v4 …", worldName);

        final World world;
        try (FileInputStream fis = new FileInputStream(file)) {
            world = WorldIO.load(fis);
        } catch (IOException e) {
            throw new RuntimeException("Kann Welt '" + worldName + "' für Migration nicht laden", e);
        }

        // Kein capturePlayerPosition() – kein Player-Entity in der Welt,
        // daher bleibt die geladene lastPos (aus v3) unverändert erhalten.
        try (FileOutputStream fos = new FileOutputStream(file)) {
            WorldIO.save(world, fos);
        } catch (IOException e) {
            throw new RuntimeException("Kann Welt '" + worldName + "' nach Migration nicht speichern", e);
        }
        LOGGER.info("Migration '{}' abgeschlossen.", worldName);
    }
}

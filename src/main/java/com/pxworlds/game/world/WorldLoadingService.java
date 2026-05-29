package com.pxworlds.game.world;

import com.pxworlds.Constants;
import com.pxworlds.game.rendering.Camera;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Lädt und speichert {@link World}-Instanzen aus/in {@code .pxwl}-Dateien.
 *
 * <p>Ladereihenfolge:
 * <ol>
 *   <li>Benutzerdatei in {@link Constants#WORLDS_DIRECTORY_PATH} — enthält letzte Position.</li>
 *   <li>Classpath-Ressource unter {@code /levels/} — initiales gebündeltes Level.</li>
 * </ol>
 */
public final class WorldLoadingService {

    /** Logger dieser Klasse. */
    private static final Logger LOGGER = LoggerFactory.getLogger(WorldLoadingService.class);

    private WorldLoadingService() {
    }

    /**
     * Lädt eine Welt nach Name und platziert den Spieler an der gespeicherten oder Spawn-Position.
     *
     * @param worldName   Level-Name ohne Dateiendung (z.B. {@code "test_level"})
     * @param camera      Kamera, die auf den Spieler-Spawn positioniert wird
     * @param useLastPos  {@code true} um die letzte Spielerposition wiederherzustellen (LevelState);
     *                    {@code false} um immer das Spawn-Tile zu verwenden (MenuState-Hintergrund)
     * @param context     Klasse für den Classpath-Ressourcenzugriff
     * @return geladene, vollständig initialisierte Welt
     * @throws RuntimeException wenn das Level nicht gefunden oder geparst werden kann
     */
    public static World load(final String worldName, final Camera camera,
                             final boolean useLastPos, final Class<?> context) {
        final File userFile = new File(Constants.WORLDS_DIRECTORY_PATH, worldName + ".pxwl");
        try {
            final InputStream stream;
            if (userFile.exists()) {
                stream = new FileInputStream(userFile);
            } else {
                stream = context.getResourceAsStream("/levels/" + worldName + ".pxwl");
            }
            if (stream == null) {
                throw new IOException("Kein .pxwl-Stream verfügbar für '" + worldName + "'");
            }
            try (InputStream s = stream) {
                final World world = WorldIO.load(s);
                world.spawnPlayer(camera, useLastPos);
                LOGGER.info("Level '{}' geladen (Lastpos={}, SpawnTile={}/{})",
                        worldName, world.hasLastPos(),
                        world.getSpawnTileX(), world.getSpawnTileY());
                return world;
            }
        } catch (IOException e) {
            throw new RuntimeException("Kann Level '" + worldName + "' nicht laden", e);
        }
    }

    /** Name der gebündelten Vorlage für neue Welten. */
    private static final String TEMPLATE_WORLD = "test_level";
    /** Standardbreite einer neuen Fallback-Welt in Tiles. */
    private static final int DEFAULT_WORLD_WIDTH = 64;
    /** Standardhöhe einer neuen Fallback-Welt in Tiles. */
    private static final int DEFAULT_WORLD_HEIGHT = 20;
    /** Standard-Scale einer neuen Fallback-Welt. */
    private static final int DEFAULT_WORLD_SCALE = 16;
    /** Standard-Spawn-X für die Fallback-Welt. */
    private static final int DEFAULT_SPAWN_X = 32;
    /** Standard-Spawn-Y für die Fallback-Welt. */
    private static final int DEFAULT_SPAWN_Y = 10;

    /**
     * Erstellt eine neue Welt als Kopie der gebündelten Vorlage ({@value #TEMPLATE_WORLD}).
     *
     * <p>Die Vorlage wird aus dem Classpath geladen und unter dem neuen Namen gespeichert.
     * Spawn-Punkt und Tiles der Vorlage bleiben erhalten.
     * Fällt die Vorlage weg, wird eine leere 64×20-Welt als Fallback erstellt.</p>
     *
     * @param worldName Weltname ohne Dateiendung
     */
    public static void createNew(final String worldName) {
        try (InputStream s = WorldLoadingService.class
                .getResourceAsStream("/levels/" + TEMPLATE_WORLD + ".pxwl")) {
            if (s != null) {
                final World world = WorldIO.load(s);
                save(world, worldName);
                LOGGER.info("Neue Welt '{}' aus Vorlage '{}' erstellt.", worldName, TEMPLATE_WORLD);
                return;
            }
        } catch (IOException e) {
            LOGGER.warn("Vorlage '{}' konnte nicht geladen werden, erstelle leere Welt: {}",
                    TEMPLATE_WORLD, e.getMessage());
        }
        // Fallback: leere Welt mit Spawn in der Mitte
        final World fallback = new World(DEFAULT_WORLD_WIDTH, DEFAULT_WORLD_HEIGHT, DEFAULT_WORLD_SCALE);
        fallback.setSpawn(DEFAULT_SPAWN_X, DEFAULT_SPAWN_Y);
        save(fallback, worldName);
        LOGGER.info("Neue leere Welt '{}' (Fallback) erstellt.", worldName);
    }

    /**
     * Speichert die Welt nach {@link Constants#WORLDS_DIRECTORY_PATH} als {@code <worldName>.pxwl},
     * dabei wird zuerst die aktuelle Spielerposition gespeichert.
     *
     * <p>Hat keinen Effekt wenn {@code world} {@code null} ist.</p>
     *
     * @param world     die zu speichernde Welt; kann {@code null} sein
     * @param worldName Level-Name ohne Dateiendung
     */
    public static void save(final World world, final String worldName) {
        if (world == null) {
            return;
        }
        final File dir = new File(Constants.WORLDS_DIRECTORY_PATH);
        dir.mkdirs();
        final File file = new File(dir, worldName + ".pxwl");
        world.capturePlayerPosition();
        try (FileOutputStream fos = new FileOutputStream(file)) {
            WorldIO.save(world, fos);
            LOGGER.info("Welt gespeichert: {}", file.getAbsolutePath());
        } catch (IOException e) {
            LOGGER.error("Fehler beim Speichern der Welt '{}'", worldName, e);
        }
    }
}

package com.pxworlds;

import com.pxworlds.util.OSHelperUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Constants {

    /** Aktuelle Spielversion, aus pom.xml via Filtered Resources ermittelt. */
    public static final String VERSION               = loadVersion();
    /** {@code true} wenn das Spiel nicht aus einer JAR gestartet wurde (d.h. via mvnw/IDE). */
    public static final boolean DEV_MODE             = detectDevMode();

    private static boolean detectDevMode() {
        final java.net.URL url = Constants.class.getResource("Constants.class");
        return url == null || !"jar".equals(url.getProtocol());
    }
    /** Titel der Anwendung. */
    public static final String TITLE                 = "pxWorlds - Let's Adventure!";
    /** Name des Programmverzeichnisses. */
    public static final String PROGRAM_NAME          = ".pxworlds";
    /** Pfad zum Home-Verzeichnis. */
    public static final String HOME_DIR              = OSHelperUtil.getHomeDirectory() + File.separator + PROGRAM_NAME + File.separator;
    /** Pfad zum Konfigurationsverzeichnis. */
    public static final String CONFIG_DIRECTORY_PATH = HOME_DIR + "config" + File.separator;
    /** Pfad zum Welten-Verzeichnis. */
    public static final String WORLDS_DIRECTORY_PATH = HOME_DIR + "worlds" + File.separator;
    /** Web-Root-URL. */
    public static final String WEB_ROOT = "https://<API_URL>/pxworlds/";
    /** Web-API-Root-URL. */
    public static final String WEB_API_ROOT = WEB_ROOT + "api/";

    // --- Spielschleife ---
    /** Feste Logik-Update-Rate in Ticks pro Sekunde.
     *  Das Rendering läuft unabhängig davon mit der konfigurierten FPS. */
    public static final int TICK_RATE = 20;
    /** Maximale Anzahl an Update-Ticks pro Frame.
     *  Begrenzt akkumulierte Zeit, um die Spiral-of-Death bei Lag-Spikes zu verhindern. */
    public static final int MAX_UPDATES_PER_FRAME = 10;

    // --- Welt / Tiles ---
    /** Größe eines Tiles in Weltkoordinaten.
     *  Wird für Tile-Rendering (TileRenderer) und Entity/AABB-Positionierung (World) verwendet.
     *  Ändert man diesen Wert, skaliert die gesamte Welt konsistent. */
    public static final float TILE_SIZE = 2.0f;

    /** Anzahl Tiles pro Chunk-Seite (Chunks sind CHUNK_SIZE × CHUNK_SIZE Tiles).
     *  Wird vom Chunk-System und der prozeduralen Weltgenerierung verwendet. */
    public static final int CHUNK_SIZE = 16;

    private static String loadVersion() {
        final Properties properties = new Properties();
        try (InputStream input = Constants.class.getResourceAsStream("/build.properties")) {
            if (input == null) {
                return "dev";
            }
            properties.load(input);
            return properties.getProperty("app.version", "dev");
        } catch (IOException e) {
            return "dev";
        }
    }
}

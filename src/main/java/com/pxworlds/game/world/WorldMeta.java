package com.pxworlds.game.world;

/**
 * Leichtgewichtige Metadaten einer Welt — wird ohne vollständiges Laden
 * der Tile-Daten aus dem GZIP-Header gelesen (siehe {@link WorldMetaReader}).
 *
 * @param name            Weltname (Dateiname ohne {@code .pxwl})
 * @param formatVersion   Format-Version der Datei (1–{@link WorldIO#FORMAT_VERSION})
 * @param gameVersion     Spielversion beim letzten Speichern; {@code null} für Welten vor v4
 * @param lastPlayed      Zeitstempel des letzten Speicherns (Unix ms);
 *                        Fallback auf Datei-mtime für Welten vor v4
 * @param needsMigration  {@code true} wenn {@code formatVersion < FORMAT_VERSION}
 */
public record WorldMeta(
        String  name,
        int     formatVersion,
        String  gameVersion,
        long    lastPlayed,
        boolean needsMigration
) { }

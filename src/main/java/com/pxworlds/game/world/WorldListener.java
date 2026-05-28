package com.pxworlds.game.world;

/**
 * Listener für Tile-Änderungen in der Welt.
 *
 * <p>Registrierte Listener werden von {@link World} benachrichtigt sobald
 * Tiles gesetzt werden. Der erste Anwendungsfall ist das Dirty-Flag des
 * Chunk-Systems — künftige Anwendungen: Lighting, Sound, Pathfinding-Cache.</p>
 */
public interface WorldListener {

    /**
     * Wird aufgerufen wenn ein einzelnes Tile geändert wurde.
     *
     * @param x  globale Tile-X-Koordinate
     * @param y  globale Tile-Y-Koordinate
     */
    void tileChanged(int x, int y);

    /**
     * Wird aufgerufen wenn die gesamte Welt neu geladen oder generiert wurde
     * (z.B. beim Laden einer gespeicherten Welt oder nach prozeduraler Generierung).
     * Alle Caches / Dirty-Flags sollten vollständig invalidiert werden.
     */
    void worldReloaded();
}

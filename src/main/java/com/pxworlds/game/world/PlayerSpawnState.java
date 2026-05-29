package com.pxworlds.game.world;

/**
 * Speichert den Spawn-Punkt und die zuletzt gespeicherte Position des Spielers in einer Welt.
 *
 * <p>Beide Werte werden von {@link WorldIO} nach dem Laden geschrieben und von
 * {@link World#spawnPlayer} und {@link World#capturePlayerPosition} gelesen.</p>
 */
public final class PlayerSpawnState {

    /** Sentinel-Wert für „kein Spawn gesetzt“. */
    public static final int NO_SPAWN = -1;

    /** Spawn-Tile-X-Koordinate oder {@value #NO_SPAWN}. */
    private int   spawnTileX    = NO_SPAWN;
    /** Spawn-Tile-Y-Koordinate oder {@value #NO_SPAWN}. */
    private int   spawnTileY    = NO_SPAWN;
    /** Zuletzt gespeicherte X-Position im World-Space (NaN = nicht gesetzt). */
    private float lastSavedPosX = Float.NaN;
    /** Zuletzt gespeicherte Y-Position im World-Space (NaN = nicht gesetzt). */
    private float lastSavedPosY = Float.NaN;

    /** @return Spawn-Tile-X oder {@value #NO_SPAWN} wenn nicht gesetzt */
    public int getSpawnTileX() {
        return spawnTileX;
    }

    /** @return Spawn-Tile-Y oder {@value #NO_SPAWN} wenn nicht gesetzt */
    public int getSpawnTileY() {
        return spawnTileY;
    }

    /**
     * Setzt die Spawn-Tile-Koordinaten.
     * @param tileX Spawn-Tile-X
     * @param tileY Spawn-Tile-Y
     */
    public void setSpawn(final int tileX, final int tileY) {
        this.spawnTileX = tileX;
        this.spawnTileY = tileY;
    }

    /** @return zuletzt gespeicherte X-Position im World-Space, oder {@link Float#NaN} wenn nicht gesetzt */
    public float getLastSavedPosX() {
        return lastSavedPosX;
    }

    /** @return zuletzt gespeicherte Y-Position im World-Space, oder {@link Float#NaN} wenn nicht gesetzt */
    public float getLastSavedPosY() {
        return lastSavedPosY;
    }

    /** @return {@code true} wenn eine zuletzt gespeicherte Position vorhanden ist */
    public boolean hasLastPos() {
        return !Float.isNaN(lastSavedPosX);
    }

    /**
     * Setzt die zuletzt gespeicherte Spielerposition im World-Space.
     * @param x X-Position im World-Space
     * @param y Y-Position im World-Space
     */
    public void setLastPos(final float x, final float y) {
        this.lastSavedPosX = x;
        this.lastSavedPosY = y;
    }
}

package com.pxworlds.game.world;

/**
 * Holds the spawn point and last-saved position of the player within a world.
 *
 * <p>Both pieces of data are written by {@link WorldIO} after loading and read by
 * {@link World#spawnPlayer} and {@link World#capturePlayerPosition}.</p>
 */
public final class PlayerSpawnState {

    /** Sentinel value meaning "no spawn set". */
    public static final int NO_SPAWN = -1;

    private int   spawnTileX    = NO_SPAWN;
    private int   spawnTileY    = NO_SPAWN;
    private float lastSavedPosX = Float.NaN;
    private float lastSavedPosY = Float.NaN;

    /** @return spawn tile X or {@value #NO_SPAWN} if not set */
    public int getSpawnTileX() { return spawnTileX; }

    /** @return spawn tile Y or {@value #NO_SPAWN} if not set */
    public int getSpawnTileY() { return spawnTileY; }

    /** Sets the spawn tile coordinates. */
    public void setSpawn(final int tileX, final int tileY) {
        this.spawnTileX = tileX;
        this.spawnTileY = tileY;
    }

    /** @return last saved X position in world space, or {@link Float#NaN} if not set */
    public float getLastSavedPosX() { return lastSavedPosX; }

    /** @return last saved Y position in world space, or {@link Float#NaN} if not set */
    public float getLastSavedPosY() { return lastSavedPosY; }

    /** @return {@code true} if a last-saved position has been stored */
    public boolean hasLastPos() { return !Float.isNaN(lastSavedPosX); }

    /** Sets the last-saved player position in world space. */
    public void setLastPos(final float x, final float y) {
        this.lastSavedPosX = x;
        this.lastSavedPosY = y;
    }
}

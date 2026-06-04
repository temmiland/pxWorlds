package land.temmi.pxworlds.world;

/**
 * Stores the spawn point and the last saved position of the player in a world.
 *
 * <p>Both values are written by {@link WorldReader} after loading and read by
 * {@link World#spawnPlayer} and {@link World#capturePlayerPosition}.</p>
 */
public final class PlayerWorldPosition {

    /** Sentinel value for "no spawn set". */
    public static final int NO_SPAWN = -1;

    /** Spawn tile X coordinate, or {@value #NO_SPAWN}. */
    private int   spawnTileX    = NO_SPAWN;
    /** Spawn tile Y coordinate, or {@value #NO_SPAWN}. */
    private int   spawnTileY    = NO_SPAWN;
    /** Last saved X position in world space (NaN = not set). */
    private float lastSavedPosX = Float.NaN;
    /** Last saved Y position in world space (NaN = not set). */
    private float lastSavedPosY = Float.NaN;

    /** @return spawn tile X, or {@value #NO_SPAWN} if not set */
    public int getSpawnTileX() {
        return spawnTileX;
    }

    /** @return spawn tile Y, or {@value #NO_SPAWN} if not set */
    public int getSpawnTileY() {
        return spawnTileY;
    }

    /**
     * Sets the spawn tile coordinates.
     * @param tileX spawn tile X
     * @param tileY spawn tile Y
     */
    public void setSpawn(final int tileX, final int tileY) {
        this.spawnTileX = tileX;
        this.spawnTileY = tileY;
    }

    /** @return last saved X position in world space, or {@link Float#NaN} if not set */
    public float getLastSavedPosX() {
        return lastSavedPosX;
    }

    /** @return last saved Y position in world space, or {@link Float#NaN} if not set */
    public float getLastSavedPosY() {
        return lastSavedPosY;
    }

    /** @return {@code true} if a last saved position is available */
    public boolean hasLastPos() {
        return !Float.isNaN(lastSavedPosX);
    }

    /**
     * Sets the last saved player position in world space.
     * @param x X position in world space
     * @param y Y position in world space
     */
    public void setLastPos(final float x, final float y) {
        this.lastSavedPosX = x;
        this.lastSavedPosY = y;
    }
}

package land.temmi.pxworlds.world;

/**
 * Listener for tile changes in the world.
 *
 * <p>Registered listeners are notified by {@link World} whenever tiles are set.
 * The primary use case is the dirty flag of the chunk system; future uses
 * include lighting, sound and pathfinding caches.</p>
 */
public interface WorldListener {

    /**
     * Called when a single tile has been changed.
     *
     * @param x global tile X coordinate
     * @param y global tile Y coordinate
     */
    void tileChanged(int x, int y);

    /**
     * Called when the entire world has been reloaded or generated (e.g. when
     * loading a saved world or after procedural generation). All caches and
     * dirty flags should be fully invalidated.
     */
    void worldReloaded();
}

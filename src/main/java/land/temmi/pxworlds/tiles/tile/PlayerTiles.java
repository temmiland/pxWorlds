package land.temmi.pxworlds.tiles.tile;

import land.temmi.pxworlds.tiles.Tile;

/** Registered tiles for all player animation frames. */
public final class PlayerTiles {

    // Idle animation frames
    /** Player idle frame 0. */
    public static final Tile IDLE_0 = new Tile("player/idle/0", false);
    /** Player idle frame 1. */
    public static final Tile IDLE_1 = new Tile("player/idle/1", false);
    /** Player idle frame 2. */
    public static final Tile IDLE_2 = new Tile("player/idle/2", false);
    /** Player idle frame 3. */
    public static final Tile IDLE_3 = new Tile("player/idle/3", false);

    // Walking animation frames
    /** Player walking frame 0. */
    public static final Tile WALK_0 = new Tile("player/walking/0", false);
    /** Player walking frame 1. */
    public static final Tile WALK_1 = new Tile("player/walking/1", false);
    /** Player walking frame 2. */
    public static final Tile WALK_2 = new Tile("player/walking/2", false);
    /** Player walking frame 3. */
    public static final Tile WALK_3 = new Tile("player/walking/3", false);

    private PlayerTiles() { }
}

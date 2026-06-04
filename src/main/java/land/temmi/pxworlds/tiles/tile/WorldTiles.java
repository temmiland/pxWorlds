package land.temmi.pxworlds.tiles.tile;

import land.temmi.pxworlds.tiles.Tile;

/** Registered tiles for all world (chunk-stored) tile types. */
public final class WorldTiles {

    /** Walkable grass tile. */
    public static final Tile GRASS = new Tile("grass",  false);
    /** Solid brick tile. */
    public static final Tile BRICK = new Tile("bricks", true);
    /** Solid dark brick tile. */
    public static final Tile DARK_BRICK = new Tile("dark_bricks", true);

    private WorldTiles() { }
}

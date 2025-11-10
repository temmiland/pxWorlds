package com.pxworlds.game.rendering.tiles;

import com.pxworlds.game.rendering.tiles.tile.Brick;
import com.pxworlds.game.rendering.tiles.tile.Grass;

public class Tile {
	/** The maximum number of tiles. */
	public static final int MAX_TILES = 255;
	/** The starting ID for tiles. */
	public static final byte START_ID = 0;

	/** The array of all tiles. */
	public static Tile[] tiles = new Tile[MAX_TILES];
	/** The next available tile ID. */
	public static byte not = START_ID;

	/** The grass tile instance. */
	public static final Tile GRASS = new Grass();
	/** The brick tile instance. */
	public static final Tile BRICK = new Brick();

	/** The unique ID of the tile. */
	private byte id;
	/** Whether the tile is solid. */
	private boolean solid;
	/** The texture name of the tile. */
	private String texture;

	public Tile(String newTexture, boolean newSolid) {
		this.id = not;
		not++;
		this.texture = newTexture;
		this.solid = newSolid;
		if (tiles[id] != null) {
			throw new IllegalStateException("Tiles at [" + id + "] is already being used!");
		}
		tiles[id] = this;
	}

	public Tile setSolid() {
		this.solid = true;
		return this;
	}

	public boolean isSolid() {
		return solid;
	}

	public byte getId() {
		return id;
	}

	public String getTexture() {
		return texture;
	}

    public static Tile[] getTiles() {
        return tiles;
    }
}

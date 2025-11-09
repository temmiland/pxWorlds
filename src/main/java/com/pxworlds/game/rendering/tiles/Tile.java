package com.pxworlds.game.rendering.tiles;

import com.pxworlds.game.rendering.tiles.tile.Brick;
import com.pxworlds.game.rendering.tiles.tile.Grass;

public class Tile {
	public static Tile[] tiles = new Tile[255];
	public static byte not = 0;

	public static final Tile GRASS = new Grass();
	public static final Tile BRICK = new Brick();

	/** The unique ID of the tile. */
	private byte id;
	/** Whether the tile is solid. */
	private boolean solid;
	/** The texture name of the tile. */
	private String texture;

	public Tile(String texture, boolean solid) {
		this.id = not;
		not++;
		this.texture = texture;
		this.solid = solid;
		if (tiles[id] != null) throw new IllegalStateException("Tiles at [" + id + "] is already being used!");
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

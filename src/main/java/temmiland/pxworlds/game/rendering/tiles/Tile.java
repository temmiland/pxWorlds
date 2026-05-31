package temmiland.pxworlds.game.rendering.tiles;

import temmiland.pxworlds.game.rendering.tiles.tile.Brick;
import temmiland.pxworlds.game.rendering.tiles.tile.Grass;

public class Tile {
	/** Maximale Anzahl an Tiles. */
	public static final int MAX_TILES = 255;
	/** Start-ID für Tiles. */
	public static final byte START_ID = 0;

	/** Registry aller Tiles, indiziert durch ID. */
	private static final Tile[] TILES = new Tile[MAX_TILES];
	/** Nächste freie Tile-ID. */
	private static byte not = START_ID;

	/** Gras-Tile. */
	public static final Tile GRASS = new Grass();
	/** Backstein-Tile. */
	public static final Tile BRICK = new Brick();

	/** Eindeutige ID dieses Tiles. */
	private byte id;
	/** Ob dieses Tile solide (nicht begehbar) ist. */
	private boolean solid;
	/** Texturpfad dieses Tiles. */
	private String texture;

	public Tile(String newTexture, boolean newSolid) {
		this.id = not;
		not++;
		this.texture = newTexture;
		this.solid = newSolid;
		if (TILES[id] != null) {
			throw new IllegalStateException("Tiles at [" + id + "] is already being used!");
		}
		TILES[id] = this;
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
        return TILES;
    }
}

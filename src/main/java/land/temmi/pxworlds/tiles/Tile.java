package land.temmi.pxworlds.tiles;

import land.temmi.pxworlds.tiles.tile.ButtonTiles;
import land.temmi.pxworlds.tiles.tile.LogoTiles;
import land.temmi.pxworlds.tiles.tile.PlayerTiles;
import land.temmi.pxworlds.tiles.tile.WorldTiles;
import land.temmi.rollercoaster.graphics.Sprite;

/**
 * A tile type with a texture and a solidity flag.
 *
 * <p>Every instance registers itself in the static {@link #TILES} registry under
 * an auto-incrementing id, so all known tile types can be enumerated (e.g. by the
 * {@code TextureAtlas}). All tile constants live in dedicated classes
 * ({@link WorldTiles}, {@link PlayerTiles}, {@link ButtonTiles}, {@link LogoTiles})
 * which are force-loaded here so their static initialisers run before the first
 * atlas build.</p>
 */
public class Tile implements Sprite {

	/** Pixel size of a single tile in the texture atlas. */
	public static final int TILE_PX = 32;

	/** Maximum number of tiles. */
	public static final int MAX_TILES = 255;
	/** Start id for tiles. */
	public static final byte START_ID = 0;

	/** Registry of all tiles, indexed by id. */
	private static final Tile[] TILES = new Tile[MAX_TILES];
	/** Next free tile id. */
	private static byte not = START_ID;

	// Force-load all tile group classes so their static fields register before atlas build.
	// NOTE: a class literal (Foo.class) does NOT trigger static initialisation (JLS 12.4.1),
	// so we must initialise each class explicitly via Class.forName(..., initialize=true).
	static {
		forceInit(WorldTiles.class);
		forceInit(PlayerTiles.class);
		forceInit(ButtonTiles.class);
		forceInit(LogoTiles.class);
	}

	/**
	 * Forces the static initialiser of {@code clazz} to run, registering all its tiles.
	 *
	 * @param clazz the tile group class to initialise
	 */
	private static void forceInit(final Class<?> clazz) {
		try {
			Class.forName(clazz.getName(), true, clazz.getClassLoader());
		} catch (ClassNotFoundException e) {
			throw new ExceptionInInitializerError(e);
		}
	}

	/** Unique id of this tile. */
	private byte id;
	/** Whether this tile is solid (not walkable). */
	private boolean solid;
	/** Texture path of this tile. */
	private String texture;

	/**
	 * Creates a tile, assigns it the next free id and registers it.
	 *
	 * @param texture the texture path of this tile
	 * @param solid   whether the tile is solid (not walkable)
	 * @throws IllegalStateException if the assigned id is already in use
	 */
	public Tile(String texture, boolean solid) {
		this.id = not;
		not++;
		this.texture = texture;
		this.solid = solid;
		if (TILES[id] != null) {
			throw new IllegalStateException("Tiles at [" + id + "] is already being used!");
		}
		TILES[id] = this;
	}

	/** @return the registry of all tiles, indexed by id */
	public static Tile[] getTiles() {
		return TILES;
	}

	/**
	 * Marks this tile as solid.
	 * @return this tile, for chaining
	 */
	public Tile setSolid() {
		this.solid = true;
		return this;
	}

	/** @return {@code true} if this tile is solid (not walkable) */
	public boolean isSolid() {
		return solid;
	}

	/** @return the unique id of this tile */
	public byte getId() {
		return id;
	}

	/** @return the texture path of this tile */
	public String getTexture() {
		return texture;
	}
}

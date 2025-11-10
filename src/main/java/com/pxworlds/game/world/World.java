package com.pxworlds.game.world;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import com.pxworlds.game.collision.AABB;
import com.pxworlds.game.entity.Entity;
import com.pxworlds.game.entity.Player;
import com.pxworlds.game.entity.Transform;
import com.pxworlds.game.io.Window;
import com.pxworlds.game.rendering.Camera;
import com.pxworlds.game.rendering.Shader;
import com.pxworlds.game.rendering.tiles.Tile;
import com.pxworlds.game.rendering.tiles.TileRenderer;
import org.joml.*;

public class World {
	/** The default world width. */
	private static final int DEFAULT_WORLD_WIDTH = 64;
	/** The default world height. */
	private static final int DEFAULT_WORLD_HEIGHT = 64;
	/** The default world scale. */
	private static final int DEFAULT_WORLD_SCALE = 16;
	/** The alpha mask. */
	private static final int ALPHA_MASK = 0xFF;
	/** The red mask. */
	private static final int RED_MASK = 0xFF;
	/** The green mask. */
	private static final int GREEN_MASK = 0xFF;
	/** The blue mask. */
	private static final int BLUE_MASK = 0xFF;
	/** The red shift. */
	private static final int RED_SHIFT = 16;
	/** The green shift. */
	private static final int GREEN_SHIFT = 8;
	/** The blue shift. */
	private static final int BLUE_SHIFT = 0;
	/** The alpha shift. */
	private static final int ALPHA_SHIFT = 24;
	/** The multiplier for tile positioning. */
	private static final int TILE_POSITION_MULTIPLIER = 2;
	/** The view offset. */
	private static final int VIEW_OFFSET = 4;
	/** The divisor for half calculations. */
	private static final int HALF_DIVISOR = 2;
	/** The render offset. */
	private static final int RENDER_OFFSET = 1;
	/** The bounding box size. */
	private static final float BOUNDING_BOX_SIZE = 1.0f;
	/** The player entity index. */
	private static final int PLAYER_ENTITY_INDEX = 1;
	/** The view X position. */
	private int viewX;
	/** The view Y position. */
	private int viewY;
	/** The tile data. */
	private byte[] tiles;
	/** The bounding boxes for collision. */
	private AABB[] boundingBoxes;
	/** The list of entities in the world. */
	private List<Entity> entities;
	/** The width of the world. */
	private int width;
	/** The height of the world. */
	private int height;
	/** The scale of the world. */
	private int scale;

	/** The world transformation matrix. */
	private Matrix4f world;

	public World(String worldName, Camera camera, boolean playable, int newScale) {

		try {
			final BufferedImage tileSheet = ImageIO.read(getClass().getResourceAsStream("/levels/" + worldName + "/tiles.png"));
			final BufferedImage entitySheet = ImageIO.read(getClass().getResourceAsStream("/levels/" + worldName + "/entities.png"));

			width = tileSheet.getWidth();
			height = tileSheet.getHeight();
            this.scale = newScale;

			this.world = new Matrix4f().setTranslation(new Vector3f(0));
			this.world.scale(scale);

			final int[] colorTileSheet = tileSheet.getRGB(0, 0, width, height, null, 0, width);
			final int[] colorEntitySheet = entitySheet.getRGB(0, 0, width, height, null, 0, width);

			tiles = new byte[width * height];
			boundingBoxes = new AABB[width * height];
			entities = new ArrayList<>();

			Transform transform;

			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					final int red = (colorTileSheet[x + y * width] >> RED_SHIFT) & RED_MASK;
					final int entityIndex = (colorEntitySheet[x + y * width] >> RED_SHIFT) & RED_MASK;
					final int entityAlpha = (colorEntitySheet[x + y * width] >> ALPHA_SHIFT) & ALPHA_MASK;

					Tile t;
					try {
						t = Tile.tiles[red];
					} catch (ArrayIndexOutOfBoundsException e) {
						t = null;
					}

					if (t != null) {
						setTile(t, x, y);
					}

					if (entityAlpha > 0) {
						transform = new Transform();
						transform.pos.x = x * TILE_POSITION_MULTIPLIER;
						transform.pos.y = -y * TILE_POSITION_MULTIPLIER;
						switch (entityIndex) {
							case PLAYER_ENTITY_INDEX :
							    if(playable) {
                                    // Player
                                    final Player player = new Player(transform);
                                    entities.add(player);
                                }
								camera.getPosition().set(transform.pos.mul(-scale, new Vector3f()));
								break;
							default :
								break;
						}
					}
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public World() {
		width = DEFAULT_WORLD_WIDTH;
		height = DEFAULT_WORLD_HEIGHT;
		scale = DEFAULT_WORLD_SCALE;

		tiles = new byte[width * height];
		boundingBoxes = new AABB[width * height];

		world = new Matrix4f().setTranslation(new Vector3f(0));
		world.scale(scale);
	}

	public void calculateView(Window window) {
		viewX = (window.getWidth() / (scale * HALF_DIVISOR)) + VIEW_OFFSET;
		viewY = (window.getHeight() / (scale * HALF_DIVISOR)) + VIEW_OFFSET;
	}

	public Matrix4f getWorldMatrix() {
		return world;
	}

	public void render(TileRenderer render, Shader shader, Camera cam) {
		final int posX = (int) cam.getPosition().x / (scale * HALF_DIVISOR);
		final int posY = (int) cam.getPosition().y / (scale * HALF_DIVISOR);

		for (int i = 0; i < viewX; i++) {
			for (int j = 0; j < viewY; j++) {
				final Tile t = getTile(i - posX - (viewX / HALF_DIVISOR) + RENDER_OFFSET, j + posY - (viewY / HALF_DIVISOR));
				if (t != null) {
					render.renderTile(t, i - posX - (viewX / HALF_DIVISOR) + RENDER_OFFSET, -j - posY + (viewY / HALF_DIVISOR), shader, world, cam);
				}
			}
		}

		for (Entity entity : entities) {
			entity.render(shader, cam, this);
		}
	}

	public void update(float delta, Window window, Camera camera) {
		for (Entity entity : entities) {
			entity.update(delta, window, camera, this);
		}

		for (int i = 0; i < entities.size(); i++) {
			entities.get(i).collideWithTiles(this);
			for (int j = i + 1; j < entities.size(); j++) {
				entities.get(i).collideWithEntity(entities.get(j));
			}
			entities.get(i).collideWithTiles(this);
		}
	}

	public void correctCamera(Camera camera, Window window) {
		final Vector3f pos = camera.getPosition();

		final int w = -width * scale * HALF_DIVISOR;
		final int h = height * scale * HALF_DIVISOR;

		if (pos.x > -(window.getWidth() / HALF_DIVISOR) + scale) {
			pos.x = -(window.getWidth() / HALF_DIVISOR) + scale;
		}
		if (pos.x < w + (window.getWidth() / HALF_DIVISOR) + scale) {
			pos.x = w + (window.getWidth() / HALF_DIVISOR) + scale;
		}

		if (pos.y < (window.getHeight() / HALF_DIVISOR) - scale) {
			pos.y = (window.getHeight() / HALF_DIVISOR) - scale;
		}
		if (pos.y > h - (window.getHeight() / HALF_DIVISOR) - scale) {
			pos.y = h - (window.getHeight() / HALF_DIVISOR) - scale;
		}
	}

	public void setTile(Tile tile, int x, int y) {
		tiles[x + y * width] = tile.getId();
		if (tile.isSolid()) {
			boundingBoxes[x + y * width] = new AABB(new Vector2f(x * TILE_POSITION_MULTIPLIER, -y * TILE_POSITION_MULTIPLIER), new Vector2f(BOUNDING_BOX_SIZE, BOUNDING_BOX_SIZE));
		} else {
			boundingBoxes[x + y * width] = null;
		}
	}

	public Tile getTile(int x, int y) {
		try {
			return Tile.tiles[tiles[x + y * width]];
		} catch (ArrayIndexOutOfBoundsException e) {
			return null;
		}
	}

	public AABB getTileBoundingBox(int x, int y) {
		try {
			return boundingBoxes[x + y * width];
		} catch (ArrayIndexOutOfBoundsException e) {
			return null;
		}
	}

	public int getScale() {
		return scale;
	}
}

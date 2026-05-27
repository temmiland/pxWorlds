package com.pxworlds.game.world;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import com.pxworlds.Constants;
import com.pxworlds.game.collision.AABB;
import com.pxworlds.game.entity.Entity;
import com.pxworlds.game.entity.Player;
import com.pxworlds.game.entity.Transform;
import com.pxworlds.game.io.Window;
import com.pxworlds.game.rendering.Camera;
import com.pxworlds.game.rendering.Shader;
import com.pxworlds.game.rendering.tiles.Tile;
import com.pxworlds.game.rendering.tiles.TileRenderer;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

import static org.lwjgl.opengl.GL11.GL_LINE_LOOP;
import static org.lwjgl.opengl.GL11.GL_MODELVIEW;
import static org.lwjgl.opengl.GL11.GL_PROJECTION;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glColor3f;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glLoadIdentity;
import static org.lwjgl.opengl.GL11.glLoadMatrixf;
import static org.lwjgl.opengl.GL11.glMatrixMode;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glLineWidth;
import static org.lwjgl.opengl.GL11.glVertex2f;
import static org.lwjgl.opengl.GL20.glUseProgram;

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
	/** Größe eines 4×4-Matrizen-FloatBuffer (16 Einträge). */
	private static final int MATRIX_FLOAT_COUNT = 16;
	/** OpenGL-Programm-ID 0 — deaktiviert jeden aktiven Shader. */
	private static final int NO_SHADER = 0;
	/** Debug-Farbe Grün: volle Intensität (für Tile-AABBs). */
	private static final float COLOR_FULL = 1.0f;
	/** Debug-Farbe: keine Intensität (Kanal ausgeschaltet). */
	private static final float COLOR_NONE = 0.0f;
	/** Linienbreite der Debug-AABB-Rahmen in Pixeln. */
	private static final float DEBUG_LINE_WIDTH = 2.0f;
	/** Standard-Linienbreite — wird nach dem Debug-Rendering wiederhergestellt. */
	private static final float DEFAULT_LINE_WIDTH = 1.0f;
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

	/**
	 * Wiederverwendbare Scratch-AABB für {@link #renderDebugBoxes} — vermeidet
	 * eine Heap-Allokation pro Entity pro Frame im Debug-Modus.
	 */
	private final AABB scratchEntityDebugBox = new AABB(0, 0, 0, 0);

	/**
	 * Wiederverwendbare Ergebnisliste für {@link #getCollidingBoxes(AABB)}.
	 * Vermeidet eine {@code new ArrayList<>()} pro Tick pro Entity.
	 */
	private final List<AABB> scratchColliders = new ArrayList<>();

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
						transform.pos.x = x * Constants.TILE_SIZE;
						transform.pos.y = -y * Constants.TILE_SIZE;
						switch (entityIndex) {
							case PLAYER_ENTITY_INDEX :
							    if(playable) {
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

	public void render(TileRenderer render, Shader shader, Camera cam, float alpha) {
		final int posX = (int) cam.getPosition().x / (scale * HALF_DIVISOR);
		final int posY = (int) cam.getPosition().y / (scale * HALF_DIVISOR);

		for (int i = 0; i < viewX; i++) {
			for (int j = 0; j < viewY; j++) {
				final Tile t = getTile(i - posX - (viewX / HALF_DIVISOR) + RENDER_OFFSET, j + posY - (viewY / HALF_DIVISOR));
				if (t != null) {
					render.renderTile(t, i - posX - (viewX / HALF_DIVISOR) + RENDER_OFFSET, -j - posY + (viewY / HALF_DIVISOR), shader, world, cam, alpha);
				}
			}
		}

		for (Entity entity : entities) {
			entity.render(shader, cam, this, alpha);
		}
	}

	public void update(float delta, Window window, Camera camera) {
		camera.savePrevPosition();
		for (Entity entity : entities) {
			entity.tick(delta, window, camera, this);
		}

		// Entity-Entity Push-out erfolgt nach den Tick-Updates
		for (int i = 0; i < entities.size(); i++) {
			for (int j = i + 1; j < entities.size(); j++) {
				entities.get(i).collideWithEntity(entities.get(j));
			}
		}
	}

	/**
	 * Liefert alle Tile-Bounding-Boxes, die die gegebene AABB überschneiden.
	 *
	 * @param area  die zu prüfende AABB
	 * @return Liste aller schneidenden Tile-Bounding-Boxes (nie null)
	 */
	public List<AABB> getCollidingBoxes(AABB area) {
		scratchColliders.clear();

		// Y-Achse ist im Welt-Koordinatensystem negiert (Tile y=0 liegt oben),
		// daher minY → y1 und maxY → y0. Puffer ±1 verhindert Rand-Lücken.
		final int x0 = (int) Math.floor( area.minX / Constants.TILE_SIZE) - 1;
		final int x1 = (int) Math.ceil(  area.maxX / Constants.TILE_SIZE) + 1;
		final int y0 = (int) Math.floor(-area.maxY / Constants.TILE_SIZE) - 1;
		final int y1 = (int) Math.ceil( -area.minY / Constants.TILE_SIZE) + 1;

		for (int y = Math.max(0, y0); y <= Math.min(height - 1, y1); y++) {
			for (int x = Math.max(0, x0); x <= Math.min(width - 1, x1); x++) {
				final AABB bb = getTileBoundingBox(x, y);
				if (bb != null && bb.intersects(area)) {
					scratchColliders.add(bb);
				}
			}
		}
		return scratchColliders;
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
			final float cx = x * Constants.TILE_SIZE;
			final float cy = -y * Constants.TILE_SIZE;
			boundingBoxes[x + y * width] = new AABB(
				cx - BOUNDING_BOX_SIZE, cy - BOUNDING_BOX_SIZE,
				cx + BOUNDING_BOX_SIZE, cy + BOUNDING_BOX_SIZE
			);
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

	/**
	 * Zeichnet Kollisions-AABBs als Linienrahmen: Tile-AABBs grün, Entity-AABBs rot.
	 * Nutzt dieselbe kombinierte Kamera-Welt-Matrix wie das normale Rendering.
	 *
	 * @param camera die aktive Kamera
	 * @param alpha  Interpolationsfaktor 0.0–1.0
	 */
	public void renderDebugBoxes(final Camera camera, final float alpha) {
		final Matrix4f combined = camera.getProjection(alpha).mul(world);
		final FloatBuffer matBuf = BufferUtils.createFloatBuffer(MATRIX_FLOAT_COUNT);
		combined.get(matBuf);

		glUseProgram(NO_SHADER);
		glLineWidth(DEBUG_LINE_WIDTH);

		glMatrixMode(GL_PROJECTION);
		glPushMatrix();
		glLoadMatrixf(matBuf);

		glMatrixMode(GL_MODELVIEW);
		glPushMatrix();
		glLoadIdentity();

		glColor3f(COLOR_NONE, COLOR_FULL, COLOR_NONE);
		for (final AABB bb : boundingBoxes) {
			if (bb != null) {
				drawDebugBox(bb);
			}
		}

		glColor3f(COLOR_FULL, COLOR_NONE, COLOR_NONE);
		for (final Entity entity : entities) {
			entity.getInterpolatedBoundingBox(alpha, scratchEntityDebugBox);
			drawDebugBox(scratchEntityDebugBox);
		}

		glMatrixMode(GL_PROJECTION);
		glPopMatrix();
		glMatrixMode(GL_MODELVIEW);
		glPopMatrix();

		glLineWidth(DEFAULT_LINE_WIDTH);
	}

	/**
	 * Zeichnet eine einzelne AABB als Linienrahmen (GL_LINE_LOOP mit 4 Ecken).
	 *
	 * @param bb die zu zeichnende AABB (Koordinaten in Tile-Einheiten)
	 */
	private static void drawDebugBox(final AABB bb) {
		glBegin(GL_LINE_LOOP);
		glVertex2f(bb.minX, bb.minY);
		glVertex2f(bb.maxX, bb.minY);
		glVertex2f(bb.maxX, bb.maxY);
		glVertex2f(bb.minX, bb.maxY);
		glEnd();
	}

	public int getScale() {
		return scale;
	}
}

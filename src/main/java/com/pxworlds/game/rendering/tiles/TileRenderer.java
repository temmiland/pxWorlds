package com.pxworlds.game.rendering.tiles;

import java.util.HashMap;
import java.util.Map;

import com.pxworlds.Constants;
import com.pxworlds.game.rendering.Camera;
import com.pxworlds.game.rendering.Model;
import com.pxworlds.game.rendering.Shader;
import com.pxworlds.game.rendering.Texture;
import com.pxworlds.game.rendering.tiles.Tile;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class TileRenderer {
	/** The map of tile textures. */
	private Map<String, Texture> tileTextures;
	/** The model for tiles. */
	private Model tileModel;

	// Tile rendering constants
	// Tile size: see Constants.TILE_SIZE
	/** The sampler unit for textures. */
	private static final int SAMPLER_UNIT = 0;
	/** The Z depth for tiles. */
	private static final float Z_DEPTH = 0.0f;

	// Quad geometry constants
	/** The vertices for the quad. */
	private static final float[] VERTICES = new float[]{
		-1f, 1f, Z_DEPTH,  // TOP LEFT
		1f, 1f, Z_DEPTH,   // TOP RIGHT
		1f, -1f, Z_DEPTH,  // BOTTOM RIGHT
		-1f, -1f, Z_DEPTH  // BOTTOM LEFT
	};

	/** The texture coordinates for the quad. */
	private static final float[] TEXTURE_COORDS = new float[]{0, 0, 1, 0, 1, 1, 0, 1};

	/** The indices for the quad. */
	private static final int[] INDICES = new int[]{0, 1, 2, 2, 3, 0};

	public TileRenderer() {
		tileTextures = new HashMap<>();

		tileModel = new Model(VERTICES, TEXTURE_COORDS, INDICES);

		for (int i = 0; i < Tile.tiles.length; i++) {
			if (Tile.tiles[i] != null) {
				if (!tileTextures.containsKey(Tile.tiles[i].getTexture())) {
					final String tex = Tile.tiles[i].getTexture();
					tileTextures.put(tex, new Texture(tex + ".png"));
				}
			}
		}
	}

	/**
	 * Renders a single tile at the given grid position using an interpolated camera.
	 * @param tile   The tile to render.
	 * @param x      Tile grid X coordinate.
	 * @param y      Tile grid Y coordinate.
	 * @param shader The active shader.
	 * @param world  The world transformation matrix.
	 * @param cam    The camera.
	 * @param alpha  Render-interpolation factor 0.0–1.0.
	 */
	public void renderTile(final Tile tile, final int x, final int y, final Shader shader,
							final Matrix4f world, final Camera cam, final float alpha) {
		shader.bind();
		if (tileTextures.containsKey(tile.getTexture())) {
			tileTextures.get(tile.getTexture()).bind(SAMPLER_UNIT);
		}

		final Matrix4f tilePos = new Matrix4f().translate(
				new Vector3f(x * Constants.TILE_SIZE, y * Constants.TILE_SIZE, Z_DEPTH));
		final Matrix4f target = new Matrix4f();

		cam.getProjection(alpha).mul(world, target);
		target.mul(tilePos);

		shader.setUniform("sampler", SAMPLER_UNIT);
		shader.setUniform("projection", target);

		tileModel.render();
	}
}

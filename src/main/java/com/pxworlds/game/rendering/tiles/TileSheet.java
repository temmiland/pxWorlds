package com.pxworlds.game.rendering.tiles;

import com.pxworlds.game.rendering.Shader;
import com.pxworlds.game.rendering.Texture;
import org.joml.Matrix4f;

public class TileSheet {
	/** Textur des TileSheets. */
	private Texture texture;

	/** Skalierungsmatrix. */
	private Matrix4f scale;
	/** Translationsmatrix. */
	private Matrix4f translation;

	/** Anzahl der Tiles im Sheet. */
	private int amountOfTiles;

	public TileSheet(String newTexture, int newAmountOfTiles) {
		this.texture = new Texture("sheets/" + newTexture);

		scale = new Matrix4f().scale(1.0f / (float)newAmountOfTiles);
		translation = new Matrix4f();

		this.amountOfTiles = newAmountOfTiles;
	}

	public void bindTile(Shader shader, int x, int y) {
		scale.translate(x, y, 0, translation);

		shader.setUniform("sampler", 0);
		shader.setUniform("texModifier", translation);
		texture.bind(0);
	}

	public void bindTile(Shader shader, int tile) {
		final int x = tile % amountOfTiles;
		final int y = tile / amountOfTiles;
		bindTile(shader, x, y);
	}
}

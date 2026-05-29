package com.pxworlds.game.gui;

import com.pxworlds.game.assets.Assets;
import com.pxworlds.game.collision.AABB;
import com.pxworlds.game.collision.Collision;
import com.pxworlds.game.debug.DebugRenderer;
import com.pxworlds.game.io.InputProcessor;
import com.pxworlds.game.rendering.Camera;
import com.pxworlds.game.rendering.Shader;
import com.pxworlds.game.rendering.tiles.TileSheet;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.lwjgl.stb.STBEasyFont;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.glUseProgram;


public class Button {
	/**
	 * Tile-Koordinaten für das Button-Rendering.
	 * {@code baseCol} ist die Spalte im IDLE-Zustand; SELECTED addiert +3, CLICKED +6.
	 * {@code row} ist die feste Zeile im Tilesheet (0=oben, 1=mitte, 2=unten).
	 */
	private enum ButtonTile {
		FILL(1, 1),
		TOP(1, 0),
		BOTTOM(1, 2),
		LEFT(0, 1),
		RIGHT(2, 1),
		CORNER_TOP_LEFT(0, 0),
		CORNER_TOP_RIGHT(2, 0),
		CORNER_BOTTOM_LEFT(0, 2),
		CORNER_BOTTOM_RIGHT(2, 2);

		final int baseCol;
		final int row;

		ButtonTile(int baseCol, int row) {
			this.baseCol = baseCol;
			this.row = row;
		}

		int col(ButtonState state) {
			return baseCol + state.ordinal() * 3;
		}
	}

	/** Corner size in pixels. */
	private static final int BUTTON_CORNER_SIZE = 16;
	/** Border size in pixels. */
	private static final int BUTTON_BORDER_SIZE = 16;

	/** STBEasyFont font height in coordinate units. */
	private static final float FONT_HEIGHT = 7.0f;
	/** Scale applied to the font for readable button text. */
	private static final float FONT_SCALE = 2.5f;

	/** The bounding box for the button. */
	private AABB boundingBox;

	/** The current selected state of the button. */
	private ButtonState selectedState;

	/** The label text displayed on the button (may be null for no text). */
	private String label;

	/** Action executed when the button is clicked (may be null). */
	private Runnable onClick;

	/** The transform matrix for the button. */
	private static Matrix4f transform = new Matrix4f();

	public Button(Vector2f position, Vector2f scale, String label, Runnable onClick) {
		this.boundingBox = new AABB(
			position.x - scale.x, position.y - scale.y,
			position.x + scale.x, position.y + scale.y
		);
		this.label = label;
		this.onClick = onClick;
		selectedState = ButtonState.IDLE;
	}

	public Button(Vector2f position, Vector2f scale, String label) {
		this(position, scale, label, null);
	}

	public Button(Vector2f position, Vector2f scale) {
		this(position, scale, null, null);
	}

	/**
	 * Aktualisiert den Button-Zustand basierend auf der Mausposition und Mausbuttons.
	 *
	 * @param input    der aktuelle {@link InputProcessor}
	 * @param mousePos transformierte Mausposition (Fenstermitte = Ursprung, Y oben)
	 */
	public void update(final InputProcessor input, final Vector2f mousePos) {
		final Collision data = boundingBox.getCollision(mousePos);

		if (data.isIntersecting()) {
			selectedState = ButtonState.SELECTED;

			if (input.wasMouseButtonPressed(0)) {
				selectedState = ButtonState.CLICKED;
				if (onClick != null) {
					onClick.run();
				}
			} else if (input.isMouseButtonDown(0)) {
				selectedState = ButtonState.CLICKED;
			}
		} else {
			selectedState = ButtonState.IDLE;
		}
	}

	public void render(Camera camera, TileSheet sheet, Shader shader) {
		final Vector2f position = boundingBox.getCenter(), scale = boundingBox.getHalfExtent();

		renderTile(position.x, position.y, scale.x, scale.y, ButtonTile.FILL, camera, sheet, shader);

		renderSides(position, scale, camera, sheet, shader);
		renderCorners(position, scale, camera, sheet, shader);
		renderLabel(camera);
		shader.bind();
	}

	private void renderSides(Vector2f position, Vector2f scale, Camera camera, TileSheet sheet, Shader shader) {
		renderTile(position.x, position.y + scale.y - BUTTON_BORDER_SIZE, scale.x, BUTTON_BORDER_SIZE, ButtonTile.TOP, camera, sheet, shader);
		renderTile(position.x, position.y - scale.y + BUTTON_BORDER_SIZE, scale.x, BUTTON_BORDER_SIZE, ButtonTile.BOTTOM, camera, sheet, shader);
		renderTile(position.x - scale.x + BUTTON_BORDER_SIZE, position.y, BUTTON_BORDER_SIZE, scale.y, ButtonTile.LEFT, camera, sheet, shader);
		renderTile(position.x + scale.x - BUTTON_BORDER_SIZE, position.y, BUTTON_BORDER_SIZE, scale.y, ButtonTile.RIGHT, camera, sheet, shader);
	}

	private void renderCorners(Vector2f position, Vector2f scale, Camera camera, TileSheet sheet, Shader shader) {
		renderTile(position.x - scale.x + BUTTON_CORNER_SIZE, position.y + scale.y - BUTTON_CORNER_SIZE, BUTTON_CORNER_SIZE, BUTTON_CORNER_SIZE, ButtonTile.CORNER_TOP_LEFT, camera, sheet, shader);
		renderTile(position.x + scale.x - BUTTON_CORNER_SIZE, position.y + scale.y - BUTTON_CORNER_SIZE, BUTTON_CORNER_SIZE, BUTTON_CORNER_SIZE, ButtonTile.CORNER_TOP_RIGHT, camera, sheet, shader);
		renderTile(position.x - scale.x + BUTTON_CORNER_SIZE, position.y - scale.y + BUTTON_CORNER_SIZE, BUTTON_CORNER_SIZE, BUTTON_CORNER_SIZE, ButtonTile.CORNER_BOTTOM_LEFT, camera, sheet, shader);
		renderTile(position.x + scale.x - BUTTON_CORNER_SIZE, position.y - scale.y + BUTTON_CORNER_SIZE, BUTTON_CORNER_SIZE, BUTTON_CORNER_SIZE, ButtonTile.CORNER_BOTTOM_RIGHT, camera, sheet, shader);
	}

	private void renderTile(float x, float y, float w, float h, ButtonTile tile, Camera camera, TileSheet sheet, Shader shader) {
		transform.identity().translate(x, y, 0).scale(w, h, 1);
		shader.setUniform("projection", camera.getProjection().mul(transform));
		sheet.bindTile(shader, tile.col(selectedState), tile.row);
		Assets.getModel().render();
	}

	/**
	 * Rendert den Button-Label-Text zentriert über dem Button.
	 * Nutzt STBEasyFont (gleicher Font wie das Debug-Overlay).
	 */
	private void renderLabel(final Camera camera) {
		if (label == null || label.isEmpty()) {
			return;
		}

		final Vector2f center = boundingBox.getCenter();
		final int vw = camera.getVirtualWidth();
		final int vh = camera.getVirtualHeight();

		// Textbreite in STBEasyFont-Einheiten messen
		final float textWidth = STBEasyFont.stb_easy_font_width(label);
		final float scaledTextWidth  = textWidth * FONT_SCALE;
		final float scaledTextHeight = FONT_HEIGHT * FONT_SCALE;

		// Koordinaten: GUI-Kamera ist zentriert (-vw/2..+vw/2, -vh/2..+vh/2)
		// glOrtho setzt origin oben-links, also Y invertiert gegenüber dem GUI-System
		final float drawX = (center.x - scaledTextWidth / 2.0f) + vw / 2.0f;
		final float drawY = (-center.y - scaledTextHeight / 2.0f) + vh / 2.0f;

		// Trailing Space verhindert, dass STBEasyFont den letzten schmalen Glyph
		// (t, e, l etc.) unvollständig in den Vertex-Buffer schreibt.
		final String renderLabel = label + " ";

		glUseProgram(0);
		glDisable(GL_TEXTURE_2D);
		glBindTexture(GL_TEXTURE_2D, 0);
		glMatrixMode(GL_PROJECTION);
		glPushMatrix();
		glLoadIdentity();
		glOrtho(0, vw, vh, 0, -1, 1);

		glMatrixMode(GL_MODELVIEW);
		glPushMatrix();
		glLoadIdentity();
		glScalef(FONT_SCALE, FONT_SCALE, 1.0f);

		DebugRenderer.drawStringShadowed(renderLabel, drawX / FONT_SCALE, drawY / FONT_SCALE);

		glMatrixMode(GL_PROJECTION);
		glPopMatrix();
		glMatrixMode(GL_MODELVIEW);
		glPopMatrix();
		glEnable(GL_TEXTURE_2D);
	}
}

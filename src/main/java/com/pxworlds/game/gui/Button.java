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
	/** Idle state for button. */
	public static final int STATE_IDLE = 0;
	/** Selected state for button. */
	public static final int STATE_SELECTED = 1;
	/** Clicked state for button. */
	public static final int STATE_CLICKED = 2;

	/** Tile coordinates for button rendering. */
	/** Top left corner tile. */
	private static final int BUTTON_CORNER_TOP_LEFT = 4;
	/** Top right corner tile. */
	private static final int BUTTON_CORNER_TOP_RIGHT = 7;
	/** Bottom left corner tile. */
	private static final int BUTTON_CORNER_BOTTOM_LEFT = 1;
	/** Bottom right corner tile. */
	private static final int BUTTON_CORNER_BOTTOM_RIGHT = 1;
	/** Top edge tile. */
	private static final int BUTTON_EDGE_TOP = 4;
	/** Bottom edge tile. */
	private static final int BUTTON_EDGE_BOTTOM = 7;
	/** Fill row tile. */
	private static final int BUTTON_FILL_ROW = 1;
	/** Top row. */
	private static final int BUTTON_TOP_ROW = 0;
	/** Bottom row. */
	private static final int BUTTON_BOTTOM_ROW = 2;
	/** Left column. */
	private static final int BUTTON_LEFT_COL = 3;
	/** Right column. */
	private static final int BUTTON_RIGHT_COL = 5;
	/** Left selected column. */
	private static final int BUTTON_LEFT_SELECTED_COL = 6;
	/** Right selected column. */
	private static final int BUTTON_RIGHT_SELECTED_COL = 8;
	/** Left idle column. */
	private static final int BUTTON_LEFT_IDLE_COL = 0;
	/** Right idle column. */
	private static final int BUTTON_RIGHT_IDLE_COL = 2;
	/** Top left corner column. */
	private static final int BUTTON_CORNER_TOP_LEFT_COL = 3;
	/** Top right corner column. */
	private static final int BUTTON_CORNER_TOP_RIGHT_COL = 5;
	/** Bottom left corner column. */
	private static final int BUTTON_CORNER_BOTTOM_LEFT_COL = 3;
	/** Bottom right corner column. */
	private static final int BUTTON_CORNER_BOTTOM_RIGHT_COL = 5;
	/** Selected left corner column. */
	private static final int BUTTON_CORNER_SELECTED_LEFT_COL = 6;
	/** Selected right corner column. */
	private static final int BUTTON_CORNER_SELECTED_RIGHT_COL = 8;
	/** Idle left corner column. */
	private static final int BUTTON_CORNER_IDLE_LEFT_COL = 0;
	/** Idle right corner column. */
	private static final int BUTTON_CORNER_IDLE_RIGHT_COL = 2;
	/** Corner size. */
	private static final int BUTTON_CORNER_SIZE = 16;
	/** Top left corner row. */
	private static final int BUTTON_CORNER_TOP_LEFT_ROW = 0;
	/** Top right corner row. */
	private static final int BUTTON_CORNER_TOP_RIGHT_ROW = 0;
	/** Bottom left corner row. */
	private static final int BUTTON_CORNER_BOTTOM_LEFT_ROW = 2;
	/** Bottom right corner row. */
	private static final int BUTTON_CORNER_BOTTOM_RIGHT_ROW = 2;
	/** Border size. */
	private static final int BUTTON_BORDER_SIZE = 16;

	/** STBEasyFont font height in coordinate units. */
	private static final float FONT_HEIGHT = 7.0f;
	/** Scale applied to the font for readable button text. */
	private static final float FONT_SCALE = 2.5f;

	/** The bounding box for the button. */
	private AABB boundingBox;

	/** The current selected state of the button. */
	private int selectedState;

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
		selectedState = STATE_IDLE;
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

		if (data.isIntersecting) {
			selectedState = STATE_SELECTED;

			if (input.isMouseButtonDown(0)) {
				selectedState = STATE_CLICKED;
				if (onClick != null) {
					onClick.run();
				}
			}
		} else {
			selectedState = STATE_IDLE;
		}
	}

	public void render(Camera camera, TileSheet sheet, Shader shader) {
		final Vector2f position = boundingBox.getCenter(), scale = boundingBox.getHalfExtent();

		transform.identity().translate(position.x, position.y, 0).scale(scale.x, scale.y, 1); // Middle/Fill

		shader.setUniform("projection", camera.getProjection().mul(transform));
		switch (selectedState) {
			case STATE_SELECTED :
				sheet.bindTile(shader, BUTTON_CORNER_TOP_LEFT, BUTTON_FILL_ROW);
				break;
			case STATE_CLICKED :
				sheet.bindTile(shader, BUTTON_CORNER_TOP_RIGHT, BUTTON_FILL_ROW);
				break;
			default :
				sheet.bindTile(shader, BUTTON_CORNER_BOTTOM_LEFT, BUTTON_FILL_ROW);
				break;
		}
		Assets.getModel().render();

		renderSides(position, scale, camera, sheet, shader);
		renderCorners(position, scale, camera, sheet, shader);
		renderLabel(camera);
		shader.bind();
	}

	private void renderSides(Vector2f position, Vector2f scale, Camera camera, TileSheet sheet, Shader shader) {
		transform.identity().translate(position.x, position.y + scale.y - BUTTON_BORDER_SIZE, 0).scale(scale.x, BUTTON_BORDER_SIZE, 1); // Top

		shader.setUniform("projection", camera.getProjection().mul(transform));
		switch (selectedState) {
			case STATE_SELECTED :
				sheet.bindTile(shader, BUTTON_EDGE_TOP, BUTTON_TOP_ROW);
				break;
			case STATE_CLICKED :
				sheet.bindTile(shader, BUTTON_EDGE_BOTTOM, BUTTON_TOP_ROW);
				break;
			default :
				sheet.bindTile(shader, BUTTON_CORNER_BOTTOM_LEFT, BUTTON_TOP_ROW);
				break;
		}
		Assets.getModel().render();

		transform.identity().translate(position.x, position.y - scale.y + BUTTON_BORDER_SIZE, 0).scale(scale.x, BUTTON_BORDER_SIZE, 1); // Bottom

		shader.setUniform("projection", camera.getProjection().mul(transform));
		switch (selectedState) {
			case STATE_SELECTED :
				sheet.bindTile(shader, BUTTON_EDGE_TOP, BUTTON_BOTTOM_ROW);
				break;
			case STATE_CLICKED :
				sheet.bindTile(shader, BUTTON_EDGE_BOTTOM, BUTTON_BOTTOM_ROW);
				break;
			default :
				sheet.bindTile(shader, BUTTON_CORNER_BOTTOM_LEFT, BUTTON_BOTTOM_ROW);
				break;
		}
		Assets.getModel().render();

		transform.identity().translate(position.x - scale.x + BUTTON_BORDER_SIZE, position.y, 0).scale(BUTTON_BORDER_SIZE, scale.y, 1); // Left

		shader.setUniform("projection", camera.getProjection().mul(transform));
		switch (selectedState) {
			case STATE_SELECTED :
				sheet.bindTile(shader, BUTTON_LEFT_COL, BUTTON_FILL_ROW);
				break;
			case STATE_CLICKED :
				sheet.bindTile(shader, BUTTON_LEFT_SELECTED_COL, BUTTON_FILL_ROW);
				break;
			default :
				sheet.bindTile(shader, BUTTON_LEFT_IDLE_COL, BUTTON_FILL_ROW);
				break;
		}
		Assets.getModel().render();

		transform.identity().translate(position.x + scale.x - BUTTON_BORDER_SIZE, position.y, 0).scale(BUTTON_BORDER_SIZE, scale.y, 1); // Right

		shader.setUniform("projection", camera.getProjection().mul(transform));
		switch (selectedState) {
			case STATE_SELECTED :
				sheet.bindTile(shader, BUTTON_RIGHT_COL, BUTTON_FILL_ROW);
				break;
			case STATE_CLICKED :
				sheet.bindTile(shader, BUTTON_RIGHT_SELECTED_COL, BUTTON_FILL_ROW);
				break;
			default :
				sheet.bindTile(shader, BUTTON_RIGHT_IDLE_COL, BUTTON_FILL_ROW);
				break;
		}
		Assets.getModel().render();
	}

	private void renderCorners(Vector2f position, Vector2f scale, Camera camera, TileSheet sheet, Shader shader) {
		transform.identity().translate(position.x - scale.x + BUTTON_CORNER_SIZE, position.y + scale.y - BUTTON_CORNER_SIZE, 0).scale(BUTTON_CORNER_SIZE, BUTTON_CORNER_SIZE, 1); // Top Left

		shader.setUniform("projection", camera.getProjection().mul(transform));
		switch (selectedState) {
			case STATE_SELECTED :
				sheet.bindTile(shader, BUTTON_CORNER_TOP_LEFT_COL, BUTTON_CORNER_TOP_LEFT_ROW);
				break;
			case STATE_CLICKED :
				sheet.bindTile(shader, BUTTON_CORNER_SELECTED_LEFT_COL, BUTTON_CORNER_TOP_LEFT_ROW);
				break;
			default :
				sheet.bindTile(shader, BUTTON_CORNER_IDLE_LEFT_COL, BUTTON_CORNER_TOP_LEFT_ROW);
				break;
		}
		Assets.getModel().render();

		transform.identity().translate(position.x + scale.x - BUTTON_CORNER_SIZE, position.y + scale.y - BUTTON_CORNER_SIZE, 0).scale(BUTTON_CORNER_SIZE, BUTTON_CORNER_SIZE, 1); // Top Right

		shader.setUniform("projection", camera.getProjection().mul(transform));
		switch (selectedState) {
			case STATE_SELECTED :
				sheet.bindTile(shader, BUTTON_CORNER_TOP_RIGHT_COL, BUTTON_CORNER_TOP_RIGHT_ROW);
				break;
			case STATE_CLICKED :
				sheet.bindTile(shader, BUTTON_CORNER_SELECTED_RIGHT_COL, BUTTON_CORNER_TOP_RIGHT_ROW);
				break;
			default :
				sheet.bindTile(shader, BUTTON_CORNER_IDLE_RIGHT_COL, BUTTON_CORNER_TOP_RIGHT_ROW);
				break;
		}
		Assets.getModel().render();

		transform.identity().translate(position.x - scale.x + BUTTON_CORNER_SIZE, position.y - scale.y + BUTTON_CORNER_SIZE, 0).scale(BUTTON_CORNER_SIZE, BUTTON_CORNER_SIZE, 1); // Bottom Left

		shader.setUniform("projection", camera.getProjection().mul(transform));
		switch (selectedState) {
			case STATE_SELECTED :
				sheet.bindTile(shader, BUTTON_CORNER_BOTTOM_LEFT_COL, BUTTON_CORNER_BOTTOM_LEFT_ROW);
				break;
			case STATE_CLICKED :
				sheet.bindTile(shader, BUTTON_CORNER_SELECTED_LEFT_COL, BUTTON_CORNER_BOTTOM_LEFT_ROW);
				break;
			default :
				sheet.bindTile(shader, BUTTON_CORNER_IDLE_LEFT_COL, BUTTON_CORNER_BOTTOM_LEFT_ROW);
				break;
		}
		Assets.getModel().render();

		transform.identity().translate(position.x + scale.x - BUTTON_CORNER_SIZE, position.y - scale.y + BUTTON_CORNER_SIZE, 0).scale(BUTTON_CORNER_SIZE, BUTTON_CORNER_SIZE, 1); // Bottom Right

		shader.setUniform("projection", camera.getProjection().mul(transform));
		switch (selectedState) {
			case STATE_SELECTED :
				sheet.bindTile(shader, BUTTON_CORNER_BOTTOM_RIGHT_COL, BUTTON_CORNER_BOTTOM_RIGHT_ROW);
				break;
			case STATE_CLICKED :
				sheet.bindTile(shader, BUTTON_CORNER_SELECTED_RIGHT_COL, BUTTON_CORNER_BOTTOM_RIGHT_ROW);
				break;
			default :
				sheet.bindTile(shader, BUTTON_CORNER_IDLE_RIGHT_COL, BUTTON_CORNER_BOTTOM_RIGHT_ROW);
				break;
		}
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

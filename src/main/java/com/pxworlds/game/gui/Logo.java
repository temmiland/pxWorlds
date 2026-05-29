package com.pxworlds.game.gui;

import com.pxworlds.game.assets.Assets;
import com.pxworlds.game.collision.AABB;
import com.pxworlds.game.collision.Collision;
import com.pxworlds.game.io.InputProcessor;
import com.pxworlds.game.rendering.Camera;
import com.pxworlds.game.rendering.Shader;
import com.pxworlds.game.rendering.tiles.TileSheet;
import org.joml.Matrix4f;
import org.joml.Vector2f;

public class Logo {

    /** Kollisions-AABB des Logos. */
    private AABB boundingBox;

    /** Transformationsmatrix für das Rendering. */
    private static Matrix4f transform = new Matrix4f();

    // Logo-Rendering-Konstanten
    /** Oberer Versatz für Logo-Rendering. */
    private static final int LOGO_TOP_OFFSET = 2;
    /** Unterer Versatz für Logo-Rendering. */
    private static final int LOGO_BOTTOM_OFFSET = -2;
    /** Tile-Index für den oberen Logo-Teil. */
    private static final int LOGO_TILE_TOP = 3;
    /** Tile-Index für den mittleren Logo-Teil. */
    private static final int LOGO_TILE_MIDDLE = 4;
    /** Tile-Index für den unteren Logo-Teil. */
    private static final int LOGO_TILE_BOTTOM = 5;

    // Logo-Zeilen-Daten
    /** Array mit Positionen und Tile-Indizes der Logo-Zeilen. */
    private static final int[][] LOGO_LINES = {
        {10, 0}, {8, 1}, {6, 2}, {4, 3}, {2, 4},
        {0, 5}, {-2, 6}, {-4, 7}, {-6, 8}, {-8, 9}, {-10, 10}
    };

    public Logo(Vector2f position, Vector2f scale) {
        this.boundingBox = new AABB(
            position.x - scale.x, position.y - scale.y,
            position.x + scale.x, position.y + scale.y
        );
    }

    /**
     * Aktualisiert das Logo (aktuell keine Interaktion).
     *
     * @param input    der aktuelle {@link InputProcessor}
     * @param mousePos transformierte Mausposition (Fenstermitte = Ursprung, Y oben)
     */
    public void update(final InputProcessor input, final Vector2f mousePos) {

    }

    public void render(Camera camera, TileSheet sheet, Shader shader) {
        for (int[] line : LOGO_LINES) {
            renderLine(line[0], line[1], camera, sheet, shader);
        }
    }

    private void renderLine(int factor, int sheetLine, Camera camera, TileSheet sheet, Shader shader) {
        final Vector2f position = boundingBox.getCenter();
        final Vector2f scale = boundingBox.getHalfExtent();

        transform.identity().translate(position.x - (scale.x * factor), position.y + (scale.y * LOGO_TOP_OFFSET), 0).scale(scale.x, scale.y, 1); // Top
        shader.setUniform("projection", camera.getProjection().mul(transform));
        sheet.bindTile(shader, sheetLine, LOGO_TILE_TOP);
        Assets.getModel().render();

        transform.identity().translate(position.x - (scale.x * factor), position.y, 0).scale(scale.x, scale.y, 1); // Middle/Fill
        shader.setUniform("projection", camera.getProjection().mul(transform));
        sheet.bindTile(shader, sheetLine, LOGO_TILE_MIDDLE);
        Assets.getModel().render();

        transform.identity().translate(position.x - (scale.x * factor), position.y - (scale.y * -LOGO_BOTTOM_OFFSET), 0).scale(scale.x, scale.y, 1); // Bottom
        shader.setUniform("projection", camera.getProjection().mul(transform));
        sheet.bindTile(shader, sheetLine, LOGO_TILE_BOTTOM);
        Assets.getModel().render();
    }

}

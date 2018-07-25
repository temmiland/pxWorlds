package com.pxworlds.game.gui;

import com.pxworlds.game.assets.Assets;
import com.pxworlds.game.collision.AABB;
import com.pxworlds.game.collision.Collision;
import com.pxworlds.game.io.Input;
import com.pxworlds.game.rendering.Camera;
import com.pxworlds.game.rendering.Shader;
import com.pxworlds.game.rendering.tiles.TileSheet;
import org.joml.Matrix4f;
import org.joml.Vector2f;

public class Logo {

    private AABB boundingBox;

    private static Matrix4f transform = new Matrix4f();

    public Logo(Vector2f position, Vector2f scale) {
        this.boundingBox = new AABB(position, scale);
    }

    public void update(Input input) {

    }

    public void render(Camera camera, TileSheet sheet, Shader shader) {
        renderLine(10, 0, camera, sheet, shader);
        renderLine(8, 1, camera, sheet, shader);
        renderLine(6, 2, camera, sheet, shader);
        renderLine(4, 3, camera, sheet, shader);
        renderLine(2, 4, camera, sheet, shader);
        renderLine(0, 5, camera, sheet, shader);
        renderLine(-2, 6, camera, sheet, shader);
        renderLine(-4, 7, camera, sheet, shader);
        renderLine(-6, 8, camera, sheet, shader);
        renderLine(-8, 9, camera, sheet, shader);
        renderLine(-10, 10, camera, sheet, shader);
    }

    private void renderLine(int factor, int sheetLine, Camera camera, TileSheet sheet, Shader shader) {
        Vector2f position = boundingBox.getCenter();
        Vector2f scale = boundingBox.getHalfExtent();

        transform.identity().translate(position.x - (scale.x * factor), position.y + (scale.y * 2), 0).scale(scale.x, scale.y, 1); // Top
        shader.setUniform("projection", camera.getProjection().mul(transform));
        sheet.bindTile(shader, sheetLine, 3);
        Assets.getModel().render();

        transform.identity().translate(position.x - (scale.x * factor), position.y, 0).scale(scale.x, scale.y, 1); // Middle/Fill
        shader.setUniform("projection", camera.getProjection().mul(transform));
        sheet.bindTile(shader, sheetLine, 4);
        Assets.getModel().render();

        transform.identity().translate(position.x - (scale.x * factor), position.y - (scale.y * 2), 0).scale(scale.x, scale.y, 1); // Bottom
        shader.setUniform("projection", camera.getProjection().mul(transform));
        sheet.bindTile(shader, sheetLine, 5);
        Assets.getModel().render();
    }

}

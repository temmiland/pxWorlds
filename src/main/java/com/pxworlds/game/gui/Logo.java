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

    private static final int STATE_IDLE = 0;
    private static final int STATE_SELECTED = 1;
    private static final int STATE_CLICKED = 2;

    private AABB boundingBox;
    private int selectedState;

    private static Matrix4f transform = new Matrix4f();

    public Logo(Vector2f position, Vector2f scale) {
        this.boundingBox = new AABB(position, scale);
        selectedState = STATE_IDLE;
    }

    public void update(Input input) {
        Collision data = boundingBox.getCollision(input.getMousePosition());

        if (data.isIntersecting) {
            selectedState = STATE_SELECTED;

            if (input.isMouseButtonDown(0)) {
                selectedState = STATE_CLICKED;
            }
        }
        else selectedState = STATE_IDLE;
    }

    public void render(Camera camera, TileSheet sheet, Shader shader) {
        Vector2f position = boundingBox.getCenter(), scale = boundingBox.getHalfExtent();
        transform.identity().translate(position.x, position.y, 0).scale(scale.x, scale.y, 1); // Middle/Fill

            shader.setUniform("projection", camera.getProjection().mul(transform));
            //TileRenderer rendering = new TileRenderer();
            //rendering.renderTile(Tile.BRICK, 50,50, shader, transform, camera);
            Assets.getModel().render();

        }


}

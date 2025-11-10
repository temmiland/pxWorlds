package com.pxworlds.game.states;

import com.pxworlds.game.io.Window;
import com.pxworlds.game.rendering.Camera;
import com.pxworlds.game.rendering.Shader;
import com.pxworlds.game.rendering.tiles.TileRenderer;
import com.pxworlds.game.world.World;

/**
 * Created by temmiland on 28.09.2017.
 */
public class LevelState extends GameState {

    /** The default world scale for level rendering. */
    private static final int DEFAULT_WORLD_SCALE = 48;

    /** The camera for rendering. */
    private Camera camera;
    /** The world being rendered. */
    private World world;
    /** The tile renderer. */
    private TileRenderer tiles;
    /** The shader for rendering. */
    private Shader shader;
    /** The window. */
    private Window window;

    public LevelState(GameStateManager gsm) {
        this.gsm = gsm;
    }

    @Override
    public void init(Window newWindow) {
        final Camera newCamera = new Camera(newWindow.getWidth(), newWindow.getHeight());
        final TileRenderer newTiles = new TileRenderer();
        final Shader newShader = new Shader("shader");
        final World newWorld = new World("test_level", newCamera, true, DEFAULT_WORLD_SCALE);

        this.camera = newCamera;
        this.world = newWorld;
        this.tiles = newTiles;
        this.shader = newShader;
        this.window = newWindow;

        newWorld.calculateView(newWindow);
    }

    @Override
    public void resize() {
        camera.setProjection(window.getWidth(), window.getHeight());
        world.calculateView(window);
    }

    @Override
    public void update(double frameCap) {
        world.update((float) frameCap, window, camera);
        world.correctCamera(camera, window);
    }

    @Override
    public void render() {
        /*shader.bind();
        shader.setUniform("sampler", 0);
        shader.setUniform("projection",
        camera.getProjection().mul(target));
        model.rendering();
        tex.bind(0);*/

        world.render(tiles, shader, camera);
    }
}

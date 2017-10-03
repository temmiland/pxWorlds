package com.pxworlds.game.states;

import com.pxworlds.game.io.Window;
import com.pxworlds.game.rendering.Camera;
import com.pxworlds.game.rendering.Shader;
import com.pxworlds.game.rendering.tiles.TileRenderer;
import com.pxworlds.game.world.World;

/**
 * Created by tompi on 28.09.2017.
 */
public class LevelState extends GameState {

    private Camera camera;
    private World world;
    private TileRenderer tiles;
    private Shader shader;
    private Window window;

    public LevelState(GameStateManager gsm) {
        this.gsm = gsm;
    }

    @Override
    public void init(Window window) {
        Camera camera = new Camera(window.getWidth(), window.getHeight());
        TileRenderer tiles = new TileRenderer();
        Shader shader = new Shader("shader");
        World world = new World("test_level", camera, true, 48);

        this.camera = camera;
        this.world = world;
        this.tiles = tiles;
        this.shader = shader;
        this.window = window;

        world.calculateView(window);
    }

    @Override
    public void resize() {
        camera.setProjection(window.getWidth(), window.getHeight());
        world.calculateView(window);
    }

    @Override
    public void update(double frame_cap) {
        world.update((float) frame_cap, window, camera);
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

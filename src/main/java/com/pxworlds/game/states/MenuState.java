package com.pxworlds.game.states;

import com.pxworlds.game.gui.MenuGui;
import com.pxworlds.game.io.Window;
import com.pxworlds.game.rendering.Camera;
import com.pxworlds.game.rendering.Shader;
import com.pxworlds.game.rendering.tiles.TileRenderer;
import com.pxworlds.game.world.World;

/**
 * Created by tompi on 28.09.2017.
 */
public class MenuState extends GameState {

    private MenuGui gui;

    private World world;
    private Camera camera;
    private TileRenderer tiles;
    private Shader shader;
    private Window window;

    public MenuState(GameStateManager gsm) {
        this.gsm = gsm;
    }

    @Override
    public void init(Window window) {
        Camera camera = new Camera(window.getWidth(), window.getHeight());
        TileRenderer tiles = new TileRenderer();
        Shader shader = new Shader("shader");
        World world = new World("demo_level", camera, false, 25);

        this.gui = new MenuGui(window, "menustate", 21);
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
        gui.resizeCamera();
    }

    @Override
    public void update(double frame_cap)  {
        world.update((float) frame_cap, window, camera);
        world.correctCamera(camera, window);
        gui.update(window.getInput());
    }

    @Override
    public void render() {
        world.render(tiles, shader, camera);
        gui.render();
    }
}

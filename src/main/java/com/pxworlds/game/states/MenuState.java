package com.pxworlds.game.states;

import com.pxworlds.game.gui.MenuGui;
import com.pxworlds.game.io.Window;
import com.pxworlds.game.rendering.Camera;
import com.pxworlds.game.rendering.Shader;
import com.pxworlds.game.rendering.tiles.TileRenderer;
import com.pxworlds.game.world.World;

/**
 * Created by temmiland on 28.09.2017.
 */
public class MenuState extends GameState {

    /** The default world scale for menu rendering. */
    private static final int MENU_WORLD_SCALE = 25;
    /** The default GUI scale for menu. */
    private static final int MENU_GUI_SCALE = 21;

    /** The menu GUI. */
    private MenuGui gui;

    /** The world being rendered. */
    private World world;
    /** The camera for rendering. */
    private Camera camera;
    /** The tile renderer. */
    private TileRenderer tiles;
    /** The shader for rendering. */
    private Shader shader;
    /** The window. */
    private Window window;

    public MenuState(GameStateManager gsm) {
        this.gsm = gsm;
    }

    @Override
    public void init(Window newWindow) {
        final Camera newCamera = new Camera(newWindow.getWidth(), newWindow.getHeight());
        final TileRenderer newTiles = new TileRenderer();
        final Shader newShader = new Shader("shader");
        final World newWorld = new World("demo_level", newCamera, false, MENU_WORLD_SCALE);

        this.gui = new MenuGui(newWindow, "menustate", MENU_GUI_SCALE);
        this.camera = newCamera;
        this.world = newWorld;
        this.tiles = newTiles;
        this.shader = newShader;
        this.window = newWindow;

        world.calculateView(newWindow);
    }

    @Override
    public void resize() {
        camera.setProjection(window.getWidth(), window.getHeight());
        world.calculateView(window);
        gui.resizeCamera();
    }

    @Override
    public void update(double frameCap)  {
        world.update((float) frameCap, window, camera);
        world.correctCamera(camera, window);
        gui.update(window.getInput());
    }

    @Override
    public void render() {
        world.render(tiles, shader, camera);
        gui.render();
    }
}

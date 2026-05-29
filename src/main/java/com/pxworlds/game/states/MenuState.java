package com.pxworlds.game.states;

import com.pxworlds.game.gui.MenuGui;
import com.pxworlds.game.io.Window;
import com.pxworlds.game.states.GameStateType;
import com.pxworlds.game.rendering.Camera;
import com.pxworlds.game.rendering.Shader;
import com.pxworlds.game.rendering.tiles.TileRenderer;
import com.pxworlds.game.world.World;
import com.pxworlds.game.world.WorldLoadingService;

/**
 * Created by temmiland on 28.09.2017.
 */
public class MenuState extends GameState {

    /** Name des Levels das als Menü-Hintergrund geladen wird. */
    private static final String WORLD_NAME = "test_level";
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

        final World newWorld = WorldLoadingService.load(WORLD_NAME, newCamera, false, getClass());

        this.gui = new MenuGui(newWindow, "menustate", MENU_GUI_SCALE,
                () -> gsm.setState(GameStateType.LEVEL));
        this.camera = newCamera;
        this.world = newWorld;
        this.tiles = newTiles;
        this.shader = newShader;
        this.window = newWindow;

        world.calculateView(newCamera);
    }

    @Override
    public void resize() {
        camera.setProjection(window.getWidth(), window.getHeight());
        world.calculateView(camera);
        gui.resizeCamera();
    }

    @Override
    public void update(double delta) {
        world.update((float) delta, window, camera);
        world.correctCamera(camera);
        gui.update(window.getInputProcessor(), window.getMousePosition(),
                   window.getWidth(), window.getHeight());
    }

    @Override
    public void render(float alpha) {
        world.render(tiles, shader, camera, alpha);
        gui.render();
    }
}

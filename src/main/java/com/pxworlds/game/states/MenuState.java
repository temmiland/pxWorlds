package com.pxworlds.game.states;

import com.pxworlds.Constants;
import com.pxworlds.game.gui.MenuGui;
import com.pxworlds.game.io.InputAction;
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
    /** Standard GUI-Skalierung für das Menü. */
    private static final int MENU_GUI_SCALE = 21;

    /** Menü-GUI. */
    private MenuGui gui;
    /** Debug-UI sichtbar (nur im Dev-Mode per F4 umschaltbar). */
    private boolean debugUiVisible = false;

    /** Die Hintergrundwelt. */
    private World world;
    /** Kamera für das Rendering. */
    private Camera camera;
    /** Tile-Renderer. */
    private TileRenderer tiles;
    /** Shader für das Rendering. */
    private Shader shader;
    /** Das Spielfenster. */
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

        this.camera = newCamera;
        this.world = newWorld;
        this.tiles = newTiles;
        this.shader = newShader;
        this.window = newWindow;
        rebuildGui();

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
        if (Constants.DEV_MODE && window.getInputProcessor().wasPressed(InputAction.DEBUG_UI_TOGGLE)) {
            debugUiVisible = !debugUiVisible;
            rebuildGui();
        }
        gui.update(window.getInputProcessor(), window.getMousePosition(),
                   window.getWidth(), window.getHeight());
    }

    @Override
    public void render(float alpha) {
        world.render(tiles, shader, camera, alpha);
    }

    @Override
    public void renderGui(final float alpha) {
        gui.render();
    }

    private void rebuildGui() {
        this.gui = new MenuGui(window, "menustate", MENU_GUI_SCALE,
                () -> gsm.setState(GameStateType.WORLD_SELECT),
                () -> gsm.showMigrationError("Debug: Simulated migration error for testing."),
                Constants.DEV_MODE && debugUiVisible);
    }
}

package temmiland.pxworlds.game.statemanagement.states;

import temmiland.pxworlds.game.Version;
import temmiland.pxworlds.game.gui.widgets.Logo;
import temmiland.pxworlds.game.gui.PxGuiInput;
import temmiland.pxworlds.game.gui.PxGuiRenderer;
import temmiland.pxworlds.game.gui.widgets.PxButtonSkin;
import temmiland.pxworlds.game.gui.widgets.PxTextFieldSkin;
import temmiland.pxworlds.game.io.InputAction;

import temmiland.pxworlds.game.statemanagement.PxGameStateManager;
import temmiland.rollercoaster.game.state.GameState;
import temmiland.pxworlds.game.statemanagement.PxGameStates;
import temmiland.pxworlds.game.rendering.Camera;
import temmiland.pxworlds.game.rendering.Shader;
import temmiland.pxworlds.game.rendering.tiles.TileRenderer;
import temmiland.pxworlds.game.world.World;
import temmiland.pxworlds.game.world.WorldLoadingService;
import temmiland.rollercoaster.gui.GuiContext;
import temmiland.rollercoaster.platform.Window;

public class MainMenuGameState extends GameState<PxGameStateManager> {

    private static final String WORLD_NAME = "test_level";
    private static final int MENU_GUI_SCALE = 21;

    // GUI-Layout-Konstanten (GUI-Koordinaten: Ursprung Mitte, Y oben)
    /** Y-Position des Logos. */
    private static final float LOGO_Y = 250f;
    /** Halbe Kachelgröße des Logos. */
    private static final float LOGO_SCALE = 64f;
    /** Halbe Breite der Hauptbuttons. */
    private static final float BUTTON_W = 200f;
    /** Halbe Höhe der Hauptbuttons. */
    private static final float BUTTON_H = 45f;
    /** Y-Position des Play-Buttons. */
    private static final float PLAY_Y = -50f;
    /** Y-Position des Optionen-Buttons. */
    private static final float OPTIONS_Y = -145f;
    /** Y-Position des Beenden-Buttons. */
    private static final float EXIT_Y = -240f;
    /** X-Position des Debug-Buttons. */
    private static final float DBG_X = 250f;
    /** Y-Position des Debug-Buttons. */
    private static final float DBG_Y = -290f;
    /** Halbe Breite des Debug-Buttons. */
    private static final float DBG_W = 45f;
    /** Halbe Höhe des Debug-Buttons. */
    private static final float DBG_H = 14f;

    private GuiContext gui;
    private PxGuiInput guiInput;
    private boolean debugUiVisible = false;
    private World world;
    private Camera camera;
    private TileRenderer tiles;
    private Shader shader;

    public MainMenuGameState(final PxGameStateManager gsm) {
        super(gsm);
    }

    @Override
    protected void onInit(final Window cWindow) {
        final Camera cCamera = new Camera(cWindow.getWidth(), cWindow.getHeight());
        final TileRenderer cTiles = new TileRenderer();
        final Shader cShader = new Shader("shader");
        final World cWorld = WorldLoadingService.load(WORLD_NAME, cCamera, false, getClass());

        this.camera = cCamera;
        this.world = cWorld;
        this.tiles = cTiles;
        this.shader = cShader;

        final PxGuiRenderer renderer = new PxGuiRenderer(cWindow, "menustate", MENU_GUI_SCALE);
        this.gui = new GuiContext(renderer, PxButtonSkin.INSTANCE, PxTextFieldSkin.INSTANCE);
        this.guiInput = new PxGuiInput(cWindow, renderer);

        world.calculateView(cCamera);
    }

    @Override
    protected void onResize() {
        camera.setProjection(window.getWidth(), window.getHeight());
        world.calculateView(camera);
        gui.resize(window.getWidth(), window.getHeight());
    }

    @Override
    protected void onUpdate(final double delta) {
        world.update((float) delta, window, camera);
        world.correctCamera(camera);
        if (Version.DEV_MODE && window.getInputProcessor().wasPressed(InputAction.DEBUG_UI_TOGGLE.getDefaultKey())) {
            debugUiVisible = !debugUiVisible;
        }
        gui.interact(guiInput, delta, this::buildGui);
    }

    @Override
    protected void onRender(final float alpha) {
        world.render(tiles, shader, camera, alpha);
    }

    @Override
    protected void onRenderGui(final float alpha) {
        gui.render(guiInput, this::buildGui);
    }

    private void buildGui(final GuiContext g) {
        Logo.draw(g, 0, LOGO_Y, LOGO_SCALE, LOGO_SCALE);

        if (g.button("Singleplayer", 0, PLAY_Y, BUTTON_W, BUTTON_H)) {
            gsm.setCurrentState(PxGameStates.WORLD_SELECT);
        }
        if (g.button("Options", 0, OPTIONS_Y, BUTTON_W, BUTTON_H)) {
            gsm.setCurrentState(PxGameStates.OPTIONS);
        }
        if (g.button("Exit", 0, EXIT_Y, BUTTON_W, BUTTON_H)) {
            window.requestClose();
        }

        if (Version.DEV_MODE && debugUiVisible
                && g.button("DBG:MigErr", DBG_X, DBG_Y, DBG_W, DBG_H)) {
            gsm.setMigrationErrorState("Debug: Simulated migration error for testing.");
        }
    }
}

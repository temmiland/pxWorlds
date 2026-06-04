package land.temmi.pxworlds.statemanagement.states;

import land.temmi.pxworlds.gui.widgets.Logo;
import land.temmi.pxworlds.Version;
import land.temmi.pxworlds.gui.PxGuiInput;
import land.temmi.pxworlds.gui.PxGuiRenderer;
import land.temmi.pxworlds.gui.widgets.PxButtonSkin;
import land.temmi.pxworlds.gui.widgets.PxTextFieldSkin;
import land.temmi.pxworlds.io.InputAction;

import land.temmi.pxworlds.statemanagement.PxGameStateManager;
import land.temmi.rollercoaster.game.state.GameState;
import land.temmi.pxworlds.statemanagement.PxGameStates;
import land.temmi.pxworlds.tiles.tile.WorldTiles;
import land.temmi.rollercoaster.gui.GuiContext;
import land.temmi.rollercoaster.platform.Window;

/**
 * Main menu screen.
 *
 * <p>Renders a tiled dark-brick background behind the logo and the menu buttons
 * (Singleplayer, Options, Exit) and, in dev mode, an optional debug button to
 * simulate a migration error.</p>
 */
public class MainMenuGameState extends GameState<PxGameStateManager> {

    /** GUI scale of the menu. */
    private static final int MENU_GUI_SCALE = 21;

    // GUI layout constants (GUI coordinates: origin at center, Y up)
    /** Y position of the logo. */
    private static final float LOGO_Y = 250f;
    /** Half tile size of the logo. */
    private static final float LOGO_SCALE = 64f;
    /** Half width of the main buttons. */
    private static final float BUTTON_W = 200f;
    /** Half height of the main buttons. */
    private static final float BUTTON_H = 45f;
    /** Y position of the play button. */
    private static final float PLAY_Y = -50f;
    /** Y position of the options button. */
    private static final float OPTIONS_Y = -145f;
    /** Y position of the exit button. */
    private static final float EXIT_Y = -240f;
    /** X position of the debug button. */
    private static final float DBG_X = 250f;
    /** Y position of the debug button. */
    private static final float DBG_Y = -290f;
    /** Half width of the debug button. */
    private static final float DBG_W = 45f;
    /** Half height of the debug button. */
    private static final float DBG_H = 14f;

    /** The immediate-mode GUI context. */
    private GuiContext gui;
    /** GUI input adapter. */
    private PxGuiInput guiInput;
    /** Whether the dev-mode debug UI is currently visible. */
    private boolean debugUiVisible = false;

    /**
     * Creates the main menu state.
     *
     * @param gsm the game state manager
     */
    public MainMenuGameState(final PxGameStateManager gsm) {
        super(gsm);
    }

    @Override
    protected void onInit(final Window window) {
        final PxGuiRenderer renderer = new PxGuiRenderer(window);
        this.gui = new GuiContext(renderer, PxButtonSkin.INSTANCE, PxTextFieldSkin.INSTANCE);
        this.guiInput = new PxGuiInput(window, renderer);
    }

    @Override
    protected void onResize() {
        gui.resize(window.getWidth(), window.getHeight());
    }

    @Override
    protected void onUpdate(final double delta) {
        if (Version.DEV_MODE && window.getInputProcessor().wasPressed(InputAction.DEBUG_UI_TOGGLE.getDefaultKey())) {
            debugUiVisible = !debugUiVisible;
        }
        gui.interact(guiInput, delta, this::buildGui);
    }

    @Override
    protected void onRender(final float alpha) { }

    @Override
    protected void onRenderGui(final float alpha) {
        gui.render(guiInput, this::buildGui);
    }

    /**
     * Builds the immediate-mode menu GUI for one frame.
     *
     * @param g the GUI context
     */
    private void buildGui(final GuiContext g) {
        g.background(WorldTiles.DARK_BRICK.getTexture());

        Logo.draw(g, 0, LOGO_Y, LOGO_SCALE, LOGO_SCALE);

        if (g.button("Singleplayer", 0, PLAY_Y, BUTTON_W, BUTTON_H)) {
            gsm.setCurrentState(PxGameStates.WORLD_SELECT);
        }
        if (g.button("Options", 0, OPTIONS_Y, BUTTON_W, BUTTON_H)) {
            gsm.setOptionsReturnState(PxGameStates.MENU);
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

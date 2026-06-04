package land.temmi.pxworlds.statemanagement.states;

import land.temmi.pxworlds.PxWorlds;
import land.temmi.pxworlds.gui.PxGuiInput;
import land.temmi.pxworlds.gui.PxGuiRenderer;
import land.temmi.pxworlds.gui.widgets.PxButtonSkin;
import land.temmi.pxworlds.gui.widgets.PxTextFieldSkin;

import land.temmi.pxworlds.tiles.tile.WorldTiles;
import land.temmi.pxworlds.statemanagement.PxGameStateManager;
import land.temmi.rollercoaster.game.state.GameState;
import land.temmi.pxworlds.statemanagement.PxGameStates;
import land.temmi.pxworlds.world.WorldLoadingService;
import land.temmi.rollercoaster.gui.GuiContext;
import land.temmi.rollercoaster.gui.widgets.textfield.TextField;
import land.temmi.rollercoaster.platform.Window;

import java.io.File;

/**
 * New-world creation screen.
 *
 * <p>Shows a text field for the world name plus Create/Back buttons, validates
 * the name (non-empty, not already existing) and, on success, creates the world
 * and enters it.</p>
 */
public class NewWorldGameState extends GameState<PxGameStateManager> {

    /** GUI scale of the screen. */
    private static final int GUI_SCALE = 21;

    // GUI layout constants
    /** Y position of the title. */
    private static final float TITLE_Y = 100f;
    /** Scale of the title. */
    private static final float TITLE_SCALE = 2.5f;
    /** Y position of the text field. */
    private static final float INPUT_Y = 20f;
    /** Half width of the text field. */
    private static final float INPUT_HW = 150f;
    /** Half height of the text field. */
    private static final float INPUT_HH = 22f;
    /** Y position of the error message. */
    private static final float ERROR_Y = -30f;
    /** Scale of the error message. */
    private static final float ERROR_SCALE = 1.8f;
    /** X position of the create button. */
    private static final float CREATE_X = -115f;
    /** X position of the back button. */
    private static final float BACK_X = 115f;
    /** Y position of the action buttons. */
    private static final float ACTION_Y = -100f;
    /** Half width of the action buttons. */
    private static final float ACTION_HW = 100f;
    /** Half height of the action buttons. */
    private static final float ACTION_HH = 22f;

    /** White. */
    private static final float WHITE = 1.0f;
    /** Error color red. */
    private static final float ERROR_R = 1.0f;
    /** Error color green/blue. */
    private static final float ERROR_GB = 0.3f;

    /** The immediate-mode GUI context. */
    private GuiContext gui;
    /** GUI input adapter. */
    private PxGuiInput guiInput;
    /** Text field for the world name. */
    private TextField nameInput;
    /** Current validation error message, or empty if none. */
    private String errorMessage = "";

    /**
     * Creates the new-world state.
     *
     * @param gsm the game state manager
     */
    public NewWorldGameState(final PxGameStateManager gsm) {
        super(gsm);
    }

    @Override
    protected void onInit(final Window window) {
        final PxGuiRenderer renderer = new PxGuiRenderer(window);
        this.gui = new GuiContext(renderer, PxButtonSkin.INSTANCE, PxTextFieldSkin.INSTANCE);
        this.guiInput = new PxGuiInput(window, renderer);
        this.nameInput = new TextField(0, INPUT_Y, INPUT_HW, INPUT_HH);
        this.errorMessage = "";
    }

    @Override
    protected void onResize() {
        gui.resize(window.getWidth(), window.getHeight());
    }

    @Override
    protected void onUpdate(final double delta) {
        gui.interact(guiInput, delta, this::buildGui);
    }

    @Override
    protected void onRender(final float alpha) { }

    @Override
    protected void onRenderGui(final float alpha) {
        gui.render(guiInput, this::buildGui);
    }

    /**
     * Builds the immediate-mode creation GUI for one frame.
     *
     * @param g the GUI context
     */
    private void buildGui(final GuiContext g) {
        g.background(WorldTiles.DARK_BRICK.getTexture());

        g.label("New World", 0, TITLE_Y, TITLE_SCALE, WHITE, WHITE, WHITE);

        if (g.textField(nameInput)) {
            onCreateConfirm();
        }

        if (g.button("Create", CREATE_X, ACTION_Y, ACTION_HW, ACTION_HH)) {
            onCreateConfirm();
        }
        if (g.button("Back", BACK_X, ACTION_Y, ACTION_HW, ACTION_HH)) {
            gsm.setCurrentState(PxGameStates.WORLD_SELECT);
        }

        if (!errorMessage.isEmpty()) {
            g.label(errorMessage, 0, ERROR_Y, ERROR_SCALE, ERROR_R, ERROR_GB, ERROR_GB);
        }
    }

    /** Validates the entered name and, if valid, creates and enters the world. */
    private void onCreateConfirm() {
        final String name = nameInput.getValue().trim();

        if (name.isEmpty()) {
            errorMessage = "Name must not be empty.";
            return;
        }

        final File target = new File(PxWorlds.WORLD_DIRECTORY, name + ".pxwl");
        if (target.exists()) {
            errorMessage = "A world with this name already exists.";
            return;
        }

        WorldLoadingService.createNew(name);
        gsm.setLevelState(name);
    }
}

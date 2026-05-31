package temmiland.pxworlds.game.statemanagement.states;

import temmiland.pxworlds.game.PxWorlds;
import temmiland.pxworlds.game.gui.PxGuiInput;
import temmiland.pxworlds.game.gui.PxGuiRenderer;
import temmiland.pxworlds.game.gui.widgets.PxButtonSkin;
import temmiland.pxworlds.game.gui.widgets.PxTextFieldSkin;

import temmiland.pxworlds.game.statemanagement.PxGameStateManager;
import temmiland.rollercoaster.game.state.GameState;
import temmiland.pxworlds.game.statemanagement.PxGameStates;
import temmiland.pxworlds.game.world.WorldLoadingService;
import temmiland.rollercoaster.gui.GuiContext;
import temmiland.rollercoaster.gui.widgets.textfield.TextField;
import temmiland.rollercoaster.platform.Window;

import java.io.File;

public class NewWorldGameState extends GameState<PxGameStateManager> {

    private static final int GUI_SCALE = 21;

    // GUI-Layout-Konstanten
    /** Y-Position des Titels. */
    private static final float TITLE_Y = 100f;
    /** Skalierung des Titels. */
    private static final float TITLE_SCALE = 2.5f;
    /** Y-Position des Textfelds. */
    private static final float INPUT_Y = 20f;
    /** Halbe Breite des Textfelds. */
    private static final float INPUT_HW = 150f;
    /** Halbe Höhe des Textfelds. */
    private static final float INPUT_HH = 22f;
    /** Y-Position der Fehlermeldung. */
    private static final float ERROR_Y = -30f;
    /** Skalierung der Fehlermeldung. */
    private static final float ERROR_SCALE = 1.8f;
    /** X-Position des Erstellen-Buttons. */
    private static final float CREATE_X = -115f;
    /** X-Position des Zurück-Buttons. */
    private static final float BACK_X = 115f;
    /** Y-Position der Aktions-Buttons. */
    private static final float ACTION_Y = -100f;
    /** Halbe Breite der Aktions-Buttons. */
    private static final float ACTION_HW = 100f;
    /** Halbe Höhe der Aktions-Buttons. */
    private static final float ACTION_HH = 22f;

    /** Weiß. */
    private static final float WHITE = 1.0f;
    /** Fehlerfarbe Rot. */
    private static final float ERROR_R = 1.0f;
    /** Fehlerfarbe Grün/Blau. */
    private static final float ERROR_GB = 0.3f;

    private GuiContext gui;
    private PxGuiInput guiInput;
    private TextField nameInput;
    private String errorMessage = "";

    public NewWorldGameState(final PxGameStateManager gsm) {
        super(gsm);
    }

    @Override
    protected void onInit(final Window newWindow) {
        final PxGuiRenderer renderer = new PxGuiRenderer(newWindow, "menustate", GUI_SCALE);
        this.gui = new GuiContext(renderer, PxButtonSkin.INSTANCE, PxTextFieldSkin.INSTANCE);
        this.guiInput = new PxGuiInput(newWindow, renderer);
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

    private void buildGui(final GuiContext g) {
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

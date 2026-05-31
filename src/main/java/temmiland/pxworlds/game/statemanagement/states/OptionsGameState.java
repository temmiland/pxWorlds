package temmiland.pxworlds.game.statemanagement.states;

import temmiland.pxworlds.game.gui.PxGuiInput;
import temmiland.pxworlds.game.gui.PxGuiRenderer;
import temmiland.pxworlds.game.gui.widgets.PxButtonSkin;
import temmiland.pxworlds.game.gui.widgets.PxTextFieldSkin;
import temmiland.pxworlds.game.statemanagement.PxGameStateManager;
import temmiland.pxworlds.game.statemanagement.PxGameStates;
import temmiland.rollercoaster.configuration.screen.ScreenConfiguration;
import temmiland.rollercoaster.game.state.GameState;
import temmiland.rollercoaster.gui.GuiContext;
import temmiland.rollercoaster.platform.Window;

public class OptionsGameState extends GameState<PxGameStateManager> {

    /** Kachelanzahl des GUI-Tilesheets. */
    private static final int GUI_SCALE = 21;

    // --- Auflösungs-Presets ---
    /** Breiten der auswählbaren Auflösungs-Presets. */
    private static final int[] RESOLUTION_WIDTHS  = {800, 1024, 1280, 1366, 1600, 1920, 2560};
    /** Höhen der auswählbaren Auflösungs-Presets. */
    private static final int[] RESOLUTION_HEIGHTS = {600,  768,  720,  768,  900, 1080, 1440};
    /** Anzeigetexte der Auflösungs-Presets. */
    private static final String[] RESOLUTION_LABELS = {
        "800 x 600", "1024 x 768", "1280 x 720",
        "1366 x 768", "1600 x 900", "1920 x 1080", "2560 x 1440"
    };
    /** Fallback-Index für unbekannte Auflösung (1280 x 720). */
    private static final int DEFAULT_RESOLUTION_INDEX = 2;

    // --- Fenstermodus-Presets ---
    /** Anzeigetexte des Fenstermodus. */
    private static final String[] WINDOW_MODE_LABELS = {"Fenster", "Vollbild"};
    /** Index des Vollbildmodus. */
    private static final int FULLSCREEN_MODE_INDEX = 1;

    // --- FPS-Presets ---
    /** 30 FPS. */
    private static final int FPS_30 = 30;
    /** 60 FPS. */
    private static final int FPS_60 = 60;
    /** 120 FPS. */
    private static final int FPS_120 = 120;
    /** 144 FPS. */
    private static final int FPS_144 = 144;
    /** 240 FPS. */
    private static final int FPS_240 = 240;
    /** Auswählbare FPS-Werte. */
    private static final int[] FPS_VALUES = {FPS_30, FPS_60, FPS_120, FPS_144, FPS_240};
    /** Anzeigetexte der FPS-Presets. */
    private static final String[] FPS_LABELS = {"30 FPS", "60 FPS", "120 FPS", "144 FPS", "240 FPS"};
    /** Fallback-Index für unbekannte FPS (60). */
    private static final int DEFAULT_FPS_INDEX = 1;

    // --- Layout: Titel ---
    /** Y-Position des Titels. */
    private static final float TITLE_Y     = 230f;
    /** Schriftgröße des Titels. */
    private static final float TITLE_SCALE =   2.5f;

    // --- Layout: Optionszeilen (Label über Pfeilreihe) ---
    /** Y-Position der Auflösungs-Beschriftung. */
    private static final float ROW1_LABEL_Y =  155f;
    /** Y-Position der Auflösungs-Pfeilreihe. */
    private static final float ROW1_Y       =  110f;
    /** Y-Position der Fenstermodus-Beschriftung. */
    private static final float ROW2_LABEL_Y =   50f;
    /** Y-Position der Fenstermodus-Pfeilreihe. */
    private static final float ROW2_Y       =    5f;
    /** Y-Position der FPS-Beschriftung. */
    private static final float ROW3_LABEL_Y =  -55f;
    /** Y-Position der FPS-Pfeilreihe. */
    private static final float ROW3_Y       = -100f;
    /** Schriftgröße der Zeilenbeschriftungen. */
    private static final float LABEL_SCALE  =    2.0f;
    /** X-Position des linken Pfeil-Buttons. */
    private static final float ARROW_L_X    = -140f;
    /** X-Position des rechten Pfeil-Buttons. */
    private static final float ARROW_R_X    =  140f;
    /** Halbe Breite der Pfeil-Buttons. */
    private static final float ARROW_HW     =   28f;
    /** Halbe Höhe der Pfeil-Buttons. */
    private static final float ARROW_HH     =   22f;
    /** Schriftgröße des aktuellen Wertelabels. */
    private static final float VALUE_SCALE  =    2.2f;

    // --- Layout: Hinweistext ---
    /** Y-Position des FPS-Neustart-Hinweises. */
    private static final float HINT_Y     = -145f;
    /** Schriftgröße des Hinweistextes. */
    private static final float HINT_SCALE =    1.8f;

    // --- Layout: Aktions-Buttons (nebeneinander) ---
    /** Y-Position der Aktions-Buttons. */
    private static final float ACTION_Y  = -220f;
    /** X-Position des Übernehmen-Buttons (links). */
    private static final float APPLY_X   = -115f;
    /** X-Position des Zurück-Buttons (rechts). */
    private static final float BACK_X    =  115f;
    /** Halbe Breite der Aktions-Buttons. */
    private static final float ACTION_HW =  100f;
    /** Halbe Höhe der Aktions-Buttons. */
    private static final float ACTION_HH =   22f;

    // --- Farben ---
    /** Weiß. */
    private static final float WHITE       = 1.0f;
    /** Grün-Kanal der Bernstein-Hinweisfarbe. */
    private static final float COL_HINT_G  = 0.75f;
    /** Blau-Kanal der Bernstein-Hinweisfarbe. */
    private static final float COL_HINT_B  =  0.2f;
    /** Blau-Kanal der Wertefarbe (gelber Ton). */
    private static final float COL_VAL_B   =  0.4f;

    private GuiContext gui;
    private PxGuiInput guiInput;

    private int resolutionIndex;
    private int windowModeIndex;
    private int fpsIndex;
    private int originalFpsIndex;

    public OptionsGameState(final PxGameStateManager gsm) {
        super(gsm);
    }

    @Override
    protected void onInit(final Window cWindow) {
        final PxGuiRenderer renderer = new PxGuiRenderer(cWindow, "menustate", GUI_SCALE);
        this.gui      = new GuiContext(renderer, PxButtonSkin.INSTANCE, PxTextFieldSkin.INSTANCE);
        this.guiInput = new PxGuiInput(cWindow, renderer);
        loadSettingsFromConfig();
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
        g.label("Einstellungen", 0, TITLE_Y, TITLE_SCALE, WHITE, WHITE, WHITE);

        // Auflösung
        g.label("Aufloesung", 0, ROW1_LABEL_Y, LABEL_SCALE, WHITE, WHITE, WHITE);
        if (g.button("<", ARROW_L_X, ROW1_Y, ARROW_HW, ARROW_HH)) {
            resolutionIndex = (resolutionIndex - 1 + RESOLUTION_WIDTHS.length) % RESOLUTION_WIDTHS.length;
        }
        g.label(RESOLUTION_LABELS[resolutionIndex], 0, ROW1_Y, VALUE_SCALE, WHITE, WHITE, COL_VAL_B);
        if (g.button(">", ARROW_R_X, ROW1_Y, ARROW_HW, ARROW_HH)) {
            resolutionIndex = (resolutionIndex + 1) % RESOLUTION_WIDTHS.length;
        }

        // Fenstermodus
        g.label("Fenstermodus", 0, ROW2_LABEL_Y, LABEL_SCALE, WHITE, WHITE, WHITE);
        if (g.button("<", ARROW_L_X, ROW2_Y, ARROW_HW, ARROW_HH)) {
            windowModeIndex = (windowModeIndex - 1 + WINDOW_MODE_LABELS.length) % WINDOW_MODE_LABELS.length;
        }
        g.label(WINDOW_MODE_LABELS[windowModeIndex], 0, ROW2_Y, VALUE_SCALE, WHITE, WHITE, COL_VAL_B);
        if (g.button(">", ARROW_R_X, ROW2_Y, ARROW_HW, ARROW_HH)) {
            windowModeIndex = (windowModeIndex + 1) % WINDOW_MODE_LABELS.length;
        }

        // FPS-Limit
        g.label("FPS-Limit", 0, ROW3_LABEL_Y, LABEL_SCALE, WHITE, WHITE, WHITE);
        if (g.button("<", ARROW_L_X, ROW3_Y, ARROW_HW, ARROW_HH)) {
            fpsIndex = (fpsIndex - 1 + FPS_VALUES.length) % FPS_VALUES.length;
        }
        g.label(FPS_LABELS[fpsIndex], 0, ROW3_Y, VALUE_SCALE, WHITE, WHITE, COL_VAL_B);
        if (g.button(">", ARROW_R_X, ROW3_Y, ARROW_HW, ARROW_HH)) {
            fpsIndex = (fpsIndex + 1) % FPS_VALUES.length;
        }

        if (fpsIndex != originalFpsIndex) {
            g.label("* FPS-Limit wirkt nach Neustart", 0, HINT_Y, HINT_SCALE, WHITE, COL_HINT_G, COL_HINT_B);
        }

        if (g.button("Uebernehmen", APPLY_X, ACTION_Y, ACTION_HW, ACTION_HH)) {
            applySettings();
            gsm.setCurrentState(PxGameStates.MENU);
        }
        if (g.button("Zurueck", BACK_X, ACTION_Y, ACTION_HW, ACTION_HH)) {
            gsm.setCurrentState(PxGameStates.MENU);
        }
    }

    private void loadSettingsFromConfig() {
        final ScreenConfiguration sc = gsm.getConfigurationStorage().getScreenConfiguration();
        resolutionIndex = findResolutionIndex(sc.getLastWidth(), sc.getLastHeight());
        if (sc.isFullscreen()) {
            windowModeIndex = FULLSCREEN_MODE_INDEX;
        } else {
            windowModeIndex = 0;
        }
        fpsIndex = findFpsIndex(sc.getFrameRate());
        originalFpsIndex = fpsIndex;
    }

    private void applySettings() {
        final ScreenConfiguration sc = gsm.getConfigurationStorage().getScreenConfiguration();
        final int w = RESOLUTION_WIDTHS[resolutionIndex];
        final int h = RESOLUTION_HEIGHTS[resolutionIndex];
        final boolean fullscreen = windowModeIndex == FULLSCREEN_MODE_INDEX;
        final int fps = FPS_VALUES[fpsIndex];

        window.applyDisplayMode(fullscreen, w, h);

        sc.setLastWidth(w)
          .setLastHeight(h)
          .setFullscreen(fullscreen)
          .setFrameRate(fps);

        gsm.getConfigurationStorage().saveScreenConfiguration();
        originalFpsIndex = fpsIndex;
    }

    private int findResolutionIndex(final int cWidth, final int cHeight) {
        for (int i = 0; i < RESOLUTION_WIDTHS.length; i++) {
            if (RESOLUTION_WIDTHS[i] == cWidth && RESOLUTION_HEIGHTS[i] == cHeight) {
                return i;
            }
        }
        return DEFAULT_RESOLUTION_INDEX;
    }

    private int findFpsIndex(final int cFps) {
        for (int i = 0; i < FPS_VALUES.length; i++) {
            if (FPS_VALUES[i] == cFps) {
                return i;
            }
        }
        return DEFAULT_FPS_INDEX;
    }
}

package land.temmi.pxworlds.statemanagement.states;

import land.temmi.pxworlds.gui.PxGuiInput;
import land.temmi.pxworlds.gui.PxGuiRenderer;
import land.temmi.pxworlds.gui.widgets.PxButtonSkin;
import land.temmi.pxworlds.gui.widgets.PxTextFieldSkin;
import land.temmi.pxworlds.tiles.tile.WorldTiles;
import land.temmi.pxworlds.statemanagement.PxGameStateManager;
import land.temmi.rollercoaster.config.screen.ScreenConfiguration;
import land.temmi.rollercoaster.game.state.GameState;
import land.temmi.rollercoaster.gui.GuiContext;
import land.temmi.rollercoaster.platform.Window;

/**
 * Options/settings screen.
 *
 * <p>Lets the player cycle through resolution, window mode, VSync and FPS-limit
 * presets via arrow buttons, loads the current values from the screen
 * configuration on entry and applies/persists them on confirm.</p>
 */
public class OptionsGameState extends GameState<PxGameStateManager> {

    /** Tile count of the GUI tilesheet. */
    private static final int GUI_SCALE = 21;

    // --- Resolution presets ---
    /** Widths of the selectable resolution presets. */
    private static final int[] RESOLUTION_WIDTHS  = {800, 1024, 1280, 1366, 1600, 1920, 2560};
    /** Heights of the selectable resolution presets. */
    private static final int[] RESOLUTION_HEIGHTS = {600,  768,  720,  768,  900, 1080, 1440};
    /** Display texts of the resolution presets. */
    private static final String[] RESOLUTION_LABELS = {
        "800 x 600", "1024 x 768", "1280 x 720",
        "1366 x 768", "1600 x 900", "1920 x 1080", "2560 x 1440"
    };
    /** Fallback index for an unknown resolution (1280 x 720). */
    private static final int DEFAULT_RESOLUTION_INDEX = 2;

    // --- Window mode presets ---
    /** Display texts of the window mode. */
    private static final String[] WINDOW_MODE_LABELS = {"Windowed", "Fullscreen"};
    /** Index of the fullscreen mode. */
    private static final int FULLSCREEN_MODE_INDEX = 1;

    // --- VSync presets ---
    /** Display texts of the VSync option. */
    private static final String[] VSYNC_LABELS = {"Off", "On"};

    // --- FPS presets ---
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
    /** Unlimited FPS (no frame cap). */
    private static final int FPS_UNLIMITED = 0;
    /** Selectable FPS values. */
    private static final int[] FPS_VALUES = {FPS_30, FPS_60, FPS_120, FPS_144, FPS_240, FPS_UNLIMITED};
    /** Display texts of the FPS presets. */
    private static final String[] FPS_LABELS = {"30 FPS", "60 FPS", "120 FPS", "144 FPS", "240 FPS", "Unlimited"};
    /** Fallback index for an unknown FPS value (60). */
    private static final int DEFAULT_FPS_INDEX = 1;

    // --- Layout: title ---
    /** Y position of the title. */
    private static final float TITLE_Y     = 235f;
    /** Font size of the title. */
    private static final float TITLE_SCALE =   2.5f;

    // --- Layout: option rows (label above arrow row) ---
    /** Y position of the resolution label. */
    private static final float ROW1_LABEL_Y =  170f;
    /** Y position of the resolution arrow row. */
    private static final float ROW1_Y       =  132f;
    /** Y position of the window mode label. */
    private static final float ROW2_LABEL_Y =   92f;
    /** Y position of the window mode arrow row. */
    private static final float ROW2_Y       =   54f;
    /** Y position of the VSync label. */
    private static final float ROW3_LABEL_Y =   14f;
    /** Y position of the VSync arrow row. */
    private static final float ROW3_Y       =  -24f;
    /** Y position of the FPS label. */
    private static final float ROW4_LABEL_Y =  -64f;
    /** Y position of the FPS arrow row. */
    private static final float ROW4_Y       = -102f;
    /** Font size of the row labels. */
    private static final float LABEL_SCALE  =    2.0f;
    /** X position of the left arrow button. */
    private static final float ARROW_L_X    = -140f;
    /** X position of the right arrow button. */
    private static final float ARROW_R_X    =  140f;
    /** Half width of the arrow buttons. */
    private static final float ARROW_HW     =   28f;
    /** Half height of the arrow buttons. */
    private static final float ARROW_HH     =   22f;
    /** Font size of the current value label. */
    private static final float VALUE_SCALE  =    2.2f;

    // --- Layout: hint text ---
    /** Y position of the FPS hint. */
    private static final float HINT_Y     = -148f;
    /** Font size of the hint text. */
    private static final float HINT_SCALE =    1.8f;

    // --- Layout: action buttons (side by side) ---
    /** Y position of the action buttons. */
    private static final float ACTION_Y  = -215f;
    /** X position of the apply button (left). */
    private static final float APPLY_X   = -115f;
    /** X position of the back button (right). */
    private static final float BACK_X    =  115f;
    /** Half width of the action buttons. */
    private static final float ACTION_HW =  100f;
    /** Half height of the action buttons. */
    private static final float ACTION_HH =   22f;

    // --- Colors ---
    /** White. */
    private static final float WHITE       = 1.0f;
    /** Green channel of the amber hint color. */
    private static final float COL_HINT_G  = 0.75f;
    /** Blue channel of the amber hint color. */
    private static final float COL_HINT_B  =  0.2f;
    /** Blue channel of the value color (yellow tone). */
    private static final float COL_VAL_B   =  0.4f;

    /** The immediate-mode GUI context. */
    private GuiContext gui;
    /** GUI input adapter. */
    private PxGuiInput guiInput;

    /** Currently selected resolution preset index. */
    private int resolutionIndex;
    /** Currently selected window mode index. */
    private int windowModeIndex;
    /** Currently selected FPS preset index. */
    private int fpsIndex;
    /** Whether VSync is currently enabled. */
    private boolean vsyncEnabled;

    /**
     * Creates the options state.
     *
     * @param gsm the game state manager
     */
    public OptionsGameState(final PxGameStateManager gsm) {
        super(gsm);
    }

    @Override
    protected void onInit(final Window window) {
        final PxGuiRenderer renderer = new PxGuiRenderer(window);
        this.gui      = new GuiContext(renderer, PxButtonSkin.INSTANCE, PxTextFieldSkin.INSTANCE);
        this.guiInput = new PxGuiInput(window, renderer);
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

    /**
     * Builds the immediate-mode settings GUI for one frame.
     *
     * @param g the GUI context
     */
    private void buildGui(final GuiContext g) {
        g.background(WorldTiles.DARK_BRICK.getTexture());

        g.label("Settings", 0, TITLE_Y, TITLE_SCALE, WHITE, WHITE, WHITE);

        // Resolution
        g.label("Resolution", 0, ROW1_LABEL_Y, LABEL_SCALE, WHITE, WHITE, WHITE);
        if (g.button("<", ARROW_L_X, ROW1_Y, ARROW_HW, ARROW_HH)) {
            resolutionIndex = (resolutionIndex - 1 + RESOLUTION_WIDTHS.length) % RESOLUTION_WIDTHS.length;
        }
        g.label(RESOLUTION_LABELS[resolutionIndex], 0, ROW1_Y, VALUE_SCALE, WHITE, WHITE, COL_VAL_B);
        if (g.button(">", ARROW_R_X, ROW1_Y, ARROW_HW, ARROW_HH)) {
            resolutionIndex = (resolutionIndex + 1) % RESOLUTION_WIDTHS.length;
        }

        // Window mode
        g.label("Window Mode", 0, ROW2_LABEL_Y, LABEL_SCALE, WHITE, WHITE, WHITE);
        if (g.button("<", ARROW_L_X, ROW2_Y, ARROW_HW, ARROW_HH)) {
            windowModeIndex = (windowModeIndex - 1 + WINDOW_MODE_LABELS.length) % WINDOW_MODE_LABELS.length;
        }
        g.label(WINDOW_MODE_LABELS[windowModeIndex], 0, ROW2_Y, VALUE_SCALE, WHITE, WHITE, COL_VAL_B);
        if (g.button(">", ARROW_R_X, ROW2_Y, ARROW_HW, ARROW_HH)) {
            windowModeIndex = (windowModeIndex + 1) % WINDOW_MODE_LABELS.length;
        }

        // VSync
        g.label("VSync", 0, ROW3_LABEL_Y, LABEL_SCALE, WHITE, WHITE, WHITE);
        if (g.button("<", ARROW_L_X, ROW3_Y, ARROW_HW, ARROW_HH)) {
            vsyncEnabled = !vsyncEnabled;
        }
        g.label(VSYNC_LABELS[vsyncEnabled ? 1 : 0], 0, ROW3_Y, VALUE_SCALE, WHITE, WHITE, COL_VAL_B);
        if (g.button(">", ARROW_R_X, ROW3_Y, ARROW_HW, ARROW_HH)) {
            vsyncEnabled = !vsyncEnabled;
        }

        // FPS limit
        g.label("FPS Limit", 0, ROW4_LABEL_Y, LABEL_SCALE, WHITE, WHITE, WHITE);
        if (g.button("<", ARROW_L_X, ROW4_Y, ARROW_HW, ARROW_HH)) {
            fpsIndex = (fpsIndex - 1 + FPS_VALUES.length) % FPS_VALUES.length;
        }
        g.label(FPS_LABELS[fpsIndex], 0, ROW4_Y, VALUE_SCALE, WHITE, WHITE, COL_VAL_B);
        if (g.button(">", ARROW_R_X, ROW4_Y, ARROW_HW, ARROW_HH)) {
            fpsIndex = (fpsIndex + 1) % FPS_VALUES.length;
        }

        if (vsyncEnabled) {
            g.label("* FPS limit only applies without VSync", 0, HINT_Y, HINT_SCALE, WHITE, COL_HINT_G, COL_HINT_B);
        }

        if (g.button("Apply", APPLY_X, ACTION_Y, ACTION_HW, ACTION_HH)) {
            applySettings();
            gsm.setCurrentState(gsm.getOptionsReturnState());
        }
        if (g.button("Back", BACK_X, ACTION_Y, ACTION_HW, ACTION_HH)) {
            gsm.setCurrentState(gsm.getOptionsReturnState());
        }
    }

    /** Loads the current settings from the persistent screen configuration. */
    private void loadSettingsFromConfig() {
        final ScreenConfiguration sc = gsm.getConfigurationStorage().getScreenConfiguration();
        resolutionIndex = findResolutionIndex(sc.getLastWidth(), sc.getLastHeight());
        if (sc.isFullscreen()) {
            windowModeIndex = FULLSCREEN_MODE_INDEX;
        } else {
            windowModeIndex = 0;
        }
        fpsIndex = findFpsIndex(sc.getFrameRate());
        vsyncEnabled = sc.isVsync();
    }

    /** Applies the currently selected settings to the window and persists them. */
    private void applySettings() {
        final ScreenConfiguration sc = gsm.getConfigurationStorage().getScreenConfiguration();
        final int w = RESOLUTION_WIDTHS[resolutionIndex];
        final int h = RESOLUTION_HEIGHTS[resolutionIndex];
        final boolean fullscreen = windowModeIndex == FULLSCREEN_MODE_INDEX;
        final int fps = FPS_VALUES[fpsIndex];

        window.applyDisplayMode(fullscreen, w, h);
        window.setVSync(vsyncEnabled);

        sc.setLastWidth(w)
          .setLastHeight(h)
          .setFullscreen(fullscreen)
          .setFrameRate(fps)
          .setVsync(vsyncEnabled);

        gsm.getConfigurationStorage().saveScreenConfiguration();
    }

    /**
     * Returns the index of the resolution preset matching the given dimensions,
     * or {@link #DEFAULT_RESOLUTION_INDEX} if not found.
     *
     * @param width  the width to look up
     * @param height the height to look up
     * @return the preset index
     */
    private int findResolutionIndex(final int width, final int height) {
        for (int i = 0; i < RESOLUTION_WIDTHS.length; i++) {
            if (RESOLUTION_WIDTHS[i] == width && RESOLUTION_HEIGHTS[i] == height) {
                return i;
            }
        }
        return DEFAULT_RESOLUTION_INDEX;
    }

    /**
     * Returns the index of the FPS preset matching the given value,
     * or {@link #DEFAULT_FPS_INDEX} if not found.
     *
     * @param fps the FPS value to look up
     * @return the preset index
     */
    private int findFpsIndex(final int fps) {
        for (int i = 0; i < FPS_VALUES.length; i++) {
            if (FPS_VALUES[i] == fps) {
                return i;
            }
        }
        return DEFAULT_FPS_INDEX;
    }
}

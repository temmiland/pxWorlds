package land.temmi.pxworlds.statemanagement.states;

import land.temmi.pxworlds.gui.PxGuiInput;
import land.temmi.pxworlds.gui.PxGuiRenderer;
import land.temmi.pxworlds.gui.widgets.PxButtonSkin;
import land.temmi.pxworlds.gui.widgets.PxTextFieldSkin;
import land.temmi.pxworlds.io.InputAction;

import land.temmi.pxworlds.statemanagement.PxGameStateManager;
import land.temmi.rollercoaster.game.state.GameState;
import land.temmi.pxworlds.statemanagement.PxGameStates;
import land.temmi.rollercoaster.graphics.Camera;
import land.temmi.pxworlds.tiles.TileRenderer;
import land.temmi.pxworlds.world.World;
import land.temmi.pxworlds.world.WorldDebugRenderer;
import land.temmi.pxworlds.world.WorldLoadingService;
import land.temmi.rollercoaster.gui.GuiContext;
import land.temmi.rollercoaster.platform.Window;

/**
 * In-world gameplay screen.
 *
 * <p>Loads and renders the selected world, drives entity updates and the
 * camera, and provides a pause menu (Resume, Options, Main Menu). When entering
 * the options menu the world state is preserved instead of being reloaded.</p>
 */
public class LevelGameState extends GameState<PxGameStateManager> {

    /** GUI scale of the pause menu. */
    private static final int PAUSE_GUI_SCALE = 21;

    // Pause GUI layout constants
    /** Half width of the pause buttons. */
    private static final float BUTTON_W = 200f;
    /** Half height of the pause buttons. */
    private static final float BUTTON_H = 45f;
    /** Y position of the resume button. */
    private static final float RESUME_Y = 80f;
    /** Y position of the options button. */
    private static final float OPTIONS_Y = -15f;
    /** Y position of the main menu button. */
    private static final float MAIN_MENU_Y = -110f;

    /** Name of the world to load and play. */
    private String worldName;
    /** Camera for the world. */
    private Camera camera;
    /** The loaded world. */
    private World world;
    /** Tile renderer for the world. */
    private TileRenderer tiles;
    /** The immediate-mode GUI context (pause menu). */
    private GuiContext gui;
    /** GUI input adapter. */
    private PxGuiInput guiInput;
    /** Whether the game is currently paused. */
    private boolean paused;
    /** Renderer for entity/collision debug boxes. */
    private WorldDebugRenderer debugRenderer;
    /**
     * Whether {@link #onInit} should reload the world from disk. {@code true} on
     * a normal world entry; {@code false} when returning from the options menu,
     * so that position, entities and pause state are preserved.
     */
    private boolean reloadWorldOnInit = true;

    /**
     * Creates the level state.
     *
     * @param gsm the game state manager
     */
    public LevelGameState(final PxGameStateManager gsm) {
        super(gsm);
    }

    /**
     * Sets the world to load and forces a reload on the next init.
     *
     * @param worldName the name of the world to load
     */
    public void setWorldName(final String worldName) {
        this.worldName = worldName;
        this.reloadWorldOnInit = true;
    }

    @Override
    protected void onInit(final Window window) {
        // Returning from the options menu: keep the running state, do not reload.
        if (!reloadWorldOnInit && world != null) {
            reloadWorldOnInit = true;
            return;
        }

        // Release old OpenGL resources before creating new ones —
        // prevents a GPU leak on repeated init() (e.g. state transition).
        if (tiles  != null) tiles.close();
        if (world  != null) world.close();

        final Camera camera = new Camera(window.getWidth(), window.getHeight());
        final TileRenderer tiles = new TileRenderer();
        final World world = WorldLoadingService.load(worldName, camera, true, getClass());

        this.camera = camera;
        this.tiles  = tiles;
        this.world  = world;
        this.paused = false;

        final PxGuiRenderer renderer = new PxGuiRenderer(window);
        this.gui = new GuiContext(renderer, PxButtonSkin.INSTANCE, PxTextFieldSkin.INSTANCE);
        this.guiInput = new PxGuiInput(window, renderer);

        this.debugRenderer = new WorldDebugRenderer(world);
        world.calculateView(camera);
    }

    @Override
    protected void onResize() {
        camera.setProjection(window.getWidth(), window.getHeight());
        world.calculateView(camera);
        gui.resize(window.getWidth(), window.getHeight());
    }

    @Override
    protected void onUpdate(final double delta) {
        if (window.getInputProcessor().wasPressed(InputAction.PAUSE.getDefaultKey())) {
            paused = !paused;
            if (paused) {
                camera.savePrevPosition();
                world.snapAllEntities();
                world.pauseAllAnimations();
            } else {
                world.resumeAllAnimations();
            }
            return;
        }

        if (paused) {
            gui.interact(guiInput, delta, this::buildGui);
            return;
        }

        world.update((float) delta, window, camera);
        world.correctCamera(camera);
    }

    @Override
    protected void onRender(final float alpha) {
        world.render(tiles, camera, alpha);
    }

    @Override
    protected void onRenderGui(final float alpha) {
        if (paused) {
            gui.render(guiInput, this::buildGui);
        }
    }

    @Override
    protected void onRenderDebug(final float alpha) {
        debugRenderer.renderDebugBoxes(camera, alpha);
    }

    @Override
    protected String[] onDebugLines() {
        return world.getDebugLines(camera);
    }

    @Override
    protected void onSave() {
        WorldLoadingService.save(world, worldName);
    }

    /**
     * Builds the immediate-mode pause menu GUI for one frame.
     *
     * @param g the GUI context
     */
    private void buildGui(final GuiContext g) {
        if (g.button("Resume", 0, RESUME_Y, BUTTON_W, BUTTON_H)) {
            paused = false;
        }
        if (g.button("Options", 0, OPTIONS_Y, BUTTON_W, BUTTON_H)) {
            reloadWorldOnInit = false;
            gsm.setOptionsReturnState(PxGameStates.LEVEL);
            gsm.setCurrentState(PxGameStates.OPTIONS);
        }
        if (g.button("Main Menu", 0, MAIN_MENU_Y, BUTTON_W, BUTTON_H)) {
            onSave();
            gsm.setCurrentState(PxGameStates.MENU);
        }
    }
}

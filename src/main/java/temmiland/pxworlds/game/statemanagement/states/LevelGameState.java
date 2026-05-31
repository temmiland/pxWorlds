package temmiland.pxworlds.game.statemanagement.states;

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
import temmiland.pxworlds.game.world.WorldDebugRenderer;
import temmiland.pxworlds.game.world.WorldLoadingService;
import temmiland.rollercoaster.gui.GuiContext;
import temmiland.rollercoaster.platform.Window;

public class LevelGameState extends GameState<PxGameStateManager> {

    private static final int PAUSE_GUI_SCALE = 21;

    // Pause-GUI-Layout-Konstanten
    /** Halbe Breite der Pause-Buttons. */
    private static final float BUTTON_W = 200f;
    /** Halbe Höhe der Pause-Buttons. */
    private static final float BUTTON_H = 45f;
    /** Y-Position des Weiter-Buttons. */
    private static final float RESUME_Y = 80f;
    /** Y-Position des Optionen-Buttons. */
    private static final float OPTIONS_Y = -15f;
    /** Y-Position des Hauptmenü-Buttons. */
    private static final float MAIN_MENU_Y = -110f;

    private String worldName;
    private Camera camera;
    private World world;
    private TileRenderer tiles;
    private Shader shader;
    private GuiContext gui;
    private PxGuiInput guiInput;
    private boolean paused;
    private WorldDebugRenderer debugRenderer;

    public LevelGameState(final PxGameStateManager gsm) {
        super(gsm);
    }

    public void setWorldName(final String newWorldName) {
        this.worldName = newWorldName;
    }

    @Override
    protected void onInit(final Window newWindow) {
        // Alte OpenGL-Ressourcen freigeben bevor neue erzeugt werden —
        // verhindert GPU-Leak bei wiederholtem init() (z.B. State-Transition).
        if (tiles  != null) tiles.close();
        if (shader != null) shader.close();
        if (world  != null) world.close();

        final Camera newCamera = new Camera(newWindow.getWidth(), newWindow.getHeight());
        final TileRenderer newTiles = new TileRenderer();
        final Shader newShader = new Shader("shader");
        final World newWorld = WorldLoadingService.load(worldName, newCamera, true, getClass());

        this.camera = newCamera;
        this.tiles  = newTiles;
        this.shader = newShader;
        this.world  = newWorld;
        this.paused = false;

        final PxGuiRenderer renderer = new PxGuiRenderer(newWindow, "menustate", PAUSE_GUI_SCALE);
        this.gui = new GuiContext(renderer, PxButtonSkin.INSTANCE, PxTextFieldSkin.INSTANCE);
        this.guiInput = new PxGuiInput(newWindow, renderer);

        this.debugRenderer = new WorldDebugRenderer(newWorld);
        newWorld.calculateView(newCamera);
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
        world.render(tiles, shader, camera, alpha);
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
    protected String[] onGetDebugLines() {
        return world.getDebugLines(camera);
    }

    @Override
    protected void onSave() {
        WorldLoadingService.save(world, worldName);
    }

    private void buildGui(final GuiContext g) {
        if (g.button("Resume", 0, RESUME_Y, BUTTON_W, BUTTON_H)) {
            paused = false;
        }
        g.button("Options", 0, OPTIONS_Y, BUTTON_W, BUTTON_H);
        if (g.button("Main Menu", 0, MAIN_MENU_Y, BUTTON_W, BUTTON_H)) {
            onSave();
            gsm.setCurrentState(PxGameStates.MENU);
        }
    }
}

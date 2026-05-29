package com.pxworlds.game.states;

import com.pxworlds.game.gui.PauseGui;
import com.pxworlds.game.io.InputAction;
import com.pxworlds.game.io.Window;
import com.pxworlds.game.states.GameStateType;
import com.pxworlds.game.rendering.Camera;
import com.pxworlds.game.rendering.Shader;
import com.pxworlds.game.rendering.tiles.TileRenderer;
import com.pxworlds.game.world.World;
import com.pxworlds.game.world.WorldDebugRenderer;
import com.pxworlds.game.world.WorldLoadingService;

/**
 * Created by temmiland on 28.09.2017.
 */
public class LevelState extends GameState {

    /** Pause-Menu GUI scale (gleich wie MenuGui). */
    private static final int PAUSE_GUI_SCALE = 21;

    /** Name des aktuellen Levels (Unterordner unter {@code /levels/} und Dateiname ohne Endung). */
    private final String worldName;

    /** The camera for rendering. */
    private Camera camera;
    /** The world being rendered. */
    private World world;
    /** The tile renderer. */
    private TileRenderer tiles;
    /** The shader for rendering. */
    private Shader shader;
    /** The window. */
    private Window window;
    /** Pause-Menü GUI. */
    private PauseGui pauseGui;
    /** Ob das Spiel pausiert ist. */
    private boolean paused;
    /** Debug-Renderer für Kollisionsrahmen und Chunk-Grenzen. */
    private WorldDebugRenderer debugRenderer;

    public LevelState(GameStateManager gsm, String worldName) {
        this.gsm = gsm;
        this.worldName = worldName;
    }

    @Override
    public void init(Window newWindow) {
        // Alte OpenGL-Ressourcen freigeben bevor neue erzeugt werden —
        // verhindert GPU-Leak bei wiederholtem init() (z.B. State-Transition).
        if (tiles  != null) {
            tiles.close();
        }
        if (shader != null) {
            shader.close();
        }
        if (world  != null) {
            world.close();
        }

        final Camera newCamera = new Camera(newWindow.getWidth(), newWindow.getHeight());
        final TileRenderer newTiles = new TileRenderer();
        final Shader newShader = new Shader("shader");

        final World newWorld = WorldLoadingService.load(worldName, newCamera, true, getClass());

        this.camera = newCamera;
        this.tiles  = newTiles;
        this.shader = newShader;
        this.world  = newWorld;
        this.window = newWindow;
        this.paused = false;
        this.pauseGui = new PauseGui(newWindow, "menustate", PAUSE_GUI_SCALE,
                () -> paused = false,
                () -> { save(); gsm.setState(GameStateType.MENU); });
        this.debugRenderer = new WorldDebugRenderer(newWorld);

        newWorld.calculateView(newCamera);
    }

    @Override
    public void resize() {
        camera.setProjection(window.getWidth(), window.getHeight());
        world.calculateView(camera);
        pauseGui.resizeCamera();
    }

    @Override
    public void update(double delta) {
        if (window.getInputProcessor().wasPressed(InputAction.PAUSE)) {
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
            pauseGui.update(window.getInputProcessor(), window.getMousePosition(),
                    window.getWidth(), window.getHeight());
            return;
        }

        world.update((float) delta, window, camera);
        world.correctCamera(camera);
    }

    @Override
    public void render(float alpha) {
        world.render(tiles, shader, camera, alpha);
    }

    @Override
    public void renderGui(final float alpha) {
        if (paused) {
            pauseGui.render();
        }
    }

    /**
     * Zeichnet Kollisions-AABBs aller Tiles und Entities über das Spielbild.
     * Wird nur im F3-Debug-Modus aufgerufen.
     *
     * @param alpha Interpolationsfaktor 0.0–1.0
     */
    @Override
    public void renderDebug(final float alpha) {
        debugRenderer.renderDebugBoxes(camera, alpha);
    }

    /**
     * Liefert Chunk- und Tile-Positions-Infos aus der aktuellen Kamera-Perspektive.
     * @return Debug-Zeilen für das F3-Panel
     */
    @Override
    public String[] getDebugLines() {
        return world.getDebugLines(camera);
    }

    @Override
    public void save() {
        WorldLoadingService.save(world, worldName);
    }
}

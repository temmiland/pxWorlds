package com.pxworlds.game.states;

import com.pxworlds.Constants;
import com.pxworlds.game.gui.PauseGui;
import com.pxworlds.game.io.InputAction;
import com.pxworlds.game.io.Window;
import com.pxworlds.game.states.GameStateType;
import com.pxworlds.game.rendering.Camera;
import com.pxworlds.game.rendering.Shader;
import com.pxworlds.game.rendering.tiles.TileRenderer;
import com.pxworlds.game.world.World;
import com.pxworlds.game.world.WorldIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by temmiland on 28.09.2017.
 */
public class LevelState extends GameState {

    private static final Logger LOGGER = LoggerFactory.getLogger(LevelState.class);

    /** Name des aktuellen Levels (Unterordner unter {@code /levels/} und Dateiname ohne Endung). */
    private static final String WORLD_NAME = "test_level";

    /** Pause-Menu GUI scale (gleich wie MenuGui). */
    private static final int PAUSE_GUI_SCALE = 21;

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

    public LevelState(GameStateManager gsm) {
        this.gsm = gsm;
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

        // Lade-Reihenfolge:
        //  1. User-Datei  (~/.pxworlds/worlds/<name>.pxwl) — enthält Letztposition
        //  2. Classpath-Resource (/levels/<name>.pxwl)     — initiales Level
        final File userFile = new File(Constants.WORLDS_DIRECTORY_PATH, WORLD_NAME + ".pxwl");
        final World newWorld;
        try {
            newWorld = loadFromPxwl(userFile.exists()
                    ? new java.io.FileInputStream(userFile)
                    : getClass().getResourceAsStream("/levels/" + WORLD_NAME + ".pxwl"),
                    newCamera);
        } catch (IOException e) {
            throw new RuntimeException("Kann Level '" + WORLD_NAME + "' nicht laden", e);
        }

        this.camera = newCamera;
        this.tiles  = newTiles;
        this.shader = newShader;
        this.world  = newWorld;
        this.window = newWindow;
        this.paused = false;
        this.pauseGui = new PauseGui(newWindow, "menustate", PAUSE_GUI_SCALE,
                () -> paused = false,
                () -> { save(); gsm.setState(GameStateType.MENU); });

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
        world.renderDebugBoxes(camera, alpha);
    }

    /**
     * Liefert Chunk- und Tile-Positions-Infos aus der aktuellen Kamera-Perspektive.
     * @return Debug-Zeilen für das F3-Panel
     */
    @Override
    public String[] getDebugLines() {
        return world.getDebugLines(camera);
    }

    /**
     * Lädt eine Welt aus dem gegebenen Stream und spawnt den Spieler.
     *
     * <ul>
     *   <li>Ist ein Spawn in der Datei gespeichert (Version ≥ 2), wird
     *       {@link World#spawnPlayer} verwendet — das nutzt automatisch die
     *       Lastpos (Version 3) wenn vorhanden.</li>
     *   <li>{@code stream == null} wirft eine IOException.</li>
     * </ul>
     *
     * @param stream  Quell-Stream der .pxwl-Datei; wird nach dem Lesen geschlossen
     * @param camera  wird auf die Startposition gesetzt
     * @return geladene, fertig initialisierte Welt
     * @throws IOException wenn stream null ist oder das Format ungültig ist
     */
    private World loadFromPxwl(final InputStream stream, final Camera camera) throws IOException {
        if (stream == null) {
            throw new IOException("Kein .pxwl-Stream verfügbar für '" + WORLD_NAME + "'");
        }
        try (InputStream s = stream) {
            final World w = WorldIO.load(s);
            w.spawnPlayer(camera, true);
            LOGGER.info("Level '{}' geladen (Lastpos={}, SpawnTile={}/{})",
                    WORLD_NAME, w.hasLastPos(),
                    w.getSpawnTileX(), w.getSpawnTileY());
            return w;
        }
    }

    /**
     * Speichert die aktuelle Welt als {@code <worldName>.pxwl} in
     * {@link Constants#WORLDS_DIRECTORY_PATH}. Wird beim Schließen des Spiels
     * aufgerufen.
     *
     * <p>Ist die Welt noch nicht initialisiert (z.B. wenn der LevelState
     * nie aktiv war), wird nichts gespeichert.</p>
     */
    @Override
    public void save() {
        if (world == null) {
            return;
        }
        final File dir = new File(Constants.WORLDS_DIRECTORY_PATH);
        dir.mkdirs();
        final File file = new File(dir, WORLD_NAME + ".pxwl");
        world.capturePlayerPosition();
        try (FileOutputStream fos = new FileOutputStream(file)) {
            WorldIO.save(world, fos);
            LOGGER.info("Welt gespeichert: {}", file.getAbsolutePath());
        } catch (IOException e) {
            LOGGER.error("Fehler beim Speichern der Welt '{}'", WORLD_NAME, e);
        }
    }
}

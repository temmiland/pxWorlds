package com.pxworlds.game.states;

import com.pxworlds.Constants;
import com.pxworlds.game.gui.MenuGui;
import com.pxworlds.game.io.Window;
import com.pxworlds.game.rendering.Camera;
import com.pxworlds.game.rendering.Shader;
import com.pxworlds.game.rendering.tiles.TileRenderer;
import com.pxworlds.game.world.World;
import com.pxworlds.game.world.WorldIO;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by temmiland on 28.09.2017.
 */
public class MenuState extends GameState {

    /** Name des Levels das als Menü-Hintergrund geladen wird. */
    private static final String WORLD_NAME = "test_level";
    /** The default GUI scale for menu. */
    private static final int MENU_GUI_SCALE = 21;

    /** The menu GUI. */
    private MenuGui gui;

    /** The world being rendered. */
    private World world;
    /** The camera for rendering. */
    private Camera camera;
    /** The tile renderer. */
    private TileRenderer tiles;
    /** The shader for rendering. */
    private Shader shader;
    /** The window. */
    private Window window;

    public MenuState(GameStateManager gsm) {
        this.gsm = gsm;
    }

    @Override
    public void init(Window newWindow) {
        final Camera newCamera = new Camera(newWindow.getWidth(), newWindow.getHeight());
        final TileRenderer newTiles = new TileRenderer();
        final Shader newShader = new Shader("shader");

        final File userFile = new File(Constants.WORLDS_DIRECTORY_PATH, WORLD_NAME + ".pxwl");
        final World newWorld;
        try {
            final InputStream stream = userFile.exists()
                    ? new FileInputStream(userFile)
                    : getClass().getResourceAsStream("/levels/" + WORLD_NAME + ".pxwl");
            if (stream == null) {
                throw new IOException("Kein .pxwl-Stream verfügbar für '" + WORLD_NAME + "'");
            }
            try (InputStream s = stream) {
                newWorld = WorldIO.load(s);
                newWorld.spawnPlayer(newCamera, false);
            }
        } catch (IOException e) {
            throw new RuntimeException("Kann Menü-Level '" + WORLD_NAME + "' nicht laden", e);
        }

        this.gui = new MenuGui(newWindow, "menustate", MENU_GUI_SCALE);
        this.camera = newCamera;
        this.world = newWorld;
        this.tiles = newTiles;
        this.shader = newShader;
        this.window = newWindow;

        world.calculateView(newCamera);
    }

    @Override
    public void resize() {
        camera.setProjection(window.getWidth(), window.getHeight());
        world.calculateView(camera);
        gui.resizeCamera();
    }

    @Override
    public void update(double delta) {
        if (window.getInputProcessor().wasPressed(GLFW_KEY_ESCAPE)) {
            glfwSetWindowShouldClose(window.getWindow(), true);
            return;
        }
        world.update((float) delta, window, camera);
        world.correctCamera(camera);
        gui.update(window.getInputProcessor(), window.getMousePosition(),
                   window.getWidth(), window.getHeight());
    }

    @Override
    public void render(float alpha) {
        world.render(tiles, shader, camera, alpha);
        gui.render();
    }
}

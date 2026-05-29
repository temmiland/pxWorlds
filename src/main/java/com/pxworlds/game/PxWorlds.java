package com.pxworlds.game;

import com.pxworlds.Constants;
import com.pxworlds.configuration.ConfigurationStorage;
import com.pxworlds.game.assets.Assets;
import com.pxworlds.game.debug.DebugOverlay;
import com.pxworlds.game.io.Window;
import com.pxworlds.game.states.GameStateManager;
import org.lwjgl.opengl.GL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class PxWorlds {
	/** Logger dieser Klasse. */
	private static final Logger LOGGER = LoggerFactory.getLogger(PxWorlds.class);

	/** Fenstertitel. */
	private String title;
	/** Fensterbreite in Pixeln. */
	private int width;
	/** Fensterhöhe in Pixeln. */
	private int height;
	/** Ob das Fenster im Vollbildmodus ist. */
	private boolean fullscreen;
	/** Ziel-FPS. */
	private int fpsRate;
	/** Game-State-Manager. */
	private GameStateManager gsm;
	/** Konfigurations-Storage für Speichern beim Beenden. */
	private ConfigurationStorage configurationStorage;

	public PxWorlds(String newTitle, int newWidth, int newHeight, boolean newFullscreen, int newFpsRate, ConfigurationStorage newConfigurationStorage) {
		this.title = newTitle;
		this.width = newWidth;
		this.height = newHeight;
		this.fullscreen = newFullscreen;
        this.fpsRate = newFpsRate;
		this.gsm = new GameStateManager();
		this.configurationStorage = newConfigurationStorage;
	}

	public GameStateManager getGameStateManager() {
		return gsm;
	}

	public void run() {
		Window.setCallbacks();

		if (!glfwInit()) {
			LOGGER.error("GLFW Failed to initialize!");
			throw new RuntimeException("Failed to initialize GLFW");
		}

		final Window window = new Window();
		window.setSize(width, height);
		window.setFullscreen(fullscreen);
		window.createWindow(title);

		initOpenGL(window);
		Assets.initAsset();
		gsm.init(window);

		final DebugOverlay debugOverlay = new DebugOverlay();
		final GameLoop loop = new GameLoop(gsm, debugOverlay, fpsRate, fromShutdownHook -> {
			gsm.save();
		});

		try {
			loop.run(window);
		} finally {
			Assets.deleteAsset();
			configurationStorage.getScreenConfiguration()
					.setLastWidth(window.getWidth())
					.setLastHeight(window.getHeight());
			configurationStorage.saveAllConfigurations();
			window.cleanUp();
			glfwTerminate();
		}
	}

	/**
	 * Initialises OpenGL capabilities and base render state for the given window.
	 * @param window das initialisierte GLFW-Fenster
	 */
	private void initOpenGL(final Window window) {
		GL.createCapabilities();
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		glViewport(0, 0, window.getFramebufferWidth(), window.getFramebufferHeight());
	}

}

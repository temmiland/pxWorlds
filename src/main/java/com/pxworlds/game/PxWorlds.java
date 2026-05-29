package com.pxworlds.game;

import com.pxworlds.Bootstrap;
import com.pxworlds.Constants;
import com.pxworlds.game.assets.Assets;
import com.pxworlds.game.debug.DebugOverlay;
import com.pxworlds.game.states.GameStateManager;
import com.pxworlds.game.io.Timer;
import com.pxworlds.game.io.Window;
import org.lwjgl.opengl.GL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class PxWorlds {
	/** The logger for PxWorlds. */
	private static final Logger LOGGER = LoggerFactory.getLogger(PxWorlds.class);

    /** The singleton instance of PxWorlds. */
    private static PxWorlds instance;
	/** The title of the window. */
	private String title;
	/** The width of the window. */
	private int width;
	/** The height of the window. */
	private int height;
	/** Whether the window is in fullscreen mode. */
	private boolean fullscreen;
	/** The target FPS rate. */
	private int fpsRate;
	/** The game state manager. */
	private GameStateManager gsm;

    /** Wiederverwendbares leeres Array — vermeidet Heap-Allokation pro Render-Frame. */
    private static final String[] NO_DEBUG_LINES = new String[0];

	public PxWorlds(String newTitle, int newWidth, int newHeight, boolean newFullscreen, int newFpsRate) {
	    instance = this;

		this.title = newTitle;
		this.width = newWidth;
		this.height = newHeight;
		this.fullscreen = newFullscreen;
        this.fpsRate = newFpsRate;
		this.gsm = new GameStateManager();
	}

	public static PxWorlds getInstance() {
	    return instance;
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

		GL.createCapabilities();

		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		glViewport(0, 0, window.getFramebufferWidth(), window.getFramebufferHeight());

        Assets.initAsset();

        gsm.init(window);

        // Shutdown-Hook: sichert Spielstand auch bei IDE-Stop (SIGTERM).
        // AtomicBoolean verhindert Doppel-Save wenn der Loop normal endet.
        final AtomicBoolean saveGuard = new AtomicBoolean(false);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (saveGuard.compareAndSet(false, true)) {
                LOGGER.info("Shutdown-Hook: Spielstand wird gesichert...");
                gsm.save();
            }
        }, "pxworlds-save-hook"));

        final DebugOverlay debugOverlay = new DebugOverlay();

        // Logik läuft fest mit TICK_RATE, Rendering mit fpsRate — entkoppelt
		final double tickDuration = 1.0 / Constants.TICK_RATE;
		final double renderCap   = 1.0 / fpsRate;

		double tickAccumulator   = 0;
		double renderAccumulator = 0;
		double frameTime         = 0;
		int frames               = 0;

		double time = Timer.getTime();

        try {
            while (!window.shouldClose()) {

                final double now    = Timer.getTime();
                final double passed = now - time;
                time = now;

                tickAccumulator   += passed;
                renderAccumulator += passed;
                frameTime         += passed;

                // Spiral-of-Death-Schutz: bei Lag-Spikes maximale Nachholzeit begrenzen
                if (tickAccumulator > tickDuration * Constants.MAX_UPDATES_PER_FRAME) {
                    tickAccumulator = tickDuration * Constants.MAX_UPDATES_PER_FRAME;
                }

                // --- Logik: fixer Zeitschritt (TICK_RATE TPS) ---
                while (tickAccumulator >= tickDuration) {
                    tickAccumulator -= tickDuration;

                    window.update();

                    if (window.hasResized()) {
                        gsm.resize();
                        glViewport(0, 0, window.getFramebufferWidth(), window.getFramebufferHeight());
                    }

                    if (window.getInputProcessor().wasPressed(GLFW_KEY_F3)) {
                        debugOverlay.toggleDetailed();
                    }

                    gsm.update(tickDuration);
                }

                // --- Rendering: fpsRate cap ---
                if (renderAccumulator >= renderCap) {
                    renderAccumulator -= renderCap;

                    // Wie weit sind wir zwischen letztem und nächstem Tick? (0.0 – 1.0)
                    final float alpha = (float) (tickAccumulator / tickDuration);

                    glClear(GL_COLOR_BUFFER_BIT);
                    gsm.render(alpha);
                    final String[] debugLines;
                    if (debugOverlay.isShowDetailed()) {
                        gsm.renderDebug(alpha);
                        debugLines = gsm.getDebugLines(); // nur wenn F3-Panel sichtbar
                    } else {
                        debugLines = NO_DEBUG_LINES;
                    }
                    debugOverlay.render(window, alpha, debugLines);
                    window.swapBuffers();
                    frames++;

                    if (frameTime >= 1.0) {
                        frameTime = 0;
                        debugOverlay.updateFps(frames);
                        frames = 0;
                    }
                }
            }
        } finally {
            // Normaler Exit-Pfad: saveGuard verhindert Doppel-Save mit dem Shutdown-Hook
            if (saveGuard.compareAndSet(false, true)) {
                gsm.save();
            }
            Assets.deleteAsset();
            Bootstrap.getInstance().getConfigurationStorage().saveAllConfigurations();
            window.cleanUp();
            glfwTerminate();
        }
	}

}

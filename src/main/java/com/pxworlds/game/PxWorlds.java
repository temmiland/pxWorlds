package com.pxworlds.game;

import com.pxworlds.Bootstrap;
import com.pxworlds.game.assets.Assets;
import com.pxworlds.game.states.GameStateManager;
import com.pxworlds.game.io.Timer;
import com.pxworlds.game.io.Window;
import org.lwjgl.opengl.GL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	public GameStateManager gsm;

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

        Assets.initAsset();

        gsm.init(window);

		final double frameCap = 1.0 / fpsRate;

		double frameTime = 0;
		int frames = 0;

		double time = Timer.getTime();
		double unprocessed = 0;

		while (!window.shouldClose()) {

		    boolean canRender = false;

			final double time2 = Timer.getTime();
			final double passed = time2 - time;

            unprocessed += passed;
			frameTime += passed;
            time = time2;

            while (unprocessed >= frameCap) {
				if (window.hasResized()) {
				    gsm.resize();
					glViewport(0, 0, window.getWidth(), window.getHeight());
				}

				unprocessed -= frameCap;
				canRender = true;
                gsm.update(frameCap);

				if (window.getInput().isKeyReleased(GLFW_KEY_ESCAPE)) {
					glfwSetWindowShouldClose(window.getWindow(), true);
				}

				window.update();

				if (frameTime >= 1.0) {
					frameTime = 0;
					//TODO System.out.println("FPS: " + frames);
					frames = 0;
				}
			}

			if (canRender) {
				glClear(GL_COLOR_BUFFER_BIT);

				gsm.render();

				window.swapBuffers();
				frames++;
			}
		}

		Assets.deleteAsset();
        Bootstrap.getInstance().getConfigurationStorage().saveAllConfigurations();
		glfwTerminate();
	}

}

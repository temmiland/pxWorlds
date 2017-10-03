package com.pxworlds.game;

import com.pxworlds.Bootstrap;
import com.pxworlds.game.assets.Assets;
import com.pxworlds.game.states.GameStateManager;
import com.pxworlds.game.io.Timer;
import com.pxworlds.game.io.Window;
import org.lwjgl.opengl.GL;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class PxWorlds {

    private static PxWorlds instance;
	private String title;
	private int width;
	private int height;
	private boolean fullscreen;
	private int fpsRate;
	public GameStateManager gsm;

	public PxWorlds(String title, int width, int height, boolean fullscreen, int fpsRate) {
	    instance = this;

		this.title = title;
		this.width = width;
		this.height = height;
		this.fullscreen = fullscreen;
        this.fpsRate = fpsRate;
		this.gsm = new GameStateManager();
	}

	public static PxWorlds getInstance() {
	    return instance;
    }

	public void run() {

		Window.setCallbacks();
		
		if (!glfwInit()) {
			System.err.println("GLFW Failed to initialize!");
			System.exit(1);
		}
		
		Window window = new Window();
		window.setSize(width, height);
		window.setFullscreen(fullscreen);
		window.createWindow(title);
		
		GL.createCapabilities();
		
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_TEXTURE_2D);

        Assets.initAsset();

        gsm.init(window);

		double frame_cap = 1.0 / fpsRate;
		
		double frame_time = 0;
		int frames = 0;
		
		double time = Timer.getTime();
		double unprocessed = 0;

		while (!window.shouldClose()) {

		    boolean can_render = false;
			
			double time_2 = Timer.getTime();
			double passed = time_2 - time;

            unprocessed += passed;
			frame_time += passed;
            time = time_2;

            while (unprocessed >= frame_cap) {
				if (window.hasResized()) {
				    gsm.resize();
					glViewport(0, 0, window.getWidth(), window.getHeight());
				}
				
				unprocessed -= frame_cap;
				can_render = true;
                gsm.update(frame_cap);

				if (window.getInput().isKeyReleased(GLFW_KEY_ESCAPE)) {
					glfwSetWindowShouldClose(window.getWindow(), true);
				}
				
				window.update();
				
				if (frame_time >= 1.0) {
					frame_time = 0;
					System.out.println("FPS: " + frames);
					frames = 0;
				}
			}
			
			if (can_render) {
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

package com.pxworlds.game.io;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;

import org.joml.Vector2f;
import org.lwjgl.glfw.*;

public class Window {
	/** The default window width. */
	private static final int DEFAULT_WIDTH = 640;
	/** The default window height. */
	private static final int DEFAULT_HEIGHT = 480;
	/** The GLFW window handle. */
	private long window;

	/** The width of the window. */
	private int width, height;
	/** Whether the window is in fullscreen mode. */
	private boolean fullscreen;
	/** Whether the window has been resized. */
	private boolean hasResized;
	/** The callback for window size changes. */
	private GLFWWindowSizeCallback windowSizeCallback;

	/** Event-Produzent: GLFW-Callbacks → InputEvent-Queue. */
	private InputHandler inputHandler;
	/** Event-Konsument: übersetzt InputEvents in GLFW-freie Spielaktionen. */
	private InputProcessor inputProcessor;

	public static void setCallbacks() {
		glfwSetErrorCallback(new GLFWErrorCallbackI() {
			@Override
			public void invoke(int error, long description) {
				throw new IllegalStateException(GLFWErrorCallback.getDescription(description));
			}
		});
	}

	private void setLocalCallbacks() {
		windowSizeCallback = new GLFWWindowSizeCallback() {
			@Override
			public void invoke(long argWindow, int argWidth, int argHeight) {
				width = argWidth;
				height = argHeight;
				hasResized = true;
			}
		};

		glfwSetWindowSizeCallback(window, windowSizeCallback);
	}

	public Window() {
		setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
		setFullscreen(false);
		hasResized = false;
	}

	public void createWindow(String title) {
		final long monitor;
		if (fullscreen) {
			monitor = glfwGetPrimaryMonitor();
		} else {
			monitor = 0;
		}

		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
		window = glfwCreateWindow(width, height, title, monitor, 0);

		if (window == 0) {
            throw new IllegalStateException("Failed to create window!");
        }

		if (!fullscreen) {
			final GLFWVidMode vid = glfwGetVideoMode(glfwGetPrimaryMonitor());
			glfwSetWindowPos(window, (vid.width() - width) / 2, (vid.height() - height) / 2);
		}

		// Callbacks VOR glfwShowWindow registrieren, damit initiale Resize-Events
		// (z.B. durch macOS Window-Server) nicht verloren gehen.
		inputHandler = new InputHandler();
		inputHandler.registerCallbacks(window);
		inputProcessor = new InputProcessor();
		setLocalCallbacks();

		glfwShowWindow(window);
		glfwMakeContextCurrent(window);

		// Tatsächliche Fenstergröße von GLFW abfragen — kann auf macOS von der
		// angeforderten Größe abweichen (z.B. durch System-Constraints).
		final int[] w = new int[1];
		final int[] h = new int[1];
		glfwGetWindowSize(window, w, h);
		this.width  = w[0];
		this.height = h[0];
	}

	/**
	 * Gibt alle Callbacks und Fenster-Ressourcen frei.
	 * Muss vor {@code glfwTerminate()} aufgerufen werden.
	 */
	public void cleanUp() {
		// InputHandler-Callbacks zuerst detachen und freigeben,
		// damit glfwFreeCallbacks() sie nicht nochmals freed (Double-Free)
		inputHandler.free();
		glfwFreeCallbacks(window);
	}

	public boolean shouldClose() {
		return glfwWindowShouldClose(window);
	}

	public void requestClose() {
		glfwSetWindowShouldClose(window, true);
	}

	public void swapBuffers() {
		glfwSwapBuffers(window);
	}

	public void setSize(int newWidth, int newHeight) {
		this.width = newWidth;
		this.height = newHeight;
	}

	public void setFullscreen(boolean newFullscreen) {
		this.fullscreen = newFullscreen;
	}

	public void update() {
		hasResized = false;
		glfwPollEvents(); // Callbacks feuern → Events in die Queue schreiben
		inputProcessor.processEvents(inputHandler.getEventQueue()); // Queue leeren & Aktionen ableiten
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public boolean hasResized() {
		return hasResized;
	}

	public boolean isFullscreen() {
		return fullscreen;
	}

	public long getWindow() {
		return window;
	}

	/** @return Framebuffer-Breite in Pixeln (auf Retina-Displays ≠ Fensterbreite). */
	public int getFramebufferWidth() {
		final int[] w = new int[1];
		glfwGetFramebufferSize(window, w, null);
		return w[0];
	}

	/** @return Framebuffer-Höhe in Pixeln (auf Retina-Displays ≠ Fensterhöhe). */
	public int getFramebufferHeight() {
		final int[] h = new int[1];
		glfwGetFramebufferSize(window, null, h);
		return h[0];
	}

	/**
	 * Convenience-Methode: gibt die Mausposition im GUI-Koordinatensystem zurück
	 * (Fenstermitte = Ursprung, Y-Achse nach oben).
	 *
	 * @return transformierte Mausposition
	 */
	public Vector2f getMousePosition() {
		return inputProcessor.getMousePosition(width, height);
	}

	/**
	 * @return den {@link InputProcessor} für GLFW-freie Spielaktionen
	 */
	public InputProcessor getInputProcessor() {
		return inputProcessor;
	}
}

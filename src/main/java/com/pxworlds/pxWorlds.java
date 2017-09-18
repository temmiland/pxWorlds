package com.pxworlds;

import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;

import java.nio.ByteBuffer;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;

/**
 * Instantiate the gamescreen
 *
 * @author "tomxpcvx <privat@<API_URL>>"
 * @version 0.1
 */

public class pxWorlds {

    private String TITLE;
	private int WIDTH;
    private int HEIGHT;

    // We need to strongly reference callback instances.
    private GLFWErrorCallback errorCallback;

    /* Is not used this time
    private GLFWKeyCallback   keyCallback; */

    // The window handle
    private long window;

    /**
     * Method to instantiate the gamescreen object
     *
     * @author "tomxpcvx <privat@<API_URL>>"
     *
     * @param width Defines the width of the window
     * @param height Defines the height of the window
     * @param title Defines the title of the window
     */

	public pxWorlds(int width, int height, String title) {
		this.HEIGHT = height;
		this.WIDTH = width;
		this.TITLE = title;
	}

    /**
     * Method to run the gamescreen
     *
     * @author "tomxpcvx <privat@<API_URL>>"
     */

    public void run() {

		printDebugInformations();

		try {
			init();
			loop();

			// Release window and window callbacks
			glfwDestroyWindow(window);

			/* Is not used this time
			keyCallback.release(); */
		} finally {
			// Terminate GLFW and release the GLFWerrorfun
			glfwTerminate();
			errorCallback.release();
		}
	}

    /**
     * Method to initialize the gamescreen
     *
     * @author "tomxpcvx <privat@<API_URL>>"
     */

	private void init() {

		// Setup an error callback. The default implementation
		// will print the error message in System.err.
		glfwSetErrorCallback(errorCallback = errorCallbackPrint(System.err));

		// Initialize GLFW. Most GLFW functions will not work before doing this.
		if ( glfwInit() != GL11.GL_TRUE )
			throw new IllegalStateException("Unable to initialize GLFW");

		// Configure our window
		glfwDefaultWindowHints(); // optional, the current window hints are already the default
		glfwWindowHint(GLFW_VISIBLE, GL_TRUE); // the window will start in foreground after creation
		glfwWindowHint(GLFW_RESIZABLE, GL_FALSE); // the window won't be resizable


		// Create the window
		window = glfwCreateWindow(WIDTH, HEIGHT, TITLE, NULL, NULL);
		if ( window == NULL )
			throw new RuntimeException("Failed to create the GLFW window");

		/* Is not used this time

		Setup a key callback. It will be called every time a key is pressed, repeated or released.

		glfwSetKeyCallback(window, keyCallback = new GLFWKeyCallback() {
			@Override
			public void invoke(long window, int key, int scancode, int action, int mods) {
				if ( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE )
					glfwSetWindowShouldClose(window, GL_TRUE); // We will detect this in our rendering loop
			}
		}); */

		// Get the resolution of the primary monitor
		ByteBuffer vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

		// Center our window
		glfwSetWindowPos(
				window,
				(GLFWvidmode.width(vidmode) - WIDTH) / 2,
				(GLFWvidmode.height(vidmode) - HEIGHT) / 2
		);

		// Make the OpenGL context current
		glfwMakeContextCurrent(window);
		// Enable v-sync
		glfwSwapInterval(1);

		// Make the window visible
		glfwShowWindow(window);
	}

    /**
     * Method to loop the gamescreen
     *
     * @author "tomxpcvx <privat@<API_URL>>"
     */

    private void loop() {
		// This line is critical for LWJGL's interoperation with GLFW's
		// OpenGL context, or any context that is managed externally.
		// LWJGL detects the context that is current in the current thread,
		// creates the ContextCapabilities instance and makes the OpenGL
		// bindings available for use.
		GLContext.createFromCurrent();

		// Set the clear color
		glClearColor(1.0f, 1.0f, 1.0f, 0.0f);

		// Run the rendering loop until the user has attempted to close the window
		while ( glfwWindowShouldClose(window) == GL_FALSE ) {
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

			glfwSwapBuffers(window); // swap the color buffers

			// Poll for window events. The key callback above will only be
			// invoked during this call.
			glfwPollEvents();
		}
	}

	/**
	 *  It prints informations about the os and the java installation in the console
	 *
	 *	@author "tomxpcvx <privat@<API_URL>>"
	 */

	private void printDebugInformations() {

		System.out.println("#############################################################");
		System.out.println("OS name: " + System.getProperties().getProperty("os.name"));
		System.out.println("OS architecture: " + System.getProperties().getProperty("os.arch"));
		System.out.println("Java class version: " + System.getProperties().getProperty("java.class.version"));
		System.out.println("Java version: " + System.getProperties().getProperty("java.version"));
		System.out.println("Java vendor: " + System.getProperties().getProperty("java.vendor"));
		System.out.println("#############################################################");

	}



}
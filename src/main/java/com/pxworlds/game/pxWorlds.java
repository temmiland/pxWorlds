package com.pxworlds.game;

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

public class pxWorlds implements  Runnable {

    private String TITLE;
	private int WIDTH;
    private int HEIGHT;

    private long window;
    private Thread thread;
    private boolean running = false;

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
     * Method to start the game process
     *
     * @author "tomxpcvx <privat@<API_URL>>"
     */

    public void start() {
        running = true;
        thread = new Thread(this,"pxWorlds");
        thread.start();
    }

    /**
     * Method to run the gamescreen
     *
     * @author "tomxpcvx <privat@<API_URL>>"
     */

    public void run() {

		printDebugInformations();

		init();
		while(running) {
		    update();
		    render();

		    if(glfwWindowShouldClose(window) == GL_TRUE) {
		        running = false;
            }
        }
	}

    /**
     * Method to initialize the gamescreen
     *
     * @author "tomxpcvx <privat@<API_URL>>"
     */

	private void init() {

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

		// Get the resolution of the primary monitor
		ByteBuffer vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

		// Center our window
		glfwSetWindowPos(window,(GLFWvidmode.width(vidmode) - WIDTH) / 2,(GLFWvidmode.height(vidmode) - HEIGHT) / 2);

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

    private void update() {
		glfwPollEvents();
	}

    private void render() {
        glfwSwapBuffers(window);
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
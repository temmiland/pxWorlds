package com.pxworlds.gui;

import com.pxworlds.render.Camera;
import com.pxworlds.render.Shader;
import com.pxworlds.render.TileSheet;
import org.joml.Vector2f;

import com.pxworlds.io.Input;
import com.pxworlds.io.Window;

public class Gui {
    private Window window;
	private Shader shader;
	private Camera camera;
	private TileSheet sheet;
	
	private Button playButton;
	private Button exitButton;

	public Gui(Window window) {
        this.window = window;
		shader = new Shader("gui");
		camera = new Camera(window.getWidth(), window.getHeight());
		sheet = new TileSheet("gui.png", 9);

		playButton = new Button(new Vector2f(-32, -32), new Vector2f(100, 30));
		exitButton = new Button(new Vector2f(-32, -132), new Vector2f(100, 30));
	}
	
	public void resizeCamera() {
		camera.setProjection(window.getWidth(), window.getHeight());
	}
	
	public void update(Input input) {
		playButton.update(input);
		exitButton.update(input);
	}
	
	public void render() {
		shader.bind();
		playButton.render(camera, sheet, shader);
		exitButton.render(camera, sheet, shader);
	}
}

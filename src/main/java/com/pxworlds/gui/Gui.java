package com.pxworlds.gui;

import com.pxworlds.render.Camera;
import com.pxworlds.render.Shader;
import com.pxworlds.render.TileSheet;
import org.joml.Vector2f;

import com.pxworlds.io.Input;
import com.pxworlds.io.Window;

public class Gui {
	private Shader shader;
	private Camera camera;
	private TileSheet sheet;
	
	private Button temporary;
	
	public Gui(Window window) {
		shader = new Shader("gui");
		camera = new Camera(window.getWidth(), window.getHeight());
		sheet = new TileSheet("gui.png", 9);
		temporary = new Button(new Vector2f(-32, -32), new Vector2f(96, 96));
	}
	
	public void resizeCamera(Window window) {
		camera.setProjection(window.getWidth(), window.getHeight());
	}
	
	public void update(Input input) {
		temporary.update(input);
	}
	
	public void render() {
		shader.bind();
		temporary.render(camera, sheet, shader);
	}
}

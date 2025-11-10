package com.pxworlds.game.rendering;

import com.pxworlds.game.io.Timer;

public class Animation {
	/** The array of texture frames. */
	private Texture[] frames;
	/** The current frame index. */
	private int texturePointer;

	/** The elapsed time since the last frame. */
	private double elapsedTime;
	/** The current time. */
	private double currentTime;
	/** The last time the frame was updated. */
	private double lastTime;
	/** The frames per second. */
	private double fps;

	public Animation(int amount, int newFps, String filename) {
		this.texturePointer = 0;
		this.elapsedTime = 0;
		this.currentTime = 0;
		this.lastTime = Timer.getTime();
		this.fps = 1.0 / newFps;

		this.frames = new Texture[amount];
		for (int i = 0; i < amount; i++) {
			this.frames[i] = new Texture(filename + "/" + i + ".png");
		}
	}

	public void bind() {
		bind(0);
	}

	public void bind(int sampler) {
		this.currentTime = Timer.getTime();
		this.elapsedTime += currentTime - lastTime;

		if (elapsedTime >= fps) {
			elapsedTime = 0;
			texturePointer++;
		}

		if (texturePointer >= frames.length) {
			texturePointer = 0;
		}

		this.lastTime = currentTime;

		frames[texturePointer].bind(sampler);
	}
}

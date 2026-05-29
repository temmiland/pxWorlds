package com.pxworlds.game.rendering;

import com.pxworlds.game.io.Timer;

public class Animation {
	/** Das Array der Textur-Frames. */
	private Texture[] frames;
	/** Der aktuelle Frame-Index. */
	private int texturePointer;

	/** Die seit dem letzten Frame vergangene Zeit. */
	private double elapsedTime;
	/** Die aktuelle Zeit. */
	private double currentTime;
	/** Der Zeitpunkt des letzten Frame-Wechsels. */
	private double lastTime;
	/** Die Frames pro Sekunde (als Sekundenintervall). */
	private double fps;
	/** Ob diese Animation pausiert ist. */
	private boolean paused;

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

	public void pause() {
		this.paused = true;
	}

	public void resume() {
		this.paused = false;
		this.lastTime = Timer.getTime();
	}

	public void bind() {
		bind(0);
	}

	public void bind(int sampler) {
		if (!paused) {
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
		}

		frames[texturePointer].bind(sampler);
	}
}

package com.pxworlds.game.rendering;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Camera {
	/** The current camera position. */
	private Vector3f position;
	/** The camera position at the start of the last tick — used for render interpolation. */
	private Vector3f prevPosition;
	/** The projection matrix. */
	private Matrix4f projection;

	/** Reference height used to keep proportional scaling on resize. */
	private final int referenceHeight;
	/** Virtual viewport width (derived from referenceHeight + aspect ratio). */
	private int virtualWidth;
	/** Virtual viewport height (= referenceHeight). */
	private int virtualHeight;

	/**
	 * Creates a new Camera with the given viewport size.
	 * @param width  Viewport width in pixels.
	 * @param height Viewport height in pixels.
	 */
	public Camera(int width, int height) {
		position = new Vector3f(0, 0, 0);
		prevPosition = new Vector3f(0, 0, 0);
		referenceHeight = height;
		setProjection(width, height);
	}

	public void setProjection(int width, int height) {
		float aspect = (float) width / height;
		virtualHeight = referenceHeight;
		virtualWidth  = (int) (referenceHeight * aspect);
		projection = new Matrix4f().setOrtho2D(
				-virtualWidth / 2f, virtualWidth / 2f,
				-virtualHeight / 2f, virtualHeight / 2f);
	}

	/** @return virtual viewport width (in world-pixel units) */
	public int getVirtualWidth() { return virtualWidth; }
	/** @return virtual viewport height (in world-pixel units) */
	public int getVirtualHeight() { return virtualHeight; }

	public void setPosition(Vector3f newPosition) {
		this.position.set(newPosition);
	}

	public void addPosition(Vector3f newPosition) {
		this.position.add(newPosition);
	}

	public Vector3f getPosition() {
		return position;
	}

	/**
	 * Saves the current position as prevPosition.
	 * Must be called at the start of each tick, before any entity updates the camera.
	 */
	public void savePrevPosition() {
		prevPosition.set(position);
	}

	/**
	 * Returns the untransformed projection matrix (no camera translation applied).
	 * @return The raw projection matrix.
	 */
	public Matrix4f getUntransformedProjection() {
		return projection;
	}

	/**
	 * Returns the projection matrix translated by the current camera position.
	 * Use only where interpolation is not needed (e.g. GUI).
	 * @return The projection matrix.
	 */
	public Matrix4f getProjection() {
		return projection.translate(position, new Matrix4f());
	}

	/**
	 * Returns the projection matrix with the camera position interpolated between
	 * the last tick and the current tick. Use this for all in-world rendering.
	 * @param alpha Interpolation factor 0.0–1.0 (from the game loop).
	 * @return The interpolated projection matrix.
	 */
	public Matrix4f getProjection(final float alpha) {
		final Vector3f interpolated = new Vector3f(prevPosition).lerp(position, alpha);
		return projection.translate(interpolated, new Matrix4f());
	}
}

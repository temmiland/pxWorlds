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

	/**
	 * Creates a new Camera with the given viewport size.
	 * @param width  Viewport width in pixels.
	 * @param height Viewport height in pixels.
	 */
	public Camera(int width, int height) {
		position = new Vector3f(0, 0, 0);
		prevPosition = new Vector3f(0, 0, 0);
		setProjection(width, height);
	}

	public void setProjection(int width, int height) {
		projection = new Matrix4f().setOrtho2D(-width/2, width/2, -height/2, height/2);
	}

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

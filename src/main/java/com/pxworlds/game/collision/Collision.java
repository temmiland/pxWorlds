package com.pxworlds.game.collision;

import org.joml.Vector2f;

public class Collision {
	/** The distance of the collision. */
	public Vector2f distance;
	/** Whether the objects are intersecting. */
	public boolean isIntersecting;

	public Collision(Vector2f distance, boolean intersects) {
		this.distance = distance;
		this.isIntersecting = intersects;
	}
}

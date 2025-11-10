package com.pxworlds.game.collision;

import org.joml.Vector2f;

public class Collision {
	/** The distance of the collision. */
	public Vector2f distance;
	/** Whether the objects are intersecting. */
	public boolean isIntersecting;

	public Collision(Vector2f newDistance, boolean intersects) {
		this.distance = newDistance;
		this.isIntersecting = intersects;
	}
}

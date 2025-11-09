package com.pxworlds.game.collision;

import org.joml.Vector2f;

public class AABB {
	/** The center of the AABB. */
	private Vector2f center;
	/** The half extent of the AABB. */
	private Vector2f halfExtent;

	public AABB(Vector2f center, Vector2f halfExtent) {
		this.center = center;
		this.halfExtent = halfExtent;
	}

	public Collision getCollision(AABB box2) {
		Vector2f distance = box2.center.sub(center, new Vector2f());
		distance.x = Math.abs(distance.x);
		distance.y = Math.abs(distance.y);

		distance.sub(halfExtent.add(box2.halfExtent, new Vector2f()));

		return new Collision(distance, distance.x < 0 && distance.y < 0);
	}

	public Collision getCollision(Vector2f point) {
		Vector2f distance = point.sub(center);
		distance.x = Math.abs(distance.x);
		distance.y = Math.abs(distance.y);

		distance.sub(halfExtent);

		return new Collision(distance, distance.x < 0 && distance.y < 0);
	}

	public void correctPosition(AABB box2, Collision data) {
		Vector2f correctionDistance = box2.center.sub(center, new Vector2f());
		if (data.distance.x > data.distance.y) {
			if (correctionDistance.x > 0) {
				center.add(data.distance.x, 0);
			}
			else {
				center.add(-data.distance.x, 0);
			}
		} else {
			if (correctionDistance.y > 0) {
				center.add(0, data.distance.y);
			}
			else {
				center.add(0, -data.distance.y);
			}
		}
	}

	public Vector2f getCenter() {
		return center;
	}

	public Vector2f getHalfExtent() {
		return halfExtent;
	}
}

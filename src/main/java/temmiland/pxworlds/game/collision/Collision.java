package temmiland.pxworlds.game.collision;

import org.joml.Vector2f;

public class Collision {
	/** Der Abstandsvektor der Kollision. */
	private Vector2f distance;
	/** Ob die Objekte sich überschneiden. */
	private boolean intersecting;

	public Collision(Vector2f newDistance, boolean intersects) {
		this.distance = newDistance;
		this.intersecting = intersects;
	}

	/** @return der Abstandsvektor der Kollision */
	public Vector2f getDistance() {
		return distance;
	}

	/** @return {@code true} wenn sich die Objekte überschneiden */
	public boolean isIntersecting() {
		return intersecting;
	}
}

package com.pxworlds.game.entity;

import com.pxworlds.game.assets.Assets;
import com.pxworlds.game.collision.AABB;
import com.pxworlds.game.collision.Collision;
import com.pxworlds.game.io.Window;
import com.pxworlds.game.rendering.Animation;
import com.pxworlds.game.rendering.Camera;
import com.pxworlds.game.rendering.Shader;
import com.pxworlds.game.world.World;
import org.joml.*;


public abstract class Entity {
	/** The bounding box for collision detection. */
	protected AABB boundingBox;
	// private Texture texture;
	/** The array of animations. */
	protected Animation[] animations;
	/** The index of the current animation. */
	private int useAnimation;

	/** The transform of the entity. */
	protected Transform transform;
	/** Position am Ende des letzten Ticks — Ausgangspunkt für Render-Interpolation. */
	protected Vector3f prevPos;

    /** The tile check size. */
    private static final int TILE_CHECK_SIZE = 5;
    /** The total tile check. */
    private static final int TILE_CHECK_TOTAL = TILE_CHECK_SIZE * TILE_CHECK_SIZE;
    /** The half value. */
    private static final float HALF = 0.5f;
    /** The divide by two value. */
    private static final int DIVIDE_BY_TWO = 2;

	public Entity(int maxAnimations, Transform newTransform) {
		this.animations = new Animation[maxAnimations];

		this.transform = newTransform;
		this.useAnimation = 0;
		this.prevPos = new Vector3f(transform.pos);

		boundingBox = new AABB(new Vector2f(transform.pos.x, transform.pos.y), new Vector2f(transform.scale.x, transform.scale.y));
	}

	protected void setAnimation(int index, Animation animation) {
		animations[index] = animation;
	}

	public void useAnimation(int index) {
		this.useAnimation = index;
	}

	public void move(Vector2f direction) {
		transform.pos.add(new Vector3f(direction, 0));

		boundingBox.getCenter().set(transform.pos.x, transform.pos.y);
	}

	public void collideWithTiles(World world) {
		final AABB[] boxes = new AABB[TILE_CHECK_TOTAL];
		for (int i = 0; i < TILE_CHECK_SIZE; i++) {
			for (int j = 0; j < TILE_CHECK_SIZE; j++) {
				boxes[i + j * TILE_CHECK_SIZE] = world.getTileBoundingBox((int) (((transform.pos.x / DIVIDE_BY_TWO) + HALF) - (TILE_CHECK_SIZE / DIVIDE_BY_TWO)) + i, (int) (((-transform.pos.y / DIVIDE_BY_TWO) + HALF) - (TILE_CHECK_SIZE / DIVIDE_BY_TWO)) + j);
			}
		}

		AABB box = null;
		for (int i = 0; i < boxes.length; i++) {
			if (boxes[i] != null) {
				if (box == null) {
					box = boxes[i];
				}

				final Vector2f length1 = box.getCenter().sub(transform.pos.x, transform.pos.y, new Vector2f());
				final Vector2f length2 = boxes[i].getCenter().sub(transform.pos.x, transform.pos.y, new Vector2f());

				if (length1.lengthSquared() > length2.lengthSquared()) {
					box = boxes[i];
				}
			}
		}
		if (box != null) {
			Collision data = boundingBox.getCollision(box);
			if (data.isIntersecting) {
				boundingBox.correctPosition(box, data);
				transform.pos.set(boundingBox.getCenter(), 0);
			}

			for (int i = 0; i < boxes.length; i++) {
				if (boxes[i] != null) {
					if (box == null) {
						box = boxes[i];
					}

					final Vector2f length1 = box.getCenter().sub(transform.pos.x, transform.pos.y, new Vector2f());
					final Vector2f length2 = boxes[i].getCenter().sub(transform.pos.x, transform.pos.y, new Vector2f());

					if (length1.lengthSquared() > length2.lengthSquared()) {
						box = boxes[i];
					}
				}
			}

			data = boundingBox.getCollision(box);
			if (data.isIntersecting) {
				boundingBox.correctPosition(box, data);
				transform.pos.set(boundingBox.getCenter(), 0);
			}
		}
	}

	public abstract void update(float delta, Window window, Camera camera, World world);

	/**
	 * Sichert die aktuelle Position als prevPos und ruft dann update() auf.
	 * Wird von World.update() statt update() direkt aufgerufen,
	 * damit prevPos immer den Stand vor dem Tick enthält.
	 */
	public final void tick(float delta, Window window, Camera camera, World world) {
		prevPos.set(transform.pos);
		update(delta, window, camera, world);
	}

	public void render(Shader shader, Camera camera, World world, float alpha) {
		final Matrix4f target = camera.getProjection();
		target.mul(world.getWorldMatrix());

		// Interpolierte Position zwischen letztem und aktuellem Tick
		final float renderX = prevPos.x + (transform.pos.x - prevPos.x) * alpha;
		final float renderY = prevPos.y + (transform.pos.y - prevPos.y) * alpha;
		target.translate(renderX, renderY, transform.pos.z);
		target.scale(transform.scale);

		shader.bind();
		shader.setUniform("sampler", 0);
		shader.setUniform("projection", target);
		animations[useAnimation].bind(0);
		Assets.getModel().render();
	}

	public void collideWithEntity(Entity entity) {
		final Collision collision = boundingBox.getCollision(entity.boundingBox);

		if (collision.isIntersecting) {
			collision.distance.x /= DIVIDE_BY_TWO;
			collision.distance.y /= DIVIDE_BY_TWO;

			boundingBox.correctPosition(entity.boundingBox, collision);
			transform.pos.set(boundingBox.getCenter().x, boundingBox.getCenter().y, 0);

			entity.boundingBox.correctPosition(boundingBox, collision);
			entity.transform.pos.set(entity.boundingBox.getCenter().x, entity.boundingBox.getCenter().y, 0);
		}
	}
}

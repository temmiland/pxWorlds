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
	protected AABB bounding_box;
	// private Texture texture;
	protected Animation[] animations;
	private int use_animation;

	protected Transform transform;

    private static final int TILE_CHECK_SIZE = 5;
    private static final int TILE_CHECK_TOTAL = TILE_CHECK_SIZE * TILE_CHECK_SIZE;
    private static final float HALF = 0.5f;
    private static final int DIVIDE_BY_TWO = 2;

	public Entity(int max_animations, Transform transform) {
		this.animations = new Animation[max_animations];

		this.transform = transform;
		this.use_animation = 0;

		bounding_box = new AABB(new Vector2f(transform.pos.x, transform.pos.y), new Vector2f(transform.scale.x, transform.scale.y));
	}

	protected void setAnimation(int index, Animation animation) {
		animations[index] = animation;
	}

	public void useAnimation(int index) {
		this.use_animation = index;
	}

	public void move(Vector2f direction) {
		transform.pos.add(new Vector3f(direction, 0));

		bounding_box.getCenter().set(transform.pos.x, transform.pos.y);
	}

	public void collideWithTiles(World world) {
		AABB[] boxes = new AABB[TILE_CHECK_TOTAL];
		for (int i = 0; i < TILE_CHECK_SIZE; i++) {
			for (int j = 0; j < TILE_CHECK_SIZE; j++) {
				boxes[i + j * TILE_CHECK_SIZE] = world.getTileBoundingBox((int) (((transform.pos.x / DIVIDE_BY_TWO) + HALF) - (TILE_CHECK_SIZE / DIVIDE_BY_TWO)) + i, (int) (((-transform.pos.y / DIVIDE_BY_TWO) + HALF) - (TILE_CHECK_SIZE / DIVIDE_BY_TWO)) + j);
			}
		}

		AABB box = null;
		for (int i = 0; i < boxes.length; i++) {
			if (boxes[i] != null) {
				if (box == null) box = boxes[i];

				Vector2f length1 = box.getCenter().sub(transform.pos.x, transform.pos.y, new Vector2f());
				Vector2f length2 = boxes[i].getCenter().sub(transform.pos.x, transform.pos.y, new Vector2f());

				if (length1.lengthSquared() > length2.lengthSquared()) {
					box = boxes[i];
				}
			}
		}
		if (box != null) {
			Collision data = bounding_box.getCollision(box);
			if (data.isIntersecting) {
				bounding_box.correctPosition(box, data);
				transform.pos.set(bounding_box.getCenter(), 0);
			}

			for (int i = 0; i < boxes.length; i++) {
				if (boxes[i] != null) {
					if (box == null) box = boxes[i];

					Vector2f length1 = box.getCenter().sub(transform.pos.x, transform.pos.y, new Vector2f());
					Vector2f length2 = boxes[i].getCenter().sub(transform.pos.x, transform.pos.y, new Vector2f());

					if (length1.lengthSquared() > length2.lengthSquared()) {
						box = boxes[i];
					}
				}
			}

			data = bounding_box.getCollision(box);
			if (data.isIntersecting) {
				bounding_box.correctPosition(box, data);
				transform.pos.set(bounding_box.getCenter(), 0);
			}
		}
	}

	public abstract void update(float delta, Window window, Camera camera, World world);

	public void render(Shader shader, Camera camera, World world) {
		Matrix4f target = camera.getProjection();
		target.mul(world.getWorldMatrix());

		shader.bind();
		shader.setUniform("sampler", 0);
		shader.setUniform("projection", transform.getProjection(target));
		animations[use_animation].bind(0);
		Assets.getModel().render();
	}

	public void collideWithEntity(Entity entity) {
		Collision collision = bounding_box.getCollision(entity.bounding_box);

		if (collision.isIntersecting) {
			collision.distance.x /= DIVIDE_BY_TWO;
			collision.distance.y /= DIVIDE_BY_TWO;

			bounding_box.correctPosition(entity.bounding_box, collision);
			transform.pos.set(bounding_box.getCenter().x, bounding_box.getCenter().y, 0);

			entity.bounding_box.correctPosition(bounding_box, collision);
			entity.transform.pos.set(entity.bounding_box.getCenter().x, entity.bounding_box.getCenter().y, 0);
		}
	}
}

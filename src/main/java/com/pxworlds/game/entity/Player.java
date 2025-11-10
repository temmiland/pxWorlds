package com.pxworlds.game.entity;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import com.pxworlds.game.io.Window;
import com.pxworlds.game.rendering.Animation;
import com.pxworlds.game.rendering.Camera;
import com.pxworlds.game.world.World;

public class Player extends Entity {
	/** Idle animation index. */
	public static final int ANIM_IDLE = 0;
	/** Walk animation index. */
	public static final int ANIM_WALK = 1;
	/** Run animation index. */
	public static final int ANIM_RUN = 2;
    /** Total number of animations. */
    public static final int ANIM_SIZE = 3;
    /** The movement speed of the player. */
    private int movementSpeed = MovementSpeed.WALKING.getValue();

    /** Number of idle animation frames. */
    private static final int IDLE_FRAMES = 4;
    /** Frames per second for idle animation. */
    private static final int IDLE_FPS = 3;
    /** Number of walk/run animation frames. */
    private static final int WALK_RUN_FRAMES = 4;
    /** Multiplier for diagonal movement speed. */
    private static final float DIAGONAL_SPEED_MULTIPLIER = 1.5f;
    /** Lerp factor for camera smoothing. */
    private static final float CAMERA_LERP_FACTOR = 0.05f;

    public Player(Transform transform) {
		super(ANIM_SIZE, transform);
		setAnimation(ANIM_IDLE, new Animation(IDLE_FRAMES, IDLE_FPS, "player/idle"));
		setAnimation(ANIM_WALK, new Animation(WALK_RUN_FRAMES, MovementSpeed.WALKING.getValue(), "player/walking"));
		setAnimation(ANIM_RUN, new Animation(WALK_RUN_FRAMES, MovementSpeed.RUNNING.getValue(), "player/walking"));
	}

	public void setMovementSpeed(MovementSpeed newMovementSpeed) {
        this.movementSpeed = newMovementSpeed.getValue();
    }

	@Override
	public void update(float delta, Window window, Camera camera, World world) {
		final Vector2f movement = new Vector2f();
        final float movementP = movementSpeed * delta;
        final float movementN = -movementSpeed * delta;
        final boolean isADown = window.getInput().isKeyDown(GLFW.GLFW_KEY_A);
        final boolean isDDown = window.getInput().isKeyDown(GLFW.GLFW_KEY_D);
        final boolean isWDown = window.getInput().isKeyDown(GLFW.GLFW_KEY_W);
        final boolean isSDown = window.getInput().isKeyDown(GLFW.GLFW_KEY_S);

        if (isADown) {
            if (isWDown || isSDown) {
                movement.add(movementN / DIAGONAL_SPEED_MULTIPLIER, 0);
            } else {
                movement.add(movementN, 0);
            }
        }

		if (isDDown) {
            if (isWDown || isSDown) {
                movement.add(movementP / DIAGONAL_SPEED_MULTIPLIER, 0);
            } else {
                movement.add(movementP, 0);
            }
        }

		if (isWDown) {
            if (isADown || isDDown) {
                movement.add(0, movementP / DIAGONAL_SPEED_MULTIPLIER);
            } else {
                movement.add(0, movementP);
            }
        }

		if (isSDown) {
            if (isADown || isDDown) {
                movement.add(0, movementN / DIAGONAL_SPEED_MULTIPLIER);
            } else {
                movement.add(0, movementN);
            }
        }

        if (window.getInput().isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT)) {
            setMovementSpeed(MovementSpeed.RUNNING);
        } else {
            setMovementSpeed(MovementSpeed.WALKING);
        }
		move(movement);
        if (movement.x != 0 || movement.y != 0) {
            if (movementSpeed == MovementSpeed.WALKING.getValue()) {
                useAnimation(ANIM_WALK);
            } else {
                useAnimation(ANIM_RUN);
            }
        } else {
            useAnimation(ANIM_IDLE);
        }

		camera.getPosition().lerp(transform.pos.mul(-world.getScale(), new Vector3f()), CAMERA_LERP_FACTOR);
	}
}

package com.pxworlds.game.entity;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import com.pxworlds.game.io.Window;
import com.pxworlds.game.rendering.Animation;
import com.pxworlds.game.rendering.Camera;
import com.pxworlds.game.world.World;

public class Player extends Entity {
	public static final int ANIM_IDLE = 0;
	public static final int ANIM_WALK = 1;
	public static final int ANIM_RUN = 2;
    public static final int ANIM_SIZE = 3;
    private int movementSpeed = MovementSpeed.WALKING.getValue();

    public Player(Transform transform) {
		super(ANIM_SIZE, transform);
		setAnimation(ANIM_IDLE, new Animation(4, 3, "player/idle"));
		setAnimation(ANIM_WALK, new Animation(4, MovementSpeed.WALKING.getValue(), "player/walking"));
		setAnimation(ANIM_RUN, new Animation(4, MovementSpeed.RUNNING.getValue(), "player/walking"));
	}

	public void setMovementSpeed(MovementSpeed movementSpeed) {
        this.movementSpeed = movementSpeed.getValue();
    }
	
	@Override
	public void update(float delta, Window window, Camera camera, World world) {
		Vector2f movement = new Vector2f();
        float movementP = movementSpeed * delta;
        float movementN = -movementSpeed * delta;
        boolean isADown = window.getInput().isKeyDown(GLFW.GLFW_KEY_A);
        boolean isDDown = window.getInput().isKeyDown(GLFW.GLFW_KEY_D);
        boolean isWDown = window.getInput().isKeyDown(GLFW.GLFW_KEY_W);
        boolean isSDown = window.getInput().isKeyDown(GLFW.GLFW_KEY_S);

        if (isADown) movement.add(isWDown || isSDown ? movementN / 1.5f : movementN, 0);
		
		if (isDDown) movement.add(isWDown || isSDown ? movementP / 1.5f : movementP, 0);

		if (isWDown) movement.add(0, isADown || isDDown ? movementP / 1.5f : movementP);

		if (isSDown) movement.add(0, isADown || isDDown ? movementN / 1.5f : movementN);

        setMovementSpeed(window.getInput().isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT) ? MovementSpeed.RUNNING : MovementSpeed.WALKING);
		move(movement);
        useAnimation(movement.x != 0 || movement.y != 0 ? movementSpeed == MovementSpeed.WALKING.getValue() ? ANIM_WALK : ANIM_RUN : ANIM_IDLE);
		
		camera.getPosition().lerp(transform.pos.mul(-world.getScale(), new Vector3f()), 0.05f);
	}
}

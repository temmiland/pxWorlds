package land.temmi.pxworlds.entity;

import org.joml.Vector2f;
import org.joml.Vector3f;

import land.temmi.pxworlds.io.InputAction;
import land.temmi.rollercoaster.platform.input.InputProcessor;
import land.temmi.rollercoaster.platform.Window;
import land.temmi.rollercoaster.graphics.Animation;
import land.temmi.rollercoaster.graphics.Camera;
import land.temmi.pxworlds.tiles.Tile;
import land.temmi.pxworlds.tiles.tile.PlayerTiles;
import land.temmi.pxworlds.world.World;

public class Player extends Entity {

    private enum MovementSpeed {

        /** Default walk speed in tiles per second. */
        WALKING(5),
        /** Sprint speed in tiles per second. */
        RUNNING(8);

        /** Speed value in tiles per second. */
        private final int movementSpeed;

        MovementSpeed(int movementSpeed) {
            this.movementSpeed = movementSpeed;
        }

        public int getValue() {
            return movementSpeed;
        }
    }

    private enum Anim {
        /** Idle animation slot. */
        IDLE,
        /** Walk animation slot. */
        WALK,
        /** Run animation slot. */
        RUN
    }

    /** Frames per second for the idle animation. */
    private static final int   IDLE_FPS                  = 3;
    /** Speed multiplier applied when moving diagonally. */
    private static final float DIAGONAL_SPEED_MULTIPLIER = 1.5f;
    /** Lerp factor for smooth camera follow. */
    private static final float CAMERA_LERP_FACTOR        = 0.09f;
    // 65 % of texture width — narrower hitbox than the render sprite
    /** Hitbox half-width as a fraction of the tile scale. */
    private static final float HITBOX_WIDTH = 0.65f;

    /** Current movement speed value (walking or running). */
    private int movementSpeed = MovementSpeed.WALKING.getValue();

    /** Accumulated movement vector for the current tick. */
    private final Vector2f movement     = new Vector2f();
    /** Reusable camera target vector. */
    private final Vector3f cameraTarget = new Vector3f();

    public Player(float posX, float posY) {
        super(Anim.values().length, posX, posY, 1f, 1f);
        setAnimation(Anim.IDLE.ordinal(), new Animation(new Tile[]{
            PlayerTiles.IDLE_0, PlayerTiles.IDLE_1, PlayerTiles.IDLE_2, PlayerTiles.IDLE_3
        }, IDLE_FPS));
        setAnimation(Anim.WALK.ordinal(), new Animation(new Tile[]{
            PlayerTiles.WALK_0, PlayerTiles.WALK_1, PlayerTiles.WALK_2, PlayerTiles.WALK_3
        }, MovementSpeed.WALKING.getValue()));
        setAnimation(Anim.RUN.ordinal(), new Animation(new Tile[]{
            PlayerTiles.WALK_0, PlayerTiles.WALK_1, PlayerTiles.WALK_2, PlayerTiles.WALK_3
        }, MovementSpeed.RUNNING.getValue()));
        setBoundingBoxSize(HITBOX_WIDTH, scaleY);
    }

    public void setMovementSpeed(MovementSpeed movementSpeed) {
        this.movementSpeed = movementSpeed.getValue();
    }

    @Override
    public void update(float delta, Window window, Camera camera, World world) {
        final InputProcessor input = window.getInputProcessor();

        if (input.isHeld(InputAction.SPRINT.getDefaultKey())) {
            setMovementSpeed(MovementSpeed.RUNNING);
        } else {
            setMovementSpeed(MovementSpeed.WALKING);
        }

        final boolean isLeft  = input.isHeld(InputAction.MOVE_LEFT.getDefaultKey());
        final boolean isRight = input.isHeld(InputAction.MOVE_RIGHT.getDefaultKey());
        final boolean isUp    = input.isHeld(InputAction.MOVE_UP.getDefaultKey());
        final boolean isDown  = input.isHeld(InputAction.MOVE_DOWN.getDefaultKey());

        float dx = 0f;
        if (isRight) {
            dx += 1f;
        }
        if (isLeft) {
            dx -= 1f;
        }
        float dy = 0f;
        if (isUp) {
            dy += 1f;
        }
        if (isDown) {
            dy -= 1f;
        }
        final boolean diag = dx != 0 && dy != 0;
        final float diagFactor;
        if (diag) {
            diagFactor = 1f / DIAGONAL_SPEED_MULTIPLIER;
        } else {
            diagFactor = 1f;
        }
        final float speed = movementSpeed * delta * diagFactor;
        movement.set(dx * speed, dy * speed);

        moveClipped(movement.x, movement.y, world);

        if (movement.x != 0 || movement.y != 0) {
            if (movementSpeed == MovementSpeed.RUNNING.getValue()) {
                useAnimation(Anim.RUN.ordinal());
            } else {
                useAnimation(Anim.WALK.ordinal());
            }
        } else {
            useAnimation(Anim.IDLE.ordinal());
        }

        cameraTarget.set(-posX * world.getScale(), -posY * world.getScale(), 0);
        camera.getPosition().lerp(cameraTarget, CAMERA_LERP_FACTOR);
    }
}

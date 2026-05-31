package temmiland.pxworlds.game.entity;

import org.joml.Vector2f;
import org.joml.Vector3f;

import temmiland.pxworlds.game.io.InputAction;
import temmiland.rollercoaster.platform.io.InputProcessor;
import temmiland.rollercoaster.platform.Window;
import temmiland.pxworlds.game.rendering.Animation;
import temmiland.pxworlds.game.rendering.Camera;
import temmiland.pxworlds.game.world.World;

public class Player extends Entity {
	/** Index der Idle-Animation. */
	public static final int ANIM_IDLE = 0;
	/** Index der Walk-Animation. */
	public static final int ANIM_WALK = 1;
	/** Index der Run-Animation. */
	public static final int ANIM_RUN = 2;
    /** Gesamtanzahl der Animationen. */
    public static final int ANIM_SIZE = 3;
    /** Aktuelle Bewegungsgeschwindigkeit in Einheiten/Sekunde. */
    private int movementSpeed = MovementSpeed.WALKING.getValue();

    /** Anzahl der Idle-Animations-Frames. */
    private static final int IDLE_FRAMES = 4;
    /** Frames pro Sekunde der Idle-Animation. */
    private static final int IDLE_FPS = 3;
    /** Anzahl der Walk/Run-Animations-Frames. */
    private static final int WALK_RUN_FRAMES = 4;
    /** Geschwindigkeitsmultiplikator für diagonale Bewegung. */
    private static final float DIAGONAL_SPEED_MULTIPLIER = 1.5f;
    /** Lerp-Faktor für die Kamera-Glättung. */
    private static final float CAMERA_LERP_FACTOR = 0.09f;
    /**
     * Halbe Breite der Spieler-Hitbox in Tile-Einheiten (65 % der Texturbreite).
     * Die Textur belegt 2 × scaleX = 2.0 Einheiten; 65 % davon ergibt
     * eine Gesamtbreite von 1.3 Einheiten (±0.65), zentriert auf der Textur.
     */
    private static final float HITBOX_HALF_WIDTH = 0.65f;

    public Player(float posX, float posY) {
		super(ANIM_SIZE, posX, posY, 1f, 1f);
		setAnimation(ANIM_IDLE, new Animation(IDLE_FRAMES, IDLE_FPS, "player/idle"));
		setAnimation(ANIM_WALK, new Animation(WALK_RUN_FRAMES, MovementSpeed.WALKING.getValue(), "player/walking"));
		setAnimation(ANIM_RUN, new Animation(WALK_RUN_FRAMES, MovementSpeed.RUNNING.getValue(), "player/walking"));
		// Hitbox schmaler als Render-Sprite: 65 % der Texturbreite, volle Höhe
		setBoundingBoxSize(HITBOX_HALF_WIDTH, scaleY);
	}

	public void setMovementSpeed(MovementSpeed newMovementSpeed) {
        this.movementSpeed = newMovementSpeed.getValue();
    }

	@Override
	public void update(float delta, Window window, Camera camera, World world) {
		final InputProcessor input = window.getInputProcessor();
		final Vector2f movement = new Vector2f();
        final float movementP = movementSpeed * delta;
        final float movementN = -movementSpeed * delta;

        final boolean isLeft  = input.isHeld(InputAction.MOVE_LEFT.getDefaultKey());
        final boolean isRight = input.isHeld(InputAction.MOVE_RIGHT.getDefaultKey());
        final boolean isUp    = input.isHeld(InputAction.MOVE_UP.getDefaultKey());
        final boolean isDown  = input.isHeld(InputAction.MOVE_DOWN.getDefaultKey());

        if (isLeft) {
            if (isUp || isDown) {
                movement.add(movementN / DIAGONAL_SPEED_MULTIPLIER, 0);
            } else {
                movement.add(movementN, 0);
            }
        }
        if (isRight) {
            if (isUp || isDown) {
                movement.add(movementP / DIAGONAL_SPEED_MULTIPLIER, 0);
            } else {
                movement.add(movementP, 0);
            }
        }
        if (isUp) {
            if (isLeft || isRight) {
                movement.add(0, movementP / DIAGONAL_SPEED_MULTIPLIER);
            } else {
                movement.add(0, movementP);
            }
        }
        if (isDown) {
            if (isLeft || isRight) {
                movement.add(0, movementN / DIAGONAL_SPEED_MULTIPLIER);
            } else {
                movement.add(0, movementN);
            }
        }

        if (input.isHeld(InputAction.SPRINT.getDefaultKey())) {
            setMovementSpeed(MovementSpeed.RUNNING);
        } else {
            setMovementSpeed(MovementSpeed.WALKING);
        }

        moveClipped(movement.x, movement.y, world);
        if (movement.x != 0 || movement.y != 0) {
            if (movementSpeed == MovementSpeed.WALKING.getValue()) {
                useAnimation(ANIM_WALK);
            } else {
                useAnimation(ANIM_RUN);
            }
        } else {
            useAnimation(ANIM_IDLE);
        }

		camera.getPosition().lerp(new Vector3f(-posX * world.getScale(), -posY * world.getScale(), 0), CAMERA_LERP_FACTOR);
	}
}

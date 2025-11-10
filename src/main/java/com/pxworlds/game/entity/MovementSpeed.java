package com.pxworlds.game.entity;

public enum MovementSpeed {

    /** Walking movement speed. */
    WALKING(5),
    /** Running movement speed. */
    RUNNING(10);

    /** The value of the movement speed. */
    private final int movementSpeed;

    MovementSpeed(int newMovementSpeed) {
        this.movementSpeed = newMovementSpeed;
    }


    public int getValue() {
        return movementSpeed;
    }
}

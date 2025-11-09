package com.pxworlds.game.entity;

public enum MovementSpeed {

    WALKING(5),
    RUNNING(10);

    /** The value of the movement speed. */
    private final int movementSpeed;

    MovementSpeed(int movementSpeed) {
        this.movementSpeed = movementSpeed;
    }


    public int getValue() {
        return movementSpeed;
    }
}

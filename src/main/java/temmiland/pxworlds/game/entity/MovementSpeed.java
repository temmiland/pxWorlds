package temmiland.pxworlds.game.entity;

public enum MovementSpeed {

    /** Gehgeschwindigkeit. */
    WALKING(5),
    /** Laufgeschwindigkeit. */
    RUNNING(8);

    /** Wert der Bewegungsgeschwindigkeit in Einheiten/Sekunde. */
    private final int movementSpeed;

    MovementSpeed(int newMovementSpeed) {
        this.movementSpeed = newMovementSpeed;
    }


    public int getValue() {
        return movementSpeed;
    }
}

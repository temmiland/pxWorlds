package com.pxworlds.worlds;

public class Location {

    /** The x coordinate. */
    private int x, y, z;

    public Location(int newX, int newY, int newZ) {
        this.x = newX;
        this.y = newY;
        this.z = newZ;
    }

    public int getX() {
        return x;
    }

    public Location setX(int newX) {
        this.x = newX;
        return this;
    }

    public int getY() {
        return y;
    }

    public Location setY(int newY) {
        this.y = newY;
        return this;
    }

    public int getZ() {
        return z;
    }

    public Location setZ(int newZ) {
        this.z = newZ;
        return this;
    }
}

package com.pxworlds.worlds;

public class Location {

    private int x, y, z;

    public Location(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public int getX() {
        return x;
    }

    public Location setX(int x) {
        this.x = x;
        return this;
    }

    public int getY() {
        return y;
    }

    public Location setY(int y) {
        this.y = y;
        return this;
    }

    public int getZ() {
        return z;
    }

    public Location setZ(int z) {
        this.z = z;
        return this;
    }
}

package me.looks.dynamicinformationboard.utils;

public enum BoardFace {

    NORTH(1, 1, 0, 0),
    EAST(0, 1, 1, 90),
    SOUTH(-1, 1, 0, 180),
    WEST(0, 1, -1, -90);

    private final double x;
    private final double y;
    private final double z;
    private final int rotation;

    BoardFace(double x, double y, double z, int rotation) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.rotation = rotation;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public int getRotation() {
        return rotation;
    }
}

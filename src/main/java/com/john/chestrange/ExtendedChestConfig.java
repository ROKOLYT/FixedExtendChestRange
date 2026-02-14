package com.john.chestrange;

public class ExtendedChestConfig {
    private int horizontalRadius;
    private int verticalRadius;
    private int chestLimit;

    public ExtendedChestConfig() {}

    public ExtendedChestConfig(int horizontalRadius, int verticalRadius, int chestLimit) {
        this.horizontalRadius = horizontalRadius;
        this.verticalRadius = verticalRadius;
        this.chestLimit = chestLimit;
    }

    public int getHorizontalRadius() {
        return horizontalRadius;
    }

    public void setHorizontalRadius(int horizontalRadius) {
        this.horizontalRadius = horizontalRadius;
    }

    public int getVerticalRadius() {
        return verticalRadius;
    }

    public void setVerticalRadius(int verticalRadius) {
        this.verticalRadius = verticalRadius;
    }

    public int getChestLimit() {
        return chestLimit;
    }

    public void setChestLimit(int chestLimit) {
        this.chestLimit = chestLimit;
    }
}

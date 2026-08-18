package com.trid.model;

public class CrosshairSettings {
    private final String imagePath;
    private final int width;
    private final int height;
    private final float opacity;
    private final int offsetX;
    private final int offsetY;

    public CrosshairSettings(String imagePath, int width, int height, float opacity, int offsetX, int offsetY) {
        this.imagePath = imagePath;
        this.width = width;
        this.height = height;
        this.opacity = Math.clamp(opacity, 0.0f, 1.0f);
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    public String getImagePath() {
        return imagePath;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public float getOpacity() {return opacity;}

    public int getOffsetX() {
        return offsetX;
    }

    public int getOffsetY() {
        return offsetY;
    }
}
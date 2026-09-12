package org.freelook.config;

/**
 * Configuration for FreeLook and 3rd-person Camera Distance Zoom.
 * Provides configurable settings with sensible defaults inspired by Lunar Client.
 */
public class FreeLookConfig {
    private static final FreeLookConfig INSTANCE = new FreeLookConfig();

    public static FreeLookConfig getInstance() {
        return INSTANCE;
    }

    // Camera look settings
    private float cameraSensitivity = 1.0f;
    private float cameraSmoothing = 18.0f; // Framerate-independent smoothing speed
    private float transitionDuration = 0.18f; // Seconds for entering / exiting transitions
    private boolean invertX = false;
    private boolean invertY = false;

    // 3rd-person camera distance zoom settings
    private float defaultDistance = 4.0f; // Vanilla Minecraft default 3rd-person distance (blocks)
    private float minDistance = 1.5f; // Minimum distance (blocks)
    private float maxDistance = 25.0f; // Maximum distance (blocks, allows 10~15+ blocks)
    private float distanceStep = 1.25f; // Distance change per scroll tick
    private float zoomSmoothing = 14.0f; // Distance interpolation speed
    private boolean invertScroll = false; // Invert scroll direction

    private FreeLookConfig() {}

    public float getCameraSensitivity() {
        return cameraSensitivity;
    }

    public void setCameraSensitivity(float cameraSensitivity) {
        this.cameraSensitivity = Math.max(0.01f, cameraSensitivity);
    }

    public float getCameraSmoothing() {
        return cameraSmoothing;
    }

    public void setCameraSmoothing(float cameraSmoothing) {
        this.cameraSmoothing = Math.max(1.0f, cameraSmoothing);
    }

    public float getTransitionDuration() {
        return transitionDuration;
    }

    public void setTransitionDuration(float transitionDuration) {
        this.transitionDuration = Math.max(0.01f, transitionDuration);
    }

    public boolean isInvertX() {
        return invertX;
    }

    public void setInvertX(boolean invertX) {
        this.invertX = invertX;
    }

    public boolean isInvertY() {
        return invertY;
    }

    public void setInvertY(boolean invertY) {
        this.invertY = invertY;
    }

    public float getDefaultDistance() {
        return defaultDistance;
    }

    public void setDefaultDistance(float defaultDistance) {
        this.defaultDistance = defaultDistance;
    }

    public float getMinDistance() {
        return minDistance;
    }

    public void setMinDistance(float minDistance) {
        this.minDistance = Math.max(0.5f, minDistance);
    }

    public float getMaxDistance() {
        return maxDistance;
    }

    public void setMaxDistance(float maxDistance) {
        this.maxDistance = Math.max(this.minDistance, maxDistance);
    }

    public float getDistanceStep() {
        return distanceStep;
    }

    public void setDistanceStep(float distanceStep) {
        this.distanceStep = Math.max(0.2f, distanceStep);
    }

    public float getZoomSmoothing() {
        return zoomSmoothing;
    }

    public void setZoomSmoothing(float zoomSmoothing) {
        this.zoomSmoothing = Math.max(1.0f, zoomSmoothing);
    }

    public boolean isInvertScroll() {
        return invertScroll;
    }

    public void setInvertScroll(boolean invertScroll) {
        this.invertScroll = invertScroll;
    }
}

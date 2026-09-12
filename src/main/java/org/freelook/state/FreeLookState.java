package org.freelook.state;

/**
 * State lifecycle of the FreeLook camera.
 * Smooth transitions are applied between NORMAL, ENTERING_FREELOOK, FREELOOK, and EXITING_FREELOOK.
 */
public enum FreeLookState {
    /**
     * Default Minecraft camera. Camera view matches the player's true look direction.
     */
    NORMAL,

    /**
     * Smoothly easing from the player's initial look direction to the FreeLook camera.
     */
    ENTERING_FREELOOK,

    /**
     * Fully decoupled FreeLook state. Mouse movement controls camera view freely.
     */
    FREELOOK,

    /**
     * Smoothly easing from the decoupled FreeLook camera back to the player's current view.
     */
    EXITING_FREELOOK
}

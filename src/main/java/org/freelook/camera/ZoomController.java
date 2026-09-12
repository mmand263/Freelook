package org.freelook.camera;

import net.minecraft.util.math.MathHelper;
import org.freelook.config.FreeLookConfig;

/**
 * Manages 3rd-person camera distance zoom target via mouse wheel scrolling during FreeLook.
 * Allows pulling the camera back up to 10~15+ blocks for wide-angle reconnaissance.
 */
public class ZoomController {
    private static final ZoomController INSTANCE = new ZoomController();

    public static ZoomController getInstance() {
        return INSTANCE;
    }

    private float targetDistance = 4.0f;

    private ZoomController() {}

    /**
     * Handles mouse scroll input while FreeLook is active.
     * Scrolling down (vertical < 0) pulls camera further away (zooms out -> increases distance).
     * Scrolling up (vertical > 0) pulls camera closer to player (zooms in -> decreases distance).
     *
     * @param vertical Scroll wheel vertical delta
     */
    public void onMouseScroll(double vertical) {
        if (Math.abs(vertical) < 1e-4) return;
        FreeLookConfig config = FreeLookConfig.getInstance();

        // Default: vertical > 0 (wheel up) is zooming in (pulling camera forward)
        //          vertical < 0 (wheel down) is zooming out (pulling camera back)
        boolean isZoomingIn = vertical > 0;
        if (config.isInvertScroll()) {
            isZoomingIn = !isZoomingIn;
        }

        float step = isZoomingIn ? config.getZoomInStep() : config.getZoomOutStep();
        float delta = (float) (Math.signum(vertical) * step);
        if (config.isInvertScroll()) {
            delta = -delta;
        }

        targetDistance = MathHelper.clamp(
                targetDistance - delta,
                config.getMinDistance(),
                config.getMaxDistance()
        );
    }

    public void resetTargetDistance() {
        this.targetDistance = FreeLookConfig.getInstance().getDefaultDistance();
    }

    public void resetImmediately() {
        this.targetDistance = FreeLookConfig.getInstance().getDefaultDistance();
    }

    public float getTargetDistance() {
        return targetDistance;
    }

    public void setTargetDistance(float targetDistance) {
        this.targetDistance = targetDistance;
    }
}

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
        float step = config.getDistanceStep();
        float delta = (float) (Math.signum(vertical) * step);
        if (config.isInvertScroll()) {
            delta = -delta;
        }

        // Wheel down (vertical < 0) -> delta < 0 -> targetDistance increases (camera pulls back)
        // Wheel up (vertical > 0) -> delta > 0 -> targetDistance decreases (camera pulls forward)
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

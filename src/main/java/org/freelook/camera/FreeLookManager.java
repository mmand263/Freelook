package org.freelook.camera;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.MathHelper;
import org.freelook.config.FreeLookConfig;
import org.freelook.input.KeybindManager;
import org.freelook.state.FreeLookState;

/**
 * Core manager for FreeLook camera orientation, state transitions, and smooth interpolation.
 * Decouples camera rendering from the player entity, providing a seamless glide from
 * 1st person into 3rd person, 360-degree free look, and distance zooming (10~15+ blocks).
 */
public class FreeLookManager {
    private static final FreeLookManager INSTANCE = new FreeLookManager();

    public static FreeLookManager getInstance() {
        return INSTANCE;
    }

    private FreeLookState state = FreeLookState.NORMAL;

    // Track whether player was in 1st person when entering FreeLook
    private boolean wasFirstPerson = true;

    // Target angles set by mouse movement
    private float targetCameraYaw = 0.0f;
    private float targetCameraPitch = 0.0f;

    // Current smoothed camera angles
    private float currentCameraYaw = 0.0f;
    private float currentCameraPitch = 0.0f;

    // Transition tracking
    private float transitionProgress = 0.0f;
    private float startTransitionYaw = 0.0f;
    private float startTransitionPitch = 0.0f;

    private float exitProgress = 0.0f;
    private float exitStartYaw = 0.0f;
    private float exitStartPitch = 0.0f;

    // Distance tracking for smooth 3rd-person glide
    private float currentDistance = 0.0f;
    private float startTransitionDistance = 0.0f;
    private float exitStartDistance = 4.0f;

    // Last rendered angles for seamless reversal
    private float lastRenderedYaw = 0.0f;
    private float lastRenderedPitch = 0.0f;

    // Frame timing
    private long lastFrameTimeNanos = -1L;

    private FreeLookManager() {}

    /**
     * Starts FreeLook transition from the player's current view.
     * Glides smoothly from 1st person (distance 0.0) out to 3rd person (distance 4.0).
     */
    public void startFreeLook(ClientPlayerEntity player) {
        if (player == null) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && client.options != null) {
            wasFirstPerson = client.options.getPerspective().isFirstPerson();
        }

        FreeLookConfig config = FreeLookConfig.getInstance();

        if (state == FreeLookState.NORMAL) {
            state = FreeLookState.ENTERING_FREELOOK;
            transitionProgress = 0.0f;

            // Pick up the exact sub-tick orientation rendered on the preceding frame
            startTransitionYaw = (lastFrameTimeNanos > 0) ? lastRenderedYaw : player.getYaw();
            startTransitionPitch = (lastFrameTimeNanos > 0) ? lastRenderedPitch : player.getPitch();

            targetCameraYaw = startTransitionYaw;
            targetCameraPitch = startTransitionPitch;
            currentCameraYaw = startTransitionYaw;
            currentCameraPitch = startTransitionPitch;

            lastRenderedYaw = startTransitionYaw;
            lastRenderedPitch = startTransitionPitch;

            startTransitionDistance = wasFirstPerson ? 0.0f : config.getDefaultDistance();
            currentDistance = startTransitionDistance;
            ZoomController.getInstance().resetTargetDistance();
        } else if (state == FreeLookState.EXITING_FREELOOK) {
            // Re-entering FreeLook before exit animation completed: smoothly reverse
            state = FreeLookState.ENTERING_FREELOOK;
            transitionProgress = 0.0f;

            startTransitionYaw = lastRenderedYaw;
            startTransitionPitch = lastRenderedPitch;

            targetCameraYaw = currentCameraYaw;
            targetCameraPitch = currentCameraPitch;

            startTransitionDistance = currentDistance;
            ZoomController.getInstance().resetTargetDistance();
        }
    }

    /**
     * Initiates smooth return to the player's true look direction and eye position.
     */
    public void stopFreeLook() {
        if (state == FreeLookState.FREELOOK || state == FreeLookState.ENTERING_FREELOOK) {
            state = FreeLookState.EXITING_FREELOOK;
            exitProgress = 0.0f;

            exitStartYaw = lastRenderedYaw;
            exitStartPitch = lastRenderedPitch;

            exitStartDistance = currentDistance;
        }
    }

    /**
     * Fully resets FreeLook state to normal.
     */
    public void reset() {
        state = FreeLookState.NORMAL;
        transitionProgress = 0.0f;
        exitProgress = 0.0f;
        lastFrameTimeNanos = -1L;
        wasFirstPerson = true;
        currentDistance = 0.0f;
        ZoomController.getInstance().resetImmediately();
    }

    /**
     * Checks key state with instantaneous responsiveness.
     */
    public void checkKeyState() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null) {
            if (state != FreeLookState.NORMAL) reset();
            return;
        }

        if (client.currentScreen != null || client.player.isDead()) {
            if (isFreeLookActive()) {
                stopFreeLook();
            }
            return;
        }

        boolean isDown = KeybindManager.getFreeLookKey() != null && KeybindManager.getFreeLookKey().isPressed();
        if (isDown) {
            if (state == FreeLookState.NORMAL || state == FreeLookState.EXITING_FREELOOK) {
                startFreeLook(client.player);
            }
        } else {
            if (isFreeLookActive()) {
                stopFreeLook();
            }
        }
    }

    /**
     * Processes mouse movement during FreeLook.
     */
    public void onMouseTurn(double dx, double dy) {
        if (!isFreeLookActive()) return;

        FreeLookConfig config = FreeLookConfig.getInstance();
        float sens = config.getCameraSensitivity();

        float deltaYaw = (float) (dx * 0.15 * sens);
        float deltaPitch = (float) (dy * 0.15 * sens);

        if (config.isInvertX()) {
            deltaYaw = -deltaYaw;
        }
        if (config.isInvertY()) {
            deltaPitch = -deltaPitch;
        }

        targetCameraYaw += deltaYaw;
        targetCameraPitch = MathHelper.clamp(targetCameraPitch + deltaPitch, -90.0f, 90.0f);
    }

    /**
     * Updates frame timing, key state, distance zoom, and camera easing logic.
     * Called on each render frame before camera calculations.
     */
    public void updateFrame() {
        checkKeyState();

        long now = System.nanoTime();
        if (lastFrameTimeNanos <= 0) {
            lastFrameTimeNanos = now;
            return;
        }

        float dt = (float) ((now - lastFrameTimeNanos) / 1_000_000_000.0);
        lastFrameTimeNanos = now;

        // Clamp delta time to avoid jumps on severe lag spikes
        dt = MathHelper.clamp(dt, 0.0001f, 0.05f);

        FreeLookConfig config = FreeLookConfig.getInstance();
        float targetDist = ZoomController.getInstance().getTargetDistance();

        // Update 3rd-person camera distance and state transitions
        if (state == FreeLookState.ENTERING_FREELOOK) {
            float duration = Math.max(0.01f, config.getTransitionDuration());
            transitionProgress += dt / duration;
            if (transitionProgress >= 1.0f) {
                transitionProgress = 1.0f;
                state = FreeLookState.FREELOOK;
                currentDistance = targetDist;
            } else {
                float eased = easeOutCubic(transitionProgress);
                currentDistance = MathHelper.lerp(eased, startTransitionDistance, targetDist);
            }
        } else if (state == FreeLookState.FREELOOK) {
            float factor = 1.0f - (float) Math.exp(-config.getZoomSmoothing() * dt);
            currentDistance += (targetDist - currentDistance) * factor;
            if (Math.abs(targetDist - currentDistance) < 0.005f) {
                currentDistance = targetDist;
            }
        } else if (state == FreeLookState.EXITING_FREELOOK) {
            float duration = Math.max(0.01f, config.getTransitionDuration());
            exitProgress += dt / duration;
            float returnDist = wasFirstPerson ? 0.0f : config.getDefaultDistance();
            if (exitProgress >= 1.0f) {
                exitProgress = 1.0f;
                currentDistance = returnDist;
                state = FreeLookState.NORMAL;
                ZoomController.getInstance().resetImmediately();
            } else {
                float eased = easeOutCubic(exitProgress);
                currentDistance = MathHelper.lerp(eased, exitStartDistance, returnDist);
            }
        }

        // Apply exponential smoothing to camera angles
        if (state == FreeLookState.ENTERING_FREELOOK || state == FreeLookState.FREELOOK) {
            float smoothFactor = 1.0f - (float) Math.exp(-config.getCameraSmoothing() * dt);

            // Shortest arc wrapping for yaw to prevent 360-degree meridian flip
            float diffYaw = MathHelper.wrapDegrees(targetCameraYaw - currentCameraYaw);
            currentCameraYaw += diffYaw * smoothFactor;
            currentCameraYaw = MathHelper.wrapDegrees(currentCameraYaw);

            // Pitch clamping and smoothing
            float diffPitch = targetCameraPitch - currentCameraPitch;
            currentCameraPitch += diffPitch * smoothFactor;
            currentCameraPitch = MathHelper.clamp(currentCameraPitch, -90.0f, 90.0f);
        }
    }

    /**
     * Calculates the rendered camera yaw angle based on current state and easing.
     */
    public float getRenderYaw(float tickDelta, float playerYaw) {
        switch (state) {
            case NORMAL:
                lastRenderedYaw = playerYaw;
                return playerYaw;

            case ENTERING_FREELOOK: {
                float eased = easeOutCubic(transitionProgress);
                lastRenderedYaw = interpolateAngle(startTransitionYaw, currentCameraYaw, eased);
                return lastRenderedYaw;
            }

            case FREELOOK:
                lastRenderedYaw = currentCameraYaw;
                return currentCameraYaw;

            case EXITING_FREELOOK: {
                float eased = easeOutCubic(exitProgress);
                lastRenderedYaw = interpolateAngle(exitStartYaw, playerYaw, eased);
                return lastRenderedYaw;
            }

            default:
                return playerYaw;
        }
    }

    /**
     * Calculates the rendered camera pitch angle based on current state and easing.
     */
    public float getRenderPitch(float tickDelta, float playerPitch) {
        switch (state) {
            case NORMAL:
                lastRenderedPitch = playerPitch;
                return playerPitch;

            case ENTERING_FREELOOK: {
                float eased = easeOutCubic(transitionProgress);
                lastRenderedPitch = MathHelper.lerp(eased, startTransitionPitch, currentCameraPitch);
                return lastRenderedPitch;
            }

            case FREELOOK:
                lastRenderedPitch = currentCameraPitch;
                return currentCameraPitch;

            case EXITING_FREELOOK: {
                float eased = easeOutCubic(exitProgress);
                lastRenderedPitch = MathHelper.lerp(eased, exitStartPitch, playerPitch);
                return lastRenderedPitch;
            }

            default:
                return playerPitch;
        }
    }

    public boolean isFreeLookActive() {
        return state == FreeLookState.ENTERING_FREELOOK || state == FreeLookState.FREELOOK;
    }

    public boolean isCameraOverridden() {
        return state != FreeLookState.NORMAL;
    }

    public FreeLookState getState() {
        return state;
    }

    public float getCurrentDistance() {
        return currentDistance;
    }

    private static float interpolateAngle(float from, float to, float progress) {
        return from + MathHelper.wrapDegrees(to - from) * progress;
    }

    private static float easeOutCubic(float t) {
        return 1.0f - (float) Math.pow(1.0f - t, 3);
    }
}

package org.freelook.mixin.client;

import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.BlockView;
import org.freelook.camera.FreeLookManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin to net.minecraft.client.render.Camera.
 * 1) Forces thirdPerson mode during FreeLook so the player model is rendered.
 * 2) Applies smoothed FreeLook orientation at the end of update.
 * 3) Moves camera backwards by the smoothly interpolated zoom distance (0.0 -> 4.0 -> 25.0 blocks).
 */
@Mixin(Camera.class)
public abstract class CameraMixin {

    @Shadow private boolean thirdPerson;
    @Shadow private float cameraY;
    @Shadow private float lastCameraY;

    @Shadow protected abstract void setRotation(float yaw, float pitch);
    @Shadow protected abstract void setPos(double x, double y, double z);
    @Shadow protected abstract void moveBy(double x, double y, double z);
    @Shadow protected abstract double clipToSpace(double desiredCameraDistance);

    /**
     * Intercepts Camera.update at RETURN.
     * When FreeLook is active or transitioning:
     * - Marks camera as thirdPerson (so player model renders).
     * - Resets position to exact interpolated eye height.
     * - Sets camera orientation to smoothed FreeLook yaw and pitch.
     * - Moves camera backwards by clipped smooth distance (gliding 0.0 -> 4.0 -> 25.0 blocks).
     */
    @Inject(method = "update", at = @At("RETURN"))
    private void onUpdate(BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci) {
        FreeLookManager manager = FreeLookManager.getInstance();
        manager.updateFrame();

        if (focusedEntity != null && manager.isCameraOverridden()) {
            this.thirdPerson = true;

            float renderYaw = manager.getRenderYaw(tickDelta, focusedEntity.getYaw(tickDelta));
            float renderPitch = manager.getRenderPitch(tickDelta, focusedEntity.getPitch(tickDelta));

            double eyeX = MathHelper.lerp(tickDelta, focusedEntity.prevX, focusedEntity.getX());
            double eyeY = MathHelper.lerp(tickDelta, focusedEntity.prevY, focusedEntity.getY())
                        + MathHelper.lerp(tickDelta, this.lastCameraY, this.cameraY);
            double eyeZ = MathHelper.lerp(tickDelta, focusedEntity.prevZ, focusedEntity.getZ());

            this.setPos(eyeX, eyeY, eyeZ);
            this.setRotation(renderYaw, renderPitch);

            double distance = manager.getCurrentDistance();
            if (distance > 0.001) {
                double clipped = this.clipToSpace(distance);
                this.moveBy(-clipped, 0.0, 0.0);
            }
        }
    }

    /**
     * Ensures WorldRenderer knows camera is in third person so it renders the player entity model.
     */
    @Inject(method = "isThirdPerson", at = @At("HEAD"), cancellable = true)
    private void onIsThirdPerson(CallbackInfoReturnable<Boolean> cir) {
        if (FreeLookManager.getInstance().isCameraOverridden()) {
            cir.setReturnValue(true);
        }
    }
}

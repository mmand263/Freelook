package org.freelook.mixin.client;

import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.freelook.camera.FreeLookManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to net.minecraft.client.render.GameRenderer.
 * Suppresses first-person hand rendering when FreeLook is active or transitioning.
 */
@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Inject(
        method = "renderHand",
        at = @At("HEAD"),
        cancellable = true
    )
    private void onRenderHand(MatrixStack matrices, Camera camera, float tickDelta, CallbackInfo ci) {
        if (FreeLookManager.getInstance().isCameraOverridden()) {
            ci.cancel();
        }
    }
}

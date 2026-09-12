package org.freelook.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.network.ClientPlayerEntity;
import org.freelook.camera.FreeLookManager;
import org.freelook.camera.ZoomController;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to net.minecraft.client.Mouse.
 * 1) Redirects mouse deltas to FreeLookManager when active, keeping player entity unaffected.
 * 2) Intercepts mouse wheel scrolling for smooth camera distance zoom without changing hotbar.
 */
@Mixin(Mouse.class)
public abstract class MouseMixin {

    @Shadow
    @Final
    private MinecraftClient client;

    /**
     * Intercepts player.changeLookDirection in Mouse.updateMouse().
     */
    @Redirect(
        method = "updateMouse",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/network/ClientPlayerEntity;changeLookDirection(DD)V"
        )
    )
    private void redirectChangeLookDirection(ClientPlayerEntity player, double dx, double dy) {
        FreeLookManager manager = FreeLookManager.getInstance();
        manager.checkKeyState();
        if (manager.isFreeLookActive()) {
            manager.onMouseTurn(dx, dy);
        } else {
            player.changeLookDirection(dx, dy);
        }
    }

    /**
     * Intercepts scroll wheel events in-game during FreeLook to adjust camera zoom.
     * Prevents hotbar switching while FreeLook is active or transitioning.
     */
    @Inject(
        method = "onMouseScroll",
        at = @At("HEAD"),
        cancellable = true
    )
    private void onMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        if (this.client != null && this.client.currentScreen == null) {
            FreeLookManager manager = FreeLookManager.getInstance();
            if (manager.isCameraOverridden()) {
                if (manager.isFreeLookActive()) {
                    ZoomController.getInstance().onMouseScroll(vertical);
                }
                ci.cancel();
            }
        }
    }
}

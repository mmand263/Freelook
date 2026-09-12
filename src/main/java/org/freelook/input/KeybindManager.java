package org.freelook.input;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.freelook.camera.FreeLookManager;
import org.lwjgl.glfw.GLFW;

/**
 * Registers and monitors keybindings for FreeLook.
 * Manages hold-to-activate behavior and handles GUI/death edge cases.
 */
public class KeybindManager {
    private static KeyBinding freeLookKey;

    public static void register() {
        freeLookKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.freelook.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_V,
                "category.freelook"
        ));
    }

    public static void handleClientTick(MinecraftClient client) {
        FreeLookManager.getInstance().checkKeyState();
    }

    public static KeyBinding getFreeLookKey() {
        return freeLookKey;
    }
}

package org.freelook;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import org.freelook.camera.FreeLookManager;
import org.freelook.input.KeybindManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client entry point for the FreeLook & Smooth Zoom mod.
 */
public class FreeLookClient implements ClientModInitializer {
    public static final String MOD_ID = "freelook";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        LOGGER.info("[FreeLook] Initializing FreeLook & Smooth Zoom mod for Minecraft 1.20.4...");

        // Register keybindings
        KeybindManager.register();

        // Register client tick handler for input monitoring
        ClientTickEvents.END_CLIENT_TICK.register(KeybindManager::handleClientTick);

        // Register disconnect handler for clean state reset
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            FreeLookManager.getInstance().reset();
        });

        LOGGER.info("[FreeLook] Initialized successfully.");
    }
}

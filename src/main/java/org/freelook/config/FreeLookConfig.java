package org.freelook.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.freelook.FreeLookClient;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;

/**
 * Configuration for FreeLook and 3rd-person Camera Distance Zoom.
 * Provides configurable settings with persistence to config/freelook.json.
 */
public class FreeLookConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("freelook.json").toFile();

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
    private float maxDistance = 25.0f; // Maximum distance (blocks)
    private float distanceStep = 1.25f; // Distance change per scroll tick
    private float zoomSmoothing = 14.0f; // Distance interpolation speed
    private boolean invertScroll = false; // Invert scroll direction

    private FreeLookConfig() {}

    /**
     * Loads configuration from freelook.json. If the file does not exist, saves default values.
     */
    public void load() {
        if (!CONFIG_FILE.exists()) {
            save();
            return;
        }

        try (FileReader reader = new FileReader(CONFIG_FILE, StandardCharsets.UTF_8)) {
            FreeLookConfig loaded = GSON.fromJson(reader, FreeLookConfig.class);
            if (loaded != null) {
                this.cameraSensitivity = loaded.cameraSensitivity;
                this.cameraSmoothing = loaded.cameraSmoothing;
                this.transitionDuration = loaded.transitionDuration;
                this.invertX = loaded.invertX;
                this.invertY = loaded.invertY;

                this.defaultDistance = loaded.defaultDistance;
                this.minDistance = loaded.minDistance;
                this.maxDistance = loaded.maxDistance;
                this.distanceStep = loaded.distanceStep;
                this.zoomSmoothing = loaded.zoomSmoothing;
                this.invertScroll = loaded.invertScroll;
                FreeLookClient.LOGGER.info("[FreeLook] Config loaded successfully.");
            }
        } catch (Exception e) {
            FreeLookClient.LOGGER.error("[FreeLook] Failed to load configuration, using defaults.", e);
        }
    }

    /**
     * Saves current configuration to freelook.json.
     */
    public void save() {
        try {
            File parentDir = CONFIG_FILE.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }

            try (FileWriter writer = new FileWriter(CONFIG_FILE, StandardCharsets.UTF_8)) {
                GSON.toJson(this, writer);
            }
            FreeLookClient.LOGGER.info("[FreeLook] Config saved successfully.");
        } catch (Exception e) {
            FreeLookClient.LOGGER.error("[FreeLook] Failed to save configuration.", e);
        }
    }

    /**
     * Resets all options to default values.
     */
    public void resetToDefaults() {
        this.cameraSensitivity = 1.0f;
        this.cameraSmoothing = 18.0f;
        this.transitionDuration = 0.18f;
        this.invertX = false;
        this.invertY = false;

        this.defaultDistance = 4.0f;
        this.minDistance = 1.5f;
        this.maxDistance = 25.0f;
        this.distanceStep = 1.25f;
        this.zoomSmoothing = 14.0f;
        this.invertScroll = false;
    }

    // Getters and Setters

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
        this.distanceStep = Math.max(0.1f, distanceStep);
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

package org.freelook.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import org.freelook.config.FreeLookConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * In-game configuration screen for FreeLook, accessible via ModMenu.
 * Provides controls for transition duration, mouse wheel zoom step, smoothing, distances, and camera controls.
 */
public class FreeLookConfigScreen extends Screen {
    private final Screen parent;
    private ConfigListWidget list;

    // Temporary working state
    private float cameraSensitivity;
    private float cameraSmoothing;
    private float transitionDuration;
    private boolean invertX;
    private boolean invertY;

    private float defaultDistance;
    private float minDistance;
    private float maxDistance;
    private float distanceStep;
    private float zoomSmoothing;
    private boolean invertScroll;

    // References to control widgets for reset functionality
    private final List<ResettableControl> resettables = new ArrayList<>();

    public FreeLookConfigScreen(Screen parent) {
        super(Text.translatable("freelook.config.title"));
        this.parent = parent;
        loadCurrentValues();
    }

    private void loadCurrentValues() {
        FreeLookConfig config = FreeLookConfig.getInstance();
        this.cameraSensitivity = config.getCameraSensitivity();
        this.cameraSmoothing = config.getCameraSmoothing();
        this.transitionDuration = config.getTransitionDuration();
        this.invertX = config.isInvertX();
        this.invertY = config.isInvertY();

        this.defaultDistance = config.getDefaultDistance();
        this.minDistance = config.getMinDistance();
        this.maxDistance = config.getMaxDistance();
        this.distanceStep = config.getDistanceStep();
        this.zoomSmoothing = config.getZoomSmoothing();
        this.invertScroll = config.isInvertScroll();
    }

    private void saveAndApplyValues() {
        FreeLookConfig config = FreeLookConfig.getInstance();
        config.setCameraSensitivity(this.cameraSensitivity);
        config.setCameraSmoothing(this.cameraSmoothing);
        config.setTransitionDuration(this.transitionDuration);
        config.setInvertX(this.invertX);
        config.setInvertY(this.invertY);

        config.setDefaultDistance(this.defaultDistance);
        config.setMinDistance(this.minDistance);
        config.setMaxDistance(this.maxDistance);
        config.setDistanceStep(this.distanceStep);
        config.setZoomSmoothing(this.zoomSmoothing);
        config.setInvertScroll(this.invertScroll);

        config.save();
    }

    @Override
    protected void init() {
        super.init();
        this.resettables.clear();

        int listTop = 32;
        int listHeight = this.height - listTop - 40;
        this.list = new ConfigListWidget(this.client, this.width, listHeight, listTop, 26);

        // ==================== CATEGORY 1: ZOOM & TRANSITION ====================
        this.list.addCategory(Text.translatable("freelook.config.category.zoom").formatted(Formatting.YELLOW, Formatting.BOLD));

        // Row 1: Transition Duration & Wheel Zoom Step (The primary requested options!)
        ConfigSliderWidget transitionSlider = new ConfigSliderWidget(
                "freelook.config.transition_duration", "freelook.config.transition_duration.tooltip",
                0.05, 1.00, 0.01, 2, this.transitionDuration,
                val -> this.transitionDuration = val, 0.18f
        );
        ConfigSliderWidget stepSlider = new ConfigSliderWidget(
                "freelook.config.distance_step", "freelook.config.distance_step.tooltip",
                0.10, 5.00, 0.05, 2, this.distanceStep,
                val -> this.distanceStep = val, 1.25f
        );
        this.resettables.add(transitionSlider);
        this.resettables.add(stepSlider);
        this.list.addEntry(new TwoWidgetEntry(transitionSlider, stepSlider));

        // Row 2: Default Distance & Zoom Smoothing
        ConfigSliderWidget defaultDistSlider = new ConfigSliderWidget(
                "freelook.config.default_distance", "freelook.config.default_distance.tooltip",
                1.0, 15.0, 0.5, 1, this.defaultDistance,
                val -> this.defaultDistance = val, 4.0f
        );
        ConfigSliderWidget zoomSmoothSlider = new ConfigSliderWidget(
                "freelook.config.zoom_smoothing", "freelook.config.zoom_smoothing.tooltip",
                2.0, 30.0, 1.0, 0, this.zoomSmoothing,
                val -> this.zoomSmoothing = val, 14.0f
        );
        this.resettables.add(defaultDistSlider);
        this.resettables.add(zoomSmoothSlider);
        this.list.addEntry(new TwoWidgetEntry(defaultDistSlider, zoomSmoothSlider));

        // Row 3: Min Distance & Max Distance
        ConfigSliderWidget minDistSlider = new ConfigSliderWidget(
                "freelook.config.min_distance", "freelook.config.min_distance.tooltip",
                0.5, 4.0, 0.5, 1, this.minDistance,
                val -> this.minDistance = val, 1.5f
        );
        ConfigSliderWidget maxDistSlider = new ConfigSliderWidget(
                "freelook.config.max_distance", "freelook.config.max_distance.tooltip",
                5.0, 50.0, 1.0, 0, this.maxDistance,
                val -> this.maxDistance = val, 25.0f
        );
        this.resettables.add(minDistSlider);
        this.resettables.add(maxDistSlider);
        this.list.addEntry(new TwoWidgetEntry(minDistSlider, maxDistSlider));

        // Row 4: Invert Scroll
        ConfigToggleButton invertScrollBtn = new ConfigToggleButton(
                "freelook.config.invert_scroll", "freelook.config.invert_scroll.tooltip",
                this.invertScroll, val -> this.invertScroll = val, false
        );
        this.resettables.add(invertScrollBtn);
        this.list.addEntry(new SingleWidgetEntry(invertScrollBtn));

        // ==================== CATEGORY 2: CAMERA ROTATION ====================
        this.list.addCategory(Text.translatable("freelook.config.category.camera").formatted(Formatting.YELLOW, Formatting.BOLD));

        // Row 5: Camera Sensitivity & Camera Smoothing
        ConfigSliderWidget sensSlider = new ConfigSliderWidget(
                "freelook.config.camera_sensitivity", "freelook.config.camera_sensitivity.tooltip",
                0.10, 3.00, 0.05, 2, this.cameraSensitivity,
                val -> this.cameraSensitivity = val, 1.0f
        );
        ConfigSliderWidget camSmoothSlider = new ConfigSliderWidget(
                "freelook.config.camera_smoothing", "freelook.config.camera_smoothing.tooltip",
                2.0, 30.0, 1.0, 0, this.cameraSmoothing,
                val -> this.cameraSmoothing = val, 18.0f
        );
        this.resettables.add(sensSlider);
        this.resettables.add(camSmoothSlider);
        this.list.addEntry(new TwoWidgetEntry(sensSlider, camSmoothSlider));

        // Row 6: Invert Mouse X & Invert Mouse Y
        ConfigToggleButton invertXBtn = new ConfigToggleButton(
                "freelook.config.invert_x", null,
                this.invertX, val -> this.invertX = val, false
        );
        ConfigToggleButton invertYBtn = new ConfigToggleButton(
                "freelook.config.invert_y", null,
                this.invertY, val -> this.invertY = val, false
        );
        this.resettables.add(invertXBtn);
        this.resettables.add(invertYBtn);
        this.list.addEntry(new TwoWidgetEntry(invertXBtn, invertYBtn));

        this.addDrawableChild(this.list);

        // ==================== BOTTOM BUTTON BAR ====================
        int buttonY = this.height - 30;
        int buttonWidth = 100;
        int spacing = 8;
        int totalWidth = (buttonWidth * 3) + (spacing * 2);
        int startX = (this.width - totalWidth) / 2;

        // Reset All Button
        this.addDrawableChild(ButtonWidget.builder(
                Text.translatable("freelook.config.reset_all"),
                button -> resetAll()
        ).dimensions(startX, buttonY, buttonWidth, 20)
        .tooltip(Tooltip.of(Text.translatable("freelook.config.reset_all.tooltip")))
        .build());

        // Done (Save) Button
        this.addDrawableChild(ButtonWidget.builder(
                ScreenTexts.DONE,
                button -> {
                    saveAndApplyValues();
                    close();
                }
        ).dimensions(startX + buttonWidth + spacing, buttonY, buttonWidth, 20).build());

        // Cancel Button
        this.addDrawableChild(ButtonWidget.builder(
                ScreenTexts.CANCEL,
                button -> close()
        ).dimensions(startX + (buttonWidth + spacing) * 2, buttonY, buttonWidth, 20).build());
    }

    private void resetAll() {
        for (ResettableControl resettable : resettables) {
            resettable.resetToDefault();
        }
    }

    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(this.parent);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 14, 0xFFFFFF);
    }

    // ==================== HELPER INTERFACE & WIDGETS ====================

    private interface ResettableControl {
        void resetToDefault();
    }

    public static class ConfigSliderWidget extends SliderWidget implements ResettableControl {
        private final String key;
        private final double min;
        private final double max;
        private final double step;
        private final int decimals;
        private final Consumer<Float> onApply;
        private final float defaultValue;

        public ConfigSliderWidget(String key, String tooltipKey,
                                  double min, double max, double step, int decimals,
                                  float initialValue, Consumer<Float> onApply, float defaultValue) {
            super(0, 0, 150, 20, ScreenTexts.EMPTY, (initialValue - min) / (max - min));
            this.key = key;
            this.min = min;
            this.max = max;
            this.step = step;
            this.decimals = decimals;
            this.onApply = onApply;
            this.defaultValue = defaultValue;

            if (tooltipKey != null) {
                this.setTooltip(Tooltip.of(Text.translatable(tooltipKey)));
            }
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            double actual = getActualValue();
            String formatted;
            if (decimals == 0) {
                formatted = String.format(Locale.ROOT, "%.0f", actual);
            } else if (decimals == 1) {
                formatted = String.format(Locale.ROOT, "%.1f", actual);
            } else {
                formatted = String.format(Locale.ROOT, "%.2f", actual);
            }
            this.setMessage(Text.translatable(this.key, formatted));
        }

        @Override
        protected void applyValue() {
            float actual = (float) getActualValue();
            onApply.accept(actual);
        }

        public double getActualValue() {
            double raw = min + (value * (max - min));
            if (step > 0) {
                raw = Math.round(raw / step) * step;
            }
            return MathHelper.clamp(raw, min, max);
        }

        @Override
        public void resetToDefault() {
            this.value = MathHelper.clamp((defaultValue - min) / (max - min), 0.0, 1.0);
            updateMessage();
            applyValue();
        }
    }

    public static class ConfigToggleButton extends ButtonWidget implements ResettableControl {
        private final String key;
        private boolean state;
        private final Consumer<Boolean> onToggle;
        private final boolean defaultState;

        public ConfigToggleButton(String key, String tooltipKey,
                                  boolean initialState, Consumer<Boolean> onToggle, boolean defaultState) {
            super(0, 0, 150, 20, ScreenTexts.EMPTY, btn -> {}, DEFAULT_NARRATION_SUPPLIER);
            this.key = key;
            this.state = initialState;
            this.onToggle = onToggle;
            this.defaultState = defaultState;

            if (tooltipKey != null) {
                this.setTooltip(Tooltip.of(Text.translatable(tooltipKey)));
            }
            updateButtonText();
        }

        @Override
        public void onPress() {
            this.state = !this.state;
            this.onToggle.accept(this.state);
            updateButtonText();
        }

        private void updateButtonText() {
            Text stateText = this.state ? ScreenTexts.ON : ScreenTexts.OFF;
            this.setMessage(Text.translatable(this.key, stateText));
        }

        @Override
        public void resetToDefault() {
            this.state = defaultState;
            this.onToggle.accept(this.state);
            updateButtonText();
        }
    }

    // ==================== LIST AND ENTRY CLASSES ====================

    public static class ConfigListWidget extends ElementListWidget<ConfigListWidget.Entry> {
        public ConfigListWidget(MinecraftClient client, int width, int height, int y, int itemHeight) {
            super(client, width, height, y, itemHeight);
        }

        @Override
        public int addEntry(ConfigListWidget.Entry entry) {
            return super.addEntry(entry);
        }

        public void addCategory(Text title) {
            this.addEntry(new CategoryEntry(title));
        }

        @Override
        public int getRowWidth() {
            return 310;
        }

        @Override
        protected int getScrollbarPositionX() {
            return (this.width / 2) + 165;
        }

        public abstract static class Entry extends ElementListWidget.Entry<Entry> {}
    }

    public static class CategoryEntry extends ConfigListWidget.Entry {
        private final Text title;

        public CategoryEntry(Text title) {
            this.title = title;
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            context.drawCenteredTextWithShadow(
                    MinecraftClient.getInstance().textRenderer,
                    this.title,
                    x + (entryWidth / 2),
                    y + 8,
                    0xFFFFFF
            );
        }

        @Override
        public List<? extends Selectable> selectableChildren() {
            return Collections.emptyList();
        }

        @Override
        public List<? extends Element> children() {
            return Collections.emptyList();
        }
    }

    public static class TwoWidgetEntry extends ConfigListWidget.Entry {
        private final ClickableWidget left;
        private final ClickableWidget right;
        private final List<ClickableWidget> childrenList = new ArrayList<>();

        public TwoWidgetEntry(ClickableWidget left, ClickableWidget right) {
            this.left = left;
            this.right = right;
            if (left != null) childrenList.add(left);
            if (right != null) childrenList.add(right);
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            int centerX = x + (entryWidth / 2);
            if (left != null) {
                left.setX(centerX - 155);
                left.setY(y);
                left.render(context, mouseX, mouseY, tickDelta);
            }
            if (right != null) {
                right.setX(centerX + 5);
                right.setY(y);
                right.render(context, mouseX, mouseY, tickDelta);
            }
        }

        @Override
        public List<? extends Selectable> selectableChildren() {
            return childrenList;
        }

        @Override
        public List<? extends Element> children() {
            return childrenList;
        }
    }

    public static class SingleWidgetEntry extends ConfigListWidget.Entry {
        private final ClickableWidget widget;
        private final List<ClickableWidget> childrenList = new ArrayList<>();

        public SingleWidgetEntry(ClickableWidget widget) {
            this.widget = widget;
            if (widget != null) {
                widget.setWidth(310);
                childrenList.add(widget);
            }
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            int centerX = x + (entryWidth / 2);
            if (widget != null) {
                widget.setX(centerX - 155);
                widget.setY(y);
                widget.render(context, mouseX, mouseY, tickDelta);
            }
        }

        @Override
        public List<? extends Selectable> selectableChildren() {
            return childrenList;
        }

        @Override
        public List<? extends Element> children() {
            return childrenList;
        }
    }
}

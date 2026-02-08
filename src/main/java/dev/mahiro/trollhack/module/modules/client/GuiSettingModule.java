package dev.mahiro.trollhack.module.modules.client;

import dev.mahiro.trollhack.gui.clickgui.GuiTheme;
import dev.mahiro.trollhack.module.Category;
import dev.mahiro.trollhack.module.Module;
import dev.mahiro.trollhack.setting.*;

import java.awt.Color;

public final class GuiSettingModule extends Module {
    public static final GuiSettingModule INSTANCE = new GuiSettingModule();

    private final IntSetting scale = new IntSetting("Scale", 100, 50, 400, 5, 5, null, "", false);
    private final BoolSetting particle = new BoolSetting("Particle", false, null, "", false);
    private final FloatSetting backgroundBlur = new FloatSetting("Background Blur", 0.0f, 0.0f, 1.0f, 0.05f, 0.01f, null, "", false);
    private final BoolSetting windowOutline = new BoolSetting("Window Outline", true, null, "", false);
    private final BoolSetting titleBar = new BoolSetting("Title Bar", false, null, "", false);
    private final IntSetting windowBlurPass = new IntSetting("Window Blur Pass", 2, 0, 10, 1, 1, null, "", false);
    private final FloatSetting xMargin = new FloatSetting("X Margin", 4.0f, 0.0f, 10.0f, 0.5f, 0.1f, null, "", false);
    private final FloatSetting yMargin = new FloatSetting("Y Margin", 1.0f, 0.0f, 10.0f, 0.5f, 0.1f, null, "", false);
    private final FloatSetting darkness = new FloatSetting("Darkness", 0.25f, 0.0f, 1.0f, 0.05f, 0.01f, null, "", false);
    private final FloatSetting fadeInTime = new FloatSetting("Fade In Time", 0.4f, 0.0f, 1.0f, 0.05f, 0.01f, null, "", false);
    private final FloatSetting fadeOutTime = new FloatSetting("Fade Out Time", 0.4f, 0.0f, 1.0f, 0.05f, 0.01f, null, "", false);
    private final ColorSetting primaryColor = new ColorSetting("Primary Color", new Color(255, 140, 180, 220), true, null, "", false);
    private final ColorSetting backgroundColor = new ColorSetting("Background Color", new Color(40, 32, 36, 160), true, null, "", false);
    private final ColorSetting textColor = new ColorSetting("Text Color", new Color(255, 250, 253, 255), true, null, "", false);
    private final IntSetting hoverAlpha = new IntSetting("Hover Alpha", 32, 0, 255, 1, 1, null, "", false);

    private GuiSettingModule() {
        super("Gui Setting", "GUI", Category.CLIENT, true);
        setVisible(false);

        applyToTheme();
        scale.addListener(this::applyToTheme);
        backgroundBlur.addListener(this::applyToTheme);
        windowOutline.addListener(this::applyToTheme);
        titleBar.addListener(this::applyToTheme);
        xMargin.addListener(this::applyToTheme);
        yMargin.addListener(this::applyToTheme);
        darkness.addListener(this::applyToTheme);
        fadeInTime.addListener(this::applyToTheme);
        fadeOutTime.addListener(this::applyToTheme);
        primaryColor.addListener(this::applyToTheme);
        backgroundColor.addListener(this::applyToTheme);
        textColor.addListener(this::applyToTheme);
        hoverAlpha.addListener(this::applyToTheme);
    }

    public void applyToTheme() {
        GuiTheme.setScalePercent(scale.getValue());
        GuiTheme.PARTICLE = particle.getValue();
        GuiTheme.BACKGROUND_BLUR = backgroundBlur.getValue();
        GuiTheme.WINDOW_OUTLINE = windowOutline.getValue();
        GuiTheme.TITLE_BAR = titleBar.getValue();
        GuiTheme.WINDOW_BLUR_PASS = windowBlurPass.getValue();
        GuiTheme.WINDOW_X_MARGIN = xMargin.getValue();
        GuiTheme.WINDOW_Y_MARGIN = yMargin.getValue();
        GuiTheme.DARKNESS = darkness.getValue();
        GuiTheme.FADE_IN_TIME_SEC = fadeInTime.getValue();
        GuiTheme.FADE_OUT_TIME_SEC = fadeOutTime.getValue();
        GuiTheme.PRIMARY = primaryColor.getValue();
        GuiTheme.BACKGROUND = backgroundColor.getValue();
        GuiTheme.TEXT = textColor.getValue();
        GuiTheme.HOVER_ALPHA = hoverAlpha.getValue();
    }
}

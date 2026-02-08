package dev.mahiro.trollhack.gui.clickgui.window;

import dev.mahiro.trollhack.gui.clickgui.GuiTheme;
import dev.mahiro.trollhack.gui.clickgui.anim.AnimatedFloat;
import dev.mahiro.trollhack.gui.clickgui.anim.Easing;
import dev.mahiro.trollhack.nanovg.font.FontLoader;
import dev.mahiro.trollhack.nanovg.util.NanoVGHelper;
import dev.mahiro.trollhack.setting.ColorSetting;

import java.awt.*;

import static org.lwjgl.nanovg.NanoVG.NVG_ALIGN_LEFT;
import static org.lwjgl.nanovg.NanoVG.NVG_ALIGN_TOP;

public final class ColorPickerWindow {
    private final ModuleSettingWindow parent;
    private final ColorSetting setting;
    private final Color original;

    private float x;
    private float y;
    private float width;
    private float height;

    private boolean dragging;
    private float dragOffsetX;
    private float dragOffsetY;

    private DragTarget dragTarget = DragTarget.NONE;

    private int r;
    private int g;
    private int b;
    private int a;

    private final AnimatedFloat hueAnim;
    private final AnimatedFloat satAnim;
    private final AnimatedFloat brightAnim;
    private final AnimatedFloat rAnim;
    private final AnimatedFloat gAnim;
    private final AnimatedFloat bAnim;
    private final AnimatedFloat aAnim;

    public ColorPickerWindow(ModuleSettingWindow parent, ColorSetting setting) {
        this.parent = parent;
        this.setting = setting;
        this.original = setting.getColor();
        Color c = original;
        r = c.getRed();
        g = c.getGreen();
        b = c.getBlue();
        a = c.getAlpha();
        updateHsbFromRgb();
        hueAnim = new AnimatedFloat(Easing.OUT_QUART, 120.0f, setting.getHue());
        satAnim = new AnimatedFloat(Easing.OUT_QUART, 120.0f, setting.getSaturation());
        brightAnim = new AnimatedFloat(Easing.OUT_QUART, 120.0f, setting.getBrightness());
        rAnim = new AnimatedFloat(Easing.OUT_QUART, 120.0f, r);
        gAnim = new AnimatedFloat(Easing.OUT_QUART, 120.0f, g);
        bAnim = new AnimatedFloat(Easing.OUT_QUART, 120.0f, b);
        aAnim = new AnimatedFloat(Easing.OUT_QUART, 120.0f, a);
        updateLayout();
    }

    public ModuleSettingWindow getParent() {
        return parent;
    }

    public boolean contains(float mouseX, float mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    private float draggableHeight() {
        return NanoVGHelper.getFontHeight(FontLoader.bold(), 11.0f) + 6.0f;
    }

    private void updateLayout() {
        float dragHeight = draggableHeight();
        float rowH = NanoVGHelper.getFontHeight(FontLoader.regular(), 11.0f) + 3.0f;
        float gap = 2.0f;

        float y0 = dragHeight + 4.0f;
        float sliderBlockHeight = (rowH + gap) * 4.0f;
        float buttonBlockHeight = (rowH + gap) * 3.0f;
        height = y0 + sliderBlockHeight + buttonBlockHeight + 4.0f;

        float fieldHeight = height - dragHeight - 4.0f;
        width = fieldHeight + 156.0f;
    }

    public void render(float mouseX, float mouseY) {
        updateLayout();
        float dragHeight = draggableHeight();

        NanoVGHelper.drawShadow(x, y, width, height, 0.0f, new Color(0, 0, 0, 120), 10.0f, 0.0f, 0.0f);
        parent.getScreen().drawWindowBlur(x, y, width, height, GuiTheme.WINDOW_BLUR_PASS > 0 ? 1.0f : 0.0f);
        NanoVGHelper.drawRect(x, y, width, height, GuiTheme.BACKGROUND);
        if (GuiTheme.WINDOW_OUTLINE) {
            Color outline = new Color(GuiTheme.PRIMARY.getRed(), GuiTheme.PRIMARY.getGreen(), GuiTheme.PRIMARY.getBlue(), 255);
            NanoVGHelper.drawRectOutline(x, y, width, height, 1.0f, outline);
        }
        if (GuiTheme.TITLE_BAR) {
            NanoVGHelper.drawRect(x, y, width, dragHeight, GuiTheme.PRIMARY);
        }
        NanoVGHelper.drawString("Color Picker", x + 3.0f, y + 3.5f, FontLoader.bold(), 11.0f, NVG_ALIGN_LEFT | NVG_ALIGN_TOP, GuiTheme.TEXT);

        float fieldH = height - dragHeight - 4.0f;
        float fieldX0 = x + 4.0f;
        float fieldY0 = y + dragHeight;
        float fieldX1 = fieldX0 + fieldH;
        float fieldY1 = fieldY0 + fieldH;

        syncAnimations();

        float hue = hueAnim.get();
        float sat = satAnim.get();
        float bright = brightAnim.get();
        Color hueColor = Color.getHSBColor(hue, 1.0f, 1.0f);
        NanoVGHelper.drawGradientRect3(fieldX0, fieldY0, fieldH, fieldH, Color.WHITE, Color.BLACK, hueColor, Color.BLACK);
        NanoVGHelper.drawGradientRect3(fieldX0, fieldY0, fieldH, fieldH, Color.WHITE, new Color(255, 255, 255, 0), hueColor, new Color(hueColor.getRed(), hueColor.getGreen(), hueColor.getBlue(), 0));
        NanoVGHelper.drawGradientRect3(fieldX0, fieldY0, fieldH, fieldH, new Color(0, 0, 0, 0), Color.BLACK, new Color(0, 0, 0, 0), Color.BLACK);
        NanoVGHelper.drawRectOutline(fieldX0, fieldY0, fieldH, fieldH, 1.0f, new Color(0, 0, 0, 140));

        float circleX = fieldX0 + fieldH * sat;
        float circleY = fieldY0 + fieldH * (1.0f - bright);
        int brightnessValue = (int) ((1.0f - (1.0f - sat) * bright) * 255.0f);
        Color circleColor = new Color(brightnessValue, brightnessValue, brightnessValue);
        NanoVGHelper.drawCircleOutline(circleX, circleY, 4.0f, 1.5f, circleColor);

        float hueX0 = fieldX1 + 6.0f;
        float hueW = 8.0f;
        NanoVGHelper.drawHueBar(hueX0, fieldY0, hueW, fieldH);
        NanoVGHelper.drawRectOutline(hueX0, fieldY0, hueW, fieldH, 1.0f, new Color(0, 0, 0, 140));
        float hueLineY = fieldY0 + fieldH * hue;
        NanoVGHelper.drawRect(hueX0 - 2.0f, hueLineY - 1.0f, hueW + 4.0f, 2.0f, new Color(0, 0, 0, 200));
        NanoVGHelper.drawRect(hueX0 - 1.0f, hueLineY, hueW + 2.0f, 1.0f, new Color(255, 255, 255, 220));

        float rightX = x + width - 4.0f - 128.0f;
        float rowH = NanoVGHelper.getFontHeight(FontLoader.regular(), 11.0f) + 3.0f;
        float gap = 2.0f;
        float rowY = y + dragHeight + 4.0f;

        drawChannelSlider(mouseX, mouseY, rightX, rowY, 128.0f, rowH, "Red", r, rAnim.get() / 255.0f, DragTarget.SLIDER_R);
        rowY += rowH + gap;
        drawChannelSlider(mouseX, mouseY, rightX, rowY, 128.0f, rowH, "Green", g, gAnim.get() / 255.0f, DragTarget.SLIDER_G);
        rowY += rowH + gap;
        drawChannelSlider(mouseX, mouseY, rightX, rowY, 128.0f, rowH, "Blue", b, bAnim.get() / 255.0f, DragTarget.SLIDER_B);
        rowY += rowH + gap;
        if (setting.isAllowAlpha()) {
            drawChannelSlider(mouseX, mouseY, rightX, rowY, 128.0f, rowH, "Alpha", a, aAnim.get() / 255.0f, DragTarget.SLIDER_A);
            rowY += rowH + gap;
        }

        float previewY = rowY + gap;
        NanoVGHelper.drawRect(rightX, previewY, 35.0f, height - (previewY - y) - 4.0f, original);
        NanoVGHelper.drawRect(rightX + 39.0f, previewY, 35.0f, height - (previewY - y) - 4.0f, setting.getColor());
        NanoVGHelper.drawRectOutline(rightX, previewY, 35.0f, height - (previewY - y) - 4.0f, 1.0f, new Color(0, 0, 0, 140));
        NanoVGHelper.drawRectOutline(rightX + 39.0f, previewY, 35.0f, height - (previewY - y) - 4.0f, 1.0f, new Color(0, 0, 0, 140));

        float buttonW = 50.0f;
        float buttonX = x + width - 4.0f - buttonW;
        float buttonY = (y + dragHeight + 4.0f) + (rowH + gap) * 4.0f;
        drawButton(mouseX, mouseY, buttonX, buttonY, buttonW, rowH, "Okay");
        buttonY += rowH + gap;
        drawButton(mouseX, mouseY, buttonX, buttonY, buttonW, rowH, "Cancel");
        buttonY += rowH + gap;
        drawButton(mouseX, mouseY, buttonX, buttonY, buttonW, rowH, "Apply");
    }

    private void drawChannelSlider(float mouseX, float mouseY, float x, float y, float w, float h, String label, int value, float t, DragTarget target) {
        t = clamp01(t);
        NanoVGHelper.drawRect(x, y, w * t, h, GuiTheme.PRIMARY);
        boolean hovered = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
        if (hovered) {
            NanoVGHelper.drawRect(x, y, w, h, GuiTheme.getHoverOverlay());
        }
        NanoVGHelper.drawString(label, x + 2.0f, y + 1.0f, FontLoader.regular(), 11.0f, NVG_ALIGN_LEFT | NVG_ALIGN_TOP, GuiTheme.TEXT);
        NanoVGHelper.drawString(String.valueOf(value), x + w - 2.0f, y + 1.0f, FontLoader.regular(), 11.0f, org.lwjgl.nanovg.NanoVG.NVG_ALIGN_RIGHT | NVG_ALIGN_TOP, GuiTheme.TEXT);
    }

    private void syncAnimations() {
        if (dragTarget != DragTarget.FIELD && dragTarget != DragTarget.HUE) {
            float hue = setting.getHue();
            float sat = setting.getSaturation();
            float bright = setting.getBrightness();
            if (Math.abs(hue - hueAnim.get()) > 0.0001f) hueAnim.update(hue);
            if (Math.abs(sat - satAnim.get()) > 0.0001f) satAnim.update(sat);
            if (Math.abs(bright - brightAnim.get()) > 0.0001f) brightAnim.update(bright);
        }

        if (dragTarget != DragTarget.SLIDER_R) {
            if (Math.abs(r - rAnim.get()) > 0.0001f) rAnim.update(r);
        }
        if (dragTarget != DragTarget.SLIDER_G) {
            if (Math.abs(g - gAnim.get()) > 0.0001f) gAnim.update(g);
        }
        if (dragTarget != DragTarget.SLIDER_B) {
            if (Math.abs(b - bAnim.get()) > 0.0001f) bAnim.update(b);
        }
        if (dragTarget != DragTarget.SLIDER_A) {
            if (Math.abs(a - aAnim.get()) > 0.0001f) aAnim.update(a);
        }
    }

    private void drawButton(float mouseX, float mouseY, float x, float y, float w, float h, String label) {
        boolean hovered = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
        if (hovered) {
            NanoVGHelper.drawRect(x, y, w, h, GuiTheme.getHoverOverlay());
        }
        NanoVGHelper.drawRectOutline(x, y, w, h, 1.0f, new Color(0, 0, 0, 120));
        NanoVGHelper.drawString(label, x + 2.0f, y + 1.0f, FontLoader.regular(), 11.0f, NVG_ALIGN_LEFT | NVG_ALIGN_TOP, GuiTheme.TEXT);
    }

    public void mouseClicked(float mouseX, float mouseY, int button) {
        float localX = mouseX - x;
        float localY = mouseY - y;

        if (localY <= draggableHeight()) {
            if (button == 0) {
                dragging = true;
                dragOffsetX = localX;
                dragOffsetY = localY;
            }
            return;
        }

        DragTarget target = hitTest(mouseX, mouseY);
        dragTarget = target;
        if (target == DragTarget.FIELD || target == DragTarget.HUE || target.isSlider()) {
            updateByMouse(mouseX, mouseY);
            return;
        }

        if (target == DragTarget.OKAY) {
            apply();
            parent.getScreen().closeFloatingWindow(this);
            return;
        }
        if (target == DragTarget.CANCEL) {
            setting.setValue(original);
            parent.getScreen().closeFloatingWindow(this);
            return;
        }
        if (target == DragTarget.APPLY) {
            apply();
        }
    }

    public void mouseReleased(float mouseX, float mouseY, int button) {
        if (button == 0) dragging = false;
        if (button == 0) dragTarget = DragTarget.NONE;
    }

    public void mouseDragged(float mouseX, float mouseY, int button) {
        if (button != 0) return;
        if (dragging) {
            float trollWidth = parent.getScreen().getTrollWidth();
            float trollHeight = parent.getScreen().getTrollHeight();
            x = clamp(mouseX - dragOffsetX, 0.0f, trollWidth - width - 1.0f);
            y = clamp(mouseY - dragOffsetY, 0.0f, trollHeight - height - 1.0f);
            return;
        }

        if (dragTarget != DragTarget.NONE) {
            updateByMouse(mouseX, mouseY);
        }
    }

    private void updateByMouse(float mouseX, float mouseY) {
        float dragHeight = draggableHeight();
        float fieldH = height - dragHeight - 4.0f;
        float fieldX0 = x + 4.0f;
        float fieldY0 = y + dragHeight;
        float hueX0 = fieldX0 + fieldH + 6.0f;
        float hueW = 8.0f;

        if (dragTarget == DragTarget.FIELD) {
            float sx = (mouseX - fieldX0) / fieldH;
            float sy = (mouseY - fieldY0) / fieldH;
            setting.setHsb(setting.getHue(), clamp01(sx), clamp01(1.0f - sy));
            updateRgbFromHsb();
            satAnim.forceUpdate(setting.getSaturation());
            brightAnim.forceUpdate(setting.getBrightness());
            rAnim.forceUpdate(r);
            gAnim.forceUpdate(g);
            bAnim.forceUpdate(b);
            aAnim.forceUpdate(a);
            return;
        }
        if (dragTarget == DragTarget.HUE) {
            float hy = (mouseY - fieldY0) / fieldH;
            setting.setHsb(clamp01(hy), setting.getSaturation(), setting.getBrightness());
            updateRgbFromHsb();
            hueAnim.forceUpdate(setting.getHue());
            rAnim.forceUpdate(r);
            gAnim.forceUpdate(g);
            bAnim.forceUpdate(b);
            aAnim.forceUpdate(a);
            return;
        }
        if (dragTarget == DragTarget.SLIDER_R) {
            r = sliderValue(mouseX, sliderX(), 128.0f);
            updateHsbFromRgb();
            applyPreview();
            hueAnim.forceUpdate(setting.getHue());
            satAnim.forceUpdate(setting.getSaturation());
            brightAnim.forceUpdate(setting.getBrightness());
            rAnim.forceUpdate(r);
        } else if (dragTarget == DragTarget.SLIDER_G) {
            g = sliderValue(mouseX, sliderX(), 128.0f);
            updateHsbFromRgb();
            applyPreview();
            hueAnim.forceUpdate(setting.getHue());
            satAnim.forceUpdate(setting.getSaturation());
            brightAnim.forceUpdate(setting.getBrightness());
            gAnim.forceUpdate(g);
        } else if (dragTarget == DragTarget.SLIDER_B) {
            b = sliderValue(mouseX, sliderX(), 128.0f);
            updateHsbFromRgb();
            applyPreview();
            hueAnim.forceUpdate(setting.getHue());
            satAnim.forceUpdate(setting.getSaturation());
            brightAnim.forceUpdate(setting.getBrightness());
            bAnim.forceUpdate(b);
        } else if (dragTarget == DragTarget.SLIDER_A) {
            a = sliderValue(mouseX, sliderX(), 128.0f);
            setting.setAlpha(a);
            applyPreview();
            aAnim.forceUpdate(a);
        }
    }

    private float sliderX() {
        return x + width - 4.0f - 128.0f;
    }

    private static int sliderValue(float mouseX, float x, float w) {
        float t = (mouseX - x) / w;
        if (t < 0.0f) t = 0.0f;
        if (t > 1.0f) t = 1.0f;
        return (int) Math.round(t * 255.0f);
    }

    private DragTarget hitTest(float mouseX, float mouseY) {
        float dragHeight = draggableHeight();
        float fieldH = height - dragHeight - 4.0f;
        float fieldX0 = x + 4.0f;
        float fieldY0 = y + dragHeight;
        float fieldX1 = fieldX0 + fieldH;
        float fieldY1 = fieldY0 + fieldH;
        float hueX0 = fieldX1 + 6.0f;
        float hueX1 = hueX0 + 8.0f;

        if (mouseX >= fieldX0 && mouseX <= fieldX1 && mouseY >= fieldY0 && mouseY <= fieldY1) return DragTarget.FIELD;
        if (mouseX >= hueX0 && mouseX <= hueX1 && mouseY >= fieldY0 && mouseY <= fieldY1) return DragTarget.HUE;

        float rightX = sliderX();
        float rowH = NanoVGHelper.getFontHeight(FontLoader.regular(), 11.0f) + 3.0f;
        float gap = 2.0f;
        float rowY = y + dragHeight + 4.0f;

        if (inRect(mouseX, mouseY, rightX, rowY, 128.0f, rowH)) return DragTarget.SLIDER_R;
        rowY += rowH + gap;
        if (inRect(mouseX, mouseY, rightX, rowY, 128.0f, rowH)) return DragTarget.SLIDER_G;
        rowY += rowH + gap;
        if (inRect(mouseX, mouseY, rightX, rowY, 128.0f, rowH)) return DragTarget.SLIDER_B;
        rowY += rowH + gap;
        if (setting.isAllowAlpha() && inRect(mouseX, mouseY, rightX, rowY, 128.0f, rowH)) return DragTarget.SLIDER_A;

        float buttonW = 50.0f;
        float buttonX = x + width - 4.0f - buttonW;
        float buttonY = (y + dragHeight + 4.0f) + (rowH + gap) * 4.0f;
        if (inRect(mouseX, mouseY, buttonX, buttonY, buttonW, rowH)) return DragTarget.OKAY;
        buttonY += rowH + gap;
        if (inRect(mouseX, mouseY, buttonX, buttonY, buttonW, rowH)) return DragTarget.CANCEL;
        buttonY += rowH + gap;
        if (inRect(mouseX, mouseY, buttonX, buttonY, buttonW, rowH)) return DragTarget.APPLY;

        return DragTarget.NONE;
    }

    private void apply() {
        setting.setValue(new Color(r, g, b, setting.isAllowAlpha() ? a : 255));
    }

    private void applyPreview() {
        setting.setValue(new Color(r, g, b, setting.isAllowAlpha() ? a : 255));
    }

    private void updateRgbFromHsb() {
        Color c = setting.getColor();
        r = c.getRed();
        g = c.getGreen();
        b = c.getBlue();
        a = c.getAlpha();
    }

    private void updateHsbFromRgb() {
        float[] hsb = Color.RGBtoHSB(r, g, b, null);
        setting.setHsb(hsb[0], hsb[1], hsb[2]);
        if (setting.isAllowAlpha()) setting.setAlpha(a);
    }

    private static boolean inRect(float mouseX, float mouseY, float x, float y, float w, float h) {
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
    }

    private static float clamp(float v, float min, float max) {
        if (v < min) return min;
        if (v > max) return max;
        return v;
    }

    private static float clamp01(float v) {
        return clamp(v, 0.0f, 1.0f);
    }

    private enum DragTarget {
        NONE,
        FIELD,
        HUE,
        SLIDER_R,
        SLIDER_G,
        SLIDER_B,
        SLIDER_A,
        OKAY,
        CANCEL,
        APPLY;

        private boolean isSlider() {
            return this == SLIDER_R || this == SLIDER_G || this == SLIDER_B || this == SLIDER_A;
        }
    }
}

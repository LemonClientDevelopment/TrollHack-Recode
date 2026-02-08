package dev.mahiro.trollhack.gui.clickgui.window;

import dev.mahiro.trollhack.gui.clickgui.GuiTheme;
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
        return NanoVGHelper.getFontHeight(FontLoader.bold(), 12.0f) + 6.0f;
    }

    private void updateLayout() {
        float dragHeight = draggableHeight();
        float rowH = 14.0f;
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
        NanoVGHelper.drawRect(x, y, width, height, GuiTheme.BACKGROUND);
        if (GuiTheme.WINDOW_OUTLINE) {
            Color outline = new Color(GuiTheme.PRIMARY.getRed(), GuiTheme.PRIMARY.getGreen(), GuiTheme.PRIMARY.getBlue(), 255);
            NanoVGHelper.drawRectOutline(x, y, width, height, 1.0f, outline);
        }
        if (GuiTheme.TITLE_BAR) {
            NanoVGHelper.drawRect(x, y, width, dragHeight, GuiTheme.PRIMARY);
        }
        NanoVGHelper.drawString("Color Picker", x + 3.0f, y + 3.0f, FontLoader.bold(), 12.0f, NVG_ALIGN_LEFT | NVG_ALIGN_TOP, GuiTheme.TEXT);

        float fieldH = height - dragHeight - 4.0f;
        float fieldX0 = x + 4.0f;
        float fieldY0 = y + dragHeight;
        float fieldX1 = fieldX0 + fieldH;
        float fieldY1 = fieldY0 + fieldH;

        Color hueColor = Color.getHSBColor(setting.getHue(), 1.0f, 1.0f);
        NanoVGHelper.drawGradientRect3(fieldX0, fieldY0, fieldH, fieldH, Color.WHITE, Color.BLACK, hueColor, Color.BLACK);
        NanoVGHelper.drawGradientRect3(fieldX0, fieldY0, fieldH, fieldH, Color.WHITE, new Color(255, 255, 255, 0), hueColor, new Color(hueColor.getRed(), hueColor.getGreen(), hueColor.getBlue(), 0));
        NanoVGHelper.drawGradientRect3(fieldX0, fieldY0, fieldH, fieldH, new Color(0, 0, 0, 0), Color.BLACK, new Color(0, 0, 0, 0), Color.BLACK);

        float circleX = fieldX0 + fieldH * setting.getSaturation();
        float circleY = fieldY0 + fieldH * (1.0f - setting.getBrightness());
        int brightness = (int) ((1.0f - (1.0f - setting.getSaturation()) * setting.getBrightness()) * 255.0f);
        Color circleColor = new Color(brightness, brightness, brightness);
        NanoVGHelper.drawCircleOutline(circleX, circleY, 4.0f, 1.5f, circleColor);

        float hueX0 = fieldX1 + 6.0f;
        float hueW = 8.0f;
        NanoVGHelper.drawHueBar(hueX0, fieldY0, hueW, fieldH);
        float hueLineY = fieldY0 + fieldH * setting.getHue();
        NanoVGHelper.drawRect(hueX0 - 2.0f, hueLineY - 1.0f, hueW + 4.0f, 2.0f, new Color(0, 0, 0, 200));
        NanoVGHelper.drawRect(hueX0 - 1.0f, hueLineY, hueW + 2.0f, 1.0f, new Color(255, 255, 255, 220));

        float rightX = x + width - 4.0f - 128.0f;
        float rowH = 14.0f;
        float gap = 2.0f;
        float rowY = y + dragHeight + 4.0f;

        drawChannelSlider(mouseX, mouseY, rightX, rowY, 128.0f, rowH, "Red", r, 255, DragTarget.SLIDER_R);
        rowY += rowH + gap;
        drawChannelSlider(mouseX, mouseY, rightX, rowY, 128.0f, rowH, "Green", g, 255, DragTarget.SLIDER_G);
        rowY += rowH + gap;
        drawChannelSlider(mouseX, mouseY, rightX, rowY, 128.0f, rowH, "Blue", b, 255, DragTarget.SLIDER_B);
        rowY += rowH + gap;
        if (setting.isAllowAlpha()) {
            drawChannelSlider(mouseX, mouseY, rightX, rowY, 128.0f, rowH, "Alpha", a, 255, DragTarget.SLIDER_A);
            rowY += rowH + gap;
        }

        float previewY = rowY + gap;
        NanoVGHelper.drawRect(rightX, previewY, 35.0f, height - (previewY - y) - 4.0f, original);
        NanoVGHelper.drawRect(rightX + 39.0f, previewY, 35.0f, height - (previewY - y) - 4.0f, setting.getColor());

        float buttonW = 50.0f;
        float buttonX = x + width - 4.0f - buttonW;
        float buttonY = (y + dragHeight + 4.0f) + (rowH + gap) * 4.0f;
        drawButton(mouseX, mouseY, buttonX, buttonY, buttonW, rowH, "Okay");
        buttonY += rowH + gap;
        drawButton(mouseX, mouseY, buttonX, buttonY, buttonW, rowH, "Cancel");
        buttonY += rowH + gap;
        drawButton(mouseX, mouseY, buttonX, buttonY, buttonW, rowH, "Apply");
    }

    private void drawChannelSlider(float mouseX, float mouseY, float x, float y, float w, float h, String label, int value, int max, DragTarget target) {
        float t = max == 0 ? 0.0f : (float) value / (float) max;
        NanoVGHelper.drawRect(x, y, w * t, h, GuiTheme.PRIMARY);
        boolean hovered = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
        if (hovered) {
            NanoVGHelper.drawRect(x, y, w, h, new Color(255, 255, 255, GuiTheme.HOVER_ALPHA));
        }
        NanoVGHelper.drawString(label, x + 2.0f, y + 2.0f, FontLoader.medium(), 11.0f, NVG_ALIGN_LEFT | NVG_ALIGN_TOP, GuiTheme.TEXT);
        NanoVGHelper.drawString(String.valueOf(value), x + w - 2.0f, y + 2.0f, FontLoader.medium(), 11.0f, org.lwjgl.nanovg.NanoVG.NVG_ALIGN_RIGHT | NVG_ALIGN_TOP, GuiTheme.TEXT);
    }

    private void drawButton(float mouseX, float mouseY, float x, float y, float w, float h, String label) {
        boolean hovered = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
        if (hovered) {
            NanoVGHelper.drawRect(x, y, w, h, new Color(255, 255, 255, GuiTheme.HOVER_ALPHA));
        }
        NanoVGHelper.drawRectOutline(x, y, w, h, 1.0f, new Color(0, 0, 0, 120));
        NanoVGHelper.drawString(label, x + 2.0f, y + 2.0f, FontLoader.medium(), 11.0f, NVG_ALIGN_LEFT | NVG_ALIGN_TOP, GuiTheme.TEXT);
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
            return;
        }
        if (dragTarget == DragTarget.HUE) {
            float hy = (mouseY - fieldY0) / fieldH;
            setting.setHsb(clamp01(hy), setting.getSaturation(), setting.getBrightness());
            updateRgbFromHsb();
            return;
        }
        if (dragTarget == DragTarget.SLIDER_R) {
            r = sliderValue(mouseX, sliderX(), 128.0f);
            updateHsbFromRgb();
            applyPreview();
        } else if (dragTarget == DragTarget.SLIDER_G) {
            g = sliderValue(mouseX, sliderX(), 128.0f);
            updateHsbFromRgb();
            applyPreview();
        } else if (dragTarget == DragTarget.SLIDER_B) {
            b = sliderValue(mouseX, sliderX(), 128.0f);
            updateHsbFromRgb();
            applyPreview();
        } else if (dragTarget == DragTarget.SLIDER_A) {
            a = sliderValue(mouseX, sliderX(), 128.0f);
            setting.setAlpha(a);
            applyPreview();
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
        float rowH = 14.0f;
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

package dev.mahiro.trollhack.gui.clickgui.window;

import dev.mahiro.trollhack.gui.clickgui.GuiTheme;
import dev.mahiro.trollhack.module.BindMode;
import dev.mahiro.trollhack.module.Module;
import dev.mahiro.trollhack.nanovg.font.FontLoader;
import dev.mahiro.trollhack.nanovg.util.NanoVGHelper;
import dev.mahiro.trollhack.setting.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.util.List;

import static org.lwjgl.nanovg.NanoVG.*;

public final class ModuleSettingWindow {
    private final Module module;

    private float x;
    private float y;
    private float width;
    private float height;
    private float scroll;

    private boolean dragging;
    private float dragOffsetX;
    private float dragOffsetY;

    private boolean sliderDragging;
    private DragTarget dragTarget = DragTarget.NONE;
    private Setting<?> dragSetting;

    private boolean closeRequested;
    private boolean bindListening;

    private Setting<?> editingSetting;
    private String editingBuffer = "";

    public ModuleSettingWindow(Module module) {
        this.module = module;
        this.width = 180.0f;
        this.height = 120.0f;
    }

    public Module getModule() {
        return module;
    }

    public boolean isCloseRequested() {
        return closeRequested;
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

    public boolean contains(float mouseX, float mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    private float draggableHeight() {
        return NanoVGHelper.getFontHeight(FontLoader.bold(), 12.0f) + 6.0f;
    }

    public boolean isBindListening() {
        return bindListening;
    }

    public boolean handlesKeyboardInput() {
        return bindListening || editingSetting != null;
    }

    public boolean onKey(int keyCode) {
        if (bindListening) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                bindListening = false;
                return true;
            }

            if (keyCode == GLFW.GLFW_KEY_BACKSPACE || keyCode == GLFW.GLFW_KEY_DELETE) {
                module.setBindKey(-1);
                bindListening = false;
                return true;
            }

            module.setBindKey(keyCode);
            bindListening = false;
            return true;
        }

        if (editingSetting != null) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                editingSetting = null;
                editingBuffer = "";
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                applyEditingBuffer();
                editingSetting = null;
                editingBuffer = "";
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE && !editingBuffer.isEmpty()) {
                editingBuffer = editingBuffer.substring(0, editingBuffer.length() - 1);
                return true;
            }
        }

        if (isCtrlDown()) {
            if (keyCode == GLFW.GLFW_KEY_C) {
                copySettingsToClipboard();
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_V) {
                pasteSettingsFromClipboard();
                return true;
            }
        }

        return false;
    }

    public boolean onChar(int codepoint) {
        if (editingSetting == null) return false;
        if (codepoint <= 0) return false;
        char c = (char) codepoint;

        if (editingSetting instanceof StringSetting) {
            editingBuffer += c;
            return true;
        }

        if (editingSetting instanceof NumberSetting<?>) {
            if ((c >= '0' && c <= '9') || c == '.' || c == '-' || c == '+') {
                editingBuffer += c;
            }
            return true;
        }

        return false;
    }

    private void applyEditingBuffer() {
        if (editingSetting == null) return;
        if (editingSetting instanceof StringSetting stringSetting) {
            stringSetting.set(editingBuffer);
            return;
        }
        if (editingSetting instanceof NumberSetting<?> numberSetting) {
            numberSetting.setValueFromString(editingBuffer);
        }
    }

    public void render(float mouseX, float mouseY) {
        float contentHeight = computeContentHeight();
        height = contentHeight;

        NanoVGHelper.drawShadow(x, y, width, height, 0.0f, new Color(0, 0, 0, 120), 10.0f, 0.0f, 0.0f);
        NanoVGHelper.drawRect(x, y, width, height, GuiTheme.BACKGROUND);
        if (GuiTheme.WINDOW_OUTLINE) {
            Color outline = new Color(GuiTheme.PRIMARY.getRed(), GuiTheme.PRIMARY.getGreen(), GuiTheme.PRIMARY.getBlue(), 255);
            NanoVGHelper.drawRectOutline(x, y, width, height, 1.0f, outline);
        }

        if (GuiTheme.TITLE_BAR) {
            NanoVGHelper.drawRect(x, y, width, draggableHeight(), GuiTheme.PRIMARY);
        }
        int titleFont = FontLoader.bold();
        NanoVGHelper.drawString(module.getName(), x + 3.0f, y + 3.0f, titleFont, 12.0f, NVG_ALIGN_LEFT | NVG_ALIGN_TOP, GuiTheme.TEXT);

        float rowX = x + 4.0f;
        float rowW = width - 8.0f;
        float rowH = 14.0f;
        float rowY = y + draggableHeight() + 4.0f - scroll;
        float rowGap = 2.0f;

        float scissorX = x;
        float scissorY = y + draggableHeight();
        float scissorW = width;
        float scissorH = height - draggableHeight();
        NanoVGHelper.save();
        NanoVGHelper.scissor(scissorX, scissorY, scissorW, scissorH);

        drawRow(mouseX, mouseY, rowX, rowY, rowW, rowH, "Enabled", module.isEnabled() ? "On" : "Off", false);
        rowY += rowH + rowGap;
        drawRow(mouseX, mouseY, rowX, rowY, rowW, rowH, "Visible", module.isVisible() ? "On" : "Off", false);
        rowY += rowH + rowGap;
        drawRow(mouseX, mouseY, rowX, rowY, rowW, rowH, "Bind", bindListening ? "..." : keyName(module.getBindKey()), false);
        rowY += rowH + rowGap;
        drawRow(mouseX, mouseY, rowX, rowY, rowW, rowH, "Mode", module.getBindMode().name(), false);
        rowY += rowH + rowGap;

        List<Setting<?>> settings = module.getSettings();
        for (Setting<?> setting : settings) {
            if (!setting.isVisible()) continue;
            rowY = renderSetting(setting, mouseX, mouseY, rowX, rowY, rowW, rowH, rowGap);
        }

        NanoVGHelper.restore();
    }

    private float renderSetting(Setting<?> setting, float mouseX, float mouseY, float rowX, float rowY, float rowW, float rowH, float rowGap) {
        if (setting instanceof BoolSetting boolSetting) {
            String value = boolSetting.get() ? "On" : "Off";
            boolean hovered = drawRow(mouseX, mouseY, rowX, rowY, rowW, rowH, setting.getName(), value, editingSetting == setting);
            if (hovered && editingSetting == setting) {
                drawEditingOverlay(rowX, rowY, rowW, rowH);
            }
            return rowY + rowH + rowGap;
        }

        if (setting instanceof EnumSetting<?> enumSetting) {
            String value = enumSetting.get().name();
            drawRow(mouseX, mouseY, rowX, rowY, rowW, rowH, setting.getName(), value, false);
            return rowY + rowH + rowGap;
        }

        if (setting instanceof StringSetting stringSetting) {
            String value = editingSetting == setting ? editingBuffer : stringSetting.get();
            drawRow(mouseX, mouseY, rowX, rowY, rowW, rowH, setting.getName(), value, editingSetting == setting);
            return rowY + rowH + rowGap;
        }

        if (setting instanceof NumberSetting<?> numberSetting) {
            double min = numberSetting.getMin();
            double max = numberSetting.getMax();
            double v = numberSetting.getValue().doubleValue();
            double t = (max - min) <= 0.0 ? 0.0 : (v - min) / (max - min);
            t = Math.max(0.0, Math.min(1.0, t));
            float barW = rowW * (float) t;

            NanoVGHelper.drawRect(rowX, rowY, barW, rowH, GuiTheme.PRIMARY);
            String value = editingSetting == setting ? editingBuffer : stripTrailingZeros(v);
            drawRow(mouseX, mouseY, rowX, rowY, rowW, rowH, setting.getName(), value, editingSetting == setting);
            return rowY + rowH + rowGap;
        }

        if (setting instanceof ColorSetting colorSetting) {
            Color color = colorSetting.getColor();
            String value = "RGBA";
            drawRow(mouseX, mouseY, rowX, rowY, rowW, rowH, setting.getName(), value, false);
            NanoVGHelper.drawRect(rowX + rowW - 20.0f, rowY + 2.0f, 18.0f, rowH - 4.0f, color);
            rowY += rowH + rowGap;

            if (!colorSetting.isExpanded()) {
                return rowY;
            }

            rowY = renderColorSlider("Hue", colorSetting.getHue(), 0.0f, 1.0f, mouseX, mouseY, rowX, rowY, rowW, rowH) + rowGap;
            rowY = renderColorSlider("Sat", colorSetting.getSaturation(), 0.0f, 1.0f, mouseX, mouseY, rowX, rowY, rowW, rowH) + rowGap;
            rowY = renderColorSlider("Bri", colorSetting.getBrightness(), 0.0f, 1.0f, mouseX, mouseY, rowX, rowY, rowW, rowH) + rowGap;
            if (colorSetting.isAllowAlpha()) {
                rowY = renderColorSlider("Alpha", colorSetting.getAlpha(), 0.0f, 255.0f, mouseX, mouseY, rowX, rowY, rowW, rowH) + rowGap;
            }
            drawRow(mouseX, mouseY, rowX, rowY, rowW, rowH, "Rainbow", colorSetting.isRainbow() ? "On" : "Off", false);
            return rowY + rowH + rowGap;
        }

        if (setting instanceof MultiBoolSetting multi) {
            drawRow(mouseX, mouseY, rowX, rowY, rowW, rowH, setting.getName(), multi.isExpanded() ? "-" : "+", false);
            rowY += rowH + rowGap;
            if (!multi.isExpanded()) return rowY;
            for (BoolSetting option : multi.getOptions()) {
                drawRow(mouseX, mouseY, rowX, rowY, rowW, rowH, option.getName(), option.get() ? "On" : "Off", false);
                rowY += rowH + rowGap;
            }
            return rowY;
        }

        drawRow(mouseX, mouseY, rowX, rowY, rowW, rowH, setting.getName(), "", false);
        return rowY + rowH + rowGap;
    }

    private float renderColorSlider(String label, float value, float min, float max, float mouseX, float mouseY, float x, float y, float w, float h) {
        float t = (max - min) <= 0.0f ? 0.0f : (value - min) / (max - min);
        t = Math.max(0.0f, Math.min(1.0f, t));
        NanoVGHelper.drawRect(x, y, w * t, h, GuiTheme.PRIMARY);
        drawRow(mouseX, mouseY, x, y, w, h, label, stripTrailingZeros(value), false);
        return y + h;
    }

    private boolean drawRow(float mouseX, float mouseY, float x, float y, float w, float h, String label, String value, boolean focused) {
        boolean hovered = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
        if (hovered) {
            NanoVGHelper.drawRect(x, y, w, h, new Color(255, 255, 255, GuiTheme.HOVER_ALPHA));
        }
        int font = FontLoader.medium();
        NanoVGHelper.drawString(label, x + 2.0f, y + 2.0f, font, 11.0f, NVG_ALIGN_LEFT | NVG_ALIGN_TOP, GuiTheme.TEXT);
        NanoVGHelper.drawString(value, x + w - 2.0f, y + 2.0f, font, 11.0f, NVG_ALIGN_RIGHT | NVG_ALIGN_TOP, GuiTheme.TEXT);
        if (focused) {
            NanoVGHelper.drawRectOutline(x, y, w, h, 1.0f, GuiTheme.PRIMARY);
        }
        return hovered;
    }

    private void drawEditingOverlay(float x, float y, float w, float h) {
        NanoVGHelper.drawRectOutline(x, y, w, h, 1.0f, GuiTheme.PRIMARY);
    }

    private static String keyName(int keyCode) {
        if (keyCode <= 0) return "None";
        String name = GLFW.glfwGetKeyName(keyCode, 0);
        if (name != null && !name.isBlank()) return name.toUpperCase();
        return "Key " + keyCode;
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

        float rowX = x + 4.0f;
        float rowW = width - 8.0f;
        float rowH = 14.0f;
        float rowY = y + draggableHeight() + 4.0f - scroll;
        float rowGap = 2.0f;

        editingSetting = null;
        editingBuffer = "";

        if (inRect(mouseX, mouseY, rowX, rowY, rowW, rowH)) {
            if (button == 0) module.toggle();
            if (button == 1) module.setEnabled(false);
            return;
        }
        rowY += rowH + rowGap;
        if (inRect(mouseX, mouseY, rowX, rowY, rowW, rowH)) {
            if (button == 0) module.setVisible(!module.isVisible());
            return;
        }
        rowY += rowH + rowGap;
        if (inRect(mouseX, mouseY, rowX, rowY, rowW, rowH)) {
            if (button == 0) bindListening = !bindListening;
            if (button == 1) module.setBindKey(-1);
            return;
        }
        rowY += rowH + rowGap;
        if (inRect(mouseX, mouseY, rowX, rowY, rowW, rowH)) {
            if (button == 0) module.setBindMode(nextBindMode(module.getBindMode()));
            return;
        }
        rowY += rowH + rowGap;

        for (Setting<?> setting : module.getSettings()) {
            if (!setting.isVisible()) continue;

            if (setting instanceof BoolSetting boolSetting) {
                if (inRect(mouseX, mouseY, rowX, rowY, rowW, rowH)) {
                    if (button == 0) boolSetting.set(!boolSetting.get());
                    if (button == 1) boolSetting.resetValue();
                    return;
                }
                rowY += rowH + rowGap;
                continue;
            }

            if (setting instanceof EnumSetting<?> enumSetting) {
                if (inRect(mouseX, mouseY, rowX, rowY, rowW, rowH)) {
                    if (button == 0) enumSetting.nextValue();
                    if (button == 1) enumSetting.previousValue();
                    return;
                }
                rowY += rowH + rowGap;
                continue;
            }

            if (setting instanceof StringSetting stringSetting) {
                if (inRect(mouseX, mouseY, rowX, rowY, rowW, rowH)) {
                    if (button == 0) {
                        editingSetting = stringSetting;
                        editingBuffer = "";
                    }
                    if (button == 1) stringSetting.resetValue();
                    return;
                }
                rowY += rowH + rowGap;
                continue;
            }

            if (setting instanceof NumberSetting<?> numberSetting) {
                if (inRect(mouseX, mouseY, rowX, rowY, rowW, rowH)) {
                    if (button == 0) {
                        sliderDragging = true;
                        dragTarget = DragTarget.NUMBER;
                        dragSetting = numberSetting;
                        updateNumberSettingFromMouse(numberSetting, mouseX, rowX, rowW);
                    } else if (button == 1) {
                        numberSetting.resetValue();
                    } else if (button == 2) {
                        editingSetting = numberSetting;
                        editingBuffer = "";
                    }
                    return;
                }
                rowY += rowH + rowGap;
                continue;
            }

            if (setting instanceof ColorSetting colorSetting) {
                if (inRect(mouseX, mouseY, rowX, rowY, rowW, rowH)) {
                    if (button == 0) colorSetting.setExpanded(!colorSetting.isExpanded());
                    if (button == 1) colorSetting.resetValue();
                    return;
                }
                rowY += rowH + rowGap;

                if (colorSetting.isExpanded()) {
                    if (inRect(mouseX, mouseY, rowX, rowY, rowW, rowH)) {
                        if (button == 0) beginColorDrag(colorSetting, DragTarget.COLOR_HUE, mouseX, rowX, rowW);
                        return;
                    }
                    rowY += rowH + rowGap;
                    if (inRect(mouseX, mouseY, rowX, rowY, rowW, rowH)) {
                        if (button == 0) beginColorDrag(colorSetting, DragTarget.COLOR_SAT, mouseX, rowX, rowW);
                        return;
                    }
                    rowY += rowH + rowGap;
                    if (inRect(mouseX, mouseY, rowX, rowY, rowW, rowH)) {
                        if (button == 0) beginColorDrag(colorSetting, DragTarget.COLOR_BRI, mouseX, rowX, rowW);
                        return;
                    }
                    rowY += rowH + rowGap;
                    if (colorSetting.isAllowAlpha()) {
                        if (inRect(mouseX, mouseY, rowX, rowY, rowW, rowH)) {
                            if (button == 0) beginColorDrag(colorSetting, DragTarget.COLOR_ALPHA, mouseX, rowX, rowW);
                            return;
                        }
                        rowY += rowH + rowGap;
                    }
                    if (inRect(mouseX, mouseY, rowX, rowY, rowW, rowH)) {
                        if (button == 0) colorSetting.setRainbow(!colorSetting.isRainbow());
                        return;
                    }
                    rowY += rowH + rowGap;
                }
                continue;
            }

            if (setting instanceof MultiBoolSetting multi) {
                if (inRect(mouseX, mouseY, rowX, rowY, rowW, rowH)) {
                    if (button == 0) multi.setExpanded(!multi.isExpanded());
                    if (button == 1) multi.resetValue();
                    return;
                }
                rowY += rowH + rowGap;
                if (multi.isExpanded()) {
                    for (BoolSetting option : multi.getOptions()) {
                        if (inRect(mouseX, mouseY, rowX, rowY, rowW, rowH)) {
                            if (button == 0) option.set(!option.get());
                            if (button == 1) option.resetValue();
                            return;
                        }
                        rowY += rowH + rowGap;
                    }
                }
            }
        }
    }

    public void mouseReleased(float mouseX, float mouseY, int button) {
        if (button == 0) dragging = false;
        if (button == 0) {
            sliderDragging = false;
            dragTarget = DragTarget.NONE;
            dragSetting = null;
        }
    }

    public void mouseDragged(float mouseX, float mouseY, int button) {
        if (button != 0) return;
        if (dragging) {
            x = mouseX - dragOffsetX;
            y = mouseY - dragOffsetY;
            return;
        }

        if (sliderDragging && dragSetting != null) {
            if (dragSetting instanceof NumberSetting<?> numberSetting && dragTarget == DragTarget.NUMBER) {
                float rowX = x + 4.0f;
                float rowW = width - 8.0f;
                updateNumberSettingFromMouse(numberSetting, mouseX, rowX, rowW);
                return;
            }
            if (dragSetting instanceof ColorSetting colorSetting) {
                float rowX = x + 4.0f;
                float rowW = width - 8.0f;
                updateColorFromMouse(colorSetting, mouseX, rowX, rowW);
            }
        }
    }

    public boolean mouseScrolled(float mouseX, float mouseY, double amount) {
        if (!contains(mouseX, mouseY)) return false;
        float maxScroll = Math.max(0.0f, computeContentHeight() - height);
        scroll -= (float) (amount * 12.0f);
        if (scroll < 0.0f) scroll = 0.0f;
        if (scroll > maxScroll) scroll = maxScroll;
        return true;
    }

    private static boolean inRect(float mouseX, float mouseY, float x, float y, float w, float h) {
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
    }

    private static BindMode nextBindMode(BindMode current) {
        BindMode[] values = BindMode.values();
        int index = current.ordinal() + 1;
        if (index >= values.length) index = 0;
        return values[index];
    }

    private float computeContentHeight() {
        float rowH = 14.0f;
        float rowGap = 2.0f;
        float y = draggableHeight() + 4.0f;

        y += (rowH + rowGap) * 4;

        for (Setting<?> setting : module.getSettings()) {
            if (!setting.isVisible()) continue;
            if (setting instanceof ColorSetting colorSetting) {
                y += rowH + rowGap;
                if (colorSetting.isExpanded()) {
                    y += rowH + rowGap;
                    y += rowH + rowGap;
                    y += rowH + rowGap;
                    if (colorSetting.isAllowAlpha()) y += rowH + rowGap;
                    y += rowH + rowGap;
                }
                continue;
            }
            if (setting instanceof MultiBoolSetting multi) {
                y += rowH + rowGap;
                if (multi.isExpanded()) {
                    y += (rowH + rowGap) * multi.getOptions().size();
                }
                continue;
            }
            y += rowH + rowGap;
        }

        return y + 4.0f;
    }

    private void updateNumberSettingFromMouse(NumberSetting<?> setting, float mouseX, float rowX, float rowW) {
        float t = (mouseX - rowX) / rowW;
        if (t < 0.0f) t = 0.0f;
        if (t > 1.0f) t = 1.0f;
        double value = setting.getMin() + (setting.getMax() - setting.getMin()) * t;
        setting.setFromDouble(value);
    }

    private void beginColorDrag(ColorSetting setting, DragTarget target, float mouseX, float rowX, float rowW) {
        sliderDragging = true;
        dragTarget = target;
        dragSetting = setting;
        updateColorFromMouse(setting, mouseX, rowX, rowW);
    }

    private void updateColorFromMouse(ColorSetting setting, float mouseX, float rowX, float rowW) {
        float t = (mouseX - rowX) / rowW;
        if (t < 0.0f) t = 0.0f;
        if (t > 1.0f) t = 1.0f;
        switch (dragTarget) {
            case COLOR_HUE -> setting.setHsb(t, setting.getSaturation(), setting.getBrightness());
            case COLOR_SAT -> setting.setHsb(setting.getHue(), t, setting.getBrightness());
            case COLOR_BRI -> setting.setHsb(setting.getHue(), setting.getSaturation(), t);
            case COLOR_ALPHA -> setting.setAlpha((int) Math.round(t * 255.0f));
        }
    }

    private static String stripTrailingZeros(double value) {
        String s = String.valueOf(value);
        if (!s.contains(".")) return s;
        while (s.endsWith("0")) s = s.substring(0, s.length() - 1);
        if (s.endsWith(".")) s = s.substring(0, s.length() - 1);
        return s;
    }

    private enum DragTarget {
        NONE,
        NUMBER,
        COLOR_HUE,
        COLOR_SAT,
        COLOR_BRI,
        COLOR_ALPHA
    }

    private static boolean isCtrlDown() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.getWindow() == null) return false;
        return InputUtil.isKeyPressed(client.getWindow(), GLFW.GLFW_KEY_LEFT_CONTROL)
            || InputUtil.isKeyPressed(client.getWindow(), GLFW.GLFW_KEY_RIGHT_CONTROL);
    }

    private void copySettingsToClipboard() {
        try {
            com.google.gson.JsonObject obj = new com.google.gson.JsonObject();
            for (Setting<?> setting : module.getSettings()) {
                if (setting.isTransient()) continue;
                obj.add(setting.getName(), setting.write());
            }
            String json = new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(obj);
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(json), null);
        } catch (Throwable ignored) {
        }
    }

    private void pasteSettingsFromClipboard() {
        try {
            String text = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
            if (text == null || text.isBlank()) return;
            com.google.gson.JsonObject obj = com.google.gson.JsonParser.parseString(text).getAsJsonObject();
            for (Setting<?> setting : module.getSettings()) {
                if (obj.has(setting.getName())) {
                    setting.read(obj.get(setting.getName()));
                }
            }
        } catch (Throwable ignored) {
        }
    }
}

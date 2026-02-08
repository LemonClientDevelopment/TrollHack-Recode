package dev.mahiro.trollhack.gui.clickgui.window;

import dev.mahiro.trollhack.gui.clickgui.GuiTheme;
import dev.mahiro.trollhack.module.BindMode;
import dev.mahiro.trollhack.module.Module;
import dev.mahiro.trollhack.nanovg.font.FontLoader;
import dev.mahiro.trollhack.nanovg.util.NanoVGHelper;
import org.lwjgl.glfw.GLFW;

import java.awt.Color;

import static org.lwjgl.nanovg.NanoVG.NVG_ALIGN_LEFT;
import static org.lwjgl.nanovg.NanoVG.NVG_ALIGN_RIGHT;
import static org.lwjgl.nanovg.NanoVG.NVG_ALIGN_TOP;

public final class ModuleSettingWindow {
    private final Module module;

    private float x;
    private float y;
    private float width;
    private float height;

    private boolean dragging;
    private float dragOffsetX;
    private float dragOffsetY;

    private boolean closeRequested;
    private boolean bindListening;

    public ModuleSettingWindow(Module module) {
        this.module = module;
        this.width = 140.0f;
        this.height = 110.0f;
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
        return 16.0f;
    }

    public boolean isBindListening() {
        return bindListening;
    }

    public void onKey(int keyCode) {
        if (!bindListening) return;

        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            bindListening = false;
            return;
        }

        if (keyCode == GLFW.GLFW_KEY_BACKSPACE || keyCode == GLFW.GLFW_KEY_DELETE) {
            module.setBindKey(-1);
            bindListening = false;
            return;
        }

        module.setBindKey(keyCode);
        bindListening = false;
    }

    public void render(float mouseX, float mouseY) {
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
        float rowY = y + draggableHeight() + 4.0f;
        float rowGap = 2.0f;

        drawRow(mouseX, mouseY, rowX, rowY, rowW, rowH, "Enabled", module.isEnabled() ? "On" : "Off");
        rowY += rowH + rowGap;
        drawRow(mouseX, mouseY, rowX, rowY, rowW, rowH, "Visible", module.isVisible() ? "On" : "Off");
        rowY += rowH + rowGap;
        drawRow(mouseX, mouseY, rowX, rowY, rowW, rowH, "Bind", bindListening ? "..." : keyName(module.getBindKey()));
        rowY += rowH + rowGap;
        drawRow(mouseX, mouseY, rowX, rowY, rowW, rowH, "Mode", module.getBindMode().name());
    }

    private void drawRow(float mouseX, float mouseY, float x, float y, float w, float h, String label, String value) {
        boolean hovered = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
        if (hovered) {
            NanoVGHelper.drawRect(x, y, w, h, new Color(255, 255, 255, GuiTheme.HOVER_ALPHA));
        }
        int font = FontLoader.medium();
        NanoVGHelper.drawString(label, x + 2.0f, y + 2.0f, font, 11.0f, NVG_ALIGN_LEFT | NVG_ALIGN_TOP, GuiTheme.TEXT);
        NanoVGHelper.drawString(value, x + w - 2.0f, y + 2.0f, font, 11.0f, NVG_ALIGN_RIGHT | NVG_ALIGN_TOP, GuiTheme.TEXT);
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
            } else if (button == 1) {
                closeRequested = true;
            }
            return;
        }

        if (button != 0) return;

        float rowX = x + 4.0f;
        float rowW = width - 8.0f;
        float rowH = 14.0f;
        float rowY = y + draggableHeight() + 4.0f;
        float rowGap = 2.0f;

        if (inRect(mouseX, mouseY, rowX, rowY, rowW, rowH)) {
            module.toggle();
            return;
        }
        rowY += rowH + rowGap;
        if (inRect(mouseX, mouseY, rowX, rowY, rowW, rowH)) {
            module.setVisible(!module.isVisible());
            return;
        }
        rowY += rowH + rowGap;
        if (inRect(mouseX, mouseY, rowX, rowY, rowW, rowH)) {
            bindListening = !bindListening;
            return;
        }
        rowY += rowH + rowGap;
        if (inRect(mouseX, mouseY, rowX, rowY, rowW, rowH)) {
            module.setBindMode(nextBindMode(module.getBindMode()));
        }
    }

    public void mouseReleased(float mouseX, float mouseY, int button) {
        if (button == 0) dragging = false;
    }

    public void mouseDragged(float mouseX, float mouseY, int button) {
        if (!dragging || button != 0) return;
        x = mouseX - dragOffsetX;
        y = mouseY - dragOffsetY;
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
}

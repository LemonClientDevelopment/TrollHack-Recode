package dev.mahiro.trollhack.gui.clickgui.component;

import dev.mahiro.trollhack.gui.clickgui.GuiTheme;
import dev.mahiro.trollhack.module.Module;
import dev.mahiro.trollhack.nanovg.font.FontLoader;
import dev.mahiro.trollhack.nanovg.util.NanoVGHelper;

import java.awt.*;
import java.util.Locale;

import static org.lwjgl.nanovg.NanoVG.NVG_ALIGN_LEFT;
import static org.lwjgl.nanovg.NanoVG.NVG_ALIGN_TOP;

public final class ModuleButtonComponent {
    private final Module module;

    private float x;
    private float y;
    private float width;

    private boolean visible = true;

    public ModuleButtonComponent(Module module) {
        this.module = module;
    }

    public Module getModule() {
        return module;
    }

    public float getY() {
        return y;
    }

    public float getHeight() {
        return 14.0f;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean matches(String normalizedSearch) {
        String normalizedName = module.getName().replace(" ", "").toLowerCase(Locale.ROOT);
        return normalizedName.contains(normalizedSearch);
    }

    public void layout(float x, float y, float width) {
        this.x = x;
        this.y = y;
        this.width = width;
    }

    public boolean contains(float mouseX, float mouseY) {
        float h = getHeight();
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + h;
    }

    public void render(float mouseX, float mouseY) {
        float h = getHeight();

        if (module.isEnabled()) {
            NanoVGHelper.drawShadow(x, y, width, h, 0.0f, new Color(0, 0, 0, 80), 6.0f, 0.0f, 0.0f);
            NanoVGHelper.drawRect(x, y, width, h, GuiTheme.PRIMARY);
        }

        boolean hovered = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + h;
        if (hovered) {
            NanoVGHelper.drawRect(x, y, width, h, new Color(255, 255, 255, GuiTheme.HOVER_ALPHA));
        }

        NanoVGHelper.drawString(module.getName(), x + 2.0f, y + 2.0f, FontLoader.medium(), 12.0f, NVG_ALIGN_LEFT | NVG_ALIGN_TOP, GuiTheme.TEXT);
    }

    public void mouseClicked(float mouseX, float mouseY, int button) {
        if (button == 0) {
            module.toggle();
        }
    }
}

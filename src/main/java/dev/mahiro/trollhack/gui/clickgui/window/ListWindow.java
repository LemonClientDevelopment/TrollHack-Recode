package dev.mahiro.trollhack.gui.clickgui.window;

import dev.mahiro.trollhack.gui.clickgui.ClickGuiScreen;
import dev.mahiro.trollhack.gui.clickgui.GuiTheme;
import dev.mahiro.trollhack.gui.clickgui.component.ModuleButtonComponent;
import dev.mahiro.trollhack.module.Category;
import dev.mahiro.trollhack.nanovg.font.FontLoader;
import dev.mahiro.trollhack.nanovg.util.NanoVGHelper;

import java.awt.Color;
import java.util.List;

import static org.lwjgl.nanovg.NanoVG.NVG_ALIGN_LEFT;
import static org.lwjgl.nanovg.NanoVG.NVG_ALIGN_TOP;

public final class ListWindow {
    private final ClickGuiScreen screen;
    private final Category category;
    private final String title;
    private final List<ModuleButtonComponent> children;

    private float x;
    private float y;
    private float width;
    private float height;

    private boolean dragging;
    private float dragOffsetX;
    private float dragOffsetY;

    private float scroll;

    public ListWindow(ClickGuiScreen screen, Category category, String title, List<ModuleButtonComponent> children) {
        this.screen = screen;
        this.category = category;
        this.title = title;
        this.children = children;
    }

    public Category getCategory() {
        return category;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public boolean contains(float mouseX, float mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    private float draggableHeight() {
        return 16.0f;
    }

    public void render(float mouseX, float mouseY, String searchString) {
        float dragHeight = draggableHeight();

        NanoVGHelper.drawRect(x, y, width, height, GuiTheme.BACKGROUND);
        if (GuiTheme.WINDOW_OUTLINE) {
            Color outline = new Color(GuiTheme.PRIMARY.getRed(), GuiTheme.PRIMARY.getGreen(), GuiTheme.PRIMARY.getBlue(), 255);
            NanoVGHelper.drawRectOutline(x, y, width, height, 1.0f, outline);
        }

        NanoVGHelper.drawString(title, x + 3.0f, y + 3.0f, FontLoader.bold(), 12.0f, NVG_ALIGN_LEFT | NVG_ALIGN_TOP, GuiTheme.TEXT);

        float childY = (height == dragHeight ? 0.0f : dragHeight) + GuiTheme.WINDOW_Y_MARGIN;
        float childX = GuiTheme.WINDOW_X_MARGIN;
        float childWidth = width - GuiTheme.WINDOW_X_MARGIN * 2.0f;

        for (ModuleButtonComponent child : children) {
            child.layout(x + childX, y + childY, childWidth);
            if (child.isVisible()) {
                childY += child.getHeight() + GuiTheme.WINDOW_Y_MARGIN;
            }
        }

        NanoVGHelper.save();
        NanoVGHelper.scissor(x + childX, y + dragHeight, width - childX * 2.0f, height - dragHeight);
        NanoVGHelper.translate(0.0f, -scroll);

        for (ModuleButtonComponent child : children) {
            if (!child.isVisible()) continue;
            float ry = (child.getY() - y) - scroll;
            if (ry + child.getHeight() < dragHeight) continue;
            if (ry > height) continue;
            child.render(mouseX, mouseY + scroll);
        }

        NanoVGHelper.restore();
    }

    public void applyFilter(String normalizedSearch) {
        if (normalizedSearch == null) normalizedSearch = "";
        for (ModuleButtonComponent child : children) {
            child.setVisible(normalizedSearch.isEmpty() || child.matches(normalizedSearch));
        }
        scroll = 0.0f;
    }

    public void mouseClicked(float mouseX, float mouseY, int button, String searchString) {
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

        for (ModuleButtonComponent child : children) {
            if (!child.isVisible()) continue;
            if (child.contains(mouseX, mouseY + scroll)) {
                if (button == 1) {
                    screen.openModuleSettings(child.getModule(), mouseX, mouseY);
                } else {
                    child.mouseClicked(mouseX, mouseY + scroll, button);
                }
                return;
            }
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

    public void mouseScrolled(float mouseX, float mouseY, double verticalAmount, String searchString) {
        float maxScroll = getMaxScroll();
        scroll -= (float) (verticalAmount * 12.0f);
        if (scroll < 0.0f) scroll = 0.0f;
        if (scroll > maxScroll) scroll = maxScroll;
    }

    private float getMaxScroll() {
        float dragHeight = draggableHeight();
        float y = (height == dragHeight ? 0.0f : dragHeight) + GuiTheme.WINDOW_Y_MARGIN;
        ModuleButtonComponent lastVisible = null;
        for (ModuleButtonComponent child : children) {
            if (!child.isVisible()) continue;
            lastVisible = child;
            y += child.getHeight() + GuiTheme.WINDOW_Y_MARGIN;
        }
        if (lastVisible == null) return 0.0f;
        float contentHeight = y;
        float max = contentHeight - height;
        return Math.max(max, 0.0f);
    }
}

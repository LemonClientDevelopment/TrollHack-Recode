package dev.mahiro.trollhack.gui.clickgui.window;

import dev.mahiro.trollhack.gui.clickgui.ClickGuiScreen;
import dev.mahiro.trollhack.gui.clickgui.GuiTheme;
import dev.mahiro.trollhack.gui.clickgui.component.ModuleButtonComponent;
import dev.mahiro.trollhack.module.Category;
import dev.mahiro.trollhack.nanovg.font.FontLoader;
import dev.mahiro.trollhack.nanovg.util.NanoVGHelper;

import java.awt.*;
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

    private boolean minimized;

    private boolean dragging;
    private float dragOffsetX;
    private float dragOffsetY;
    private float preDragX;
    private float preDragY;
    private float preDragWidth;
    private float preDragHeight;
    private float preClickX;
    private float preClickY;
    private int preButton;

    private long doubleClickTime = -1L;

    private long lastScrollSpeedUpdateMs = System.currentTimeMillis();
    private long lastScrollBounceMs = System.currentTimeMillis();
    private float scrollSpeed;
    private float scrollProgress;

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
        float h = minimized ? draggableHeight() : height;
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + h;
    }

    private float draggableHeight() {
        return NanoVGHelper.getFontHeight(FontLoader.bold(), 12.0f) + 6.0f;
    }

    public void render(float mouseX, float mouseY, String searchString) {
        float dragHeight = draggableHeight();
        updateScrollProgress();
        float renderHeight = minimized ? dragHeight : height;

        NanoVGHelper.drawShadow(x, y, width, renderHeight, 0.0f, new Color(0, 0, 0, 120), 10.0f, 0.0f, 0.0f);
        NanoVGHelper.drawRect(x, y, width, renderHeight, GuiTheme.BACKGROUND);
        if (GuiTheme.WINDOW_OUTLINE) {
            Color outline = new Color(GuiTheme.PRIMARY.getRed(), GuiTheme.PRIMARY.getGreen(), GuiTheme.PRIMARY.getBlue(), 255);
            NanoVGHelper.drawRectOutline(x, y, width, renderHeight, 1.0f, outline);
        }

        if (GuiTheme.TITLE_BAR) {
            NanoVGHelper.drawRect(x, y, width, dragHeight, GuiTheme.PRIMARY);
        }
        NanoVGHelper.drawString(title, x + 3.0f, y + 3.0f, FontLoader.bold(), 12.0f, NVG_ALIGN_LEFT | NVG_ALIGN_TOP, GuiTheme.TEXT);

        if (minimized) return;

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
        NanoVGHelper.translate(0.0f, -scrollProgress);

        for (ModuleButtonComponent child : children) {
            if (!child.isVisible()) continue;
            float ry = (child.getY() - y) - scrollProgress;
            if (ry + child.getHeight() < dragHeight) continue;
            if (ry > height) continue;
            child.render(mouseX, mouseY + scrollProgress);
        }

        NanoVGHelper.restore();
    }

    public void applyFilter(String normalizedSearch) {
        if (normalizedSearch == null) normalizedSearch = "";
        for (ModuleButtonComponent child : children) {
            child.setVisible(normalizedSearch.isEmpty() || child.matches(normalizedSearch));
        }
        scrollSpeed = 0.0f;
        scrollProgress = 0.0f;
    }

    public void onClick(float mouseX, float mouseY, int button, float trollWidth, float trollHeight) {
        preDragX = x;
        preDragY = y;
        preDragWidth = width;
        preDragHeight = height;
        preClickX = mouseX;
        preClickY = mouseY;
        preButton = button;

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

        if (minimized) return;

        for (ModuleButtonComponent child : children) {
            if (!child.isVisible()) continue;
            if (child.contains(mouseX, mouseY + scrollProgress)) {
                if (button == 1) {
                    screen.openModuleSettings(child.getModule(), mouseX, mouseY);
                } else {
                    child.mouseClicked(mouseX, mouseY + scrollProgress, button);
                }
                return;
            }
        }
    }

    public void onRelease(float mouseX, float mouseY, int button, float trollHeight, boolean wasDrag) {
        if (button == 0) dragging = false;

        if (!wasDrag && button == 1 && preClickY - preDragY < draggableHeight()) {
            minimized = !minimized;
        }

        if (!wasDrag) {
            handleDoubleClick(mouseX, mouseY, button, trollHeight);
        }
    }

    public void onDrag(float mouseX, float mouseY, int button, float trollWidth, float trollHeight) {
        if (!dragging || button != 0) return;
        x = clamp(mouseX - dragOffsetX, 0.0f, trollWidth - width - 1.0f);
        y = clamp(mouseY - dragOffsetY, 0.0f, trollHeight - height - 1.0f);
    }

    public void onMouseWheel(double amount) {
        scrollSpeed -= (float) (amount * 24.0f);
        lastScrollBounceMs = System.currentTimeMillis();
    }

    private void handleDoubleClick(float mouseX, float mouseY, int button, float trollHeight) {
        if (button != 0) {
            doubleClickTime = -1L;
            return;
        }
        if (mouseY - y >= draggableHeight()) {
            doubleClickTime = -1L;
            return;
        }

        long now = System.currentTimeMillis();
        if (doubleClickTime == -1L || now - doubleClickTime > 500L) {
            doubleClickTime = now;
            return;
        }

        float optimalHeight = getOptimalHeight();
        if (optimalHeight < height) {
            doubleClickTime = -1L;
            return;
        }

        float maxHeight = trollHeight - 2.0f;
        height = Math.min(optimalHeight, trollHeight - 2.0f);
        y = Math.min(y, maxHeight - optimalHeight);
        doubleClickTime = -1L;
    }

    private float getOptimalHeight() {
        float dragHeight = draggableHeight();
        float sum = 0.0f;
        for (ModuleButtonComponent child : children) {
            if (!child.isVisible()) continue;
            sum += child.getHeight() + GuiTheme.WINDOW_Y_MARGIN;
        }
        return sum + dragHeight + Math.max(GuiTheme.WINDOW_X_MARGIN, GuiTheme.WINDOW_Y_MARGIN);
    }

    private void updateScrollProgress() {
        if (children.isEmpty()) return;

        long now = System.currentTimeMillis();
        double x = (now - lastScrollSpeedUpdateMs) / 100.0;
        double lnHalf = Math.log(0.25);
        float newSpeed = (float) (scrollSpeed * Math.pow(0.25, x));
        scrollProgress += (float) ((newSpeed / lnHalf) - (scrollSpeed / lnHalf));
        scrollSpeed = newSpeed;
        lastScrollSpeedUpdateMs = now;

        if (now - lastScrollBounceMs >= 100L) {
            float maxScrollProgress = getMaxScrollProgress();
            if (scrollProgress < 0.0f) {
                scrollSpeed = scrollProgress * -0.4f;
                lastScrollBounceMs = now;
            } else if (scrollProgress > maxScrollProgress) {
                scrollSpeed = (scrollProgress - maxScrollProgress) * -0.4f;
                lastScrollBounceMs = now;
            }
        }
    }

    private float getMaxScrollProgress() {
        float dragHeight = draggableHeight();
        float y = (height == dragHeight ? 0.0f : dragHeight) + GuiTheme.WINDOW_Y_MARGIN;
        ModuleButtonComponent lastVisible = null;
        for (ModuleButtonComponent child : children) {
            if (!child.isVisible()) continue;
            lastVisible = child;
            y += child.getHeight() + GuiTheme.WINDOW_Y_MARGIN;
        }
        if (lastVisible == null) return dragHeight;
        return Math.max(y - height, 0.01f);
    }

    private static float clamp(float v, float min, float max) {
        if (v < min) return min;
        if (v > max) return max;
        return v;
    }
}

package dev.mahiro.trollhack.gui.clickgui;

import dev.mahiro.trollhack.TrollHack;
import dev.mahiro.trollhack.gui.clickgui.anim.AnimatedFloat;
import dev.mahiro.trollhack.gui.clickgui.anim.Easing;
import dev.mahiro.trollhack.gui.clickgui.component.ModuleButtonComponent;
import dev.mahiro.trollhack.gui.clickgui.window.ListWindow;
import dev.mahiro.trollhack.gui.clickgui.window.ModuleSettingWindow;
import dev.mahiro.trollhack.module.Category;
import dev.mahiro.trollhack.module.Module;
import dev.mahiro.trollhack.nanovg.NanoVGRenderer;
import dev.mahiro.trollhack.nanovg.font.FontLoader;
import dev.mahiro.trollhack.nanovg.util.NanoVGHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

import static org.lwjgl.nanovg.NanoVG.NVG_ALIGN_LEFT;
import static org.lwjgl.nanovg.NanoVG.NVG_ALIGN_MIDDLE;

public final class ClickGuiScreen extends Screen {
    private final LinkedHashMap<Category, ListWindow> windows = new LinkedHashMap<>();
    private final List<ModuleSettingWindow> moduleSettingWindows = new ArrayList<>();
    private final List<Object> windowOrder = new ArrayList<>();

    private String searchString = "";
    private final AnimatedFloat searchWidth = new AnimatedFloat(Easing.OUT_CUBIC, 250.0f, 0.0f);
    private long searchUpdateTimeMs;

    private MouseState mouseState = MouseState.NONE;
    private Object lastClicked;
    private float lastClickX;
    private float lastClickY;
    private long lastClickTimeMs;
    private int lastClickButton = -1;

    private boolean closing;
    private long transitionStartMs;
    private float transitionFrom;
    private float transitionTo;

    public ClickGuiScreen() {
        super(Text.literal("TrollHack ClickGUI"));
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
    }

    @Override
    protected void init() {
        super.init();

        if (windows.isEmpty()) {
            float posX = 0.0f;
            float posY = 0.0f;

            float trollWidth = width / GuiTheme.SCALE_FACTOR;

            for (Category category : Category.values()) {
                List<ModuleButtonComponent> children = new ArrayList<>();
                for (Module module : TrollHack.MODULE_MANAGER.getModules(category)) {
                    children.add(new ModuleButtonComponent(module));
                }

                ListWindow window = new ListWindow(this, category, category.getDisplayName(), children);
                window.setX(posX);
                window.setY(posY);
                window.setWidth(80.0f);
                window.setHeight(400.0f);
                windows.put(category, window);
                windowOrder.add(window);

                posX += 80.0f;
                if (posX > trollWidth) {
                    posX = 0.0f;
                    posY += 100.0f;
                }
            }
        }

        beginTransition(false);
    }

    private void beginTransition(boolean closing) {
        this.closing = closing;
        this.transitionStartMs = System.currentTimeMillis();
        this.transitionFrom = closing ? 1.0f : 0.0f;
        this.transitionTo = closing ? 0.0f : 1.0f;
    }

    private float getTransitionMultiplier() {
        float durationMs = 400.0f;
        float t = (System.currentTimeMillis() - transitionStartMs) / durationMs;
        if (t >= 1.0f) return transitionTo;
        if (t <= 0.0f) return transitionFrom;

        float eased = 1.0f - (float) Math.pow(1.0f - t, 3.0);
        return transitionFrom + (transitionTo - transitionFrom) * eased;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        MinecraftClient client = MinecraftClient.getInstance();
        float multiplier = getTransitionMultiplier();

        float trollMouseX = mouseX / GuiTheme.SCALE_FACTOR;
        float trollMouseY = mouseY / GuiTheme.SCALE_FACTOR;

        for (int i = moduleSettingWindows.size() - 1; i >= 0; i--) {
            ModuleSettingWindow window = moduleSettingWindows.get(i);
            if (lastClicked != window && !window.handlesKeyboardInput()) {
                closeWindow(window);
            }
        }

        if (GuiTheme.BACKGROUND_BLUR > 0.0f) {
            try {
                context.applyBlur();
            } catch (Throwable ignored) {
            }
        }

        NanoVGRenderer.INSTANCE.draw(vg -> {
            float trollWidth = width / GuiTheme.SCALE_FACTOR;
            float trollHeight = height / GuiTheme.SCALE_FACTOR;

            NanoVGHelper.save();
            NanoVGHelper.scale(vg, GuiTheme.SCALE_FACTOR, GuiTheme.SCALE_FACTOR);

            if (GuiTheme.DARKNESS > 0.0f) {
                int alpha = (int) (GuiTheme.DARKNESS * 255.0f * multiplier);
                NanoVGHelper.drawRect(0.0f, 0.0f, trollWidth, trollHeight, new Color(0, 0, 0, alpha));
            }

            NanoVGHelper.translate(vg, 0.0f, -(trollHeight * (1.0f - multiplier)));

            for (Object window : windowOrder) {
                if (window instanceof ListWindow listWindow) {
                    listWindow.render(trollMouseX, trollMouseY, searchString);
                } else if (window instanceof ModuleSettingWindow settingWindow) {
                    settingWindow.render(trollMouseX, trollMouseY);
                }
            }

            drawSearchString(trollWidth, trollHeight);

            NanoVGHelper.restore();
        });

        if (closing && multiplier <= 0.0f) {
            client.setScreen(null);
        }
    }

    private void drawSearchString(float trollWidth, float trollHeight) {
        if (searchString.isBlank()) return;
        long now = System.currentTimeMillis();
        if (now - searchUpdateTimeMs > 5000L) return;

        int font = FontLoader.medium();
        float size = 16.0f;
        float w = searchWidth.get();
        float x = trollWidth / 2.0f - w / 2.0f;
        float y = trollHeight / 2.0f;

        float t = (now - searchUpdateTimeMs) / 5000.0f;
        int alpha = (int) (255.0f * (1.0f - Easing.IN_CUBIC.apply(Easing.clamp01(t))));
        Color color = new Color(GuiTheme.TEXT.getRed(), GuiTheme.TEXT.getGreen(), GuiTheme.TEXT.getBlue(), alpha);
        NanoVGHelper.drawString(searchString, x, y, font, size, NVG_ALIGN_LEFT | NVG_ALIGN_MIDDLE, color);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        float x = (float) (click.x() / GuiTheme.SCALE_FACTOR);
        float y = (float) (click.y() / GuiTheme.SCALE_FACTOR);
        int button = click.button();

        mouseState = MouseState.CLICK;
        lastClickX = x;
        lastClickY = y;
        lastClickTimeMs = System.currentTimeMillis();
        lastClickButton = button;

        Object hovered = getHoveredWindow(x, y);
        lastClicked = hovered;
        if (hovered == null) return false;

        float trollWidth = width / GuiTheme.SCALE_FACTOR;
        float trollHeight = height / GuiTheme.SCALE_FACTOR;

        if (hovered instanceof ModuleSettingWindow window) {
            window.mouseClicked(x, y, button);
            if (window.isCloseRequested()) {
                closeWindow(window);
                return true;
            }
        } else if (hovered instanceof ListWindow window) {
            window.onClick(x, y, button, trollWidth, trollHeight);
        }

        bringToFront(hovered);
        return true;
    }

    @Override
    public boolean mouseReleased(Click click) {
        float x = (float) (click.x() / GuiTheme.SCALE_FACTOR);
        float y = (float) (click.y() / GuiTheme.SCALE_FACTOR);
        int button = click.button();

        float trollHeight = height / GuiTheme.SCALE_FACTOR;
        boolean wasDrag = mouseState == MouseState.DRAG;

        if (lastClicked instanceof ModuleSettingWindow window) {
            window.mouseReleased(x, y, button);
            if (window.isCloseRequested()) closeWindow(window);
        } else if (lastClicked instanceof ListWindow window) {
            window.onRelease(x, y, button, trollHeight, wasDrag);
        }

        mouseState = MouseState.NONE;
        return true;
    }

    @Override
    public boolean mouseDragged(Click click, double offsetX, double offsetY) {
        float x = (float) (click.x() / GuiTheme.SCALE_FACTOR);
        float y = (float) (click.y() / GuiTheme.SCALE_FACTOR);
        int button = click.button();

        long now = System.currentTimeMillis();
        float dx = x - lastClickX;
        float dy = y - lastClickY;
        float dist2 = dx * dx + dy * dy;
        if (mouseState != MouseState.DRAG) {
            if (dist2 < 16.0f || now - lastClickTimeMs < 50L) return true;
            mouseState = MouseState.DRAG;
        }

        float trollWidth = width / GuiTheme.SCALE_FACTOR;
        float trollHeight = height / GuiTheme.SCALE_FACTOR;

        if (lastClicked instanceof ModuleSettingWindow window) {
            window.mouseDragged(x, y, button);
        } else if (lastClicked instanceof ListWindow window) {
            window.onDrag(x, y, button, trollWidth, trollHeight);
        }
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        float x = (float) (mouseX / GuiTheme.SCALE_FACTOR);
        float y = (float) (mouseY / GuiTheme.SCALE_FACTOR);

        Object hovered = getHoveredWindow(x, y);
        if (hovered instanceof ModuleSettingWindow window) {
            return window.mouseScrolled(x, y, verticalAmount);
        }
        if (hovered instanceof ListWindow window) {
            window.onMouseWheel(verticalAmount);
            return true;
        }
        return false;
    }

    private Object getHoveredWindow(float mouseX, float mouseY) {
        if (mouseState != MouseState.NONE) return lastClicked;
        Object result = null;
        for (Object window : windowOrder) {
            if (window instanceof ListWindow listWindow) {
                if (listWindow.contains(mouseX, mouseY)) result = window;
            } else if (window instanceof ModuleSettingWindow settingWindow) {
                if (settingWindow.contains(mouseX, mouseY)) result = window;
            }
        }
        return result;
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (lastClicked instanceof ModuleSettingWindow window) {
            if (window.handlesKeyboardInput() && window.onKey(input.key())) return true;
        }

        if (input.isEscape() || input.key() == GLFW.GLFW_KEY_RIGHT_SHIFT) {
            if (searchString.isBlank() && !isKeyboardInputActive()) {
                requestClose();
                return true;
            }
            return true;
        }

        if (input.key() == GLFW.GLFW_KEY_BACKSPACE || input.key() == GLFW.GLFW_KEY_DELETE) {
            searchString = "";
            searchWidth.forceUpdate(0.0f);
            return true;
        }

        return super.keyPressed(input);
    }

    @Override
    public boolean charTyped(CharInput input) {
        if (lastClicked instanceof ModuleSettingWindow window) {
            if (window.handlesKeyboardInput() && window.onChar(input.codepoint())) return true;
        }

        char chr = (char) input.codepoint();
        if (Character.isLetter(chr) || chr == ' ') {
            setSearchString(searchString + chr);
            return true;
        }
        return super.charTyped(input);
    }

    private void setSearchString(String value) {
        searchString = value == null ? "" : value;
        int font = FontLoader.medium();
        float size = 16.0f;
        float w = NanoVGHelper.getTextWidth(searchString, font, size);
        searchWidth.update(w);
        searchUpdateTimeMs = System.currentTimeMillis();
        applySearchFilter();
    }

    private void applySearchFilter() {
        String normalized = searchString.replace(" ", "").toLowerCase(Locale.ROOT);
        for (ListWindow window : windows.values()) {
            window.applyFilter(normalized);
        }
    }

    private void requestClose() {
        if (closing) return;
        searchString = "";
        for (ListWindow window : windows.values()) window.applyFilter("");
        beginTransition(true);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    public void openModuleSettings(Module module, float mouseX, float mouseY) {
        moduleSettingWindows.removeIf(w -> w.getModule() == module);
        ModuleSettingWindow window = new ModuleSettingWindow(module);

        float trollWidth = width / GuiTheme.SCALE_FACTOR;
        float trollHeight = height / GuiTheme.SCALE_FACTOR;

        float x = (mouseX + window.getWidth() <= trollWidth) ? mouseX : mouseX - window.getWidth();
        float y = Math.min(mouseY, trollHeight - window.getHeight());

        window.setX(x);
        window.setY(y);
        moduleSettingWindows.add(window);
        windowOrder.add(window);
        bringToFront(window);
    }

    private void bringToFront(Object window) {
        windowOrder.remove(window);
        windowOrder.add(window);
    }

    private void closeWindow(Object window) {
        windowOrder.remove(window);
        if (window instanceof ModuleSettingWindow settingWindow) {
            moduleSettingWindows.remove(settingWindow);
        }
    }

    private boolean isKeyboardInputActive() {
        if (lastClicked instanceof ModuleSettingWindow window) {
            return window.handlesKeyboardInput();
        }
        return false;
    }

    private enum MouseState {
        NONE,
        CLICK,
        DRAG
    }
}

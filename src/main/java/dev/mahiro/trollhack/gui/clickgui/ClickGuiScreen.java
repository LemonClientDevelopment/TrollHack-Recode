package dev.mahiro.trollhack.gui.clickgui;

import dev.mahiro.trollhack.TrollHack;
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

    private String searchString = "";
    private boolean closing;
    private long transitionStartMs;
    private float transitionFrom;
    private float transitionTo;

    public ClickGuiScreen() {
        super(Text.literal("TrollHack ClickGUI"));
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

            for (ListWindow window : windows.values()) {
                window.render(trollMouseX, trollMouseY, searchString);
            }

            for (ModuleSettingWindow window : moduleSettingWindows) {
                window.render(trollMouseX, trollMouseY);
            }

            if (!searchString.isBlank()) {
                int font = FontLoader.medium();
                float size = 16.0f;
                float textWidth = NanoVGHelper.getTextWidth(searchString, font, size);
                float x = trollWidth / 2.0f - textWidth / 2.0f;
                float y = trollHeight / 2.0f;
                NanoVGHelper.drawString(searchString, x, y, font, size, NVG_ALIGN_LEFT | NVG_ALIGN_MIDDLE, GuiTheme.TEXT);
            }

            NanoVGHelper.restore();
        });

        if (closing && multiplier <= 0.0f) {
            client.setScreen(null);
        }
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        float x = (float) (click.x() / GuiTheme.SCALE_FACTOR);
        float y = (float) (click.y() / GuiTheme.SCALE_FACTOR);
        int button = click.button();

        for (int i = moduleSettingWindows.size() - 1; i >= 0; i--) {
            ModuleSettingWindow window = moduleSettingWindows.get(i);
            if (!window.contains(x, y)) continue;
            window.mouseClicked(x, y, button);
            moduleSettingWindows.remove(i);
            if (!window.isCloseRequested()) {
                moduleSettingWindows.add(window);
            }
            return true;
        }

        ListWindow hovered = getHoveredWindow(x, y);
        if (hovered != null) {
            hovered.mouseClicked(x, y, button, searchString);

            Category category = hovered.getCategory();
            windows.remove(category);
            windows.put(category, hovered);
            return true;
        }

        return false;
    }

    @Override
    public boolean mouseReleased(Click click) {
        float x = (float) (click.x() / GuiTheme.SCALE_FACTOR);
        float y = (float) (click.y() / GuiTheme.SCALE_FACTOR);
        int button = click.button();

        for (ModuleSettingWindow window : moduleSettingWindows) {
            window.mouseReleased(x, y, button);
        }
        for (ListWindow window : windows.values()) {
            window.mouseReleased(x, y, button);
        }
        return true;
    }

    @Override
    public boolean mouseDragged(Click click, double offsetX, double offsetY) {
        float x = (float) (click.x() / GuiTheme.SCALE_FACTOR);
        float y = (float) (click.y() / GuiTheme.SCALE_FACTOR);
        int button = click.button();

        for (ModuleSettingWindow window : moduleSettingWindows) {
            window.mouseDragged(x, y, button);
        }
        for (ListWindow window : windows.values()) {
            window.mouseDragged(x, y, button);
        }
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        float x = (float) (mouseX / GuiTheme.SCALE_FACTOR);
        float y = (float) (mouseY / GuiTheme.SCALE_FACTOR);

        for (int i = moduleSettingWindows.size() - 1; i >= 0; i--) {
            ModuleSettingWindow window = moduleSettingWindows.get(i);
            if (window.mouseScrolled(x, y, verticalAmount)) {
                return true;
            }
        }

        ListWindow hovered = getHoveredWindow(x, y);
        if (hovered != null) {
            hovered.mouseScrolled(x, y, verticalAmount, searchString);
            return true;
        }
        return false;
    }

    private ListWindow getHoveredWindow(float mouseX, float mouseY) {
        ListWindow result = null;
        for (ListWindow window : windows.values()) {
            if (window.contains(mouseX, mouseY)) {
                result = window;
            }
        }
        return result;
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        for (int i = moduleSettingWindows.size() - 1; i >= 0; i--) {
            ModuleSettingWindow window = moduleSettingWindows.get(i);
            if (window.handlesKeyboardInput() && window.onKey(input.key())) {
                return true;
            }
        }

        if (input.isEscape() || input.key() == GLFW.GLFW_KEY_RIGHT_SHIFT) {
            requestClose();
            return true;
        }

        if (input.key() == GLFW.GLFW_KEY_BACKSPACE || input.key() == GLFW.GLFW_KEY_DELETE) {
            searchString = "";
            return true;
        }

        return super.keyPressed(input);
    }

    @Override
    public boolean charTyped(CharInput input) {
        for (int i = moduleSettingWindows.size() - 1; i >= 0; i--) {
            ModuleSettingWindow window = moduleSettingWindows.get(i);
            if (window.handlesKeyboardInput() && window.onChar(input.codepoint())) {
                return true;
            }
        }

        char chr = (char) input.codepoint();
        if (Character.isLetter(chr) || chr == ' ') {
            searchString += chr;
            applySearchFilter();
            return true;
        }
        return super.charTyped(input);
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

        float x = mouseX + 8.0f;
        float y = mouseY + 8.0f;
        if (x + window.getWidth() > trollWidth) x = trollWidth - window.getWidth();
        if (y + window.getHeight() > trollHeight) y = trollHeight - window.getHeight();
        if (x < 0.0f) x = 0.0f;
        if (y < 0.0f) y = 0.0f;

        window.setX(x);
        window.setY(y);
        moduleSettingWindows.add(window);
    }
}

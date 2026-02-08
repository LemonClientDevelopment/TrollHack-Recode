package dev.mahiro.trollhack.gui.clickgui.component;

import dev.mahiro.trollhack.gui.clickgui.GuiTheme;
import dev.mahiro.trollhack.gui.clickgui.anim.AnimatedFloat;
import dev.mahiro.trollhack.gui.clickgui.anim.Easing;
import dev.mahiro.trollhack.module.Module;
import dev.mahiro.trollhack.nanovg.font.FontLoader;
import dev.mahiro.trollhack.nanovg.util.NanoVGHelper;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.lwjgl.nanovg.NanoVG.NVG_ALIGN_LEFT;
import static org.lwjgl.nanovg.NanoVG.NVG_ALIGN_TOP;

public final class ModuleButtonComponent {
    private final Module module;

    private float x;
    private float y;
    private float width;

    private boolean visible = true;

    private boolean pressed;
    private int pressedButton = -1;

    private State state = State.NONE;
    private Color overlayFrom = GuiTheme.getIdleOverlay();
    private Color overlayTo = GuiTheme.getIdleOverlay();
    private final AnimatedFloat overlayBlend = new AnimatedFloat(Easing.OUT_EXPO, 300.0f, 1.0f);

    private boolean lastEnabled;
    private final AnimatedFloat enabledProgress;

    private final AnimatedFloat hoverAmount = new AnimatedFloat(Easing.OUT_BACK, 300.0f, 0.0f);
    private final AnimatedFloat clickAmount = new AnimatedFloat(Easing.OUT_CUBIC, 300.0f, 0.0f);

    private final AnimatedFloat tooltipAlpha = new AnimatedFloat(Easing.OUT_CUBIC, 250.0f, 0.0f);
    private long tooltipHoverStartMs;
    private float tooltipAnchorX;
    private boolean tooltipArmed;
    private List<String> tooltipLines;
    private float tooltipTextWidth;
    private float tooltipTextHeight;

    public ModuleButtonComponent(Module module) {
        this.module = module;
        this.lastEnabled = module.isEnabled();
        this.enabledProgress = new AnimatedFloat(Easing.OUT_QUART, 300.0f, lastEnabled ? 1.0f : 0.0f);
    }

    public Module getModule() {
        return module;
    }

    public float getY() {
        return y;
    }

    public float getHeight() {
        float fontHeight = NanoVGHelper.getFontHeight(FontLoader.regular(), 11.0f);
        return fontHeight + 3.0f;
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

        boolean enabledNow = module.isEnabled();
        if (enabledNow != lastEnabled) {
            lastEnabled = enabledNow;
            enabledProgress.update(enabledNow ? 1.0f : 0.0f);
        }

        boolean hovered = contains(mouseX, mouseY);
        State newState;
        if (pressed) {
            newState = State.CLICK;
        } else if (hovered) {
            newState = State.HOVER;
        } else {
            newState = State.NONE;
        }
        updateState(newState);

        float p = enabledProgress.get();
        if (p > 0.0f) {
            NanoVGHelper.drawRect(x, y, width * p, h, GuiTheme.PRIMARY);
        }

        Color overlay = mix(overlayFrom, overlayTo, overlayBlend.get());
        if (overlay.getAlpha() > 0) {
            NanoVGHelper.drawRect(x, y, width, h, overlay);
        }

        float fontHeight = NanoVGHelper.getFontHeight(FontLoader.regular(), 11.0f);
        float hover = hoverAmount.get();
        float click = clickAmount.get();
        float size = 11.0f * (1.0f + 0.05f * hover - 0.1f * click);

        float textX = x + 2.0f + 2.0f * hover;
        float textY = y + 1.0f - 0.025f * hover * fontHeight + 0.05f * click * fontHeight;

        NanoVGHelper.drawString(module.getName(), textX, textY, FontLoader.regular(), size, NVG_ALIGN_LEFT | NVG_ALIGN_TOP, GuiTheme.TEXT);

        renderTooltip(mouseX, mouseY, hovered, h);
    }

    public void mousePressed(float mouseX, float mouseY, int button) {
        if (!contains(mouseX, mouseY)) return;
        pressed = true;
        pressedButton = button;
        updateState(State.CLICK);
    }

    public void mouseReleased(float mouseX, float mouseY, int button, boolean wasDrag) {
        if (!pressed) return;
        if (button != pressedButton) return;

        boolean clickAction = !wasDrag && contains(mouseX, mouseY);
        pressed = false;
        pressedButton = -1;

        updateState(contains(mouseX, mouseY) ? State.HOVER : State.NONE);

        if (clickAction && button == 0) {
            module.toggle();
        }
    }

    private void updateState(State newState) {
        if (newState == state) return;

        overlayFrom = mix(overlayFrom, overlayTo, overlayBlend.get());
        overlayTo = switch (newState) {
            case NONE -> GuiTheme.getIdleOverlay();
            case HOVER -> GuiTheme.getHoverOverlay();
            case CLICK -> GuiTheme.getClickOverlay();
        };
        overlayBlend.forceUpdate(0.0f);
        overlayBlend.update(1.0f);

        hoverAmount.update(newState == State.HOVER ? 1.0f : 0.0f);
        clickAmount.update(newState == State.CLICK ? 1.0f : 0.0f);

        if (newState == State.HOVER) {
            tooltipHoverStartMs = System.currentTimeMillis();
            tooltipAnchorX = 0.0f;
            tooltipArmed = true;
        } else {
            tooltipArmed = false;
            if (tooltipAlpha.get() > 0.0f) tooltipAlpha.update(0.0f);
        }

        state = newState;
    }

    private void renderTooltip(float mouseX, float mouseY, boolean hovered, float rowHeight) {
        if (module.getDescription() == null || module.getDescription().isBlank()) return;

        long now = System.currentTimeMillis();
        if (hovered && tooltipArmed) {
            if (tooltipAnchorX == 0.0f) tooltipAnchorX = mouseX;
            if (now - tooltipHoverStartMs > 500L && tooltipAlpha.get() < 1.0f) {
                tooltipAlpha.update(1.0f);
                ensureTooltipLayout();
            }
        } else if (tooltipAlpha.get() > 0.0f && !tooltipArmed) {
            if (tooltipAlpha.get() > 0.0f) tooltipAlpha.update(0.0f);
        }

        float a = tooltipAlpha.get();
        if (a <= 0.01f) return;
        ensureTooltipLayout();

        float pad = 4.0f;
        float boxW = tooltipTextWidth + pad;
        float boxH = tooltipTextHeight + pad;

        float trollWidth = (float) (net.minecraft.client.MinecraftClient.getInstance().getWindow().getFramebufferWidth() / GuiTheme.getScaleFactor(0.0f));
        float trollHeight = (float) (net.minecraft.client.MinecraftClient.getInstance().getWindow().getFramebufferHeight() / GuiTheme.getScaleFactor(0.0f));

        float x0 = clamp(tooltipAnchorX, 0.0f, trollWidth);
        float posX = clamp(x0, 0.0f, trollWidth - boxW - 10.0f);
        float posY = clamp(y + rowHeight + 4.0f, 0.0f, trollHeight - boxH - 10.0f);

        Color bg = withAlphaScaled(GuiTheme.BACKGROUND, a);
        NanoVGHelper.drawRect(posX, posY, boxW, boxH, bg);
        if (GuiTheme.WINDOW_OUTLINE) {
            Color outline = withAlphaScaled(new Color(GuiTheme.PRIMARY.getRed(), GuiTheme.PRIMARY.getGreen(), GuiTheme.PRIMARY.getBlue(), 255), a);
            NanoVGHelper.drawRectOutline(posX, posY, boxW, boxH, 1.0f, outline);
        }

        float fontSize = 22.0f;
        int font = FontLoader.regular();
        float lineH = NanoVGHelper.getFontHeight(font, fontSize);
        Color text = withAlphaScaled(GuiTheme.TEXT, a);
        float ty = posY + 2.0f;
        for (String line : tooltipLines) {
            NanoVGHelper.drawString(line, posX + 2.0f, ty, font, fontSize, NVG_ALIGN_LEFT | NVG_ALIGN_TOP, text);
            ty += lineH;
        }
    }

    private void ensureTooltipLayout() {
        if (tooltipLines != null) return;

        String description = module.getDescription();
        float fontSize = 22.0f;
        int font = FontLoader.regular();
        float maxLineWidth = 169.0f;

        float spaceW = NanoVGHelper.getTextWidth(" ", font, fontSize);
        List<String> lines = new ArrayList<>();

        StringBuilder current = new StringBuilder();
        float lineW = -spaceW;
        for (String word : description.split(" ")) {
            if (word.isEmpty()) continue;
            float wordW = NanoVGHelper.getTextWidth(word, font, fontSize) + spaceW;
            float newW = lineW + wordW;
            if (newW > maxLineWidth && !current.isEmpty()) {
                lines.add(current.toString().trim());
                current.setLength(0);
                lineW = -spaceW + wordW;
            } else {
                lineW = newW;
            }
            current.append(word).append(' ');
        }
        if (!current.isEmpty()) lines.add(current.toString().trim());

        float w = 0.0f;
        for (String line : lines) {
            w = Math.max(w, NanoVGHelper.getTextWidth(line, font, fontSize));
        }

        float lineH = NanoVGHelper.getFontHeight(font, fontSize);
        tooltipLines = lines;
        tooltipTextWidth = w + 4.0f;
        tooltipTextHeight = lines.size() * lineH + 2.0f;
    }

    private static Color withAlphaScaled(Color base, float multiplier) {
        int a = (int) (base.getAlpha() * Easing.clamp01(multiplier));
        return new Color(base.getRed(), base.getGreen(), base.getBlue(), clamp255(a));
    }

    private static float clamp(float v, float min, float max) {
        if (v < min) return min;
        if (v > max) return max;
        return v;
    }

    private static Color mix(Color from, Color to, float t) {
        t = Easing.clamp01(t);
        int r = (int) (from.getRed() + (to.getRed() - from.getRed()) * t);
        int g = (int) (from.getGreen() + (to.getGreen() - from.getGreen()) * t);
        int b = (int) (from.getBlue() + (to.getBlue() - from.getBlue()) * t);
        int a = (int) (from.getAlpha() + (to.getAlpha() - from.getAlpha()) * t);
        return new Color(clamp255(r), clamp255(g), clamp255(b), clamp255(a));
    }

    private static int clamp255(int v) {
        if (v < 0) return 0;
        if (v > 255) return 255;
        return v;
    }

    private enum State {
        NONE,
        HOVER,
        CLICK
    }
}

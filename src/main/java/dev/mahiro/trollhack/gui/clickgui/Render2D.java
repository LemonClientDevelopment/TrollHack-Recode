package dev.mahiro.trollhack.gui.clickgui;

import net.minecraft.client.gui.DrawContext;

public final class Render2D {
    private Render2D() {
    }

    public static void fill(DrawContext context, float x, float y, float w, float h, int argb) {
        context.fill((int) x, (int) y, (int) (x + w), (int) (y + h), argb);
    }

    public static void outline(DrawContext context, float x, float y, float w, float h, int argb) {
        int x1 = (int) x;
        int y1 = (int) y;
        int x2 = (int) (x + w);
        int y2 = (int) (y + h);

        context.fill(x1, y1, x2, y1 + 1, argb);
        context.fill(x1, y2 - 1, x2, y2, argb);
        context.fill(x1, y1, x1 + 1, y2, argb);
        context.fill(x2 - 1, y1, x2, y2, argb);
    }

    public static int withAlpha(int argb, int alpha0To255) {
        int a = (alpha0To255 & 0xFF) << 24;
        return (argb & 0x00FFFFFF) | a;
    }
}


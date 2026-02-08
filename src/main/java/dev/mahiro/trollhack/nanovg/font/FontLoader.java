package dev.mahiro.trollhack.nanovg.font;

public final class FontLoader {
    private FontLoader() {
    }

    public static int regular() {
        return FontManager.fontWithCjkFallback("LexendDeca-Regular.ttf");
    }

    public static int bold() {
        return FontManager.fontWithCjkFallback("LexendDeca-Regular.ttf");
    }

    public static int medium() {
        return FontManager.fontWithCjkFallback("LexendDeca-Regular.ttf");
    }

    public static int cjk() {
        return FontManager.font("kuriyama.ttf");
    }
}

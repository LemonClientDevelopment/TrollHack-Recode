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

    public static int greycliffSemi() {
        return FontManager.fontWithCjkFallback("regular_semi.otf");
    }

    public static int solid() {
        return FontManager.font("solid.ttf");
    }

    public static int cjk() {
        return FontManager.font("kuriyama.ttf");
    }

    public static int comfortaa() {
        return FontManager.fontWithCjkFallback("Comfortaa.ttf");
    }
}

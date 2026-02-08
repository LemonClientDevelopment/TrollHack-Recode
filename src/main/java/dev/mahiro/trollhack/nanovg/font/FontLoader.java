package dev.mahiro.trollhack.nanovg.font;

public final class FontLoader {
    private FontLoader() {
    }

    public static int regular() {
        return FontManager.fontWithCjkFallback("regular.otf");
    }

    public static int bold() {
        return FontManager.fontWithCjkFallback("regular_bold.otf");
    }

    public static int medium() {
        return FontManager.fontWithCjkFallback("regular_medium.otf");
    }

    public static int greycliffSemi() {
        return FontManager.fontWithCjkFallback("regular_semi.otf");
    }

    public static int solid() {
        return FontManager.font("solid.ttf");
    }

    public static int icons() {
        return FontManager.font("woqubuzaoshuo.ttf");
    }

    public static int newIc() {
        return FontManager.font("icon.ttf");
    }

    public static int cjk() {
        return FontManager.font("kuriyama.ttf");
    }

    public static int comfortaa() {
        return FontManager.fontWithCjkFallback("Comfortaa.ttf");
    }
}


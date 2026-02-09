package dev.mahiro.trollhack.nanovg.font;

import dev.mahiro.trollhack.TrollHack;
import dev.mahiro.trollhack.nanovg.NanoVGRenderer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.lwjgl.nanovg.NanoVG.nvgAddFallbackFontId;
import static org.lwjgl.nanovg.NanoVG.nvgCreateFontMem;

public final class FontManager {
    private static final Map<String, Integer> fontCache = new HashMap<>();
    private static final Map<String, FontData> fontDataCache = new HashMap<>();
    private static final Set<String> fallbackRegistered = new HashSet<>();

    private FontManager() {
    }

    public static int font(String fontName) {
        return fontCache.computeIfAbsent(fontName, FontManager::loadFont);
    }

    public static int fontWithCjkFallback(String fontName) {
        int primaryFont = font(fontName);
        registerCjkFallback(fontName);
        return primaryFont;
    }

    private static void registerCjkFallback(String fontName) {
        String key = fontName + "-cjk";
        if (fallbackRegistered.contains(key)) return;

        long vg = NanoVGRenderer.INSTANCE.getContext();
        int cjkFont = FontLoader.cjk();
        int primaryFont = font(fontName);

        nvgAddFallbackFontId(vg, primaryFont, cjkFont);
        fallbackRegistered.add(key);
    }

    private static int loadFont(String fontName) {
        FontData fontData = fontDataCache.computeIfAbsent(fontName, FontManager::loadFontData);
        if (fontData == null) {
            throw new IllegalStateException("Failed to load font data: " + fontName);
        }

        long vg = NanoVGRenderer.INSTANCE.getContext();
        int fontId = nvgCreateFontMem(vg, fontName, fontData.buffer, false);
        if (fontId == -1) {
            throw new IllegalStateException("Failed to create font: " + fontName);
        }
        return fontId;
    }

    private static FontData loadFontData(String fontName) {
        try {
            InputStream is = openFontStream(fontName);
            if (is == null) return null;

            byte[] bytes = is.readAllBytes();
            ByteBuffer buffer = ByteBuffer.allocateDirect(bytes.length);
            buffer.put(bytes);
            buffer.flip();
            return new FontData(buffer);
        } catch (IOException e) {
            TrollHack.LOGGER.error("Failed to load font: {}", fontName, e);
            return null;
        }
    }

    private static InputStream openFontStream(String fontName) {
        String path = "/assets/sakura/fonts/" + fontName;
        InputStream is = FontManager.class.getResourceAsStream(path);
        if (is != null) return is;

        TrollHack.LOGGER.error("Missing font resource: {}", fontName);
        return null;
    }

    private record FontData(ByteBuffer buffer) {
    }
}

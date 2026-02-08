package dev.mahiro.trollhack.gui.clickgui;

import java.awt.*;

public final class GuiTheme {
    public static float SCALE_FACTOR = 2.0f;

    public static Color PRIMARY = new Color(255, 140, 180, 220);
    public static Color BACKGROUND = new Color(40, 32, 36, 160);
    public static Color TEXT = new Color(255, 250, 253, 255);

    public static float BACKGROUND_BLUR = 0.0f;
    public static float DARKNESS = 0.25f;
    public static float FADE_IN_TIME_SEC = 0.4f;
    public static float FADE_OUT_TIME_SEC = 0.4f;

    public static float WINDOW_X_MARGIN = 4.0f;
    public static float WINDOW_Y_MARGIN = 1.0f;

    public static boolean WINDOW_OUTLINE = true;
    public static boolean TITLE_BAR = false;

    public static int HOVER_ALPHA = 32;

    private GuiTheme() {
    }
}

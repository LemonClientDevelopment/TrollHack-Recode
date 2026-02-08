package dev.mahiro.trollhack.module.modules.client;

import dev.mahiro.trollhack.event.EventHandler;
import dev.mahiro.trollhack.event.events.ExampleEvent;
import dev.mahiro.trollhack.module.Category;
import dev.mahiro.trollhack.module.Module;
import dev.mahiro.trollhack.setting.*;

import java.awt.*;
import java.util.List;

public final class ExampleModule extends Module {
    private final BoolSetting enabledSetting = setting(new BoolSetting("Enabled Setting", true, null, "Example BoolSetting", false));
    private final IntSetting intSetting = setting(new IntSetting("Int", 10, 0, 100, 1, 1, enabledSetting::get, "Example IntSetting", false));
    private final FloatSetting floatSetting = setting(new FloatSetting("Float", 0.5f, 0.0f, 1.0f, 0.01f, 0.001f, enabledSetting::get, "Example FloatSetting", false));
    private final EnumSetting<ExampleMode> mode = setting(new EnumSetting<>("Mode", ExampleMode.NORMAL, null, "Example EnumSetting", false));
    private final StringSetting text = setting(new StringSetting("Text", "Hello", false, null, "Example StringSetting", false));
    private final ColorSetting color = setting(new ColorSetting("Color", new Color(255, 140, 180, 220), true, null, "Example ColorSetting", false));
    private final MultiBoolSetting multi = setting(new MultiBoolSetting("Multi", List.of(
            new BoolSetting("A", true),
            new BoolSetting("B", false),
            new BoolSetting("C", true)
    )));

    public ExampleModule() {
        super("Example", "Example module for verifying ModuleManager wiring", Category.CLIENT, false);
    }

    @EventHandler
    private void onExampleEvent(ExampleEvent event) {
    }

    public enum ExampleMode {
        NORMAL,
        FAST,
        SAFE
    }
}

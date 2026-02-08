package dev.mahiro.trollhack.module.modules.client;

import dev.mahiro.trollhack.event.EventHandler;
import dev.mahiro.trollhack.event.events.ExampleEvent;
import dev.mahiro.trollhack.module.Category;
import dev.mahiro.trollhack.module.Module;

public final class ExampleModule extends Module {
    public ExampleModule() {
        super("Example", "Example module for verifying ModuleManager wiring", Category.CLIENT, false);
    }

    @EventHandler
    private void onExampleEvent(ExampleEvent event) {
    }
}


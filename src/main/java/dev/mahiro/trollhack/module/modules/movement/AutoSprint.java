package dev.mahiro.trollhack.module.modules.movement;

import dev.mahiro.trollhack.event.EventHandler;
import dev.mahiro.trollhack.event.events.client.TickEvent;
import dev.mahiro.trollhack.module.Category;
import dev.mahiro.trollhack.module.Module;

public class AutoSprint extends Module {
    public AutoSprint() {
        super("AutoSprint", "IDK", Category.MOVEMENT, true);
    }

    @EventHandler
    private void onTick(TickEvent event) {
        mc.options.sprintKey.setPressed(true);
    }
}

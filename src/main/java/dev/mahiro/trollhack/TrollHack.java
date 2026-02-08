package dev.mahiro.trollhack;

import dev.mahiro.trollhack.event.EventBus;
import dev.mahiro.trollhack.event.IEventBus;
import dev.mahiro.trollhack.event.events.input.KeyPressEvent;
import dev.mahiro.trollhack.module.ModuleManager;
import dev.mahiro.trollhack.module.modules.client.ExampleModule;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

public class TrollHack implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("TrollHack");
    public static final IEventBus EVENT_BUS = new EventBus();
    public static final ModuleManager MODULE_MANAGER = new ModuleManager();
    private boolean rightShiftDown;

    @Override
    public void onInitializeClient() {
        EVENT_BUS.registerLambdaFactory(TrollHack.class.getPackageName(), (lookupInMethod, klass) -> (MethodHandles.Lookup) lookupInMethod.invoke(null, klass, MethodHandles.lookup()));

        MODULE_MANAGER.register(new ExampleModule());
        MODULE_MANAGER.load();

        EVENT_BUS.subscribe(MODULE_MANAGER);

        ClientTickEvents.END_CLIENT_TICK.register(this::onEndClientTick);
    }

    private void onEndClientTick(MinecraftClient client) {
        if (client == null || client.getWindow() == null) return;

        boolean down = InputUtil.isKeyPressed(client.getWindow(), InputUtil.GLFW_KEY_RIGHT_SHIFT);
        if (down && !rightShiftDown) {
            EVENT_BUS.post(new KeyPressEvent(InputUtil.GLFW_KEY_RIGHT_SHIFT));
        }
        rightShiftDown = down;
    }


}

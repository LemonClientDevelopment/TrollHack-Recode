package dev.mahiro.trollhack;

import dev.mahiro.trollhack.config.ConfigManager;
import dev.mahiro.trollhack.event.EventBus;
import dev.mahiro.trollhack.event.IEventBus;
import dev.mahiro.trollhack.module.ModuleManager;
import dev.mahiro.trollhack.module.modules.client.ExampleModule;
import dev.mahiro.trollhack.module.modules.client.GuiSettingModule;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

public class TrollHack implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("TrollHack");
    public static final IEventBus EVENT_BUS = new EventBus();
    public static final ModuleManager MODULES = new ModuleManager();

    @Override
    public void onInitializeClient() {
        EVENT_BUS.registerLambdaFactory(TrollHack.class.getPackageName(), (lookupInMethod, klass) -> (MethodHandles.Lookup) lookupInMethod.invoke(null, klass, MethodHandles.lookup()));

        Runtime.getRuntime().addShutdownHook(new Thread(ConfigManager::saveAll, "TrollHack-ConfigSaver"));

        MODULES.register(GuiSettingModule.INSTANCE);
        MODULES.register(new ExampleModule());
        MODULES.load();

        EVENT_BUS.subscribe(MODULES);

        ConfigManager.loadAll();
    }
}

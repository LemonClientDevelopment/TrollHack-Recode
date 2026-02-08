package dev.mahiro.trollhack;

import dev.mahiro.trollhack.config.ConfigManager;
import dev.mahiro.trollhack.event.EventBus;
import dev.mahiro.trollhack.event.IEventBus;
import dev.mahiro.trollhack.module.ModuleManager;
import dev.mahiro.trollhack.module.modules.client.ExampleModule;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

public class TrollHack implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("TrollHack");
    public static final IEventBus EVENT_BUS = new EventBus();
    public static final ModuleManager MODULE_MANAGER = new ModuleManager();

    @Override
    public void onInitializeClient() {
        EVENT_BUS.registerLambdaFactory(TrollHack.class.getPackageName(), (lookupInMethod, klass) -> (MethodHandles.Lookup) lookupInMethod.invoke(null, klass, MethodHandles.lookup()));

        Runtime.getRuntime().addShutdownHook(new Thread(ConfigManager::saveFromRuntime, "TrollHack-ConfigSaver"));

        MODULE_MANAGER.register(new ExampleModule());
        MODULE_MANAGER.load();

        EVENT_BUS.subscribe(MODULE_MANAGER);

        ConfigManager.loadAndApply();
    }
}

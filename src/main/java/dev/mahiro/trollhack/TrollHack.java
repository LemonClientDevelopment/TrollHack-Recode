package dev.mahiro.trollhack;

import dev.mahiro.trollhack.config.ConfigManager;
import dev.mahiro.trollhack.event.EventBus;
import dev.mahiro.trollhack.event.IEventBus;
import dev.mahiro.trollhack.module.ModuleManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

// todo: 回来吧luna5ama，我最骄傲的信仰TAT，历历在目的鬼手水晶，眼泪莫名在流淌😡😡，依稀记得2022，后面忘词了..

public class TrollHack implements ClientModInitializer {
    public static final String MOD_NAME = "TrollHack";
    public static final String MOD_VERSION = FabricLoader.getInstance().getModContainer("trollhack").orElseThrow().getMetadata().getVersion().getFriendlyString();

    public static final Logger LOGGER = LoggerFactory.getLogger("TrollHack");
    public static final IEventBus EVENT_BUS = new EventBus();
    public static ModuleManager MODULES;

    @Override
    public void onInitializeClient() {
        EVENT_BUS.registerLambdaFactory(TrollHack.class.getPackageName(), (lookupInMethod, klass) -> (MethodHandles.Lookup) lookupInMethod.invoke(null, klass, MethodHandles.lookup()));

        Runtime.getRuntime().addShutdownHook(new Thread(ConfigManager::saveAll));

        MODULES = new ModuleManager();

        ConfigManager.loadAll();
    }
}

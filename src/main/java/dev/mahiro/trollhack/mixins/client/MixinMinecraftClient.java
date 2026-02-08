package dev.mahiro.trollhack.mixins.client;

import dev.mahiro.trollhack.TrollHack;
import dev.mahiro.trollhack.event.EventType;
import dev.mahiro.trollhack.event.events.client.TickEvent;
import dev.mahiro.trollhack.gui.clickgui.GuiTheme;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public final class MixinMinecraftClient {
    @Inject(method = "tick", at = @At("HEAD"))
    private void onPreTick(CallbackInfo ci) {
        TrollHack.EVENT_BUS.post(new TickEvent(EventType.Pre));
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void onPostTick(CallbackInfo ci) {
        TrollHack.EVENT_BUS.post(new TickEvent(EventType.Post));
    }
}


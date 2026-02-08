package dev.mahiro.trollhack.mixins.render;

import dev.mahiro.trollhack.gui.clickgui.GuiTheme;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public final class MixinMinecraftClient {
    @Inject(method = "tick", at = @At("HEAD"))
    private void trollhack$tickGuiTheme(CallbackInfo ci) {
        GuiTheme.tick();
    }
}


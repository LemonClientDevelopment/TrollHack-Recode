package dev.mahiro.trollhack.mixins.render;

import dev.mahiro.trollhack.nanovg.NanoVGRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public final class MixinGameRenderer {
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;applyCursorTo(Lnet/minecraft/client/util/Window;)V"))
    private void trollhack$renderScreenNanoVgOnTop(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
        NanoVGRenderer.INSTANCE.flushScreenQueue();
    }
}


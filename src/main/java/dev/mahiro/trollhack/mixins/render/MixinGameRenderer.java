package dev.mahiro.trollhack.mixins.render;

import dev.mahiro.trollhack.TrollHack;
import dev.mahiro.trollhack.event.events.render.RenderWorldEvent;
import dev.mahiro.trollhack.nanovg.NanoVGRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.math.MatrixStack;
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
    
    @Inject(method = "renderWorld", at = @At("RETURN"))
    private void onRenderWorld(RenderTickCounter tickCounter, CallbackInfo ci) {
        MatrixStack matrices = new MatrixStack();
        TrollHack.EVENT_BUS.post(new RenderWorldEvent(matrices, 1.0f));
    }
}

package dev.mahiro.trollhack.mixins.render;

import dev.mahiro.trollhack.nanovg.NanoVGRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public final class MixinScreen {
    @Inject(method = "renderWithTooltip", at = @At("HEAD"))
    private void trollhack$beginNanoVgScreenBatch(DrawContext context, int mouseX, int mouseY, float deltaTicks, CallbackInfo ci) {
        NanoVGRenderer.INSTANCE.beginBatch();
    }

    @Inject(method = "renderWithTooltip", at = @At("RETURN"))
    private void trollhack$endNanoVgScreenBatch(DrawContext context, int mouseX, int mouseY, float deltaTicks, CallbackInfo ci) {
        NanoVGRenderer.INSTANCE.endBatch();
    }
}


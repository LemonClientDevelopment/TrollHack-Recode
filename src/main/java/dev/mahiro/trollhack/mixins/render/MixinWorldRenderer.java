package dev.mahiro.trollhack.mixins.render;

import dev.mahiro.trollhack.TrollHack;
import dev.mahiro.trollhack.event.events.render.RenderWorldEvent;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin for WorldRenderer to fire render world event
 */
@Mixin(WorldRenderer.class)
public class MixinWorldRenderer {
    
    @Shadow private BufferBuilderStorage bufferBuilders;
    
    @Inject(method = "render", at = @At("TAIL"))
    private void onRenderEnd(CallbackInfo ci) {
        try {
            MatrixStack matrices = new MatrixStack();
            RenderWorldEvent event = new RenderWorldEvent(matrices, 1.0f);
            event.setBufferBuilders(bufferBuilders);
            // Camera will be null, but we handle that in RenderUtil
            TrollHack.EVENT_BUS.post(event);
        } catch (Exception e) {
            // Silently fail
        }
    }
}

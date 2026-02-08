package dev.mahiro.trollhack.mixins.input;

import dev.mahiro.trollhack.TrollHack;
import dev.mahiro.trollhack.event.events.input.MouseButtonEvent;
import net.minecraft.client.Mouse;
import net.minecraft.client.input.MouseInput;
import net.minecraft.client.util.InputUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public final class MixinMouse {
    @Inject(method = "onMouseButton", at = @At("TAIL"))
    private void hookOnMouseButton(long window, MouseInput input, int action, CallbackInfo ci) {
        if (action != InputUtil.GLFW_PRESS && action != InputUtil.GLFW_RELEASE) return;
        TrollHack.EVENT_BUS.post(new MouseButtonEvent(input.button(), input.modifiers(), action == InputUtil.GLFW_PRESS));
    }
}

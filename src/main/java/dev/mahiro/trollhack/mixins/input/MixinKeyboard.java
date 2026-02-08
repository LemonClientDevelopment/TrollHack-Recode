package dev.mahiro.trollhack.mixins.input;

import dev.mahiro.trollhack.TrollHack;
import dev.mahiro.trollhack.event.events.input.KeyActionEvent;
import net.minecraft.client.Keyboard;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.util.InputUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public final class MixinKeyboard {
    @Inject(method = "onKey", at = @At("TAIL"))
    private void hookOnKey(long window, int action, KeyInput input, CallbackInfo ci) {
        if (action != InputUtil.GLFW_PRESS && action != InputUtil.GLFW_RELEASE) return;
        TrollHack.EVENT_BUS.post(new KeyActionEvent(input.key(), input.scancode(), input.modifiers(), action == InputUtil.GLFW_PRESS));
    }
}

package dev.mahiro.trollhack.mixins;

import dev.mahiro.trollhack.TrollHack;
import net.minecraft.client.util.Window;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(Window.class)
public class MixinWindow {
    @ModifyArg(method = "setTitle", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwSetWindowTitle(JLjava/lang/CharSequence;)V", remap = false), index = 1)
    private CharSequence setTitle(CharSequence title) {
        return TrollHack.MOD_NAME + " " + TrollHack.MOD_VERSION + " | " + title;
    }
}

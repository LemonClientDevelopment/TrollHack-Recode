package dev.mahiro.trollhack.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;

/**
 * Rendering utility class for Minecraft 1.21.11
 * Placeholder - rendering disabled due to API incompatibility
 */
public class RenderUtil {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    
    /**
     * Render a box outline at a block position
     * Currently disabled - needs proper implementation for 1.21.11
     */
    public static void drawBoxOutline(MatrixStack matrices, BlockPos pos, float red, float green, float blue, float alpha, Object camera, Object bufferBuilders) {
        // Rendering disabled - OpenGL immediate mode not supported in 1.21.11
        // TODO: Implement using modern rendering API
    }
    
    /**
     * Render a filled box at a block position
     * Currently disabled - needs proper implementation for 1.21.11
     */
    public static void drawBoxFilled(MatrixStack matrices, BlockPos pos, float red, float green, float blue, float alpha, Object camera, Object bufferBuilders) {
        // Rendering disabled - OpenGL immediate mode not supported in 1.21.11
        // TODO: Implement using modern rendering API
    }
}

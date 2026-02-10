package dev.mahiro.trollhack.event.events.render;

import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;

/**
 * Event fired when rendering the world
 */
public class RenderWorldEvent {
    private final MatrixStack matrices;
    private final float tickDelta;
    private BufferBuilderStorage bufferBuilders;
    private Camera camera;
    
    public RenderWorldEvent(MatrixStack matrices, float tickDelta) {
        this.matrices = matrices;
        this.tickDelta = tickDelta;
    }
    
    public MatrixStack getMatrices() {
        return matrices;
    }
    
    public float getTickDelta() {
        return tickDelta;
    }
    
    public BufferBuilderStorage getBufferBuilders() {
        return bufferBuilders;
    }
    
    public void setBufferBuilders(BufferBuilderStorage bufferBuilders) {
        this.bufferBuilders = bufferBuilders;
    }
    
    public Camera getCamera() {
        return camera;
    }
    
    public void setCamera(Camera camera) {
        this.camera = camera;
    }
}

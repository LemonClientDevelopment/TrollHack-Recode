package dev.mahiro.trollhack.event.events.render;

import net.minecraft.client.network.AbstractClientPlayerEntity;

/**
 * Event fired when rendering the player entity
 */
public class RenderPlayerEvent {
    private final AbstractClientPlayerEntity player;
    private final Phase phase;
    
    public RenderPlayerEvent(AbstractClientPlayerEntity player, Phase phase) {
        this.player = player;
        this.phase = phase;
    }
    
    public AbstractClientPlayerEntity getPlayer() {
        return player;
    }
    
    public Phase getPhase() {
        return phase;
    }
    
    public enum Phase {
        PRE,
        POST
    }
}

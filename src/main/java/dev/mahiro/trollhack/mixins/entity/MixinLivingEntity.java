package dev.mahiro.trollhack.mixins.entity;

import dev.mahiro.trollhack.module.modules.combat.AutoCrystal;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin for LivingEntity to completely separate first person view from third person rendering
 */
@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity {
    
    /**
     * Completely cancel head turning to prevent sync with player yaw
     */
    @Inject(method = "turnHead", at = @At("HEAD"), cancellable = true)
    private void onTurnHead(float headRotation, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        MinecraftClient mc = MinecraftClient.getInstance();
        
        // Only apply to client player when silent rotation is active
        if (entity instanceof ClientPlayerEntity && entity == mc.player) {
            if (AutoCrystal.INSTANCE != null && AutoCrystal.INSTANCE.isEnabled() && AutoCrystal.INSTANCE.hasServerRotation()) {
                // Cancel default behavior completely
                ci.cancel();
                
                // Force set to server rotation
                float serverYaw = AutoCrystal.INSTANCE.getServerYaw();
                entity.headYaw = serverYaw;
            }
        }
    }
    
    /**
     * Lock body and head yaw at the start of tick
     */
    @Inject(method = "tick", at = @At("HEAD"))
    private void onTickStart(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        MinecraftClient mc = MinecraftClient.getInstance();
        
        if (entity instanceof ClientPlayerEntity && entity == mc.player) {
            if (AutoCrystal.INSTANCE != null && AutoCrystal.INSTANCE.isEnabled() && AutoCrystal.INSTANCE.hasServerRotation()) {
                float serverYaw = AutoCrystal.INSTANCE.getServerYaw();
                entity.bodyYaw = serverYaw;
                entity.headYaw = serverYaw;
            }
        }
    }
    
    /**
     * Lock body and head yaw at the end of tick to override any changes
     */
    @Inject(method = "tick", at = @At("RETURN"))
    private void onTickEnd(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        MinecraftClient mc = MinecraftClient.getInstance();
        
        if (entity instanceof ClientPlayerEntity && entity == mc.player) {
            if (AutoCrystal.INSTANCE != null && AutoCrystal.INSTANCE.isEnabled() && AutoCrystal.INSTANCE.hasServerRotation()) {
                float serverYaw = AutoCrystal.INSTANCE.getServerYaw();
                entity.bodyYaw = serverYaw;
                entity.headYaw = serverYaw;
            }
        }
    }
    
    /**
     * Prevent body yaw from syncing during movement
     */
    @Inject(method = "tickMovement", at = @At("HEAD"))
    private void onTickMovementStart(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        MinecraftClient mc = MinecraftClient.getInstance();
        
        if (entity instanceof ClientPlayerEntity && entity == mc.player) {
            if (AutoCrystal.INSTANCE != null && AutoCrystal.INSTANCE.isEnabled() && AutoCrystal.INSTANCE.hasServerRotation()) {
                float serverYaw = AutoCrystal.INSTANCE.getServerYaw();
                entity.bodyYaw = serverYaw;
                entity.headYaw = serverYaw;
            }
        }
    }
    
    /**
     * Lock rotation after movement tick
     */
    @Inject(method = "tickMovement", at = @At("RETURN"))
    private void onTickMovementEnd(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        MinecraftClient mc = MinecraftClient.getInstance();
        
        if (entity instanceof ClientPlayerEntity && entity == mc.player) {
            if (AutoCrystal.INSTANCE != null && AutoCrystal.INSTANCE.isEnabled() && AutoCrystal.INSTANCE.hasServerRotation()) {
                float serverYaw = AutoCrystal.INSTANCE.getServerYaw();
                entity.bodyYaw = serverYaw;
                entity.headYaw = serverYaw;
            }
        }
    }
}

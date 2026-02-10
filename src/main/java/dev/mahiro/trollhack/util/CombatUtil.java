package dev.mahiro.trollhack.util;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

/**
 * Combat utility class
 */
public class CombatUtil {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    
    // For modifying block state in damage calculation
    public static BlockPos modifyPos = null;
    public static BlockState modifyBlockState = null;
    public static boolean terrainIgnore = false;
    
    /**
     * Get all enemy players within range
     */
    public static List<PlayerEntity> getEnemies(float range) {
        List<PlayerEntity> enemies = new ArrayList<>();
        
        if (mc.world == null || mc.player == null) {
            return enemies;
        }
        
        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player == mc.player) continue;
            if (player.isDead() || player.getHealth() <= 0) continue;
            if (mc.player.distanceTo(player) > range) continue;
            
            // TODO: Add friend check
            enemies.add(player);
        }
        
        return enemies;
    }
}

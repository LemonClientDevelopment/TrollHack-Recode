package dev.mahiro.trollhack.util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

/**
 * Block utility class
 */
public class BlockUtil {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    
    /**
     * Get block at position
     */
    public static Block getBlock(BlockPos pos) {
        if (mc.world == null) return Blocks.AIR;
        return mc.world.getBlockState(pos).getBlock();
    }
    
    /**
     * Get block state at position
     */
    public static BlockState getBlockState(BlockPos pos) {
        if (mc.world == null) return Blocks.AIR.getDefaultState();
        
        // Check if this position is being modified
        if (CombatUtil.modifyPos != null && pos.equals(CombatUtil.modifyPos)) {
            return CombatUtil.modifyBlockState;
        }
        
        return mc.world.getBlockState(pos);
    }
    
    /**
     * Check if block can be placed at position
     */
    public static boolean canPlace(BlockPos pos) {
        if (mc.world == null) return false;
        return mc.world.getBlockState(pos).isReplaceable();
    }
    
    /**
     * Get all crystals in box
     */
    public static List<EndCrystalEntity> getEndCrystals(Box box) {
        List<EndCrystalEntity> crystals = new ArrayList<>();
        
        if (mc.world == null) return crystals;
        
        for (Entity entity : mc.world.getOtherEntities(null, box)) {
            if (entity instanceof EndCrystalEntity crystal) {
                crystals.add(crystal);
            }
        }
        
        return crystals;
    }
    
    /**
     * Get entities in box
     */
    public static List<Entity> getEntities(Box box) {
        if (mc.world == null) return new ArrayList<>();
        return mc.world.getOtherEntities(null, box);
    }
    
    /**
     * Check if there's a crystal at position
     */
    public static boolean hasCrystal(BlockPos pos) {
        return !getEndCrystals(new Box(pos)).isEmpty();
    }
    
    /**
     * Get click side for block placement
     */
    public static Direction getClickSide(BlockPos pos) {
        for (Direction direction : Direction.values()) {
            BlockPos neighbor = pos.offset(direction);
            if (!mc.world.getBlockState(neighbor).isReplaceable()) {
                return direction.getOpposite();
            }
        }
        return Direction.UP;
    }
    
    /**
     * Get strict click side (only if visible)
     */
    public static Direction getClickSideStrict(BlockPos pos) {
        Direction side = getClickSide(pos);
        if (side == null) return null;
        
        Vec3d eyePos = mc.player.getEyePos();
        Vec3d targetPos = pos.toCenterPos().add(
            side.getVector().getX() * 0.5,
            side.getVector().getY() * 0.5,
            side.getVector().getZ() * 0.5
        );
        
        if (eyePos.distanceTo(targetPos) > 6.0) {
            return null;
        }
        
        return side;
    }
    
    /**
     * Get sphere of blocks around player
     */
    public static List<BlockPos> getSphere(float range) {
        List<BlockPos> sphere = new ArrayList<>();
        
        if (mc.player == null) return sphere;
        
        BlockPos center = mc.player.getBlockPos();
        int rangeInt = (int) Math.ceil(range);
        
        for (int x = -rangeInt; x <= rangeInt; x++) {
            for (int y = -rangeInt; y <= rangeInt; y++) {
                for (int z = -rangeInt; z <= rangeInt; z++) {
                    BlockPos pos = center.add(x, y, z);
                    if (mc.player.getBlockPos().toCenterPos().distanceTo(pos.toCenterPos()) <= range) {
                        sphere.add(pos);
                    }
                }
            }
        }
        
        return sphere;
    }
}

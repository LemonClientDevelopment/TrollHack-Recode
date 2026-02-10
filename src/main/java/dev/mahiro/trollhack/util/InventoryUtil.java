package dev.mahiro.trollhack.util;

import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * Inventory utility class
 */
public class InventoryUtil {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    
    /**
     * Find item in hotbar
     */
    public static int findItem(Item item) {
        if (mc.player == null) return -1;
        
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() == item) {
                return i;
            }
        }
        
        return -1;
    }
    
    /**
     * Find block in hotbar
     */
    public static int findBlock(Block block) {
        if (mc.player == null) return -1;
        
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (Block.getBlockFromItem(stack.getItem()) == block) {
                return i;
            }
        }
        
        return -1;
    }
    
    /**
     * Switch to slot
     */
    public static void switchToSlot(int slot) {
        if (mc.player == null) return;
        if (slot < 0 || slot > 8) return;
        
        mc.player.getInventory().selectedSlot = slot;
    }
}

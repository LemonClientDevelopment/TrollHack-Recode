package dev.mahiro.trollhack.util;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

/**
 * Utility class for damage calculations
 */
public class DamageUtils {
    
    /**
     * Calculate attack damage from attacker to target
     */
    public static float getAttackDamage(LivingEntity attacker, LivingEntity target) {
        // Base damage from attack attribute
        float damage = (float) attacker.getAttributeValue(EntityAttributes.ATTACK_DAMAGE);
        
        // Simplified - just use base damage with armor reduction
        damage = damage * (1.0f - Math.min(20.0f, target.getArmor()) / 25.0f);
        
        return Math.max(0, damage);
    }
    
    /**
     * Calculate explosion damage
     */
    public static float getExplosionDamage(double x, double y, double z, LivingEntity entity, float power) {
        double distance = Math.sqrt(
            entity.squaredDistanceTo(x, y, z)
        );
        
        if (distance > power) return 0;
        
        // Simplified explosion damage calculation
        double exposure = 1.0;
        double impact = (1.0 - distance / power) * exposure;
        float damage = (float) ((impact * impact + impact) / 2.0 * 7.0 * power + 1.0);
        
        // Apply armor reduction
        damage = damage * (1.0f - Math.min(20.0f, entity.getArmor()) / 25.0f);
        
        return Math.max(0, damage);
    }
}

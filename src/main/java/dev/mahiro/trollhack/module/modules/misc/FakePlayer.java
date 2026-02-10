package dev.mahiro.trollhack.module.modules.misc;

import com.mojang.authlib.GameProfile;
import dev.mahiro.trollhack.TrollHack;
import dev.mahiro.trollhack.event.EventHandler;
import dev.mahiro.trollhack.event.EventType;
import dev.mahiro.trollhack.event.events.client.TickEvent;
import dev.mahiro.trollhack.event.events.network.PacketEvent;
import dev.mahiro.trollhack.module.Category;
import dev.mahiro.trollhack.module.Module;
import dev.mahiro.trollhack.module.modules.combat.AutoCrystal;
import dev.mahiro.trollhack.setting.BoolSetting;
import dev.mahiro.trollhack.setting.StringSetting;
import dev.mahiro.trollhack.util.DamageUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * FakePlayer - Creates a fake player entity for testing
 * Complete implementation based on teach/fkplayer.java
 */
public class FakePlayer extends Module {
    public static FakePlayer INSTANCE;
    public static FakePlayerEntity fakePlayer;
    
    private final StringSetting name = setting(new StringSetting("Name", "FakePlayer", false, null, "Fake player name", false));
    private final BoolSetting damage = setting(new BoolSetting("Damage", true, null, "Simulate damage from explosions", false));
    private final BoolSetting autoTotem = setting(new BoolSetting("AutoTotem", true, null, "Auto equip totems", false));
    private final BoolSetting record = setting(new BoolSetting("Record", false, null, "Record player movements", false));
    private final BoolSetting play = setting(new BoolSetting("Play", false, null, "Replay recorded movements", false));
    private final BoolSetting debug = setting(new BoolSetting("Debug", false, null, "Show debug messages", false));
    
    private final List<PlayerState> positions = new ArrayList<>();
    private int movementTick = 0;
    private boolean lastRecordValue = false;
    
    public FakePlayer() {
        super("FakePlayer", "Creates a fake player entity for testing", Category.MISC, false);
        INSTANCE = this;
    }
    
    @Override
    protected void onEnable() {
        if (mc == null || mc.player == null || mc.world == null) {
            setEnabled(false);
            return;
        }
        
        fakePlayer = new FakePlayerEntity(mc.player, name.get());
        mc.world.addEntity(fakePlayer);
    }
    
    @Override
    protected void onDisable() {
        if (fakePlayer == null) return;
        
        fakePlayer.setRemoved(Entity.RemovalReason.KILLED);
        fakePlayer.onRemoved();
        fakePlayer = null;
        
        positions.clear();
        movementTick = 0;
        lastRecordValue = false;
    }
    
    @EventHandler
    private void onTick(TickEvent event) {
        if (event.getType() != EventType.Pre) return;
        if (fakePlayer == null) {
            setEnabled(false);
            return;
        }
        
        // Auto totem
        if (autoTotem.get()) {
            if (fakePlayer.getOffHandStack().getItem() != Items.TOTEM_OF_UNDYING) {
                fakePlayer.setStackInHand(Hand.OFF_HAND, new ItemStack(Items.TOTEM_OF_UNDYING));
            }
            if (fakePlayer.getMainHandStack().getItem() != Items.TOTEM_OF_UNDYING) {
                fakePlayer.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.TOTEM_OF_UNDYING));
            }
        }
        
        // Record movements
        if (record.get() != lastRecordValue && record.get()) {
            positions.clear();
        }
        lastRecordValue = record.get();
        
        if (record.get()) {
            positions.add(new PlayerState(
                mc.player.getX(), 
                mc.player.getY(), 
                mc.player.getZ(), 
                mc.player.getYaw(), 
                mc.player.getPitch()
            ));
        }
        
        // Play movements
        if (play.get() && !positions.isEmpty()) {
            movementTick++;
            if (movementTick >= positions.size()) {
                movementTick = 0;
            }
            
            PlayerState state = positions.get(movementTick);
            fakePlayer.setYaw(state.yaw);
            fakePlayer.setPitch(state.pitch);
            fakePlayer.setHeadYaw(state.yaw);
            fakePlayer.updateTrackedPosition(state.x, state.y, state.z);
            fakePlayer.updateTrackedPositionAndAngles(new Vec3d(state.x, state.y, state.z), state.yaw, state.pitch);
        }
    }
    
    @EventHandler
    private void onPacketSend(PacketEvent.Send event) {
        if (fakePlayer == null) return;
        
        Packet<?> packet = event.getPacket();
        if (packet instanceof PlayerInteractEntityC2SPacket interactPacket) {
            try {
                // Use reflection to access private fields
                Field entityIdField = interactPacket.getClass().getDeclaredField("entityId");
                entityIdField.setAccessible(true);
                int entityId = (int) entityIdField.get(interactPacket);
                
                // Check if attacking fake player
                Entity target = mc.world.getEntityById(entityId);
                if (target == fakePlayer) {
                    // Play hurt sound
                    mc.world.playSound(
                        mc.player,
                        fakePlayer.getX(),
                        fakePlayer.getY(),
                        fakePlayer.getZ(),
                        SoundEvents.ENTITY_PLAYER_HURT,
                        SoundCategory.PLAYERS,
                        1.0f,
                        1.0f
                    );
                    
                    // Calculate and apply damage
                    if (fakePlayer.hurtTime <= 0) {
                        float attackDamage = DamageUtils.getAttackDamage(mc.player, fakePlayer);
                        
                        // Check for critical hit
                        if (isCriticalHit()) {
                            attackDamage *= 1.5f;
                            mc.world.playSound(
                                mc.player,
                                fakePlayer.getX(),
                                fakePlayer.getY(),
                                fakePlayer.getZ(),
                                SoundEvents.ENTITY_PLAYER_ATTACK_CRIT,
                                SoundCategory.PLAYERS,
                                1.0f,
                                1.0f
                            );
                            mc.player.addCritParticles(fakePlayer);
                        }
                        
                        applyDamage(attackDamage);
                    }
                }
            } catch (Exception e) {
                // Ignore reflection errors
            }
        }
    }
    
    @EventHandler
    private void onPacketReceive(PacketEvent.Receive event) {
        if (!damage.get() || fakePlayer == null) return;
        
        Packet<?> packet = event.getPacket();
        if (packet instanceof ExplosionS2CPacket explosion) {
            if (debug.get()) {
                TrollHack.LOGGER.info("Received explosion packet");
            }
            
            // Schedule damage calculation on main thread to avoid OffThreadException
            mc.execute(() -> {
                try {
                    if (fakePlayer == null) return;
                    
                    // Try different possible field names for explosion position
                    double explosionX = 0, explosionY = 0, explosionZ = 0;
                    boolean foundPosition = false;
                    
                    // Try to find x, y, z fields
                    for (Field field : explosion.getClass().getDeclaredFields()) {
                        field.setAccessible(true);
                        String fieldName = field.getName();
                        
                        if (fieldName.equals("x") || fieldName.contains("X") && fieldName.length() <= 3) {
                            explosionX = (double) field.get(explosion);
                            foundPosition = true;
                        } else if (fieldName.equals("y") || fieldName.contains("Y") && fieldName.length() <= 3) {
                            explosionY = (double) field.get(explosion);
                        } else if (fieldName.equals("z") || fieldName.contains("Z") && fieldName.length() <= 3) {
                            explosionZ = (double) field.get(explosion);
                        }
                    }
                    
                    if (!foundPosition) {
                        // Fallback: use fake player position
                        explosionX = fakePlayer.getX();
                        explosionY = fakePlayer.getY();
                        explosionZ = fakePlayer.getZ();
                        
                        if (debug.get()) {
                            TrollHack.LOGGER.info("Could not find explosion position, using fake player position");
                        }
                    }
                    
                    Vec3d explosionPos = new Vec3d(explosionX, explosionY, explosionZ);
                    
                    // Check distance
                    double distance = Math.sqrt(explosionPos.squaredDistanceTo(fakePlayer.getBlockPos().toCenterPos()));
                    
                    if (debug.get()) {
                        TrollHack.LOGGER.info("Explosion at: " + explosionPos + ", distance: " + distance);
                    }
                    
                    if (distance > 12.0) {
                        if (debug.get()) {
                            TrollHack.LOGGER.info("Explosion too far away");
                        }
                        return;
                    }
                    
                    // Only apply damage if not in hurt cooldown
                    if (fakePlayer.hurtTime <= 0) {
                        // Calculate explosion damage
                        float explosionDamage = calculateCrystalDamage(explosionPos);
                        
                        if (debug.get()) {
                            TrollHack.LOGGER.info("Calculated damage: " + explosionDamage);
                        }
                        
                        if (explosionDamage > 0) {
                            applyDamage(explosionDamage);
                        }
                    } else {
                        if (debug.get()) {
                            TrollHack.LOGGER.info("Fake player in hurt cooldown: " + fakePlayer.hurtTime);
                        }
                    }
                } catch (Exception e) {
                    // Log error for debugging
                    TrollHack.LOGGER.error("Error processing explosion packet: " + e.getMessage());
                }
            });
        }
    }
    
    private boolean isCriticalHit() {
        return mc.player.fallDistance > 0.0f 
            && !mc.player.isOnGround() 
            && !mc.player.isClimbing() 
            && !mc.player.isTouchingWater() 
            && !mc.player.hasStatusEffect(StatusEffects.BLINDNESS) 
            && !mc.player.hasVehicle();
    }
    
    private void applyDamage(float damage) {
        if (damage <= 0) return;
        
        fakePlayer.onDamaged(mc.world.getDamageSources().generic());
        
        // Play hurt sound
        mc.world.playSound(
            null,
            fakePlayer.getX(),
            fakePlayer.getY(),
            fakePlayer.getZ(),
            SoundEvents.ENTITY_PLAYER_HURT,
            SoundCategory.PLAYERS,
            1.0f,
            1.0f
        );
        
        if (fakePlayer.getAbsorptionAmount() >= damage) {
            fakePlayer.setAbsorptionAmount(fakePlayer.getAbsorptionAmount() - damage);
        } else {
            float remainingDamage = damage - fakePlayer.getAbsorptionAmount();
            fakePlayer.setAbsorptionAmount(0.0f);
            fakePlayer.setHealth(fakePlayer.getHealth() - remainingDamage);
        }
        
        // Handle totem pop
        if (fakePlayer.isDead() || fakePlayer.getHealth() <= 0) {
            // Try to use totem
            if (hasTotem()) {
                fakePlayer.setHealth(1.0f);
                fakePlayer.clearStatusEffects();
                fakePlayer.setAbsorptionAmount(8.0f);
                
                // Play totem animation
                new EntityStatusS2CPacket(fakePlayer, (byte) 35).apply(mc.getNetworkHandler());
                
                // Play totem sound
                mc.world.playSound(
                    null,
                    fakePlayer.getX(),
                    fakePlayer.getY(),
                    fakePlayer.getZ(),
                    SoundEvents.ITEM_TOTEM_USE,
                    SoundCategory.PLAYERS,
                    1.0f,
                    1.0f
                );
            }
        }
    }
    
    private boolean hasTotem() {
        return fakePlayer.getOffHandStack().getItem() == Items.TOTEM_OF_UNDYING ||
               fakePlayer.getMainHandStack().getItem() == Items.TOTEM_OF_UNDYING;
    }
    
    private float calculateCrystalDamage(Vec3d crystalPos) {
        double distance = fakePlayer.getBlockPos().toCenterPos().distanceTo(crystalPos);
        if (distance > 12.0) return 0;
        
        // Simplified crystal damage calculation
        double exposure = 1.0;
        double impact = (1.0 - distance / 12.0) * exposure;
        float damage = (float) ((impact * impact + impact) / 2.0 * 7.0 * 12.0 + 1.0);
        
        // Apply armor reduction
        damage = damage * (1.0f - Math.min(20.0f, fakePlayer.getArmor()) / 25.0f);
        
        return Math.max(0, damage);
    }
    
    /**
     * Fake player entity class
     */
    public static class FakePlayerEntity extends OtherClientPlayerEntity {
        private final boolean savedOnGround;
        
        public FakePlayerEntity(PlayerEntity player, String name) {
            super(MinecraftClient.getInstance().world, new GameProfile(UUID.fromString("66666666-6666-6666-6666-666666666666"), name));
            
            // Copy position and rotation
            this.copyPositionAndRotation(player);
            this.bodyYaw = player.bodyYaw;
            this.headYaw = player.headYaw;
            
            // Copy animation state
            this.handSwingProgress = player.handSwingProgress;
            this.handSwingTicks = player.handSwingTicks;
            this.limbAnimator.setSpeed(player.limbAnimator.getSpeed());
            
            // Copy state
            this.touchingWater = player.isTouchingWater();
            this.setSneaking(player.isSneaking());
            this.setPose(player.getPose());
            this.savedOnGround = player.isOnGround();
            this.setOnGround(this.savedOnGround);
            
            // Copy inventory
            this.getInventory().clone(player.getInventory());
            
            // Copy health
            this.setAbsorptionAmount(player.getAbsorptionAmount());
            this.setHealth(player.getHealth());
            
            // Copy bounding box
            this.setBoundingBox(player.getBoundingBox());
        }
        
        @Override
        public boolean isOnGround() {
            return this.savedOnGround;
        }
        
        @Override
        public boolean isSpectator() {
            return false;
        }
        
        @Override
        public boolean isCreative() {
            return false;
        }
    }
    
    /**
     * Player state for recording/playback
     */
    private record PlayerState(double x, double y, double z, float yaw, float pitch) {
    }
}

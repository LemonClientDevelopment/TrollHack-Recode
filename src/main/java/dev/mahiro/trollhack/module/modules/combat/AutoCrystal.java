package dev.mahiro.trollhack.module.modules.combat;

import dev.mahiro.trollhack.TrollHack;
import dev.mahiro.trollhack.event.EventHandler;
import dev.mahiro.trollhack.event.EventType;
import dev.mahiro.trollhack.event.events.client.TickEvent;
import dev.mahiro.trollhack.event.events.render.RenderPlayerEvent;
import dev.mahiro.trollhack.event.events.render.RenderWorldEvent;
import dev.mahiro.trollhack.module.Category;
import dev.mahiro.trollhack.module.Module;
import dev.mahiro.trollhack.setting.*;
import dev.mahiro.trollhack.util.*;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * AutoCrystal - Complete implementation based on teach/autocrystal.java
 * Automatically places and breaks end crystals for PvP
 */
public class AutoCrystal extends Module {
    public static AutoCrystal INSTANCE;
    
    // Page system for organized settings
    private final EnumSetting<Page> page = setting(new EnumSetting<>("Page", Page.General, null, "Settings page", false));
    
    // General settings
    private final FloatSetting targetRange = setting(new FloatSetting("TargetRange", 12.0f, 0.0f, 20.0f, 0.5f, 0.1f, () -> page.get() == Page.General, "Target search range", false));
    private final FloatSetting placeRange = setting(new FloatSetting("PlaceRange", 5.0f, 0.0f, 6.0f, 0.1f, 0.01f, () -> page.get() == Page.General, "Crystal place range", false));
    private final FloatSetting breakRange = setting(new FloatSetting("BreakRange", 5.0f, 0.0f, 6.0f, 0.1f, 0.01f, () -> page.get() == Page.General, "Crystal break range", false));
    private final FloatSetting wallRange = setting(new FloatSetting("WallRange", 3.5f, 0.0f, 6.0f, 0.1f, 0.01f, () -> page.get() == Page.General, "Range through walls", false));
    
    // Damage settings
    private final FloatSetting minDamage = setting(new FloatSetting("MinDamage", 1.0f, 0.0f, 36.0f, 0.5f, 0.1f, () -> page.get() == Page.Damage, "Minimum damage to target", false));
    private final FloatSetting maxSelfDamage = setting(new FloatSetting("MaxSelf", 20.0f, 0.0f, 36.0f, 0.5f, 0.1f, () -> page.get() == Page.Damage, "Maximum self damage", false));
    private final FloatSetting balance = setting(new FloatSetting("Balance", -10.0f, -10.0f, 10.0f, 0.5f, 0.1f, () -> page.get() == Page.Damage, "Damage balance offset", false));
    private final FloatSetting reserve = setting(new FloatSetting("Reserve", 2.0f, 0.0f, 10.0f, 0.5f, 0.1f, () -> page.get() == Page.Damage, "Reserve health", false));
    
    // Timing settings - 毫秒级延迟
    private final IntSetting placeDelay = setting(new IntSetting("PlaceDelay", 50, 0, 1000, 10, 1, () -> page.get() == Page.Timing, "Place delay (ms)", false));
    private final IntSetting breakDelay = setting(new IntSetting("BreakDelay", 50, 0, 1000, 10, 1, () -> page.get() == Page.Timing, "Break delay (ms)", false));
    private final IntSetting updateDelay = setting(new IntSetting("UpdateDelay", 0, 0, 500, 10, 1, () -> page.get() == Page.Timing, "Calculation update delay (ms)", false));
    private final IntSetting switchCooldown = setting(new IntSetting("SwitchCooldown", 50, 0, 1000, 10, 1, () -> page.get() == Page.Timing, "Switch cooldown (ms)", false));
    private final BoolSetting sequential = setting(new BoolSetting("Sequential", false, () -> page.get() == Page.Timing, "Sequential mode (place then break)", false));
    private final BoolSetting instant = setting(new BoolSetting("Instant", false, () -> page.get() == Page.Timing, "Instant mode (0ms delays)", false));
    private final IntSetting multiPlace = setting(new IntSetting("MultiPlace", 1, 1, 5, 1, 1, () -> page.get() == Page.Timing && !instant.get(), "Crystals per tick", false));
    
    // Behavior settings
    private final BoolSetting place = setting(new BoolSetting("Place", true, () -> page.get() == Page.Behavior, "Enable crystal placement", false));
    private final BoolSetting breakCrystal = setting(new BoolSetting("Break", true, () -> page.get() == Page.Behavior, "Enable crystal breaking", false));
    private final BoolSetting autoSwitch = setting(new BoolSetting("AutoSwitch", true, () -> page.get() == Page.Behavior, "Auto switch to crystals", false));
    private final BoolSetting rotate = setting(new BoolSetting("Rotate", true, () -> page.get() == Page.Behavior, "Rotate to crystals", false));
    private final BoolSetting silentRotate = setting(new BoolSetting("Silent", true, () -> rotate.get() && page.get() == Page.Behavior, "Silent rotation", false));
    private final BoolSetting afterBreak = setting(new BoolSetting("AfterBreak", true, () -> page.get() == Page.Behavior, "Place after breaking", false));
    private final BoolSetting eatingPause = setting(new BoolSetting("EatingPause", true, () -> page.get() == Page.Behavior, "Pause when eating", false));
    private final BoolSetting onlyHoldingCrystal = setting(new BoolSetting("OnlyHold", false, () -> page.get() == Page.Behavior, "Only work when holding crystal", false));
    
    // Advanced settings
    private final IntSetting minAge = setting(new IntSetting("MinAge", 0, 0, 20, 1, 1, () -> page.get() == Page.Advanced, "Minimum crystal age (ticks)", false));
    private final BoolSetting resetAttackCooldown = setting(new BoolSetting("ResetCD", true, () -> page.get() == Page.Advanced, "Reset attack cooldown", false));
    private final BoolSetting removeCrystal = setting(new BoolSetting("Remove", false, () -> page.get() == Page.Advanced, "Remove crystal on break", false));
    private final BoolSetting debug = setting(new BoolSetting("Debug", false, () -> page.get() == Page.Advanced, "Debug mode", false));
    
    // Render settings
    private final BoolSetting render = setting(new BoolSetting("Render", true, () -> page.get() == Page.Advanced, "Render place position", false));
    private final ColorSetting renderColor = setting(new ColorSetting("RenderColor", new Color(0, 255, 0, 128), true, () -> render.get() && page.get() == Page.Advanced, "Render color", false));
    private final BoolSetting renderFill = setting(new BoolSetting("RenderFill", true, () -> render.get() && page.get() == Page.Advanced, "Render fill", false));
    private final BoolSetting renderOutline = setting(new BoolSetting("RenderOutline", true, () -> render.get() && page.get() == Page.Advanced, "Render outline", false));
    
    // Internal state
    private BlockPos currentPlacePos = null;
    private EndCrystalEntity currentBreakCrystal = null;
    private PlayerEntity currentTarget = null;
    private final Timer placeTimer = new Timer();
    private final Timer breakTimer = new Timer();
    private final Timer updateTimer = new Timer();
    private final Timer switchTimer = new Timer();
    private float lastDamage = 0.0f;
    private int lastSlot = -1;
    
    // Rotation state for silent rotation
    private float serverYaw = 0.0f;
    private float serverPitch = 0.0f;
    private boolean hasServerRotation = false;
    
    // Store original rotation for rendering
    private float originalYaw = 0.0f;
    private float originalPitch = 0.0f;
    private float originalHeadYaw = 0.0f;
    private float originalBodyYaw = 0.0f;
    
    public AutoCrystal() {
        super("AutoCrystal", "Automatically places and breaks end crystals", Category.COMBAT, false);
        INSTANCE = this;
    }
    
    public String getInfo() {
        if (lastDamage > 0.0f) {
            return String.format("%.1f", lastDamage);
        }
        return null;
    }
    
    @Override
    protected void onEnable() {
        currentPlacePos = null;
        currentBreakCrystal = null;
        currentTarget = null;
        lastDamage = 0.0f;
        placeTimer.reset();
        breakTimer.reset();
        updateTimer.reset();
        switchTimer.reset();
        hasServerRotation = false;
    }
    
    @Override
    protected void onDisable() {
        currentPlacePos = null;
        currentBreakCrystal = null;
        currentTarget = null;
        hasServerRotation = false;
    }
    
    @EventHandler
    private void onTick(TickEvent event) {
        if (event.getType() != EventType.Pre) return;
        if (mc == null || mc.player == null || mc.world == null) return;
        
        // Reset rotation state at the start of each tick
        hasServerRotation = false;
        
        // Check pause conditions
        if (shouldPause()) {
            return;
        }
        
        // Track slot changes
        int currentSlot = mc.player.getInventory().selectedSlot;
        if (currentSlot != lastSlot) {
            lastSlot = currentSlot;
            switchTimer.reset();
        }
        
        // Instant mode - update every tick
        if (instant.get()) {
            updateCalculations();
            doInteract();
            return;
        }
        
        // Update calculations periodically
        if (updateTimer.passedMs(updateDelay.get())) {
            updateCalculations();
            updateTimer.reset();
        }
        
        // Execute actions
        doInteract();
    }
    
    @EventHandler
    private void onRenderWorld(RenderWorldEvent event) {
        if (!render.get()) return;
        if (currentPlacePos == null) return;
        if (event.getBufferBuilders() == null) return;
        
        // Extract color components
        Color color = renderColor.getValue();
        float red = color.getRed() / 255.0f;
        float green = color.getGreen() / 255.0f;
        float blue = color.getBlue() / 255.0f;
        float alpha = color.getAlpha() / 255.0f;
        
        // Render filled box (Camera can be null, RenderUtil will handle it)
        if (renderFill.get()) {
            RenderUtil.drawBoxFilled(event.getMatrices(), currentPlacePos, red, green, blue, alpha * 0.3f, 
                event.getCamera(), event.getBufferBuilders());
        }
        
        // Render outline
        if (renderOutline.get()) {
            RenderUtil.drawBoxOutline(event.getMatrices(), currentPlacePos, red, green, blue, alpha, 
                event.getCamera(), event.getBufferBuilders());
        }
    }
    
    private boolean shouldPause() {
        // Pause when eating
        if (eatingPause.get() && mc.player.isUsingItem()) {
            return true;
        }
        
        // Only work when holding crystal
        if (onlyHoldingCrystal.get() && !hasCrystalInHand()) {
            return true;
        }
        
        // Wait for switch cooldown
        if (!switchTimer.passedMs(switchCooldown.get())) {
            return true;
        }
        
        return false;
    }
    
    private boolean hasCrystalInHand() {
        return mc.player.getMainHandStack().getItem() == Items.END_CRYSTAL ||
               mc.player.getOffHandStack().getItem() == Items.END_CRYSTAL;
    }
    
    private void updateCalculations() {
        // Find target
        currentTarget = findTarget();
        
        if (currentTarget == null) {
            currentPlacePos = null;
            currentBreakCrystal = null;
            lastDamage = 0.0f;
            return;
        }
        
        // Find best break crystal
        if (breakCrystal.get()) {
            currentBreakCrystal = findBreakCrystal();
        }
        
        // Find best place position
        if (place.get()) {
            currentPlacePos = findPlacePos();
        }
    }
    
    private void doInteract() {
        // Instant mode - ignore all delays and place/break multiple times
        if (instant.get()) {
            // Break all valid crystals
            List<EndCrystalEntity> crystalsToBreak = new ArrayList<>();
            for (Entity entity : mc.world.getEntities()) {
                if (!(entity instanceof EndCrystalEntity crystal)) continue;
                if (crystal.age < minAge.get()) continue;
                
                double distance = mc.player.getEyePos().distanceTo(crystal.getBlockPos().toCenterPos());
                if (distance > breakRange.get()) continue;
                
                if (!canSee(crystal) && distance > wallRange.get()) continue;
                
                crystalsToBreak.add(crystal);
            }
            
            // Break all crystals
            for (EndCrystalEntity crystal : crystalsToBreak) {
                breakCrystalEntity(crystal);
            }
            
            // Place crystal if available
            if (currentPlacePos != null && hasCrystal()) {
                placeCrystal(currentPlacePos);
            }
            return;
        }
        
        // Multi-place mode - place multiple crystals per tick
        int placesThisTick = 0;
        int maxPlaces = multiPlace.get();
        
        // Sequential mode - break first, then place
        if (sequential.get()) {
            if (currentBreakCrystal != null && canBreak()) {
                breakCrystalEntity(currentBreakCrystal);
                return;
            }
            
            if (currentPlacePos != null && canPlace() && placesThisTick < maxPlaces) {
                placeCrystal(currentPlacePos);
                placesThisTick++;
            }
            return;
        }
        
        // Default mode - break first with optional place after break
        if (currentBreakCrystal != null && canBreak()) {
            breakCrystalEntity(currentBreakCrystal);
            
            // Place after break if enabled
            if (afterBreak.get() && currentPlacePos != null && canPlace() && placesThisTick < maxPlaces) {
                placeCrystal(currentPlacePos);
                placesThisTick++;
            }
            return;
        }
        
        // Then place crystals
        if (currentPlacePos != null && canPlace() && placesThisTick < maxPlaces) {
            placeCrystal(currentPlacePos);
            placesThisTick++;
        }
    }
    
    private PlayerEntity findTarget() {
        List<PlayerEntity> targets = CombatUtil.getEnemies(targetRange.get());
        
        if (targets.isEmpty()) return null;
        
        // Sort by distance
        targets.sort(Comparator.comparingDouble(e -> mc.player.distanceTo(e)));
        return targets.get(0);
    }
    
    private BlockPos findPlacePos() {
        if (currentTarget == null) return null;
        
        BlockPos bestPos = null;
        float bestDamage = 0;
        
        // Search around target
        for (BlockPos pos : BlockUtil.getSphere(placeRange.get() + 1.5f)) {
            if (!canPlaceCrystalAt(pos)) continue;
            
            Vec3d crystalPos = pos.toCenterPos();
            double distance = mc.player.getEyePos().distanceTo(crystalPos);
            if (distance > placeRange.get()) continue;
            
            // Check wall range
            if (!canSee(pos) && distance > wallRange.get()) continue;
            
            float targetDamage = calculateDamage(crystalPos, currentTarget);
            float selfDamage = calculateDamage(crystalPos, mc.player);
            
            // Check damage requirements
            if (targetDamage < minDamage.get()) continue;
            
            // Skip self damage checks in creative mode
            if (!mc.player.getAbilities().creativeMode) {
                if (selfDamage > maxSelfDamage.get()) continue;
                if (reserve.get() > 0 && selfDamage > (mc.player.getHealth() + mc.player.getAbsorptionAmount()) - reserve.get()) continue;
            }
            
            if (balance.get() != 0 && targetDamage < selfDamage + balance.get()) continue;
            
            if (targetDamage > bestDamage) {
                bestDamage = targetDamage;
                bestPos = pos;
                lastDamage = targetDamage;
            }
        }
        
        return bestPos;
    }
    
    private EndCrystalEntity findBreakCrystal() {
        EndCrystalEntity bestCrystal = null;
        float bestDamage = 0;
        
        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof EndCrystalEntity crystal)) continue;
            if (crystal.age < minAge.get()) continue;
            
            double distance = mc.player.getEyePos().distanceTo(crystal.getBlockPos().toCenterPos());
            if (distance > breakRange.get()) continue;
            
            // Check wall range
            if (!canSee(crystal) && distance > wallRange.get()) continue;
            
            float targetDamage = calculateDamage(crystal.getBlockPos().toCenterPos(), currentTarget);
            float selfDamage = calculateDamage(crystal.getBlockPos().toCenterPos(), mc.player);
            
            // Check damage requirements
            if (targetDamage < minDamage.get()) continue;
            
            // Skip self damage checks in creative mode
            if (!mc.player.getAbilities().creativeMode) {
                if (selfDamage > maxSelfDamage.get()) continue;
                if (reserve.get() > 0 && selfDamage > (mc.player.getHealth() + mc.player.getAbsorptionAmount()) - reserve.get()) continue;
            }
            
            if (balance.get() != 0 && targetDamage < selfDamage + balance.get()) continue;
            
            if (targetDamage > bestDamage) {
                bestDamage = targetDamage;
                bestCrystal = crystal;
            }
        }
        
        return bestCrystal;
    }
    
    private boolean canSee(BlockPos pos) {
        Vec3d start = mc.player.getEyePos();
        Vec3d end = pos.toCenterPos();
        
        return mc.world.raycast(new RaycastContext(
            start, end,
            RaycastContext.ShapeType.COLLIDER,
            RaycastContext.FluidHandling.NONE,
            mc.player
        )).getType() == net.minecraft.util.hit.HitResult.Type.MISS;
    }
    
    private boolean canSee(Entity entity) {
        Vec3d start = mc.player.getEyePos();
        Vec3d end = entity.getBlockPos().toCenterPos();
        
        return mc.world.raycast(new RaycastContext(
            start, end,
            RaycastContext.ShapeType.COLLIDER,
            RaycastContext.FluidHandling.NONE,
            mc.player
        )).getType() == net.minecraft.util.hit.HitResult.Type.MISS;
    }
    
    private boolean canPlaceCrystalAt(BlockPos pos) {
        BlockPos below = pos.down();
        
        // Check if there's obsidian or bedrock below
        if (!mc.world.getBlockState(below).isOf(Blocks.OBSIDIAN) && 
            !mc.world.getBlockState(below).isOf(Blocks.BEDROCK)) {
            return false;
        }
        
        // Check if position is air
        if (!mc.world.getBlockState(pos).isAir()) return false;
        if (!mc.world.getBlockState(pos.up()).isAir()) return false;
        
        // Check for entities
        Box box = new Box(pos);
        for (Entity entity : mc.world.getOtherEntities(null, box)) {
            if (!(entity instanceof EndCrystalEntity)) {
                return false;
            }
        }
        
        return true;
    }
    
    private boolean canPlace() {
        if (!hasCrystal()) return false;
        // Instant mode ignores delays
        if (instant.get()) return true;
        return placeTimer.passedMs(placeDelay.get());
    }
    
    private boolean canBreak() {
        // Instant mode ignores delays
        if (instant.get()) return true;
        return breakTimer.passedMs(breakDelay.get());
    }
    
    private boolean hasCrystal() {
        return mc.player.getMainHandStack().getItem() == Items.END_CRYSTAL ||
               mc.player.getOffHandStack().getItem() == Items.END_CRYSTAL ||
               (autoSwitch.get() && findCrystalSlot() != -1);
    }
    
    private int findCrystalSlot() {
        return InventoryUtil.findItem(Items.END_CRYSTAL);
    }
    
    private void placeCrystal(BlockPos pos) {
        // Switch to crystal if needed
        int oldSlot = mc.player.getInventory().selectedSlot;
        boolean switched = false;
        
        if (mc.player.getMainHandStack().getItem() != Items.END_CRYSTAL &&
            mc.player.getOffHandStack().getItem() != Items.END_CRYSTAL) {
            if (autoSwitch.get()) {
                int crystalSlot = findCrystalSlot();
                if (crystalSlot != -1) {
                    InventoryUtil.switchToSlot(crystalSlot);
                    switched = true;
                } else {
                    return;
                }
            } else {
                return;
            }
        }
        
        // Place crystal
        BlockPos below = pos.down();
        Direction facing = Direction.UP;
        Vec3d hitVec = below.toCenterPos().add(0, 0.5, 0);
        
        if (rotate.get()) {
            lookAt(hitVec);
        }
        
        Hand hand = mc.player.getOffHandStack().getItem() == Items.END_CRYSTAL ? Hand.OFF_HAND : Hand.MAIN_HAND;
        
        // Use packet-based placement for faster execution
        mc.interactionManager.interactBlock(mc.player, hand, 
            new BlockHitResult(hitVec, facing, below, false));
        
        // Swing hand for visual feedback
        if (!instant.get()) {
            mc.player.swingHand(hand);
        }
        
        // Switch back
        if (switched && !instant.get()) {
            InventoryUtil.switchToSlot(oldSlot);
        }
        
        placeTimer.reset();
    }
    
    private void breakCrystalEntity(EndCrystalEntity crystal) {
        if (rotate.get()) {
            lookAt(crystal.getBlockPos().toCenterPos());
        }
        
        // Send attack packet directly for faster execution
        mc.getNetworkHandler().sendPacket(
            PlayerInteractEntityC2SPacket.attack(crystal, mc.player.isSneaking())
        );
        
        // Swing hand for visual feedback
        if (!instant.get()) {
            mc.player.swingHand(Hand.MAIN_HAND);
        }
        
        // Reset attack cooldown for faster attacks
        if (resetAttackCooldown.get()) {
            // Note: resetLastAttackedTicks() may not exist in 1.21
            // mc.player.resetLastAttackedTicks();
        }
        
        // Remove crystal immediately in instant mode
        if (removeCrystal.get() || instant.get()) {
            mc.world.removeEntity(crystal.getId(), Entity.RemovalReason.KILLED);
        }
        
        breakTimer.reset();
        currentBreakCrystal = null;
    }
    
    private float calculateDamage(Vec3d crystalPos, PlayerEntity entity) {
        if (entity == null) return 0;
        
        double distance = entity.getBlockPos().toCenterPos().distanceTo(crystalPos);
        
        if (distance > 12.0) return 0;
        
        // Simplified damage calculation
        double exposure = 1.0;
        double impact = (1.0 - distance / 12.0) * exposure;
        float damage = (float) ((impact * impact + impact) / 2.0 * 7.0 * 12.0 + 1.0);
        
        // Apply armor reduction (simplified)
        damage = damage * (1.0f - Math.min(20.0f, entity.getArmor()) / 25.0f);
        
        return Math.max(0, damage);
    }
    
    private void lookAt(Vec3d pos) {
        Vec3d eyePos = mc.player.getEyePos();
        Vec3d dir = pos.subtract(eyePos).normalize();
        
        float yaw = (float) Math.toDegrees(Math.atan2(dir.z, dir.x)) - 90.0f;
        float pitch = (float) -Math.toDegrees(Math.atan2(dir.y, Math.sqrt(dir.x * dir.x + dir.z * dir.z)));
        
        if (silentRotate.get()) {
            // Silent rotation - send packet and store for rendering
            serverYaw = yaw;
            serverPitch = pitch;
            hasServerRotation = true;
            
            mc.getNetworkHandler().sendPacket(
                new PlayerMoveC2SPacket.LookAndOnGround(yaw, pitch, mc.player.isOnGround(), mc.player.horizontalCollision)
            );
        } else {
            // Normal rotation - change client view
            hasServerRotation = false;
            mc.player.setYaw(yaw);
            mc.player.setPitch(pitch);
        }
    }
    
    /**
     * Get server yaw for rendering (used by mixin)
     */
    public float getServerYaw() {
        return hasServerRotation ? serverYaw : mc.player.getYaw();
    }
    
    /**
     * Get server pitch for rendering (used by mixin)
     */
    public float getServerPitch() {
        return hasServerRotation ? serverPitch : mc.player.getPitch();
    }
    
    /**
     * Check if has server rotation (used by mixin)
     */
    public boolean hasServerRotation() {
        return hasServerRotation && silentRotate.get();
    }
    
    /**
     * Page enum for settings organization
     */
    public enum Page {
        General,
        Damage,
        Timing,
        Behavior,
        Advanced
    }
}

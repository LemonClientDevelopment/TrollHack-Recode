package dev.mahiro.trollhack.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/**
 * Rotation utility class
 */
public class RotationUtil {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    
    /**
     * Get rotation to look at a position
     */
    public static float[] getRotationTo(Vec3d target) {
        if (mc.player == null) return new float[]{0, 0};
        
        Vec3d eyePos = mc.player.getEyePos();
        Vec3d dir = target.subtract(eyePos).normalize();
        
        float yaw = (float) Math.toDegrees(Math.atan2(dir.z, dir.x)) - 90.0f;
        float pitch = (float) -Math.toDegrees(Math.atan2(dir.y, Math.sqrt(dir.x * dir.x + dir.z * dir.z)));
        
        return new float[]{yaw, pitch};
    }
    
    /**
     * Set player rotation
     */
    public static void setRotation(float yaw, float pitch) {
        if (mc.player == null) return;
        
        mc.player.setYaw(yaw);
        mc.player.setPitch(MathHelper.clamp(pitch, -90.0f, 90.0f));
    }
    
    /**
     * Look at a position
     */
    public static void lookAt(Vec3d target) {
        float[] rotation = getRotationTo(target);
        setRotation(rotation[0], rotation[1]);
    }
    
    /**
     * Calculate angle difference
     */
    public static float getAngleDifference(float angle1, float angle2) {
        float diff = Math.abs(angle1 - angle2) % 360.0f;
        if (diff > 180.0f) {
            diff = 360.0f - diff;
        }
        return diff;
    }
    
    /**
     * Check if target is in FOV
     */
    public static boolean inFov(Vec3d target, float fov) {
        if (mc.player == null) return false;
        
        float[] rotation = getRotationTo(target);
        float yawDiff = getAngleDifference(mc.player.getYaw(), rotation[0]);
        float pitchDiff = getAngleDifference(mc.player.getPitch(), rotation[1]);
        
        return yawDiff <= fov && pitchDiff <= fov;
    }
}

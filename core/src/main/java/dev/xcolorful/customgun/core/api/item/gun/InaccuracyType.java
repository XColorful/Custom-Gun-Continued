package dev.xcolorful.customgun.core.api.item.gun;

import dev.xcolorful.customgun.core.api.entity.ShootState;
import net.minecraft.world.entity.LivingEntity;

import java.util.Map;

/**
 * 移步至 {@link ShootState}
 */
@Deprecated(forRemoval = true)
public enum InaccuracyType {
    NONE;
    public static ShootState getInaccuracyType(LivingEntity livingEntity) {
        return ShootState.fromLivingShooter(livingEntity);
    }
    public static Map<ShootState, Float> getDefaultInaccuracy() {
        return null;
    }
    private static boolean isMove(LivingEntity livingEntity) {
        return false;
    }
}

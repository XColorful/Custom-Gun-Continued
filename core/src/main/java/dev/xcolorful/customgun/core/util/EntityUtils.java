package dev.xcolorful.customgun.core.util;

import dev.xcolorful.customgun.core.api.entity.IEntityHitboxHistory;
import dev.xcolorful.customgun.core.api.entity.ILivingShooter;
import dev.xcolorful.customgun.core.api.entity.hitbox.IEntityHitboxHistoryGetter;
import dev.xcolorful.customgun.core.api.entity.shooter.ILivingShooterGetter;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EntityUtils {

    /**
     * 这个字段在1.20.1算三维距离，在高版本仅攀爬时算垂直距离
     */
    public static float getMoveDist(@NotNull Entity entity) {
        return entity.moveDist;
    }
    @Deprecated(forRemoval = true, since = "1.21.4")
    public static float getWalkDist(@NotNull Entity entity) {
        return entity.walkDist;
    }
    @Deprecated(forRemoval = true, since = "1.21.4")
    public static float getOldWalkDist(@NotNull Entity entity) {
        return entity.walkDistO;
    }

    public static class Hitbox {

        /**
         * @param entity 获取碰撞箱的实体
         * @param livingShooter (可选) 射手生物
         * @return 延迟补偿后的碰撞想
         */
        public static @NotNull AABB getTracedHitbox(@NotNull Entity entity, @Nullable Entity livingShooter) {
            @Nullable ILivingShooter iLivingShooter = ILivingShooterGetter.cgc$fromEntity(livingShooter);
            if (iLivingShooter == null) return entity.getBoundingBox();

            IEntityHitboxHistory entityHitboxHistory = IEntityHitboxHistoryGetter.cgc$fromEntity(entity);
            if (entityHitboxHistory != null) {
                int tickBefore = Mth.floor(iLivingShooter.cgc$getShooterLatencyMs() / 1000.0 * 20 + 0.5);
                @Nullable AABB historyBox = entityHitboxHistory.cgc$getHistoryHitbox(tickBefore);
                if (historyBox != null) {
                    return historyBox;
                }
            }

            return entity.getBoundingBox();
        }
        public static @NotNull AABB getTracedHitbox(@NotNull Entity entity) {
            return getTracedHitbox(entity, null);
        }
    }
}

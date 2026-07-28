package xiao.customgun.core.api.entity.hitbox;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public interface IEntityHitboxAccess {

    /**
     * @param tickBefore 过去的tick数, 不小于0
     * @return 历史碰撞箱，返回 null 则没有 (超出记录范围)
     */
    @Nullable AABB cgc$getHistoryHitbox(int tickBefore);

    /**
     * 清理碰撞箱史
     * <br>
     * 用于跨纬度等需要失效的场景
     */
    void cgc$resetHistoryHitbox();

    /**
     * 获取历史速度
     * @param tickBefore 过去的tick数, 不小于0
     * @return 历史移动速度，返回 null 则没有 (超出记录范围)
     */
    default @Nullable Vec3 cgc$getHistoryVelocity(int tickBefore) {
        @Nullable AABB current = cgc$getHistoryHitbox(tickBefore);
        @Nullable AABB previous = cgc$getHistoryHitbox(tickBefore + 1);
        if (current == null || previous == null) {
            return null;
        }

        double currentX = (current.minX + current.maxX) / 2;
        double currentY = current.minY;
        double currentZ = (current.minZ + current.maxZ) / 2;

        double previousX = (previous.minX + previous.maxX) / 2;
        double previousY = previous.minY;
        double previousZ = (previous.minZ + previous.maxZ) / 2;

        return new Vec3(currentX - previousX, currentY - previousY, currentZ - previousZ);
    }
}

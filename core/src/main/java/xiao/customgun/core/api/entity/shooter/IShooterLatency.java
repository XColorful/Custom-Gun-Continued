package xiao.customgun.core.api.entity.shooter;

import xiao.customgun.core.api.entity.hitbox.IEntityHitboxAccess;

public interface IShooterLatency {

    /**
     * @return 射击者的延迟(毫秒)，返回值可用于 {@link IEntityHitboxAccess#cgc$getHistoryHitbox(int)}
     */
    default int cgc$getShooterLatencyMs() {
        return 0;
    };
}

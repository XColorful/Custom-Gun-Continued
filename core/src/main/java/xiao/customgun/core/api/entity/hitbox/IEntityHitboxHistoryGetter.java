package xiao.customgun.core.api.entity.hitbox;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.api.entity.IEntityHitboxHistory;
import xiao.customgun.core.mixin.entity.ServerPlayerMixin;

public interface IEntityHitboxHistoryGetter {

    static @Nullable IEntityHitboxHistory cgc$fromEntity(Entity entity) {
        return entity instanceof IEntityHitboxHistory ? (IEntityHitboxHistory) entity : null;
    }

    /**
     * {@link ServerPlayerMixin} mixin到ServerPlayer实现该接口
     */
    static IEntityHitboxHistory cgc$fromServerPlayer(ServerPlayer serverPlayer) {
        return (IEntityHitboxHistory) serverPlayer;
    }
}

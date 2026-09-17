package dev.xcolorful.customgun.client.renderer.entity;

import dev.xcolorful.customgun.core.api.entity.IEntityHitboxHistory;
import dev.xcolorful.customgun.core.api.entity.hitbox.IEntityHitboxHistoryGetter;
import dev.xcolorful.customgun.core.config.SyncConfig;
import dev.xcolorful.customgun.core.mixin.entity.ServerPlayerMixin;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public class EntityHitboxRenderer {

    /**
     * <ul>
     *     <li>{@link ServerPlayer}在{@link ServerPlayerMixin}实现了{@link IEntityHitboxHistory}</li>
     *     <li>客户端渲染的玩家实体是{@link RemotePlayer}/{@link LocalPlayer}</li>
     *     <li>所以 {@link IEntityHitboxHistoryGetter#cgc$fromEntity}在客户端渲染路径上命中不到，需要特判 {@link AbstractClientPlayer}</li>
     * </ul>
     */
    public static boolean shouldRenderHitbox(Entity entity) {
        if (
                // 开启配置
                SyncConfig.HIDE_ENTITY_HITBOX.get()
                // 实体碰撞箱史 或 玩家
                && (IEntityHitboxHistoryGetter.cgc$fromEntity(entity) != null || entity instanceof AbstractClientPlayer)) {
            return false;
        }

        return true;
    }
}

package dev.xcolorful.customgun.core.entity.hitbox.player;

import dev.xcolorful.customgun.core.api.entity.hitbox.IEntityHitboxHistoryGetter;
import dev.xcolorful.customgun.core.api.event.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public class _PlayerHitboxHandler implements IEventHandler {
    private static class _PlayerHitboxHandlerHolder {
        private static final _PlayerHitboxHandler INSTANCE = new _PlayerHitboxHandler();
    }
    public static _PlayerHitboxHandler get() {
        return _PlayerHitboxHandlerHolder.INSTANCE;
    }
    protected _PlayerHitboxHandler() {}
    @Override public String getEventHandlerName() {
        return this.getClass().getName();
    }
    @Override
    public void handleEvent(EventType eventType, IEvent event) {
        switch (eventType) {
            case ENTITY_TRAVEL_DIMENSION_EVENT -> onEntityTravelDimension((IEntityTravelDimensionEvent) event);
            case PLAYER_CLONE_EVENT -> onPlayerClone((IPlayerCloneEvent) event);
            default -> onReceiveWrongEvent(eventType);
        }
    }

    private void onEntityTravelDimension(IEntityTravelDimensionEvent event) {
        this.resetHitboxHistory(event.getEntity());
    }
    private void onPlayerClone(IPlayerCloneEvent event) {
        this.resetHitboxHistory(event.getEntity());
    }

    /**
     * 主模组只考虑ServerPlayer(mixin注入)的IEntityHitboxHistory
     * <br>
     * 扩展模组如果搞 Entity IEntityHitboxHistory 则自己负责跨纬度是否需要清理
     */
    private void resetHitboxHistory(Entity entity) {
        if (!(entity instanceof ServerPlayer serverPlayer)) return;

        IEntityHitboxHistoryGetter.cgc$fromServerPlayer(serverPlayer).cgc$resetHistoryHitbox();
    }
}

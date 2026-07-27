/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.entity.shooter.world;

import net.minecraft.world.entity.LivingEntity;
import xiao.customgun.core.api.entity.shooter.ILivingShooterGetter;
import xiao.customgun.core.api.event.EventType;
import xiao.customgun.core.api.event.IEntityTravelDimensionEvent;
import xiao.customgun.core.api.event.IEvent;
import xiao.customgun.core.api.event.IEventHandler;
import xiao.customgun.core.api.item.gun.IGunGetter;

public class _WorldShooterHandler implements IEventHandler {
    private static class _WorldShooterHandlerHolder {
        private static final _WorldShooterHandler INSTANCE = new _WorldShooterHandler();
    }
    public static _WorldShooterHandler get() {
        return _WorldShooterHandlerHolder.INSTANCE;
    }
    protected  _WorldShooterHandler() {}
    @Override public String getEventHandlerName() {
        return this.getClass().getName();
    }
    @Override
    public void handleEvent(EventType eventType, IEvent event) {
        if (eventType == EventType.ENTITY_JOIN_LEVEL_EVENT) {
            onTravelDimension((IEntityTravelDimensionEvent) event);
        } else {
            onReceiveWrongEvent(eventType);
        }
    }

    /**
     * 主模组只考虑LivingEntity(mixin注入)的ILivingShooter
     * <br>
     * 扩展模组如果搞 Entity ILivingShooter 则自己负责跨纬度是否需要刷新
     */
    private void onTravelDimension(IEntityTravelDimensionEvent event) {
        if (!(event.getEntity() instanceof LivingEntity livingShooter)) return;

        if (IGunGetter.fromMainHand(livingShooter) != null) {
            ILivingShooterGetter.cgc$fromLivingEntity(livingShooter).cgc$initLivingShooter();
        }
    }
}

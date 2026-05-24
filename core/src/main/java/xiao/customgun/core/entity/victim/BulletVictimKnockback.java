/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

/*
 * 改成跟 BattleRoyale 同构的写法
 */

package xiao.customgun.core.entity.victim;

import xiao.customgun.core.api.entity.IBulletVictimEntity;
import xiao.customgun.core.api.entity.victim.IBulletVictimEntityGetter;
import xiao.customgun.core.api.event.EventType;
import xiao.customgun.core.api.event.IEvent;
import xiao.customgun.core.api.event.IEventHandler;
import xiao.customgun.core.api.event.ILivingKnockbackEvent;

public class BulletVictimKnockback implements IEventHandler {
    private static class BulletVictimKnockbackHolder {
        private static final BulletVictimKnockback INSTANCE = new BulletVictimKnockback();
    }
    public static BulletVictimKnockback get() {
        return BulletVictimKnockbackHolder.INSTANCE;
    }
    protected BulletVictimKnockback() {}
    @Override public String getEventHandlerName() {
        return this.getClass().getName();
    }
    @Override
    public void handleEvent(EventType eventType, IEvent event) {
        if (eventType == EventType.LIVING_KNOCKBACK_EVENT) {
            onLivingKnockback((ILivingKnockbackEvent) event);
        } else {
            onReceiveWrongEvent(eventType);
        }
    }

    private void onLivingKnockback(ILivingKnockbackEvent event) {
        IBulletVictimEntity victimEntity = IBulletVictimEntityGetter.fromLivingEntity(event.getEntity());
        float strength = victimEntity.cgc$getKnockBackStrength();
        if (strength >= 0) {
            event.setKnockbackStrength(strength);
            victimEntity.cgc$resetKnockBackStrength();
        }
    }
}

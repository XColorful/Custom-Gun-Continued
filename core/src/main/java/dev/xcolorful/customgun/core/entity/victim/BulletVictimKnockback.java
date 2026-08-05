/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

/*
 * 改成跟 BattleRoyale 同构的写法
 */

package dev.xcolorful.customgun.core.entity.victim;

import dev.xcolorful.customgun.core.api.entity.IBulletVictimEntity;
import dev.xcolorful.customgun.core.api.entity.victim.IBulletVictimEntityGetter;
import dev.xcolorful.customgun.core.api.event.EventType;
import dev.xcolorful.customgun.core.api.event.IEvent;
import dev.xcolorful.customgun.core.api.event.IEventHandler;
import dev.xcolorful.customgun.core.api.event.ILivingKnockbackEvent;

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
        float strength = victimEntity.cgc$getKnockbackStrength();
        if (strength >= 0) {
            event.setKnockbackStrength(strength);
            victimEntity.cgc$resetKnockbackStrength();
        }
    }
}

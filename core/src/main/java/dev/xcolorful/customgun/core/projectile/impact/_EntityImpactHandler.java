/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.projectile.impact;

import dev.xcolorful.customgun.core.api.event.EventType;
import dev.xcolorful.customgun.core.api.event.IEvent;
import dev.xcolorful.customgun.core.api.event.IEventHandler;
import dev.xcolorful.customgun.core.api.event.ILivingHurtEvent;
import dev.xcolorful.customgun.core.init.registry.ModDamageTypes;
import net.minecraft.world.entity.LivingEntity;

/**
 * Attribute不在数据包里，玩家是不知道的
 * 要改的话，也是主模组做一个BulletDamageEvent，而不是在这里改伤害
 */
@Deprecated
public class _EntityImpactHandler implements IEventHandler {
    private static class _EntityImpactHandlerHolder {
        private static final _EntityImpactHandler INSTANCE = new _EntityImpactHandler();
    }
    public static _EntityImpactHandler get() {
        return _EntityImpactHandlerHolder.INSTANCE;
    }
    protected  _EntityImpactHandler(){}
    @Override public String getEventHandlerName() {
        return this.getClass().getName();
    }
    @Override
    public void handleEvent(EventType eventType, IEvent event) {
        if (eventType == EventType.LIVING_HURT_EVENT) {
            onLivingHurt((ILivingHurtEvent) event);
        } else {
            onReceiveWrongEvent(eventType);
        }
    }

    private void onLivingHurt(ILivingHurtEvent event) {
        if (!event.getSource().is(ModDamageTypes.BULLET_DAMAGE)) {
            return;
        }

        LivingEntity livingEntity = event.getEntity();
//        AttributeInstance resistance = livingEntity.getAttribute();
//        if (resistance != null) {
//            float modifiedDamage = event.getDamageAmount() * (float) (1 - resistance.getValue());
//            event.setDamageAmount(modifiedDamage);
//        }
    }
}

/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.event.shooter;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import xiao.customgun.CustomGun;
import xiao.customgun.core.api.common.McLogicalSide;
import xiao.customgun.core.api.entity.ILivingShooter;
import xiao.customgun.core.api.entity.shooter.modifier.ShooterGunModifierCache;
import xiao.customgun.core.api.event.CustomEventType;
import xiao.customgun.core.api.event.ICustomEvent;
import xiao.customgun.core.api.event.ICustomEventHandler;
import xiao.customgun.core.api.event.gun.IGunEvent;
import xiao.customgun.core.api.item.IGun;
import xiao.customgun.core.event.EventDispatcher;
import xiao.customgun.core.item.gun.GunItem;

/**
 * 枪械{@link IGun}{@link GunItem} 属性缓存 更新时的事件
 */
public final class ShooterGunModifierCacheEvent extends LivingShooterEvent implements IGunEvent {

    private final @NotNull IGun iGun;
    private final @NotNull ItemStack gunItem;

    private final @NotNull ShooterGunModifierCache cache;

    public ShooterGunModifierCacheEvent(McLogicalSide logicalSide,
                                        @NotNull ILivingShooter iLivingShooter, @NotNull LivingEntity livingShooter,
                                        @NotNull IGun iGun, @NotNull ItemStack gunItem,
                                        @NotNull ShooterGunModifierCache cache) {
        super(logicalSide, iLivingShooter, livingShooter);
        this.iGun = iGun;
        this.gunItem = gunItem;
        this.cache = cache;
    }
    @Override public CustomEventType getEventType() {
        return CustomEventType.SHOOTER_GUN_MODIFIER_CACHE_EVENT;
    }

    public @NotNull IGun getIGun() {
        return this.iGun;
    }
    public @NotNull ItemStack getGunItem() {
        return this.gunItem;
    }
    public @NotNull ShooterGunModifierCache getCache() {
        return this.cache;
    }

    @Override public String getTextName() {
        return this.livingShooter != null ? this.livingShooter.getName().getString()
                : "ShooterGunModifierCacheEvent";
    }
    @Override public Component getDisplayName() {
        return this.livingShooter != null ? this.livingShooter.getDisplayName()
                : Component.literal(getTextName());
    }

    private static final EventDispatcher<ICustomEventHandler, ICustomEvent, CustomEventType> _EVENT_DISPATCHER = CustomGun.getEventPoster().getEventDispatcher(ShooterGunModifierCacheEvent.class);
    @Override public @NotNull EventDispatcher<ICustomEventHandler, ICustomEvent, CustomEventType> getEventDispatcher() {
        return _EVENT_DISPATCHER;
    }
}

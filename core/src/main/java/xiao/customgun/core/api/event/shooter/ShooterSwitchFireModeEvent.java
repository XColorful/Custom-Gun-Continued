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
import org.jetbrains.annotations.Nullable;
import xiao.customgun.CustomGun;
import xiao.customgun.core.api.common.McLogicalSide;
import xiao.customgun.core.api.entity.ILivingShooter;
import xiao.customgun.core.api.event.CustomEventType;
import xiao.customgun.core.api.event.ICustomEvent;
import xiao.customgun.core.api.event.ICustomEventHandler;
import xiao.customgun.core.api.event.gun.IGunEvent;
import xiao.customgun.core.api.item.IGun;
import xiao.customgun.core.event.EventDispatcher;

/**
 * 射手生物{@link ILivingShooter} 切换开火模式事件
 */
public final class ShooterSwitchFireModeEvent extends LivingShooterEvent implements IGunEvent {

    private final @Nullable IGun iGun;
    private final @NotNull ItemStack gunItem;

    public ShooterSwitchFireModeEvent(McLogicalSide logicalSide,
                                      @Nullable ILivingShooter iLivingShooter, @Nullable LivingEntity livingShooter,
                                      @Nullable IGun iGun, @NotNull ItemStack gunItem) {
        super(logicalSide, iLivingShooter, livingShooter);
        this.iGun = iGun;
        this.gunItem = gunItem;
    }
    @Override public CustomEventType getEventType() {
        return CustomEventType.SHOOTER_SWITCH_FIRE_MODE_EVENT;
    }

    public @Nullable IGun getIGun() {
        return this.iGun;
    }
    public @NotNull ItemStack getGunItem() {
        return this.gunItem;
    }

    @Override public String getTextName() {
        return this.livingShooter != null ? this.livingShooter.getName().getString()
                : "ShooterSwitchFireModeEvent";
    }
    @Override public Component getDisplayName() {
        return this.livingShooter != null ? this.livingShooter.getDisplayName()
                : Component.literal(getTextName());
    }

    private static final EventDispatcher<ICustomEventHandler, ICustomEvent, CustomEventType> _EVENT_DISPATCHER = CustomGun.getEventPoster().getEventDispatcher(ShooterSwitchFireModeEvent.class);
    @Override public @NotNull EventDispatcher<ICustomEventHandler, ICustomEvent, CustomEventType> getEventDispatcher() {
        return _EVENT_DISPATCHER;
    }
}

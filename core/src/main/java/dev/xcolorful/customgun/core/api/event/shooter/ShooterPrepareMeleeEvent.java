/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.api.event.shooter;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.core.api.common.McLogicalSide;
import dev.xcolorful.customgun.core.api.entity.ILivingShooter;
import dev.xcolorful.customgun.core.api.event.CustomEventType;
import dev.xcolorful.customgun.core.api.event.ICustomEvent;
import dev.xcolorful.customgun.core.api.event.ICustomEventHandler;
import dev.xcolorful.customgun.core.api.event.gun.IGunEvent;
import dev.xcolorful.customgun.core.api.item.IGun;
import dev.xcolorful.customgun.core.event.EventDispatcher;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 射手生物{@link ILivingShooter} 准备 近战(刺刀/枪托)事件
 */
public final class ShooterPrepareMeleeEvent extends LivingShooterEvent implements IGunEvent {

    private final @Nullable IGun iGun;
    private final @NotNull ItemStack gunItem;

    public ShooterPrepareMeleeEvent(McLogicalSide logicalSide,
                                    @Nullable ILivingShooter iLivingShooter, @Nullable LivingEntity livingShooter,
                                    @Nullable IGun iGun, @NotNull ItemStack gunItem) {
        super(logicalSide, iLivingShooter, livingShooter);
        this.iGun = iGun;
        this.gunItem = gunItem;
    }
    @Override public CustomEventType getEventType() {
        return CustomEventType.SHOOTER_PREPARE_MELEE_EVENT;
    }

    public @Nullable IGun getIGun() {
        return this.iGun;
    }
    public @NotNull ItemStack getGunItem() {
        return this.gunItem;
    }

    @Override public String getTextName() {
        return this.livingShooter != null ? this.livingShooter.getName().getString()
                : "ShooterPrepareMeleeEvent";
    }
    @Override public Component getDisplayName() {
        return this.livingShooter != null ? this.livingShooter.getDisplayName()
                : Component.literal(getTextName());
    }

    private static final EventDispatcher<ICustomEventHandler, ICustomEvent, CustomEventType> _EVENT_DISPATCHER = CustomGun.getEventPoster().getEventDispatcher(ShooterPrepareMeleeEvent.class);
    @Override public @NotNull EventDispatcher<ICustomEventHandler, ICustomEvent, CustomEventType> getEventDispatcher() {
        return _EVENT_DISPATCHER;
    }
}

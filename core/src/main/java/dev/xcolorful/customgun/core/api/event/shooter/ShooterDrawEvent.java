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
import dev.xcolorful.customgun.core.event.EventDispatcher;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 射手生物{@link ILivingShooter} 切枪事件
 */
public final class ShooterDrawEvent extends LivingShooterEvent {
    @Override
    public boolean isCancelable() {
        return false;
    }

    private final ItemStack previousItem;
    private final ItemStack currentItem;

    public ShooterDrawEvent(McLogicalSide logicalSide,
                            @Nullable ILivingShooter iLivingShooter, @Nullable LivingEntity livingShooter,
                            ItemStack previousItem, ItemStack currentItem) {
        super(logicalSide, iLivingShooter, livingShooter);
        this.previousItem = previousItem;
        this.currentItem = currentItem;
    }
    @Override public CustomEventType getEventType() {
        return CustomEventType.SHOOTER_DRAW_EVENT;
    }

    public ItemStack getPreviousItem() {
        return this.previousItem;
    }
    public ItemStack getCurrentItem() {
        return this.currentItem;
    }

    @Override public String getTextName() {
        return this.livingShooter != null ? this.livingShooter.getName().getString()
                : "ShooterDrawEvent";
    }
    @Override public Component getDisplayName() {
        return this.livingShooter != null ? this.livingShooter.getDisplayName()
                : Component.literal(getTextName());
    }

    private static final EventDispatcher<ICustomEventHandler, ICustomEvent, CustomEventType> _EVENT_DISPATCHER = CustomGun.getEventPoster().getEventDispatcher(ShooterDrawEvent.class);
    @Override public @NotNull EventDispatcher<ICustomEventHandler, ICustomEvent, CustomEventType> getEventDispatcher() {
        return _EVENT_DISPATCHER;
    }
}

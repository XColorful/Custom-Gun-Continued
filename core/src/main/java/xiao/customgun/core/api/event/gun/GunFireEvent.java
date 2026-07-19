/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.event.gun;

import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.CustomGun;
import xiao.customgun.core.api.common.ILogicalSideOnly;
import xiao.customgun.core.api.common.McLogicalSide;
import xiao.customgun.core.api.event.CustomEventType;
import xiao.customgun.core.api.event.ICustomEvent;
import xiao.customgun.core.api.event.ICustomEventHandler;
import xiao.customgun.core.api.item.IGun;
import xiao.customgun.core.api.minecraft.CommandLevel;
import xiao.customgun.core.event.EventDispatcher;

/**
 * 枪械{@link IGun} 射击一次 的事件
 */
public final class GunFireEvent extends GunEvent implements ILogicalSideOnly {

    protected final McLogicalSide logicalSide;

    public GunFireEvent(McLogicalSide logicalSide,
                           @Nullable IGun iGun, @NotNull ItemStack gunItem) {
        super(iGun, gunItem);
        this.logicalSide = logicalSide;
    }
    @Override public CustomEventType getEventType() {
        return CustomEventType.GUN_FIRE_EVENT;
    }

    @Override
    public McLogicalSide getLogicalSide() {
        return this.logicalSide;
    }

    @Override
    public @Nullable CommandSourceStack createCommandSourceStack(@Nullable CommandSource source) {
        if (this.logicalSide.isClient()) return null;
        return new CommandSourceStack(
                source != null ? source : CommandSource.NULL,
                Vec3.ZERO,
                Vec2.ZERO,
                null,
                CommandLevel.permission(4),
                this.getTextName(),
                this.getDisplayName(),
                CustomGun.getMinecraftServer(),
                null
        );
    }

    @Override public String getTextName() {
        return gunItem.getDisplayName().getString();
    }
    @Override public Component getDisplayName() {
        return gunItem.getDisplayName();
    }

    private static final EventDispatcher<ICustomEventHandler, ICustomEvent, CustomEventType> _EVENT_DISPATCHER = CustomGun.getEventPoster().getEventDispatcher(GunFireEvent.class);
    @Override public @NotNull EventDispatcher<ICustomEventHandler, ICustomEvent, CustomEventType> getEventDispatcher() {
        return _EVENT_DISPATCHER;
    }
}

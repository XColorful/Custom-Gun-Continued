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
import xiao.customgun.core.api.entity.gun.GunPropertyCache;
import xiao.customgun.core.api.event.CustomEventType;
import xiao.customgun.core.api.event.ICustomEvent;
import xiao.customgun.core.api.event.ICustomEventHandler;
import xiao.customgun.core.api.item.IGun;
import xiao.customgun.core.api.minecraft.CommandLevel;
import xiao.customgun.core.event.EventDispatcher;
import xiao.customgun.core.item.gun.GunItem;

/**
 * 枪械{@link IGun}{@link GunItem} 属性缓存 更新时的事件
 */
public final class GunPropertyCacheEvent extends GunEvent implements IGunEvent {

    private final @NotNull GunPropertyCache cache;

    public GunPropertyCacheEvent(@NotNull IGun iGun, @NotNull ItemStack gunItem,
                                 @NotNull GunPropertyCache cache) {
        super(iGun, gunItem);
        this.cache = cache;
    }
    @Override public CustomEventType getEventType() {
        return CustomEventType.GUN_PROPERTY_CACHE_EVENT;
    }

    public @NotNull IGun getIGun() {
        return this.iGun;
    }
    public @NotNull ItemStack getGunItem() {
        return this.gunItem;
    }
    public @NotNull GunPropertyCache getCache() {
        return this.cache;
    }

    @Override
    public CommandSourceStack createCommandSourceStack(@Nullable CommandSource source) {
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
        return this.gunItem.getDisplayName().getString();
    }
    @Override public Component getDisplayName() {
        return this.gunItem.getDisplayName();
    }

    private static final EventDispatcher<ICustomEventHandler, ICustomEvent, CustomEventType> _EVENT_DISPATCHER = CustomGun.getEventPoster().getEventDispatcher(GunPropertyCacheEvent.class);
    @Override public @NotNull EventDispatcher<ICustomEventHandler, ICustomEvent, CustomEventType> getEventDispatcher() {
        return _EVENT_DISPATCHER;
    }
}

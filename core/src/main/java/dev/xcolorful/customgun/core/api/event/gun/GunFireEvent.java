/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.api.event.gun;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.core.api.common.ILogicalSideOnly;
import dev.xcolorful.customgun.core.api.common.McLogicalSide;
import dev.xcolorful.customgun.core.api.entity.ILivingShooter;
import dev.xcolorful.customgun.core.api.event.CustomEventType;
import dev.xcolorful.customgun.core.api.event.ICustomEvent;
import dev.xcolorful.customgun.core.api.event.ICustomEventHandler;
import dev.xcolorful.customgun.core.api.event.shooter.ILivingShooterEvent;
import dev.xcolorful.customgun.core.api.item.IGun;
import dev.xcolorful.customgun.core.api.minecraft.CommandLevel;
import dev.xcolorful.customgun.core.event.EventDispatcher;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 枪械{@link IGun} 射击一次 的事件
 */
public final class GunFireEvent extends GunEvent implements ILivingShooterEvent, ILogicalSideOnly {

    private final McLogicalSide logicalSide;

    private final @Nullable ILivingShooter iLivingShooter;
    private final @Nullable LivingEntity livingShooter;

    public GunFireEvent(McLogicalSide logicalSide,
                        @Nullable IGun iGun, @NotNull ItemStack gunItem,
                        @Nullable ILivingShooter iLivingShooter, @Nullable LivingEntity livingShooter) {
        super(iGun, gunItem);
        this.logicalSide = logicalSide;
        this.iLivingShooter = iLivingShooter;
        this.livingShooter = livingShooter;
    }
    @Override public CustomEventType getEventType() {
        return CustomEventType.GUN_FIRE_EVENT;
    }

    @Override
    public McLogicalSide getLogicalSide() {
        return this.logicalSide;
    }

    @Override
    public @Nullable ILivingShooter getILivingShooter() {
        return this.iLivingShooter;
    }
    @Override
    public @Nullable LivingEntity getLivingShooter() {
        return this.livingShooter;
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

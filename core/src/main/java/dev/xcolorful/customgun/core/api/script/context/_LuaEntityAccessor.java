/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.api.script.context;

import dev.xcolorful.customgun.core.util.ChatUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

@Deprecated
public class _LuaEntityAccessor {

    private final @Nullable LivingEntity livingEntity;

    private _LuaEntityAccessor(LivingEntity livingEntity) {
        this.livingEntity = livingEntity;
    }
    public static _LuaEntityAccessor of(LivingEntity livingEntity) {
        return new _LuaEntityAccessor(livingEntity);
    }

    public void sendSystemMessage(Component message) {
        if (this.livingEntity instanceof ServerPlayer serverPlayer) {
            ChatUtils.sendComponentMessageToPlayer(serverPlayer, message);
        }
    }

    public void sendActionBar(Component message) {
        if (this.livingEntity instanceof ServerPlayer serverPlayer) {
            ChatUtils.sendActionBarToPlayer(serverPlayer, message);
        }
    }

    public float getHealth() {
        return this.livingEntity != null ? this.livingEntity.getHealth() : 0;
    }

    public boolean hurt(float amount) {
        if (this.livingEntity == null) return false;
        if (!(this.livingEntity.level() instanceof ServerLevel serverLevel)) return false;

        DamageSource damageSource = serverLevel.damageSources().generic();
        return this.livingEntity.hurtServer(serverLevel, damageSource, amount);
    }

    public Component literal(String text) {
        return Component.literal(text);
    }

    public Component translatable(String key) {
        return Component.translatable(key);
    }

    public Component translatable(String key, Component... components) {
        return Component.translatable(key, (Object[]) components);
    }
}

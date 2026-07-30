/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.gun.script;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
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
        if (this.livingEntity == null) return;
        this.livingEntity.sendSystemMessage(message);
    }

    public void sendActionBar(Component message) {
        if (!(this.livingEntity instanceof Player player)) return;
        player.displayClientMessage(message, false);
    }

    public float getHealth() {
        return this.livingEntity != null ? this.livingEntity.getHealth() : 0;
    }

    public boolean hurt(float amount) {
        if (this.livingEntity == null) return false;
        return this.livingEntity.hurt(this.livingEntity.level().damageSources().generic(), amount);
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

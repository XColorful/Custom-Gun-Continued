/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.mixin.entity;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xiao.customgun.core.api.entity.IEntityHitboxHistory;
import xiao.customgun.core.api.entity.shooter.ILivingShooterGetter;
import xiao.customgun.core.config.OtherConfig;

import java.util.ArrayDeque;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin implements IEntityHitboxHistory {

    @Inject(method = "restoreFrom", at = @At("RETURN"))
    public void cgc$initLivingShooter(ServerPlayer pThat, boolean pKeepEverything, CallbackInfo ci) {
        ServerPlayer player = (ServerPlayer) (Object) this;
        ILivingShooterGetter.cgc$fromLivingEntity(player).cgc$initLivingShooter();
    }

    // --------IEntityHitboxHistory--------

    private final ArrayDeque<AABB> cgc$hitboxHistory = new ArrayDeque<>(5); // 配置 [250ms ~ 1000ms] 对应 [ (5+0.5=5) ~ (20+0.5=20) ]

    @Inject(method = "tick", at = @At(value = "RETURN"))
    private void cgc$onServerPlayerTick(CallbackInfo ci) {
        ServerPlayer player = (ServerPlayer) (Object) this;
        cgc$recordHitbox(player);
    }
    private void cgc$recordHitbox(ServerPlayer player) {
        if (player.isSpectator()) {
            this.cgc$hitboxHistory.clear();
            return;
        }

        this.cgc$hitboxHistory.addFirst(player.getBoundingBox());

        int maxSave = Mth.floor(OtherConfig.SERVER_HITBOX_LATENCY_MAX_SAVE_MS.get() / 1000 * 20 + 0.5);

        while (this.cgc$hitboxHistory.size() > maxSave) {
            this.cgc$hitboxHistory.removeLast();
        }
    }

    @Override
    public @Nullable AABB cgc$getHistoryHitbox(int tickBefore) {
        if (tickBefore < 0 || tickBefore >= this.cgc$hitboxHistory.size()) {
            return null;
        }

        int index = 0;
        for (AABB box : this.cgc$hitboxHistory) {
            if (index++ == tickBefore) {
                return box;
            }
        }
        return null;
    }

    @Override
    public void cgc$resetHistoryHitbox() {
        this.cgc$hitboxHistory.clear();
    }
}

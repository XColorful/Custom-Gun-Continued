/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.mixin.entity;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.core.api.entity.IEntityHitboxHistory;
import dev.xcolorful.customgun.core.api.entity.shooter.ILivingShooterGetter;
import dev.xcolorful.customgun.core.api.entity.shooter.IShooterLatency;
import dev.xcolorful.customgun.core.config.OtherConfig;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin implements IEntityHitboxHistory, IShooterLatency {

    @Shadow
    public int latency;

    @Inject(method = "restoreFrom", at = @At("RETURN"))
    public void cgc$initLivingShooter(ServerPlayer pThat, boolean pKeepEverything, CallbackInfo ci) {
        ServerPlayer player = (ServerPlayer) (Object) this;
        ILivingShooterGetter.cgc$fromLivingEntity(player).cgc$initLivingShooter();
    }

    // --------IEntityHitboxHistory--------

    // --------IEntityHitboxAccess--------

    private static final int cgc$HISTORY_CAPACITY = 20;
    private final AABB[] cgc$hitboxHistory = new AABB[cgc$HISTORY_CAPACITY]; // 配置 [250ms ~ 1000ms] 对应 [ (5+0.5=5) ~ (20+0.5=20) ]
    private int cgc$historyHead = 0;
    private int cgc$historySize = 0;

    @Inject(method = "tick", at = @At(value = "RETURN"))
    private void cgc$onServerPlayerTick(CallbackInfo ci) {
        ServerPlayer player = (ServerPlayer) (Object) this;
        cgc$tickHitboxHistory(player);
    }
    private void cgc$tickHitboxHistory(ServerPlayer player) {
        if (!OtherConfig.SERVER_HITBOX_LATENCY_FIX.get()) {
            // 关闭后立即清空有效长度，避免重新开启时读到旧历史
            this.cgc$historySize = 0;
            // 不需要每tick都重置数组，外部调用读不到就行
            return;
        }

        if (player.isSpectator()) {
            this.cgc$resetHistoryHitbox();
            return;
        }

        int maxSave = Mth.floor(OtherConfig.SERVER_HITBOX_LATENCY_MAX_SAVE_MS.get() / 1000.0 * 20.0 + 0.5);
        maxSave = Mth.clamp(maxSave, 0, cgc$HISTORY_CAPACITY);

        if (this.cgc$historySize > maxSave) {
            this.cgc$historySize = maxSave;
        }

        this.cgc$historyHead = (this.cgc$historyHead - 1 + cgc$HISTORY_CAPACITY) % cgc$HISTORY_CAPACITY;
        this.cgc$hitboxHistory[this.cgc$historyHead] = player.getBoundingBox();

        if (this.cgc$historySize < maxSave) {
            this.cgc$historySize++;
        }
    }

    @Override
    public @Nullable AABB cgc$getHistoryHitbox(int tickBefore) {
        if (tickBefore < 0 || tickBefore >= this.cgc$historySize) {
            return null;
        }

        int index = (this.cgc$historyHead + tickBefore) % cgc$HISTORY_CAPACITY;
        return this.cgc$hitboxHistory[index];
    }

    @Override
    public void cgc$resetHistoryHitbox() {
        this.cgc$historySize = 0;
        Arrays.fill(this.cgc$hitboxHistory, null);
    }

    // --------IShooterLatency--------

    @Override
    public int cgc$getShooterLatencyMs() {
        return this.latency;
    }

    // 测试代码，可删
    // 测试方式为挂在cgc$onServerPlayerTick尾部
    @Deprecated(forRemoval = true)
    public int cgc$cnt = 0;
    @Deprecated(forRemoval = true)
    public void cgc$test() {
        if (cgc$cnt >= 20 * 10) return;
        cgc$cnt++;
        if (cgc$cnt > 20 && cgc$cnt < 20 * 9) return;

        CustomGun.LOGGER.debug("========== History Test Tick {} ==========", cgc$cnt);
        CustomGun.LOGGER.debug("historySize: {}, historyHead: {}", this.cgc$historySize, this.cgc$historyHead);

        for (int i = 0; i < this.cgc$historySize; i++) {
            int index = (this.cgc$historyHead + i) % cgc$HISTORY_CAPACITY;
            AABB box = this.cgc$getHistoryHitbox(i);
            CustomGun.LOGGER.debug("tickBefore {} index {} AABB: {}", i, index, box);
        }

        CustomGun.LOGGER.debug("==========================================");
    }
}

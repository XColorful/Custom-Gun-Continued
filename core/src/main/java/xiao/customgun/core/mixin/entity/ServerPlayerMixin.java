/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.mixin.entity;

import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xiao.customgun.core.api.entity.shooter.ILivingShooterGetter;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {

    @Inject(method = "restoreFrom", at = @At("RETURN"))
    public void initLivingShooter(ServerPlayer pThat, boolean pKeepEverything, CallbackInfo ci) {
        ServerPlayer player = (ServerPlayer) (Object) this;
        ILivingShooterGetter.cgc$fromLivingEntity(player).cgc$initLivingShooter();
    }
}

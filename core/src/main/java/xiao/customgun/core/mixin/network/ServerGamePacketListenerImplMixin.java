/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.mixin.network;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xiao.customgun.core.api.entity.ILivingShooter;
import xiao.customgun.core.api.entity.shooter.ILivingShooterGetter;
import xiao.customgun.core.network.message.ServerMessageSwapItem;
import xiao.customgun.core.util.SendUtils;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin {

    @Shadow
    public ServerPlayer player;

    @Inject(method = "handlePlayerAction", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;stopUsingItem()V"))
    public void cgc$handleSwapOffhandDraw(ServerboundPlayerActionPacket packetIn, CallbackInfo ci) {
        player.inventoryMenu.broadcastChanges();
        SendUtils.sendMessageToPlayer(player, new ServerMessageSwapItem());
    }

    @WrapOperation(method = "handlePlayerCommand", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;setSprinting(Z)V"))
    public void cgc$handleSprintCommand(ServerPlayer player, boolean sprint, Operation<Void> original) {
        ILivingShooter livingShooter = ILivingShooterGetter.cgc$fromLivingEntity(player);
        original.call(player, livingShooter.cgc$getProcessedSprintStatus(sprint));
    }
}

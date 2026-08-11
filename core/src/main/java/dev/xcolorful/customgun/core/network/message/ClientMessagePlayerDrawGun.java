/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.network.message;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.core.api.entity.shooter.ILivingShooterGetter;
import dev.xcolorful.customgun.core.api.network.message.IMessage;
import dev.xcolorful.customgun.core.util.InventoryUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;

import java.util.function.Consumer;

public class ClientMessagePlayerDrawGun implements IMessage<ClientMessagePlayerDrawGun> {

    public ClientMessagePlayerDrawGun() {
    }

    @Override
    public void encode(ClientMessagePlayerDrawGun message, FriendlyByteBuf buffer) {
    }

    public static ClientMessagePlayerDrawGun decode(FriendlyByteBuf buffer) {
        return new ClientMessagePlayerDrawGun();
    }

    @Override
    public void handle(ClientMessagePlayerDrawGun message, Consumer<Runnable> handler, NetworkContext context) {
        if (CustomGun.getSideExecutor().getLogicalSide().isServer()) {
            handler.accept(() -> {
                if (!(context.sender() instanceof ServerPlayer player)) {
                    return;
                }

                Inventory inventory = player.getInventory();
                int selected = InventoryUtils.getSelectedSlot(inventory);
                ILivingShooterGetter.cgc$fromLivingEntity(player).cgc$draw(() -> inventory.getItem(selected));
            });
        }
    }
}
/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.network.message.gun;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.client.network.message.gun._S2CMessageGunLevelUp;
import dev.xcolorful.customgun.core.api.common.McSide;
import dev.xcolorful.customgun.core.api.network.message.IMessage;
import dev.xcolorful.customgun.core.util.NetworkUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

public record S2CMessageGunLevelUp(ItemStack gun,
                                   int level)
        implements IMessage<S2CMessageGunLevelUp> {

    @Override
    public void encode(S2CMessageGunLevelUp message, FriendlyByteBuf buffer) {
        NetworkUtils.writeItem(buffer, message.gun);
        buffer.writeVarInt(message.level);
    }

    public static S2CMessageGunLevelUp decode(FriendlyByteBuf buffer) {
        ItemStack gun = NetworkUtils.readItem(buffer);
        int level = buffer.readInt();
        return new S2CMessageGunLevelUp(gun, level);
    }

    @Override
    public void handle(S2CMessageGunLevelUp message, Consumer<Runnable> handler, NetworkContext context) {
        if (CustomGun.getSideExecutor().getLogicalSide().isClient()) {
            handler.accept(() -> {
                CustomGun.getSideExecutor().executeOn(McSide.CLIENT, () -> () ->
                        _S2CMessageGunLevelUp.onLevelUp(message)
                );
            });
        }
    }
}
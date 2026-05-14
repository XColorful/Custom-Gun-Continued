/*
 * Copyright (c) 2025-2026 XiaoColorful (https://github.com/XColorful)
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Source: https://github.com/XColorful/BattleRoyale
 */

package xiao.customgun.neoforge.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import xiao.customgun.core.api.network.INetworkHook;

import java.util.function.Consumer;

public class NeoNetworkHook implements INetworkHook {

    @Override
    public void openScreen(ServerPlayer player, MenuProvider containerSupplier, Consumer<FriendlyByteBuf> extraDataWriter) {
        Consumer<RegistryFriendlyByteBuf> registryAdapter = (RegistryFriendlyByteBuf registryBuf) -> {
            extraDataWriter.accept((FriendlyByteBuf) registryBuf);
        };

        player.openMenu(containerSupplier, registryAdapter);
    }
}
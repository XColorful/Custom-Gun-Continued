/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.network.message.player;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.core.api.network.message.IMessage;
import dev.xcolorful.customgun.core.util.NetworkUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Consumer;

public record C2SMessagePlayerCraft(
        ResourceLocation recipeLocation, // 细节：Identifier 放同一行
        int menuId)
        implements IMessage<C2SMessagePlayerCraft> {

    @Override
    public void encode(C2SMessagePlayerCraft message, FriendlyByteBuf buffer) {
        NetworkUtils.writeResourceLocation(buffer, message.recipeLocation);
        buffer.writeVarInt(message.menuId);
    }

    public static C2SMessagePlayerCraft decode(FriendlyByteBuf buffer) {
        return new C2SMessagePlayerCraft(NetworkUtils.readResourceLocation(buffer), buffer.readVarInt());
    }

    @Override
    public void handle(C2SMessagePlayerCraft message, Consumer<Runnable> handler, NetworkContext context) {
        if (CustomGun.getSideExecutor().getLogicalSide().isServer()) {
            handler.accept(() -> {
                if (!(context.sender() instanceof ServerPlayer player)) {
                    return;
                }
                handle(this, player, context);
            });
        }
    }

    public static void handle(C2SMessagePlayerCraft message, ServerPlayer player, NetworkContext context) {
        // mixin注入点
    }
}
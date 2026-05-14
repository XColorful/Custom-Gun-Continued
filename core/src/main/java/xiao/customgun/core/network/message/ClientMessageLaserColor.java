/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.network.message;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import xiao.customgun.CustomGun;
import xiao.customgun.core.api.item.IGun;
import xiao.customgun.core.api.item.attachment.AttachmentCategory;
import xiao.customgun.core.api.network.message.IMessage;
import xiao.customgun.core.util.NetworkUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class ClientMessageLaserColor implements IMessage<ClientMessageLaserColor> {
    public final Map<AttachmentCategory, Integer> colorMap = new HashMap<>();
    public boolean applyGunColor = false;
    public int gunColor = 0;
    public int gunSlotIndex = -1;

    private ClientMessageLaserColor() {
    }

    public ClientMessageLaserColor(@NotNull ItemStack gun, int gunSlotIndex) {
        if (gun.getItem() instanceof IGun iGun) {
            for (AttachmentCategory type : AttachmentCategory.values()) {
                // TODO IGun
                // TODO IAttachment
            }
            this.gunSlotIndex = gunSlotIndex;
        }
    }

    @Override
    public void encode(ClientMessageLaserColor message, FriendlyByteBuf buffer) {
        NetworkUtils.writeEnumIntMap(buffer, message.colorMap);
        buffer.writeBoolean(message.applyGunColor);
        buffer.writeInt(message.gunColor);
        buffer.writeInt(message.gunSlotIndex);
    }

    public static ClientMessageLaserColor decode(FriendlyByteBuf buffer) {
        ClientMessageLaserColor message = new ClientMessageLaserColor();
        message.colorMap.putAll(NetworkUtils.readEnumIntMap(buffer, AttachmentCategory.class));
        message.applyGunColor = buffer.readBoolean();
        message.gunColor = buffer.readInt();
        message.gunSlotIndex = buffer.readInt();
        return message;
    }

    @Override
    public void handle(ClientMessageLaserColor message, Consumer<Runnable> handler, NetworkContext context) {
        if (CustomGun.getSideExecutor().getLogicalSide().isServer()) {
            handler.accept(() -> {
                if (!(context.sender() instanceof ServerPlayer player) || message.gunSlotIndex == -1) {
                    return;
                }
                Inventory inventory = player.getInventory();
                ItemStack gunItem = inventory.getItem(message.gunSlotIndex);
                // TODO IGun
                // TODO IAttachment
            });
        }
    }
}
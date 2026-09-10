/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.network.message.shooter;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.core.api.item.IAttachment;
import dev.xcolorful.customgun.core.api.item.IGun;
import dev.xcolorful.customgun.core.api.item.attachment.AttachmentCategory;
import dev.xcolorful.customgun.core.api.item.attachment.IAttachmentGetter;
import dev.xcolorful.customgun.core.api.item.gun.IGunGetter;
import dev.xcolorful.customgun.core.api.network.message.IMessage;
import dev.xcolorful.customgun.core.util.NetworkUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class C2SMessageShooterRefit_laserColor implements IMessage<C2SMessageShooterRefit_laserColor> {
    public final Map<AttachmentCategory, Integer> colorMap = new HashMap<>();
    public boolean applyGunColor = false;
    public int gunColor = 0;
    public int gunSlotIndex = -1;

    private C2SMessageShooterRefit_laserColor() {
    }

    public C2SMessageShooterRefit_laserColor(@NotNull ItemStack gunItem, int gunSlotIndex) {
        @Nullable IGun iGun = IGunGetter.fromItemStack(gunItem);
        if (iGun == null) return;

        for (AttachmentCategory category : AttachmentCategory.values()) {
            ItemStack attachmentItem = iGun.getAttachment(gunItem, category);
            @Nullable IAttachment iAttachment = IAttachmentGetter.fromItemStack(attachmentItem);
            if (iAttachment == null) continue;

            if (iAttachment.hasLaserColor(attachmentItem)) {
                this.colorMap.put(category, iAttachment.getLaserColorInt(attachmentItem));
            }
        }

        if (iGun.hasLaserColor(gunItem)) {
            this.gunColor = iGun.getLaserColorInt(gunItem);
            this.applyGunColor = true;
        }
        this.gunSlotIndex = gunSlotIndex;
    }

    @Override
    public void encode(C2SMessageShooterRefit_laserColor message, FriendlyByteBuf buffer) {
        NetworkUtils.writeEnumIntMap(buffer, message.colorMap);
        buffer.writeBoolean(message.applyGunColor);
        buffer.writeInt(message.gunColor);
        buffer.writeInt(message.gunSlotIndex);
    }

    public static C2SMessageShooterRefit_laserColor decode(FriendlyByteBuf buffer) {
        C2SMessageShooterRefit_laserColor message = new C2SMessageShooterRefit_laserColor();
        message.colorMap.putAll(NetworkUtils.readEnumIntMap(buffer, AttachmentCategory.class));
        message.applyGunColor = buffer.readBoolean();
        message.gunColor = buffer.readInt();
        message.gunSlotIndex = buffer.readInt();
        return message;
    }

    @Override
    public void handle(C2SMessageShooterRefit_laserColor message, Consumer<Runnable> handler, NetworkContext context) {
        if (CustomGun.getSideExecutor().getLogicalSide().isServer()) {
            handler.accept(() -> {
                if (!(context.sender() instanceof ServerPlayer player) || message.gunSlotIndex == -1) {
                    return;
                }
                Inventory inventory = player.getInventory();
                ItemStack gunItem = inventory.getItem(message.gunSlotIndex);
                @Nullable IGun iGun = IGunGetter.fromItemStack(gunItem);
                if (iGun == null) return;

                for (Map.Entry<AttachmentCategory, Integer> entry : message.colorMap.entrySet()) {
                    AttachmentCategory category = entry.getKey();
                    int colorInt = entry.getValue();
                    ItemStack attachment = iGun.getAttachment(gunItem, category);
                    @Nullable IAttachment iAttachment = IAttachmentGetter.fromItemStack(attachment);
                    if (iAttachment != null) {
                        iAttachment.setLaserColorInt(attachment, colorInt);
                    }
                }

                if (message.applyGunColor) {
                    iGun.setLaserColorInt(gunItem, message.gunColor);
                }
            });
        }
    }
}
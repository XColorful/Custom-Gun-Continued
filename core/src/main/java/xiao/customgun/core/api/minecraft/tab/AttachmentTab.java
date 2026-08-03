/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.minecraft.tab;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import xiao.customgun.core.api.item.AttachmentProperty;
import xiao.customgun.core.api.item.attachment.AttachmentCategory;
import xiao.customgun.core.api.item.builder.AttachmentBuilder;
import xiao.customgun.core.api.resource.ResourceApi;
import xiao.customgun.core.init.registry.ModItems;
import xiao.customgun.core.resource.data.index.AttachmentIndex;
import xiao.customgun.core.resource.instance.data.AttachmentIndexInstance;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class AttachmentTab {

    public static Comparator<Map.Entry<ResourceLocation, AttachmentIndexInstance>> indexSort() {
        return Comparator.comparingInt(entry -> entry.getValue().getPojo().getSlotSort());
    }

    public static List<ItemStack> buildAttachmentItems(AttachmentCategory attachmentCategory) {
        List<ItemStack> attachmentItems = new ArrayList<>();
        ResourceApi.getAllAttachmentIndexInstance().stream().sorted(indexSort()).forEach(entry -> {
            @NotNull AttachmentIndexInstance attachmentIndexInstance = entry.getValue();

            AttachmentIndex attachmentIndex = attachmentIndexInstance.getPojo();
            if (attachmentIndex.isHideInGame()) {
                return;
            }

            if (attachmentIndex.getAttachmentCategory() == attachmentCategory) {
                ItemStack attachmentItem = AttachmentBuilder.create(ModItems.ATTACHMENT.get())
                        // 配件ResourceLocation
                        .setProperty(AttachmentProperty.ATTACHMENT_LOCATION,
                                ResourceLocation.class,
                                entry.getKey())
                        // 配件类型
                        .setProperty(AttachmentProperty.ATTACHMENT_CATEGORY,
                                AttachmentCategory.class,
                                attachmentCategory)
                        .build();
                attachmentItems.add(attachmentItem);
            }
        });
        return attachmentItems;
    }
}

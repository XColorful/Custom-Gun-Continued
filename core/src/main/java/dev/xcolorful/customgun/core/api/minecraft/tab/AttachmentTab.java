/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.api.minecraft.tab;

import dev.xcolorful.customgun.core.api.item.AttachmentProperty;
import dev.xcolorful.customgun.core.api.item.attachment.AttachmentCategory;
import dev.xcolorful.customgun.core.api.item.builder.AttachmentBuilder;
import dev.xcolorful.customgun.core.api.resource.ResourceApi;
import dev.xcolorful.customgun.core.init.registry.ModItems;
import dev.xcolorful.customgun.core.resource.data.index.AttachmentIndex;
import dev.xcolorful.customgun.core.resource.instance.data.AttachmentIndexInstance;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class AttachmentTab {

    public static Comparator<Map.Entry<Identifier, AttachmentIndexInstance>> indexSort() {
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
                                Identifier.class,
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

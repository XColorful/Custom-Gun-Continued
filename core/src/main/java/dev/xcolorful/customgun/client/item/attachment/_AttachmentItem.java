/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.item.attachment;

import dev.xcolorful.customgun.core.api.item.IAttachment;
import dev.xcolorful.customgun.core.api.resource.ResourceApi;
import dev.xcolorful.customgun.core.resource.data.index.AttachmentIndex;
import dev.xcolorful.customgun.core.resource.instance.data.AttachmentIndexInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class _AttachmentItem {

    public static @Nullable Component getName(IAttachment _this,
                                              @NotNull ItemStack attachmentItem) {
        var attachmentLocation = _this.getAttachmentLocation(attachmentItem);
        @Nullable AttachmentIndexInstance attachmentIndexInstance = ResourceApi.getAttachmentIndexInstance(attachmentLocation);
        if (attachmentIndexInstance == null) return null;

        AttachmentIndex attachmentIndex = attachmentIndexInstance.getPojo();
        return attachmentIndex.getNameLang();
    }
}

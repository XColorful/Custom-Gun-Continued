/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.item.attachment;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.client.api.resource.ClientResourceApi;
import xiao.customgun.client.resource.instance.data.ClientAttachmentIndexInstance;
import xiao.customgun.core.api.item.IAttachment;

public class _AttachmentItem {

    public static @Nullable Component getName(IAttachment _this,
                                              @NotNull ItemStack attachmentItem) {
        var attachmentLocation = _this.getAttachmentLocation(attachmentItem);
        ClientAttachmentIndexInstance clientAttachmentIndexInstance = ClientResourceApi.getClientAttachmentIndexInstance(attachmentLocation);
        if (clientAttachmentIndexInstance == null) return null;

        return clientAttachmentIndexInstance.getPojo().getNameLang();
    }
}

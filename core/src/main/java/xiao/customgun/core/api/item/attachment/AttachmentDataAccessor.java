/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.item.attachment;

import net.minecraft.nbt.CompoundTag;

public interface AttachmentDataAccessor extends IAttachmentDataGetter, IAttachmentDataSetter {

    @Override
    default int getScopeViewIndex(CompoundTag attachmentCustomDataTag) {
        if (attachmentCustomDataTag == null) return 0;
        // TODO
        return 0;
    }

    AttachmentDataAccessor INSTANCE = new AttachmentDataAccessor() {};
}

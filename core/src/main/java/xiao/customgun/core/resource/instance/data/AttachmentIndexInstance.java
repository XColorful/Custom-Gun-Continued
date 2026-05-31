/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.resource.instance.data;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.CustomGun;
import xiao.customgun.core.api.item.attachment.AttachmentCategory;
import xiao.customgun.core.api.resource.ResourceApi;
import xiao.customgun.core.resource.data.data.AttachmentData;
import xiao.customgun.core.resource.data.index.AttachmentIndex;
import xiao.customgun.core.resource.instance.PojoInstance;

public class AttachmentIndexInstance extends PojoInstance<AttachmentIndex> {

    private AttachmentData attachmentDataCache;

    private AttachmentIndexInstance(@NotNull AttachmentIndex pojo) {
        super(pojo);
    }

    public static @Nullable AttachmentIndexInstance fromPojo(AttachmentIndex pojo) {
        if (pojo == null) return null;
        AttachmentIndexInstance instance = new AttachmentIndexInstance(pojo);
        if (!instance.isPojoValid()) return null;
        else return instance;
    }

    @Override public boolean resetCache() {
        this.attachmentDataCache = ResourceApi.getAttachmentData(this.getPojo().getDataLocation());
        if (this.attachmentDataCache == null) {
            CustomGun.LOGGER.debug("AttachmentIndexInstance: AttachmentData {} not found", this.getPojo().getDataLocation());
            return false;
        } else if (!this.attachmentDataCache.isValid()) {
            CustomGun.LOGGER.debug("AttachmentIndexInstance: AttachmentData {} not valid", this.getPojo().getDataLocation());
            return false;
        }

        return true;
    }
    @Override protected boolean isPojoValid() {
        var pojo = this.getPojo();
        if (!pojo.isValid()) return false;

        // AttachmentIndex
        if (this.getPojo().getSlotSort() > 65536) CustomGun.LOGGER.warn("AttachmentIndexInstance: AttachmentIndex slotSort {} > 65536", this.getPojo().getSlotSort());

        return true;
    }

    // --------Getter--------

    public AttachmentData getAttachmentData() {
        return this.attachmentDataCache;
    }

    // --------Deprecated--------

    @Deprecated public int getSort() {
        return this.getPojo().getSlotSort();
    }
    @Deprecated public AttachmentCategory getType() {
        return this.getPojo().getAttachmentCategory();
    }
}
/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.resource.instance.data;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.core.api.item.attachment.AttachmentCategory;
import dev.xcolorful.customgun.core.api.resource.ResourceApi;
import dev.xcolorful.customgun.core.resource.data.data.AttachmentData;
import dev.xcolorful.customgun.core.resource.data.index.AttachmentIndex;
import dev.xcolorful.customgun.core.resource.instance.PojoInstance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
        if (!super.isPojoValid()) return false;

        var pojo = this.getPojo();

        // AttachmentIndex
        if (pojo.getSlotSort() > 65536) CustomGun.LOGGER.warn("AttachmentIndexInstance: AttachmentIndex slotSort {} > 65536", pojo.getSlotSort());

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
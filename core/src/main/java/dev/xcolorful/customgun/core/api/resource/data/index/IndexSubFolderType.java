/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.api.resource.data.index;

import dev.xcolorful.customgun.core.api.resource.ResourceTag;

public enum IndexSubFolderType implements ResourceTag {
    GUN(IndexSubFolderTypeTag.GUN),
    ATTACHMENT(IndexSubFolderTypeTag.ATTACHMENT),
    AMMO(IndexSubFolderTypeTag.AMMO),
    BLOCK(IndexSubFolderTypeTag.BLOCK);

    public final String folderName;
    IndexSubFolderType(String folderName) {
        this.folderName = folderName;
    }

    @Override public String getTagName() {
        return this.folderName.toLowerCase();
    }

    public String getFolderName() {
        return this.folderName;
    }
}
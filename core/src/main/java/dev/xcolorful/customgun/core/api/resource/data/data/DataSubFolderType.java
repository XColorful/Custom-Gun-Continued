/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.api.resource.data.data;

import dev.xcolorful.customgun.core.api.resource.ResourceTag;

public enum DataSubFolderType implements ResourceTag {
    GUN(DataSubFolderTypeTag.GUN),
    ATTACHMENT(DataSubFolderTypeTag.ATTACHMENT),
    BLOCK(DataSubFolderTypeTag.BLOCK);

    public final String folderName;
    DataSubFolderType(String folderName) {
        this.folderName = folderName;
    }

    @Override public String getTagName() {
        return this.folderName.toLowerCase();
    }

    public String getFolderName() {
        return this.folderName;
    }
}
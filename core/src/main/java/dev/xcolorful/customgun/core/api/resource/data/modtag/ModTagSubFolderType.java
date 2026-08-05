/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.api.resource.data.modtag;

import dev.xcolorful.customgun.core.api.resource.ResourceTag;

public enum ModTagSubFolderType implements ResourceTag {
    ATTACHMENT_TAG(ModTagSubFolderTypeTag.ATTACHMENT_TAG),
    GUN_ATTACHMENT(ModTagSubFolderTypeTag.GUN_ATTACHMENT);

    public final String folderName;
    ModTagSubFolderType(String folderName) {
        this.folderName = folderName;
    }

    @Override public String getTagName() {
        return this.folderName.toLowerCase();
    }

    public String getFolderName() {
        return this.folderName;
    }
}
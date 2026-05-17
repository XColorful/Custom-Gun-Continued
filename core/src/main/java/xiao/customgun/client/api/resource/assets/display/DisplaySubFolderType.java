/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.api.resource.assets.display;

import xiao.customgun.core.api.resource.ResourceTag;
import xiao.customgun.core.api.resource.assets.display.DisplaySubFolderTypeTag;

public enum DisplaySubFolderType implements ResourceTag {
    GUN(DisplaySubFolderTypeTag.GUN),
    ATTACHMENT(DisplaySubFolderTypeTag.ATTACHMENT),
    AMMO(DisplaySubFolderTypeTag.AMMO),
    BLOCK(DisplaySubFolderTypeTag.BLOCK);

    public final String folderName;
    DisplaySubFolderType(String folderName) {
        this.folderName = folderName;
    }

    @Override
    public String getTagName() {
        return this.folderName.toLowerCase();
    }

    public String getFolderName() {
        return this.folderName;
    }
}
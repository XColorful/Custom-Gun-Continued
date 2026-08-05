/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.api.resource.assets.textures;

import dev.xcolorful.customgun.core.api.resource.ResourceTag;
import dev.xcolorful.customgun.core.api.resource.assets.textures.CrosshairFolderName;

public enum CrosshairFolderType implements ResourceTag {
    HIT(CrosshairFolderName.HIT),
    NORMAL(CrosshairFolderName.NORMAL);

    public final String folderName;
    CrosshairFolderType(final String folderName) {
        this.folderName = folderName;
    }

    @Override public String getTagName() {
        return folderName.toLowerCase();
    }
    public String getFolderName() {
        return folderName;
    }
}

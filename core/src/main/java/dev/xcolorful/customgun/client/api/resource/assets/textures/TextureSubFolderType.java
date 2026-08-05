/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.api.resource.assets.textures;

import dev.xcolorful.customgun.core.api.resource.ResourceTag;
import dev.xcolorful.customgun.core.api.resource.assets.textures.TextureSubFolderTypeTag;

public enum TextureSubFolderType implements ResourceTag {
    CROSSHAIR(TextureSubFolderTypeTag.CROSSHAIR);

    public final String folderName;
    TextureSubFolderType(String folderName) {
        this.folderName = folderName;
    }

    @Override public String getTagName() {
        return this.folderName.toLowerCase();
    }

    public String getFolderName() {
        return this.folderName;
    }
}

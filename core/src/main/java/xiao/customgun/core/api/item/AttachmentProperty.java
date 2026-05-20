/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.item;

import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.api.resource.ResourceTag;

import java.util.HashMap;
import java.util.Map;

public enum AttachmentProperty implements ResourceTag {
    // IAttachmentDataAccess
    ATTACHMENT_LOCATION(AttachmentPropertyTag.ATTACHMENT_LOCATION),
    ATTACHMENT_CATEGORY(AttachmentPropertyTag.ATTACHMENT_CATEGORY);

    public final String propertyName;
    AttachmentProperty(final String propertyName) {
        this.propertyName = propertyName;
    }

    @Override public String getTagName() {
        return this.propertyName;
    }

    private static final Map<String, AttachmentProperty> PROPERTY_MAP = new HashMap<>();

    static {
        for (AttachmentProperty property : AttachmentProperty.values()) {
            PROPERTY_MAP.put(property.propertyName, property);
        }
    }

    public static @Nullable AttachmentProperty fromString(String name) {
        return name != null ? PROPERTY_MAP.get(name) : null;
    }

    @Override
    public String toString() {
        return this.propertyName;
    }
}

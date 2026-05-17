/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.api.sound.attachment;

import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.api.resource.ResourceTag;
import xiao.customgun.core.api.sound.attachment.AttachmentSoundTypeTag;

import java.util.HashMap;
import java.util.Map;

public enum AttachmentSoundType implements ResourceTag.CategoryTag {
    // 配件
    UNINSTALL_SOUND(AttachmentSoundTypeTag.UNINSTALL_SOUND, false),
    INSTALL_SOUND(AttachmentSoundTypeTag.INSTALL_SOUND, false);

    public final String typeName;
    public final boolean preload;
    AttachmentSoundType(String name, boolean preload) {
        this.typeName = name;
        this.preload = preload;
    }

    @Override public String getTagName() {
        return this.typeName;
    }
    @Override public String getCategoryName() {
        return this.typeName;
    }

    private static final Map<String, AttachmentSoundType> SOUND_TYPES = new HashMap<>();

    static {
        for (AttachmentSoundType type : values()) {
            SOUND_TYPES.put(type.typeName, type);
        }
    }

    public static @Nullable AttachmentSoundType fromString(String name) {
        return name != null ? SOUND_TYPES.get(name) : null;
    }

    @Override
    public String toString() {
        return this.typeName;
    }
}

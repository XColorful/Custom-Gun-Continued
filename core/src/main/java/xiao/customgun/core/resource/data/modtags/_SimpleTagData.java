/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.resource.data.modtags;

import xiao.customgun.core.resource.ResourcePojo;
import xiao.customgun.core.util.ClassUtils;

public abstract class _SimpleTagData<T extends _SimpleTagData<T>> extends ResourcePojo<T> {

    private ClassUtils.ArraySet<String> tags = new ClassUtils.ArraySet<>();

    // --------Getter & Setter--------

    public final ClassUtils.ArraySet<String> getTags() {
        return tags;
    }

    public final void setTags(ClassUtils.ArraySet<String> tags) {
        this.tags = tags;
    }
}
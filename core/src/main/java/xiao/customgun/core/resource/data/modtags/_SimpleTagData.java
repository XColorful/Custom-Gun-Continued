/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.resource.data.modtags;

import xiao.customgun.core.resource.ResourcePojo;

import java.util.List;

public abstract class _SimpleTagData<T extends _SimpleTagData<T>> extends ResourcePojo<T> {

    private List<String> tags;

    // --------Getter & Setter--------

    public final List<String> getTags() {
        return tags;
    }

    public final void setTags(List<String> tags) {
        this.tags = tags;
    }

    // --------Back compatibility--------
}
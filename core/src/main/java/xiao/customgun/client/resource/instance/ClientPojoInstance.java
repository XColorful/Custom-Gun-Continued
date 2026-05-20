/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.resource.instance;

import org.jetbrains.annotations.NotNull;
import xiao.customgun.core.resource.ResourcePojo;

public abstract class ClientPojoInstance<T extends ResourcePojo<T>> {

    private final @NotNull T pojo;

    protected ClientPojoInstance(@NotNull T pojo) {
        this.pojo = pojo;
    }

    public @NotNull T getPojo() {
        return this.pojo;
    }

    abstract protected boolean isPojoValid();
}

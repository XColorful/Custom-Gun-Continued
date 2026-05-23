/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.resource.instance;

import org.jetbrains.annotations.NotNull;
import xiao.customgun.core.resource.ResourcePojo;

/**
 * 创建 Pojo instance 时，所有 Pojo 都已加载
 * <p>
 * Pojo instance 可以缓存 Pojo 引用，但不应循环依赖 instance (只保证 Pojo 已加载)
 * <p>
 * 但是进单人游戏时是先处理assets pojo manager再data，所以client instance要实时拿pojo
 */
public abstract class PojoInstance<T extends ResourcePojo<T>> {

    private final @NotNull T pojo;

    protected PojoInstance(@NotNull T pojo) {
        this.pojo = pojo;
    }

    public @NotNull T getPojo() {
        return this.pojo;
    }

    abstract protected boolean isPojoValid();
}

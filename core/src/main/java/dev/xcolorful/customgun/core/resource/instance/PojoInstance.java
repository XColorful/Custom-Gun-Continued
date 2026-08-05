/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.resource.instance;

import dev.xcolorful.customgun.core.resource.ResourcePojo;
import org.jetbrains.annotations.NotNull;

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

    /**
     * 重置缓存
     * @return 是否成功拿到全部缓存
     * 客户端侧PojoInstance不使用这个方法作为{@link #isPojoValid}的检测 (assets会先加载的读取顺序问题)
     */
    protected boolean resetCache() {
        return true;
    }
    abstract protected boolean isPojoValid();
    /**
     * log数据中所有不合法的内容
     * @param errorMask 位运算掩码
     * 预期是面对全数据合法的情况 (冷路径方法)
     */
    protected void logAllErrors(int errorMask) {};
}

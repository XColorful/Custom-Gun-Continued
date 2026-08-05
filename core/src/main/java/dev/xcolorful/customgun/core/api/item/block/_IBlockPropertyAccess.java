/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.api.item.block;

import dev.xcolorful.customgun.core.api.item.BlockProperty;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * 专供第三方脚本（如 KubeJS）调用的属性访问接口，模组内部严禁使用 (会循环调用)
 * <p>
 * 推荐脚本先缓存 {@link IBlockGetter#fromItemStack} 和 {@link BlockProperty#fromString}，避免反复调用便利方法
 * <p>
 * 由于 JavaScript 的数字默认为 Double，直接传入会导致 Java 泛型捕获失败并抛出 ClassCastException
 * <p>
 * 在 KubeJS 侧设置整型属性时，<b>必须</b>使用 <code>java(value).asInt()</code> 显式指定类型
 */
public interface _IBlockPropertyAccess {

    @Deprecated(forRemoval = false)
    default @Nullable <T> T getProperty(IBlockDataAccess blockDataAccess, ItemStack blockItem, BlockProperty property) {
        if (property == null) return null;
        return property.get(blockDataAccess, blockItem);
    }
    @Deprecated(forRemoval = false)
    default <T> void setProperty(IBlockDataAccess blockDataAccess, ItemStack blockItem, BlockProperty property, T value) {
        if (property == null) return;
        property.set(blockDataAccess, blockItem, value);
    }

    @Deprecated(forRemoval = false)
    default <T> T getProperty(ItemStack blockItem, String property) {
        @Nullable IBlockDataAccess blockDataAccess = IBlockGetter.fromItemStack(blockItem);
        if (blockDataAccess == null) return null;
        return getProperty(blockDataAccess, blockItem, BlockProperty.fromString(property));
    }
    @Deprecated(forRemoval = false)
    default <T> void setProperty(ItemStack blockItem, String property, T value) {
        @Nullable IBlockDataAccess blockDataAccess = IBlockGetter.fromItemStack(blockItem);
        if (blockDataAccess == null) return;
        setProperty(blockDataAccess, blockItem, BlockProperty.fromString(property), value);
    }
}
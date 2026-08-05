/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.api.item.ammo;

import dev.xcolorful.customgun.core.api.item.AmmoProperty;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * 专供第三方脚本（如 KubeJS）调用的属性访问接口，模组内部严禁使用 (会循环调用)
 * <p>
 * 推荐脚本先缓存 {@link IAmmoGetter#fromItemStack} 和 {@link AmmoProperty#fromString}，避免反复调用便利方法
 * <p>
 * 由于 JavaScript 的数字默认为 Double，直接传入会导致 Java 泛型捕获失败并抛出 ClassCastException
 * <p>
 * 在 KubeJS 侧设置整型属性时，<b>必须</b>使用 <code>java(value).asInt()</code> 显式指定类型
 */
public interface _IAmmoPropertyAccess {

    @Deprecated(forRemoval = false)
    default @Nullable <T> T getProperty(IAmmoDataAccess ammoDataAccess, ItemStack ammoItem, AmmoProperty property) {
        if (property == null) return null;
        return property.get(ammoDataAccess, ammoItem);
    }
    @Deprecated(forRemoval = false)
    default <T> void setProperty(IAmmoDataAccess ammoDataAccess, ItemStack ammoItem, AmmoProperty property, T value) {
        if (property == null) return;
        property.set(ammoDataAccess, ammoItem, value);
    }

    @Deprecated(forRemoval = false)
    default <T> T getProperty(ItemStack ammoItem, String property) {
        @Nullable IAmmoDataAccess ammoDataAccess = IAmmoGetter.fromItemStack(ammoItem);
        if (ammoDataAccess == null) return null;
        return getProperty(ammoDataAccess, ammoItem, AmmoProperty.fromString(property));
    }
    @Deprecated(forRemoval = false)
    default <T> void setProperty(ItemStack ammoItem, String property, T value) {
        @Nullable IAmmoDataAccess ammoDataAccess = IAmmoGetter.fromItemStack(ammoItem);
        if (ammoDataAccess == null) return;
        setProperty(ammoDataAccess, ammoItem, AmmoProperty.fromString(property), value);
    }
}
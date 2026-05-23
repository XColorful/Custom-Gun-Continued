/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.item.ammobox;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.api.item.AmmoBoxProperty;

/**
 * 专供第三方脚本（如 KubeJS）调用的属性访问接口，模组内部严禁使用 (会循环调用)
 * <p>
 * 推荐脚本先缓存 {@link IAmmoBoxGetter#fromItemStack} 和 {@link AmmoBoxProperty#fromString}，避免反复调用便利方法
 * <p>
 * 由于 JavaScript 的数字默认为 Double，直接传入会导致 Java 泛型捕获失败并抛出 ClassCastException
 * <p>
 * 在 KubeJS 侧设置整型属性时，<b>必须</b>使用 <code>java(value).asInt()</code> 显式指定类型
 */
public interface _IAmmoBoxPropertyAccess {

    @Deprecated(forRemoval = false)
    default @Nullable <T> T getProperty(IAmmoBoxDataAccess ammoBoxDataAccess, ItemStack ammoBoxItem, AmmoBoxProperty property) {
        if (property == null) return null;
        return property.get(ammoBoxDataAccess, ammoBoxItem);
    }
    @Deprecated(forRemoval = false)
    default <T> void setProperty(IAmmoBoxDataAccess ammoBoxDataAccess, ItemStack ammoBoxItem, AmmoBoxProperty property, T value) {
        if (property == null) return;
        property.set(ammoBoxDataAccess, ammoBoxItem, value);
    }

    @Deprecated(forRemoval = false)
    default <T> T getProperty(ItemStack ammoBoxItem, String property) {
        @Nullable IAmmoBoxDataAccess ammoBoxDataAccess = IAmmoBoxGetter.fromItemStack(ammoBoxItem);
        if (ammoBoxDataAccess == null) return null;
        return getProperty(ammoBoxDataAccess, ammoBoxItem, AmmoBoxProperty.fromString(property));
    }
    @Deprecated(forRemoval = false)
    default <T> void setProperty(ItemStack ammoBoxItem, String property, T value) {
        @Nullable IAmmoBoxDataAccess ammoBoxDataAccess = IAmmoBoxGetter.fromItemStack(ammoBoxItem);
        if (ammoBoxDataAccess == null) return;
        setProperty(ammoBoxDataAccess, ammoBoxItem, AmmoBoxProperty.fromString(property), value);
    }
}
/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.item.gun;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.api.item.GunProperty;

/**
 * 专供第三方脚本（如 KubeJS）调用的属性访问接口，模组内部严禁使用 (会循环调用)
 * <p>
 * 推荐脚本先缓存 {@link IGunGetter#fromItemStack} 和 {@link GunProperty#fromString}，避免反复调用便利方法
 * <p>
 * 由于 JavaScript 的数字默认为 Double，直接传入会导致 Java 泛型捕获失败并抛出 ClassCastException
 * <p>
 * 在 KubeJS 侧设置整型属性时，<b>必须</b>使用 <code>java(value).asInt()</code> 显式指定类型
 */
public interface _IGunPropertyAccess {

    @Deprecated(forRemoval = false)
    default <T> T getProperty(IGunDataAccess gunDataAccess, ItemStack gunItem, GunProperty property) {
        if (property == null) return null;
        return property.get(gunDataAccess, gunItem);
    }
    @Deprecated(forRemoval = false)
    default <T> void setProperty(IGunDataAccess gunDataAccess, ItemStack gunItem, GunProperty property, T value) {
        if (property == null) return;
        property.set(gunDataAccess, gunItem, value);
    }

    @Deprecated(forRemoval = false)
    default <T> T getProperty(ItemStack gunItem, String property) {
        @Nullable IGunDataAccess gunDataAccess = IGunGetter.fromItemStack(gunItem);
        if (gunDataAccess == null) return null;
        return getProperty(gunDataAccess, gunItem, GunProperty.fromString(property));
    }
    @Deprecated(forRemoval = false)
    default <T> T getProperty(LivingEntity livingEntity, String property) {
        @Nullable IGunDataAccess gunDataAccess = IGunGetter.fromMainHand(livingEntity);
        if (gunDataAccess == null) return null;
        return getProperty(gunDataAccess, livingEntity.getMainHandItem(), GunProperty.fromString(property));
    }
    @Deprecated(forRemoval = false)
    default <T> void setProperty(ItemStack gunItem, String property, T value) {
        @Nullable IGunDataAccess gunDataAccess = IGunGetter.fromItemStack(gunItem);
        if (gunDataAccess == null) return;
        setProperty(gunDataAccess, gunItem, GunProperty.fromString(property), value);
    }
    @Deprecated(forRemoval = false)
    default <T> void setProperty(LivingEntity livingEntity, String property, T value) {
        @Nullable IGunDataAccess gunDataAccess = IGunGetter.fromMainHand(livingEntity);
        if (gunDataAccess == null) return;
        setProperty(gunDataAccess, livingEntity.getMainHandItem(), GunProperty.fromString(property), value);
    }

    // --------Deprecated--------

    @Deprecated default <T> T modifyProperty(ItemStack gunItem, String property, T value) {
        setProperty(gunItem, property, value);
        return getProperty(gunItem, property);
    }
    @Deprecated default <T> T modifyProperty(LivingEntity livingEntity, String property, T value) {
        setProperty(livingEntity, property, value);
        return getProperty(livingEntity, property);
    }
}

/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.api.item.gun.modifier;

import dev.xcolorful.customgun.core.api.item.IGun;
import dev.xcolorful.customgun.core.api.item.IItemModifier;
import dev.xcolorful.customgun.core.resource.ResourcePojo;
import dev.xcolorful.customgun.core.resource.data.data.GunData;
import dev.xcolorful.customgun.core.util.ScriptUtils;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/*
文档译名: 枪械修饰工具 (XiaoColorful译)
 */
/**
 * 枪械修饰工具
 */
public interface IGunModifier<T extends ResourcePojo<T>, K, V> extends IItemModifier<T, K, V> {

    @Nullable V getBase(@NotNull IGun iGun, @NotNull ItemStack gunItem,
                        @NotNull GunData gunData);

    /**
     * @param base 初值
     * @param value 输入变量/当前计算值
     * @param scriptFunction 脚本
     * @return 脚本修改后的值
     */
    default V evalByScript(V base, V value, String scriptFunction) {
        return value;
    }

    static Float evalSimpleModifierDataByScript(Float base, Float value, @Nullable String scriptFunction) {
        if (scriptFunction == null || scriptFunction.isEmpty()) return value;
        return ScriptUtils.eval(base, value, scriptFunction);
    }
}

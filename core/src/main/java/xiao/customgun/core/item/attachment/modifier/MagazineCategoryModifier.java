/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.item.attachment.modifier;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import xiao.customgun.core.api.item.IGun;
import xiao.customgun.core.api.item.attachment.MagazineCategory;
import xiao.customgun.core.resource.data.data.AttachmentData;
import xiao.customgun.core.resource.data.data.GunData;

import java.util.Collection;

public final class MagazineCategoryModifier extends AttachmentModifier<MagazineCategory, MagazineCategory> {
    public static final MagazineCategoryModifier INSTANCE = new MagazineCategoryModifier();

    // --------IAttachmentModifier--------

    @Override
    public MagazineCategory getModifier(@NotNull AttachmentData pojo) {
        return pojo.getMagazineCategory();
    }

    @Override
    public MagazineCategory eval(Collection<MagazineCategory> modifiers, MagazineCategory base) {
        // TODO: eval 不能复用父类函数 — 弹匣类别不是数值，取最高等级
        return base;
    }

    // --------IGunModifier--------

    @Override
    public MagazineCategory getBase(@NotNull IGun iGun, @NotNull ItemStack gunItem, @NotNull GunData gunData) {
        return MagazineCategory.NONE;
    }
}

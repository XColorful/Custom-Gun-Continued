/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.item.attachment.modifier;

import dev.xcolorful.customgun.core.api.item.attachment.MagazineCategory;
import dev.xcolorful.customgun.core.api.item.gun.modifier.IMagazineCategoryModifier;
import dev.xcolorful.customgun.core.resource.data.data.AttachmentData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public final class MagazineCategoryModifier extends AttachmentModifier<MagazineCategory, MagazineCategory>
        implements IMagazineCategoryModifier<AttachmentData> {
    public static final MagazineCategoryModifier INSTANCE = new MagazineCategoryModifier();

    // --------IAttachmentModifier--------

    @Override
    public @Nullable MagazineCategory getModifier(@NotNull AttachmentData pojo) {
        return pojo.getMagazineCategory();
    }

    @Override
    public MagazineCategory eval(Collection<MagazineCategory> modifiers, MagazineCategory base) {
        for (MagazineCategory modifier : modifiers) {
            if (modifier != MagazineCategory.NONE) {
                return modifier;
            }
        }
        return base;
    }
}

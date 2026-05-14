/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.item.attachment;

import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.api.resource.ResourceTag;

import java.util.HashMap;
import java.util.Map;

public enum MagazineCategory implements ResourceTag.CategoryTag {
    EXTENDED_MAG_1(1, MagazineCategoryTag.EXTENDED_MAG_1),
    EXTENDED_MAG_2(2, MagazineCategoryTag.EXTENDED_MAG_2),
    EXTENDED_MAG_3(3, MagazineCategoryTag.EXTENDED_MAG_3),
    NONE(0, MagazineCategoryTag.NONE);

    public final int index;
    public final String categoryName;
    MagazineCategory(int index, String name) {
        this.index = index;
        this.categoryName = name;
    }

    @Override public String getTagName() {
        return this.categoryName;
    }
    public int getIndex() {
        return this.index;
    }
    @Override public String getCategoryName() {
        return this.categoryName;
    }

    private static final Map<String, MagazineCategory> CATEGORIES = new HashMap<>();
    private static final Map<Integer, MagazineCategory> INDEXES = new HashMap<>();

    static {
        for (MagazineCategory type : values()) {
            CATEGORIES.put(type.categoryName, type);
            INDEXES.put(type.index, type);
        }
    }

    public static @Nullable MagazineCategory fromString(String name) {
        return name != null ? CATEGORIES.get(name) : null;
    }

    @Override
    public String toString() {
        return this.categoryName;
    }

    public static @Nullable MagazineCategory fromIndex(int index) {
        return INDEXES.get(index);
    }
    public int toIndex() {
        return this.index;
    }
}
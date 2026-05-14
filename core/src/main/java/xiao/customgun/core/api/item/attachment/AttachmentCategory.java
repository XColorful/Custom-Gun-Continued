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

/*
 * 按从枪口到枪托的顺序排 (枪口 - 激光指示器 - 握把 - 弹匣 - 瞄准镜 - 枪托)
 */
public enum AttachmentCategory implements ResourceTag.CategoryTag {
    /**
     * 枪口组件
     */
    MUZZLE(AttachmentCategoryTag.MUZZLE),
    /**
     * 激光指示器
     */
    LASER(AttachmentCategoryTag.LASER),
    /**
     * 握把
     */
    GRIP(AttachmentCategoryTag.GRIP),
    /**
     * 扩容弹夹（匣）
     */
    EXTENDED_MAG(AttachmentCategoryTag.EXTENDED_MAG),
    /**
     * 瞄具
     */
    SCOPE(AttachmentCategoryTag.SCOPE),
    /**
     * 枪托
     */
    STOCK(AttachmentCategoryTag.STOCK),
    /**
     * 用于兼容已有网络包解析
     */
    NONE(AttachmentCategoryTag.NONE);

    public final String categoryName;
    AttachmentCategory(String name) {
        this.categoryName = name;
    }

    @Override public String getTagName() {
        return this.categoryName;
    }
    @Override public String getCategoryName() {
        return this.categoryName;
    }

    private static final Map<String, AttachmentCategory> CATEGORIES = new HashMap<>();

    static {
        for (AttachmentCategory type : values()) {
            CATEGORIES.put(type.categoryName, type);
        }
    }

    public static @Nullable AttachmentCategory fromString(String name) {
        return name != null ? CATEGORIES.get(name) : null;
    }

    @Override
    public String toString() {
        return this.categoryName;
    }
}
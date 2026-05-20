/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.item.attachment;

import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.api.item.GunProperty;
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
    MUZZLE(AttachmentCategoryTag.MUZZLE, null),
    /**
     * 激光指示器
     */
    LASER(AttachmentCategoryTag.LASER, null),
    /**
     * 握把
     */
    GRIP(AttachmentCategoryTag.GRIP, null),
    /**
     * 扩容弹夹（匣）
     */
    MAGAZINE(AttachmentCategoryTag.MAGAZINE, AttachmentCategoryTag.MAGAZINE_OLD1),
    /**
     * 瞄具
     */
    SCOPE(AttachmentCategoryTag.SCOPE, null),
    /**
     * 枪托
     */
    STOCK(AttachmentCategoryTag.STOCK, null),
    /**
     * 用于兼容已有网络包解析
     */
    NONE(AttachmentCategoryTag.NONE, null);

    public final String tagName;
    public final String categoryName;
    public final String categoryNameOld;
    AttachmentCategory(String name, String nameOld) {
        this.tagName = GunProperty.ATTACHMENT_PREFIX.getTagName() + name;
        this.categoryName = name;
        this.categoryNameOld = nameOld;
    }

    @Override public String getTagName() {
        return this.tagName;
    }
    @Override public String getCategoryName() {
        return this.categoryName;
    }

    private static final Map<String, AttachmentCategory> CATEGORIES = new HashMap<>();

    static {
        for (AttachmentCategory type : values()) {
            CATEGORIES.put(type.categoryName, type);
            if (type.categoryNameOld != null) CATEGORIES.put(type.categoryNameOld, type);
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
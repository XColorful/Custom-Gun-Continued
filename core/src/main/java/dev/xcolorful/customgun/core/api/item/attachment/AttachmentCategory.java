/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.api.item.attachment;

import dev.xcolorful.customgun.core.api.item.GunProperty;
import dev.xcolorful.customgun.core.api.resource.ResourceTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/*
 * 按从枪口到枪托的顺序排 (枪口 - 激光指示器 - 握把 - 弹匣 - 瞄准镜 - 枪托)
 */
public enum AttachmentCategory implements ResourceTag.CategoryTag, ResourceTag.ConstantTag {
    /**
     * 枪口组件
     */
    MUZZLE(AttachmentCategoryTag.MUZZLE, null,
            Component.translatable("customgun.attachmentcategory.muzzle")),
    /**
     * 激光指示器
     */
    LASER(AttachmentCategoryTag.LASER, null,
            Component.translatable("customgun.attachmentcategory.laser")),
    /**
     * 握把
     */
    GRIP(AttachmentCategoryTag.GRIP, null,
            Component.translatable("customgun.attachmentcategory.grip")),
    /**
     * 扩容弹夹（匣）
     */
    MAGAZINE(AttachmentCategoryTag.MAGAZINE, AttachmentCategoryTag.MAGAZINE_OLD1,
            Component.translatable("customgun.attachmentcategory.magazine")),
    /**
     * 瞄具
     */
    SCOPE(AttachmentCategoryTag.SCOPE, null,
            Component.translatable("customgun.attachmentcategory.scope")),
    /**
     * 枪托
     */
    STOCK(AttachmentCategoryTag.STOCK, null,
            Component.translatable("customgun.attachmentcategory.stock")),
    /**
     * 用于兼容已有网络包解析
     */
    NONE(AttachmentCategoryTag.NONE, null,
            Component.translatable("customgun.attachmentcategory.none"));

    public final String tagName;
    public final String categoryName;
    public final String categoryNameOld;
    public final String constantName;
    public final MutableComponent categoryLang;
    AttachmentCategory(String name, String nameOld, MutableComponent lang) {
        this.tagName = GunProperty.ATTACHMENT_PREFIX.getTagName() + name;
        this.categoryName = name;
        this.categoryNameOld = nameOld;
        this.categoryLang = lang;
        this.constantName = name;
    }
    @Override public String getTagName() {
        return this.tagName;
    }
    @Override public String getCategoryName() {
        return this.categoryName;
    }
    @Override public String getConstantName() {
        return this.constantName;
    }

    public final MutableComponent getCategoryLang() {
        return this.categoryLang;
    }

    private static final Map<String, AttachmentCategory> CATEGORIES = new HashMap<>();

    static {
        for (AttachmentCategory type : values()) {
            CATEGORIES.put(type.tagName, type);
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
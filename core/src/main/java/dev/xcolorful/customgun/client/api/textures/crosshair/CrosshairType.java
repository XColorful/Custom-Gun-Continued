/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.api.textures.crosshair;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.client.api.resource.assets.textures.CrosshairFolderType;
import dev.xcolorful.customgun.client.api.resource.assets.textures.TextureSubFolderType;
import dev.xcolorful.customgun.client.config.RenderConfig;
import dev.xcolorful.customgun.core.api.resource.ResourceTag;
import dev.xcolorful.customgun.core.api.resource.assets.textures.crosshair.CrosshairTag;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 保留枚举仅用于兼容旧的准心方式
 * 使用{@link RenderConfig#REPLACE_VANILLA_CROSSHAIR}可一键隐藏
 */
public enum CrosshairType implements ResourceTag {
    EMPTY(CrosshairFolderType.NORMAL, CrosshairTag.EMPTY),

    DOT_1(CrosshairFolderType.NORMAL, CrosshairTag.DOT_1),

    // --------Deprecated--------
    // 一般用不着，或者直接用扩展模组自制的准心效果了
    
    @Deprecated(forRemoval = false)
    CIRCLE_1(CrosshairFolderType.NORMAL, CrosshairTag.CIRCLE_1),
    @Deprecated(forRemoval = false)
    CIRCLE_2(CrosshairFolderType.NORMAL, CrosshairTag.CIRCLE_2),
    @Deprecated(forRemoval = false)
    CIRCLE_3(CrosshairFolderType.NORMAL, CrosshairTag.CIRCLE_3),

    @Deprecated(forRemoval = false)
    CROSS_1(CrosshairFolderType.NORMAL, CrosshairTag.CROSS_1),
    @Deprecated(forRemoval = false)
    CROSS_2(CrosshairFolderType.NORMAL, CrosshairTag.CROSS_2),
    @Deprecated(forRemoval = false)
    CROSS_3(CrosshairFolderType.NORMAL, CrosshairTag.CROSS_3),
    @Deprecated(forRemoval = false)
    CROSS_4(CrosshairFolderType.NORMAL, CrosshairTag.CROSS_4),
    @Deprecated(forRemoval = false)
    CROSS_5(CrosshairFolderType.NORMAL, CrosshairTag.CROSS_5),
    @Deprecated(forRemoval = false)
    CROSS_6(CrosshairFolderType.NORMAL, CrosshairTag.CROSS_6),

    @Deprecated(forRemoval = false)
    LINE_1(CrosshairFolderType.NORMAL, CrosshairTag.LINE_1),
    @Deprecated(forRemoval = false)
    LINE_2(CrosshairFolderType.NORMAL, CrosshairTag.LINE_2),
    @Deprecated(forRemoval = false)
    LINE_3(CrosshairFolderType.NORMAL, CrosshairTag.LINE_3),

    @Deprecated(forRemoval = false)
    SQUARE_1(CrosshairFolderType.NORMAL, CrosshairTag.SQUARE_1),
    @Deprecated(forRemoval = false)
    SQUARE_2(CrosshairFolderType.NORMAL, CrosshairTag.SQUARE_2),
    @Deprecated(forRemoval = false)
    SQUARE_3(CrosshairFolderType.NORMAL, CrosshairTag.SQUARE_3),
    @Deprecated(forRemoval = false)
    SQUARE_4(CrosshairFolderType.NORMAL, CrosshairTag.SQUARE_4),
    @Deprecated(forRemoval = false)
    SQUARE_5(CrosshairFolderType.NORMAL, CrosshairTag.SQUARE_5),
    @Deprecated(forRemoval = false)
    SQUARE_6(CrosshairFolderType.NORMAL, CrosshairTag.SQUARE_6),

    @Deprecated(forRemoval = false)
    TRIDENT_1(CrosshairFolderType.NORMAL, CrosshairTag.TRIDENT_1),
    @Deprecated(forRemoval = false)
    TRIDENT_2(CrosshairFolderType.NORMAL, CrosshairTag.TRIDENT_2);

    // {namespace}:textures/{crosshair}/{CrosshairType.folderType}/{filename}.png
    public static final String LOCATION_FORMAT = "%s:textures/%s/%s/%s.png";

    public final CrosshairFolderType folderType;
    public final String crosshairTag;
    private final ResourceLocation fastDefaultRl;
    CrosshairType(CrosshairFolderType folderType, String crosshairTag) {
        this.folderType = folderType;
        this.crosshairTag = crosshairTag;
        this.fastDefaultRl = CustomGun.getMcRegistry().createResourceLocation(String.format(LOCATION_FORMAT,
                CustomGun.MOD_ID,
                TextureSubFolderType.CROSSHAIR.getFolderName(),
                this.folderType.getFolderName(),
                this.getTagName()));
    }
    @Override public String getTagName() {
        return this.crosshairTag;
    }

    /**
     * 模组内置的事件监听器调用，无 Hash get 开销，线程安全
     * 扩展模组直接走别的获取/注入方式
     */
    public ResourceLocation getLocationFast() {
        return this.fastDefaultRl;
    }

    // {名称}.png -> textures/crosshair/{normal}/{名称}.png
    private static final Map<String, ResourceLocation> CACHE = new ConcurrentHashMap<>(); // 防止扩展模组不在渲染线程处理

    static {
        for (CrosshairType type : CrosshairType.values()) {
            CACHE.put(type.getTagName(),type.fastDefaultRl);
        }
    }

    public static void addCrosshairType(CrosshairFolderType folderType, String crosshairTag) {
        addCrosshairType(folderType.getFolderName(), crosshairTag);
    }
    public static ResourceLocation addCrosshairType(String folderType, String crosshair) {
        var location = CustomGun.getMcRegistry().createResourceLocation(String.format(LOCATION_FORMAT,
                CustomGun.MOD_ID,
                TextureSubFolderType.CROSSHAIR.getFolderName(),
                folderType,
                crosshair));
        CACHE.put(crosshair, location); // 返回的是旧值
        return location;
    }

    public static @Nullable ResourceLocation getTextureLocation(CrosshairType crosshair) {
        return getTextureLocation(crosshair.getTagName());
    }
    public static @Nullable ResourceLocation getTextureLocation(String crosshair) {
        return CACHE.get(crosshair);
    }
    public static ResourceLocation getOrAddTextureLocation(CrosshairType crosshair) {
        return getOrAddTextureLocation(CrosshairFolderType.NORMAL.getFolderName(), crosshair.getTagName());
    }
    public static ResourceLocation getOrAddTextureLocation(String folderType, String crosshair) {
        var location = CACHE.get(crosshair);
        return location != null ? location : addCrosshairType(folderType, crosshair);
    }
}

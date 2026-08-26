/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.api.textures.crosshair;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.client.api.minecraft.texture.CustomTexture;
import dev.xcolorful.customgun.client.api.resource.assets.textures.CrosshairFolderType;
import dev.xcolorful.customgun.client.api.resource.assets.textures.TextureSubFolderType;
import dev.xcolorful.customgun.client.config.RenderConfig;
import dev.xcolorful.customgun.core.api.resource.ResourceTag;
import dev.xcolorful.customgun.core.api.resource.assets.textures.crosshair.CrosshairTag;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 保留枚举仅用于兼容旧的准心方式
 * 使用{@link RenderConfig#REPLACE_VANILLA_CROSSHAIR}可一键隐藏
 */
public enum CrosshairType implements ResourceTag {
    DEFAULT(CrosshairTag.DEFAULT, CustomTexture.CROSSHAIR.getLocation()),
    BLANK(CrosshairTag.BLANK, CustomTexture.BLANK_128x128.getLocation()),

    // --------Deprecated--------
    // 一般用不着，或者直接用扩展模组自制的准心绘制了

    @Deprecated(forRemoval = false)
    EMPTY(CrosshairFolderType.NORMAL, CrosshairTag.EMPTY),

    @Deprecated(forRemoval = false)
    DOT_1(CrosshairFolderType.NORMAL, CrosshairTag.DOT_1),

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
    @Deprecated
    public static final String LOCATION_FORMAT = "%s:textures/%s/%s/%s.png";

    public final String crosshairTag;
    private final Identifier textureLocation;
    CrosshairType(String crosshairTag, Identifier textureLocation) {
        this.textureLocation = textureLocation;
        this.crosshairTag = crosshairTag;
    }
    @SuppressWarnings("all")
    @Deprecated
    CrosshairType(CrosshairFolderType folderType, String crosshairTag) {
        this(crosshairTag, CustomGun.getMcRegistry().createResourceLocation(String.format(LOCATION_FORMAT,
                CustomGun.MOD_ID,
                TextureSubFolderType.CROSSHAIR.getFolderName(),
                folderType.getFolderName(),
                crosshairTag)));
    }
    @Override public String getTagName() {
        return this.crosshairTag;
    }

    public Identifier getTextureLocation() {
        return this.textureLocation;
    }

    // {名称}.png -> textures/crosshair/{normal}/{名称}.png
    private static final Map<String, Identifier> CROSSHAIR_TYPES = new ConcurrentHashMap<>(); // 防止扩展模组不在渲染线程处理

    static {
        for (CrosshairType type : CrosshairType.values()) {
            CROSSHAIR_TYPES.put(type.getTagName(),type.textureLocation);
            CROSSHAIR_TYPES.put(type.getTextureLocation().toString(),type.textureLocation);
        }
    }

    public static void addCrosshairType(String crosshairTag, Identifier textureLocation) {
        CROSSHAIR_TYPES.put(crosshairTag, textureLocation);
    }

    public static @Nullable Identifier getTextureLocation(CrosshairType crosshair) {
        return getTextureLocation(crosshair.getTagName());
    }
    public static @Nullable Identifier getTextureLocation(String crosshair) {
        return CROSSHAIR_TYPES.get(crosshair);
    }
}

/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.api.resource.assets.textures.crosshair;

import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.CustomGun;
import xiao.customgun.client.api.resource.assets.textures.CrosshairFolderType;
import xiao.customgun.core.api.resource.ResourceTag;
import xiao.customgun.core.api.resource.assets.textures.crosshair.CrosshairTag;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public enum CrosshairType implements ResourceTag {
    EMPTY(CrosshairFolderType.NORMAL, CrosshairTag.EMPTY),

    DOT_1(CrosshairFolderType.NORMAL, CrosshairTag.DOT_1),

    CIRCLE_1(CrosshairFolderType.NORMAL, CrosshairTag.CIRCLE_1),
    CIRCLE_2(CrosshairFolderType.NORMAL, CrosshairTag.CIRCLE_2),
    CIRCLE_3(CrosshairFolderType.NORMAL, CrosshairTag.CIRCLE_3),

    CROSS_1(CrosshairFolderType.NORMAL, CrosshairTag.CROSS_1),
    CROSS_2(CrosshairFolderType.NORMAL, CrosshairTag.CROSS_2),
    CROSS_3(CrosshairFolderType.NORMAL, CrosshairTag.CROSS_3),
    CROSS_4(CrosshairFolderType.NORMAL, CrosshairTag.CROSS_4),
    CROSS_5(CrosshairFolderType.NORMAL, CrosshairTag.CROSS_5),
    CROSS_6(CrosshairFolderType.NORMAL, CrosshairTag.CROSS_6),

    LINE_1(CrosshairFolderType.NORMAL, CrosshairTag.LINE_1),
    LINE_2(CrosshairFolderType.NORMAL, CrosshairTag.LINE_2),
    LINE_3(CrosshairFolderType.NORMAL, CrosshairTag.LINE_3),

    SQUARE_1(CrosshairFolderType.NORMAL, CrosshairTag.SQUARE_1),
    SQUARE_2(CrosshairFolderType.NORMAL, CrosshairTag.SQUARE_2),
    SQUARE_3(CrosshairFolderType.NORMAL, CrosshairTag.SQUARE_3),
    SQUARE_4(CrosshairFolderType.NORMAL, CrosshairTag.SQUARE_4),
    SQUARE_5(CrosshairFolderType.NORMAL, CrosshairTag.SQUARE_5),
    SQUARE_6(CrosshairFolderType.NORMAL, CrosshairTag.SQUARE_6),

    TRIDENT_1(CrosshairFolderType.NORMAL, CrosshairTag.TRIDENT_1),
    TRIDENT_2(CrosshairFolderType.NORMAL, CrosshairTag.TRIDENT_2);

    public static final String LOCATION_FORMAT = "%s:textures/crosshair/%s/%s.png";

    public final CrosshairFolderType folderType;
    public final String crosshairTag;
    private final Identifier fastDefaultRl;
    CrosshairType(CrosshairFolderType folderType, String crosshairTag) {
        this.folderType = folderType;
        this.crosshairTag = crosshairTag;
        this.fastDefaultRl = CustomGun.getMcRegistry().createResourceLocation(String.format(LOCATION_FORMAT,
                CustomGun.MOD_ID,
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
    public Identifier getLocationFast() {
        return this.fastDefaultRl;
    }

    // {名称}.png -> textures/crosshair/{normal}/{名称}.png
    private static final Map<String, Identifier> CACHE = new ConcurrentHashMap<>(); // 防止扩展模组不再渲染线程处理

    static {
        for (CrosshairType type : CrosshairType.values()) {
            CACHE.put(type.getTagName(),type.fastDefaultRl);
        }
    }

    public static void addCrosshairType(CrosshairFolderType folderType, String crosshairTag) {
        addCrosshairType(folderType.getFolderName(), crosshairTag);
    }
    public static Identifier addCrosshairType(String folderType, String crosshair) {
        var location = CustomGun.getMcRegistry().createResourceLocation(String.format(LOCATION_FORMAT,
                CustomGun.MOD_ID,
                folderType,
                crosshair));
        CACHE.put(crosshair, location); // 返回的是旧值
        return location;
    }

    public static @Nullable Identifier getTextureLocation(CrosshairType crosshair) {
        return getTextureLocation(crosshair.getTagName());
    }
    public static @Nullable Identifier getTextureLocation(String crosshair) {
        return CACHE.get(crosshair);
    }
    public static Identifier getOrAddTextureLocation(CrosshairType crosshair) {
        return getOrAddTextureLocation(CrosshairFolderType.NORMAL.getFolderName(), crosshair.getTagName());
    }
    public static Identifier getOrAddTextureLocation(String folderType, String crosshair) {
        var location = CACHE.get(crosshair);
        return location != null ? location : addCrosshairType(folderType, crosshair);
    }
}

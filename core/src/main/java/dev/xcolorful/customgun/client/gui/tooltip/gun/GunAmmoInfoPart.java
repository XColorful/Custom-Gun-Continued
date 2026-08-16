/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.gui.tooltip.gun;

import dev.xcolorful.customgun.client.api.item.gun.GunTooltipMask;
import dev.xcolorful.customgun.client.gui.tooltip.AbstractTooltipPart;
import dev.xcolorful.customgun.client.resource.assets.display.GunDisplay;
import dev.xcolorful.customgun.client.resource.instance.assets.GunDisplayInstance;
import dev.xcolorful.customgun.core.api.item.IGun;
import dev.xcolorful.customgun.core.api.item.gun.AmmoCountType;
import dev.xcolorful.customgun.core.api.item.gun.BoltType;
import dev.xcolorful.customgun.core.api.minecraft.Color64;
import dev.xcolorful.customgun.core.developer.PlannedRefactor;
import dev.xcolorful.customgun.core.resource.data.data.GunData;
import dev.xcolorful.customgun.core.resource.instance.data.GunIndexInstance;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

/*
|物  | 子弹名称
|  品| 弹药/总数
 */
public final class GunAmmoInfoPart extends AbstractTooltipPart implements GunTooltipPart {
    public static final GunAmmoInfoPart INSTANCE = new GunAmmoInfoPart();
    private GunAmmoInfoPart() {}

    /**
     * 文字需要往右 1个物品 + 行距离
     */
    private static final int _textXOffset = itemWidth + textLineSpacing;
    private static final int _height = textLineSpacing + Math.max(itemHeight, textLineHeight + textLineSpacing + textLineHeight);
    private static final Color64 _defaultAmmoNameColor = Color64.fromChatFormatting(ChatFormatting.GOLD);
    private static final Color64 _defaultAmmoCountColor = Color64.fromChatFormatting(ChatFormatting.DARK_GRAY);
    private static final boolean hasTextShadow = true;

    @Override
    public void build(ClientGunTooltip.Context context) {
        Font font = Minecraft.getInstance().font;

        Component hoverName; {
            hoverName = context.gunTooltip.ammoItem().getHoverName();
            context.view.ammoName = hoverName;
            context.widenMaxWidth(_textXOffset + font.width(hoverName));
        }

        /**
         * TaCZ到处都在重新拉一大坨枪的逻辑，是其根深蒂固的问题
         * 这里只显示枪的子弹
         * 如果要硬核，唯一标准应为{@link IGun#consumeAmmoOnce}，即以枪的逻辑为标准
         */
        if (PlannedRefactor.UNIFY_GUN_API) {}

        IGun iGun = context.gunTooltip.iGun();
        ItemStack gunItem = context.gunTooltip.gunItem();

        // 弹匣大小
        int magAmmoLimit = iGun.getMagAmmoLimit(gunItem);
        // 当前枪内子弹
        int currentAmmoCount; {
            @Nullable GunIndexInstance gunIndexInstance = context.gunIndexInstance;
            if (gunIndexInstance != null) {
                GunData gunData = gunIndexInstance.getGunData();
                BoltType boltType = gunData.getBoltType();
                currentAmmoCount = iGun.getMagAmmoCountWithBarrel(gunItem, boltType);
            } else {
                currentAmmoCount = iGun.getMagAmmoCount(gunItem);
            }
        }

        @Nullable GunDisplayInstance gunDisplayInstance = context.gunDisplayInstance;
        AmmoCountType ammoCountType; {
            if (gunDisplayInstance != null) {
                GunDisplay gunDisplay = gunDisplayInstance.getPojo();
                ammoCountType = gunDisplay.getAmmoCountType();
            } else {
                ammoCountType = AmmoCountType.NORMAL;
            }
        }

        _buildAmmoCount(context, font, ammoCountType, currentAmmoCount, magAmmoLimit);
    }
    private int _buildAmmoCount(ClientGunTooltip.Context context,
                                Font font,
                                AmmoCountType ammoCountType,
                                int currentAmmoCount, int magAmmoLimit) {
        MutableComponent ammoCount;
        return switch (ammoCountType) {
            case NORMAL -> {
                ammoCount = Component.literal(String.format("%s/%s", currentAmmoCount, magAmmoLimit));
                context.view.ammoCount = ammoCount;
                context.widenMaxWidth(_textXOffset + font.width(ammoCount));
                yield 1;
            }
            case PERCENT -> {
                if (magAmmoLimit == 0) magAmmoLimit = 1;
                ammoCount = Component.literal(String.format("%.1f%%", 100f * currentAmmoCount / magAmmoLimit));
                context.view.ammoCount = ammoCount;
                context.widenMaxWidth(_textXOffset + font.width(ammoCount));
                yield 1;
            }
            // 增加类型使此处强制编译不通过
        };
    }

    @Override
    public int measureHeight(ClientGunTooltip.Context context) {
        if (!context.visibleParts.contains(GunTooltipMask.AMMO_INFO)) return 0;

        return _height;
    }

    @Override
    public void renderText(ClientGunTooltip.Context context,
                           Font font,
                           int pX, int pY,
                           Matrix4f matrix4f,
                           MultiBufferSource.BufferSource bufferSource) {
        int xOffset = pX + _textXOffset;
        int yOffset = pY;

        if (context.view.ammoName != null) {
            yOffset += textLineSpacing;

            // 弹药名
            font.drawInBatch(context.view.ammoName,
                    xOffset, yOffset,
                    _defaultAmmoNameColor.getRGB(),
                    hasTextShadow,
                    matrix4f,
                    bufferSource,
                    Font.DisplayMode.NORMAL,
                    0,
                    packedLightCoords);
            yOffset += textLineHeight;
        }

        if (context.view.ammoCount != null) {
            yOffset += textLineSpacing;

            // 弹药数
            font.drawInBatch(context.view.ammoCount,
                    xOffset, yOffset,
                    _defaultAmmoCountColor.getRGB(),
                    hasTextShadow,
                    matrix4f,
                    bufferSource,
                    Font.DisplayMode.NORMAL,
                    0,
                    packedLightCoords);
            yOffset += textLineHeight;
        }
    }

    @Override
    public void renderImage(ClientGunTooltip.Context context,
                            Font font,
                            int pX, int pY,
                            GuiGraphics guiGraphics) {
        int yOffset = pY + textLineSpacing;

        { // 子弹物品
            guiGraphics.renderItem(context.gunTooltip.ammoItem(),
                    pX,
                    yOffset);
        }
    }
}

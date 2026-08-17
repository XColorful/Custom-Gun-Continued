/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.gui.tooltip.gun;

import dev.xcolorful.customgun.client.api.item.gun.GunTooltipMask;
import dev.xcolorful.customgun.client.api.resource.ClientResourceApi;
import dev.xcolorful.customgun.client.config.RenderConfig;
import dev.xcolorful.customgun.client.gui.tooltip.AbstractTooltipPart;
import dev.xcolorful.customgun.client.resource.assets.info.GunpackInfo;
import dev.xcolorful.customgun.core.api.item.gun.GunCategory;
import dev.xcolorful.customgun.core.api.minecraft.Color64;
import dev.xcolorful.customgun.core.api.resource.ResourceApi;
import dev.xcolorful.customgun.core.resource.data.index.GunIndex;
import dev.xcolorful.customgun.core.resource.instance.data.GunIndexInstance;
import dev.xcolorful.customgun.core.util.ComponentUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

/**
 * 扩展模组可以考虑改成左边渲染一个枪包logo/物品，然后再渲染文本的方式
 */
/*
原版tab分类
枪包名
资源位置
 */
public final class GunDetailInfoPart extends AbstractTooltipPart implements GunTooltipPart {
    public static final GunDetailInfoPart INSTANCE = new GunDetailInfoPart();
    private GunDetailInfoPart() {}

    private static final Color64 _defaultCategoryColor = Color64.fromChatFormatting(ChatFormatting.BLUE);
    private static final Color64 _defaultPackColor = Color64.fromChatFormatting(ChatFormatting.BLUE);
    private static final Color64 _defaultLocationColor = _defaultCategoryColor; // MC用的DARK_GRAY，改成跟category一样
    private static final boolean hasTextShadow = true;

    @Override
    public void build(ClientGunTooltip.Context context) {
        Font font = Minecraft.getInstance().font;

        var gunLocation = context.gunTooltip.gunLocation();

        Component category; {
            GunCategory gunCategory; {
                @Nullable GunIndexInstance gunIndexInstance = ResourceApi.getGunIndexInstance(gunLocation);
                if (gunIndexInstance != null) {
                    GunIndex gunIndex = gunIndexInstance.getPojo();
                    gunCategory = gunIndex.getGunCategory();
                } else {
                    gunCategory = GunCategory.CUSTOM;
                }
            }

            category = gunCategory.getCategoryLang().copy();
        }
        context.view.category = category;
        context.widenMaxWidth(font.width(category));
        context.showCategory = context.isCreative;

        Component packInfo; {
            @Nullable GunpackInfo gunpackInfo = ClientResourceApi.getGunpackInfo(gunLocation.getNamespace());
            if (gunpackInfo != null) {
                packInfo = Component.translatable(gunpackInfo.getNameLang())
                        .withStyle(ChatFormatting.ITALIC);
            } else {
                packInfo = ComponentUtils.unknownTranslatableKey();
            }
        }
        context.view.packInfo = packInfo;
        context.widenMaxWidth(font.width(context.view.packInfo));
        context.showPackInfo = context.isCreative && context.isAdvanced;

        context.view.pojoLocation = Component.literal(gunLocation.toString());
        context.showPojoLocation = context.showPackInfo && RenderConfig.APPEND_RESOURCE_LOCATION_IN_TOOLTIP.get();
    }

    @Override
    public int measureHeight(ClientGunTooltip.Context context) {
        if (!context.visibleParts.contains(GunTooltipMask.DETAIL_INFO)) return 0;

        int height = 0;

        if (context.showCategory && context.view.category != null) height += textLineSpacing + textLineHeight;
        if (context.showPackInfo && context.view.packInfo != null) height += textLineSpacing + textLineHeight;
        if (context.showPojoLocation && context.view.pojoLocation != null) height += textLineSpacing + textLineHeight;

        return height;
    }

    @Override
    public void renderText(ClientGunTooltip.Context context,
                           Font font, int pX, int pY,
                           Matrix4f matrix4f,
                           MultiBufferSource.BufferSource bufferSource) {
        int xOffset = pX;
        int yOffset = pY;

        if (context.showCategory && context.view.category != null) {
            yOffset += textLineSpacing;

            // 枪械类型
            font.drawInBatch(context.view.category,
                    xOffset, yOffset,
                    _defaultCategoryColor.getRGB(),
                    hasTextShadow,
                    matrix4f,
                    bufferSource,
                    Font.DisplayMode.NORMAL,
                    0,
                    packedLightCoords);
            yOffset += textLineHeight;
        }

        if (context.showPackInfo && context.view.packInfo != null) {
            yOffset += textLineSpacing;

            // 枪包信息
            font.drawInBatch(context.view.packInfo,
                    xOffset, yOffset,
                    _defaultPackColor.getRGB(),
                    hasTextShadow,
                    matrix4f,
                    bufferSource,
                    Font.DisplayMode.NORMAL,
                    0, packedLightCoords);
            yOffset += textLineHeight;
        }

        if (context.showPojoLocation && context.view.pojoLocation != null) {
            yOffset += textLineSpacing;

            // 资源位置
            font.drawInBatch(context.view.pojoLocation,
                    xOffset, yOffset,
                    _defaultLocationColor.getRGB(),
                    hasTextShadow,
                    matrix4f,
                    bufferSource,
                    Font.DisplayMode.NORMAL,
                    0, packedLightCoords);
            yOffset += textLineHeight;
        }
    }

    @Override
    public void renderImage(ClientGunTooltip.Context context,
                            Font font, int pX, int pY,
                            GuiGraphics guiGraphics) {
        // mixin注入点
    }
}

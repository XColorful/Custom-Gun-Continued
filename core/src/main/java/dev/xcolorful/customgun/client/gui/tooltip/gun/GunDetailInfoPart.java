/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.gui.tooltip.gun;

import dev.xcolorful.customgun.client.api.item.gun.GunTooltipMask;
import dev.xcolorful.customgun.client.config.RenderConfig;
import dev.xcolorful.customgun.client.gui.tooltip.AbstractTooltipPart;
import dev.xcolorful.customgun.client.resource.assets.info.GunpackInfo;
import dev.xcolorful.customgun.core.api.item.gun.GunCategory;
import dev.xcolorful.customgun.core.api.minecraft.Color64;
import dev.xcolorful.customgun.core.resource.data.index.GunIndex;
import dev.xcolorful.customgun.core.resource.instance.data.GunIndexInstance;
import dev.xcolorful.customgun.core.util.ComponentUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

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
    public void build(ClientGunTooltip.Context context, Font font) {
        var gunLocation = context.gunTooltip.gunLocation();

        Component category; {
            GunCategory gunCategory; {
                @Nullable GunIndexInstance gunIndexInstance = context.gunIndexInstance;
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
            @Nullable GunpackInfo gunpackInfo = context.gunpackInfo;
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
                           int startX, int startY) {
        int currentX = startX;
        int currentY = startY;

        if (context.showCategory && context.view.category != null) {
            currentY += textLineSpacing;

            // 枪械类型
            context.drawText(context.view.category,
                    currentX, currentY,
                    _defaultCategoryColor.getRGB(),
                    hasTextShadow);
            currentY += textLineHeight;
        }

        if (context.showPackInfo && context.view.packInfo != null) {
            currentY += textLineSpacing;

            // 枪包信息
            context.drawText(context.view.packInfo,
                    currentX, currentY,
                    _defaultPackColor.getRGB(),
                    hasTextShadow);
            currentY += textLineHeight;
        }

        if (context.showPojoLocation && context.view.pojoLocation != null) {
            currentY += textLineSpacing;

            // 资源位置
            context.drawText(context.view.pojoLocation,
                    currentX, currentY,
                    _defaultLocationColor.getRGB(),
                    hasTextShadow);
            currentY += textLineHeight;
        }
    }

    @Override
    public void renderImage(ClientGunTooltip.Context context,
                            int startX, int startY) {
        // mixin注入点
    }
}

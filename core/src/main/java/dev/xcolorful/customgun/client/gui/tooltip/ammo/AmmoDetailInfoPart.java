/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.gui.tooltip.ammo;

import dev.xcolorful.customgun.client.api.item.ammo.AmmoTooltipMask;
import dev.xcolorful.customgun.client.config.RenderConfig;
import dev.xcolorful.customgun.client.gui.tooltip.AbstractTooltipPart;
import dev.xcolorful.customgun.client.resource.assets.info.GunpackInfo;
import dev.xcolorful.customgun.core.api.item.ammo.AmmoCategory;
import dev.xcolorful.customgun.core.api.minecraft.Color64;
import dev.xcolorful.customgun.core.resource.data.index.AmmoIndex;
import dev.xcolorful.customgun.core.resource.instance.data.AmmoIndexInstance;
import dev.xcolorful.customgun.core.util.ComponentUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

/*
原版tab分类
枪包名
资源位置
 */
public final class AmmoDetailInfoPart extends AbstractTooltipPart implements AmmoTooltipPart {
    public static final AmmoDetailInfoPart INSTANCE = new AmmoDetailInfoPart();
    private AmmoDetailInfoPart() {}

    private static final Color64 _defaultCategoryColor = Color64.fromChatFormatting(ChatFormatting.BLUE);
    private static final Color64 _defaultPackColor = Color64.fromChatFormatting(ChatFormatting.BLUE);
    private static final Color64 _defaultLocationColor = _defaultCategoryColor;

    @Override
    public void build(ClientAmmoTooltip.Context context, Font font) {
        var ammoLocation = context.ammoTooltip.ammoLocation();

        Component category; {
            AmmoCategory ammoCategory; {
                @Nullable AmmoIndexInstance ammoIndexInstance = context.ammoIndexInstance;
                if (ammoIndexInstance != null) {
                    AmmoIndex ammoIndex = ammoIndexInstance.getPojo();
                    ammoCategory = ammoIndex.getAmmoCategory();
                } else {
                    ammoCategory = AmmoCategory.AMMO;
                }
            }
            category = ammoCategory.getCategoryLang().copy();
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

        context.view.pojoLocation = Component.literal(ammoLocation.toString());
        context.showPojoLocation = context.showPackInfo && RenderConfig.APPEND_RESOURCE_LOCATION_IN_TOOLTIP.get();
    }

    @Override
    public int measureHeight(ClientAmmoTooltip.Context context) {
        if (!context.visibleParts.contains(AmmoTooltipMask.DETAIL_INFO)) return 0;

        int height = 0;

        if (context.showCategory && context.view.category != null) height += textLineSpacing + textLineHeight;
        if (context.showPackInfo && context.view.packInfo != null) height += textLineSpacing + textLineHeight;
        if (context.showPojoLocation && context.view.pojoLocation != null) height += textLineSpacing + textLineHeight;

        return height;
    }

    @Override
    public void renderText(ClientAmmoTooltip.Context context,
                           int startX, int startY) {
        int currentX = startX;
        int currentY = startY;

        if (context.showCategory && context.view.category != null) {
            currentY += textLineSpacing;

            // 子弹类型
            context.drawText(context.view.category,
                    currentX, currentY,
                    _defaultCategoryColor.getRGB()
            );
            currentY += textLineHeight;
        }

        if (context.showPackInfo && context.view.packInfo != null) {
            currentY += textLineSpacing;

            // 枪包信息
            context.drawText(context.view.packInfo,
                    currentX, currentY,
                    _defaultPackColor.getRGB()
            );
            currentY += textLineHeight;
        }

        if (context.showPojoLocation && context.view.pojoLocation != null) {
            currentY += textLineSpacing;

            // 资源位置
            context.drawText(context.view.pojoLocation,
                    currentX, currentY,
                    _defaultLocationColor.getRGB()
            );
            currentY += textLineHeight;
        }
    }

    @Override
    public void renderImage(ClientAmmoTooltip.Context context,
                            int startX, int startY) {
        // mixin注入点
    }
}

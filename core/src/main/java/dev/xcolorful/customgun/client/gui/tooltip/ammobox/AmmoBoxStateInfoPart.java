/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.gui.tooltip.ammobox;

import dev.xcolorful.customgun.client.api.item.ammobox.AmmoBoxTooltipMask;
import dev.xcolorful.customgun.client.gui.tooltip.AbstractTooltipPart;
import dev.xcolorful.customgun.core.api.minecraft.Color64;
import dev.xcolorful.customgun.core.resource.data.index.AmmoIndex;
import dev.xcolorful.customgun.core.resource.instance.data.AmmoIndexInstance;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

/*
|物  | 子弹名称
|  品| 弹药/总限制
 */
public final class AmmoBoxStateInfoPart extends AbstractTooltipPart implements AmmoBoxTooltipPart {
    public static final AmmoBoxStateInfoPart INSTANCE = new AmmoBoxStateInfoPart();
    private AmmoBoxStateInfoPart() {}

    /**
     * 文字需要往右 1个物品 + 行距离
     */
    private static final int _textXOffset = itemWidth + textLineSpacing;
    private static final int _height = textLineSpacing + Math.max(itemHeight, textLineHeight + textLineSpacing + textLineHeight);
    private static final Color64 _defaultAmmoNameColor = Color64.fromChatFormatting(ChatFormatting.WHITE);
    private static final Color64 _defaultAmmoCountColor = Color64.fromChatFormatting(ChatFormatting.DARK_GRAY);
    private static final boolean hasTextShadow = true;

    @Override
    public void build(ClientAmmoBoxTooltip.Context context, Font font) {
        @Nullable AmmoIndexInstance ammoIndexInstance = context.ammoIndexInstance;
        if (ammoIndexInstance == null) return;

        Component ammoName; {
            AmmoIndex ammoIndex = ammoIndexInstance.getPojo();
            ammoName = Component.translatable(ammoIndex.getNameLang());
        }
        context.view.ammoName = ammoName;

        Component ammoCount; {
            int currentAmmoCount = context.ammoBoxTooltip.ammoCount();
            ammoCount = Component.literal(String.valueOf(currentAmmoCount));
        }
        context.view.ammoCount = ammoCount;

        context.showAmmo = true;
    }

    @Override
    public int measureHeight(ClientAmmoBoxTooltip.Context context) {
        if (!context.visibleParts.contains(AmmoBoxTooltipMask.STATE_INFO)) return 0;

        if (!context.showAmmo) return 0;

        return _height;
    }

    @Override
    public void renderText(ClientAmmoBoxTooltip.Context context,
                           int startX, int startY) {
        int currentX = startX + _textXOffset;
        int currentY = startY;

        if (context.showAmmo && context.view.ammoName != null) {
            currentY += textLineSpacing;

            // 弹药名
            context.drawText(context.view.ammoName,
                    currentX, currentY,
                    _defaultAmmoNameColor.getRGB(),
                    hasTextShadow);
            currentY += textLineHeight;
        }

        if (context.showAmmo && context.view.ammoCount != null) {
            currentY += textLineSpacing;

            // 弹药数
            context.drawText(context.view.ammoCount,
                    currentX, currentY,
                    _defaultAmmoCountColor.getRGB(),
                    hasTextShadow);
            currentY += textLineHeight;
        }
    }

    @Override
    public void renderImage(ClientAmmoBoxTooltip.Context context,
                            int startX, int startY) {
        int currentX = startX;
        int currentY = startY + textLineSpacing;

        if (context.showAmmo) { // 子弹物品
            context.drawItem(context.ammoBoxTooltip.ammoItem(),
                    currentX, currentY);
        }
    }
}

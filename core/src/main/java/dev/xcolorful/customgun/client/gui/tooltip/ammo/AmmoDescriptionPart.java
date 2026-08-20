/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.gui.tooltip.ammo;

import dev.xcolorful.customgun.client.api.item.ammo.AmmoTooltipMask;
import dev.xcolorful.customgun.client.gui.tooltip.AbstractTooltipPart;
import dev.xcolorful.customgun.core.api.minecraft.Color64;
import dev.xcolorful.customgun.core.resource.data.index.AmmoIndex;
import dev.xcolorful.customgun.core.resource.instance.data.AmmoIndexInstance;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/*
描述内容，描述内容
，描述内容。
 */
public final class AmmoDescriptionPart extends AbstractTooltipPart implements AmmoTooltipPart {
    public static final AmmoDescriptionPart INSTANCE = new AmmoDescriptionPart();
    private AmmoDescriptionPart() {}

    /**
     * 扩展模组可以动态识别当前鼠标到边界的距离来调整
     */
    @ApiStatus.Internal public static volatile int _textLineWidth = 300;
    private static final Color64 _defaultDescriptionColor = Color64.fromChatFormatting(ChatFormatting.GRAY);
    private static final boolean hasTextShadow = true;

    @Override
    public void build(ClientAmmoTooltip.Context context, Font font) {
        @Nullable AmmoIndexInstance ammoIndexInstance = context.ammoIndexInstance;
        if (ammoIndexInstance == null) return;

        List<FormattedCharSequence> desc; {
            AmmoIndex ammoIndex = ammoIndexInstance.getPojo();
            String tooltipLang = ammoIndex.getTooltipLang();

            desc = font.split(Component.translatable(tooltipLang), _textLineWidth);
        }
        context.view.desc = desc;
        for (int i = 0; i < desc.size(); i++) {
            FormattedCharSequence sequence = desc.get(i);
            context.widenMaxWidth(font.width(sequence));
        }
    }

    @Override
    public int measureHeight(ClientAmmoTooltip.Context context) {
        if (!context.visibleParts.contains(AmmoTooltipMask.DESCRIPTION)) return 0;

        @Nullable List<FormattedCharSequence> desc = context.view.desc;
        if (desc == null || desc.isEmpty()) return 0;

        return
//                textLineSpacing +
                        desc.size() * textLineHeight;
    }

    @Override
    public void renderText(ClientAmmoTooltip.Context context,
                           int startX, int startY) {
        int currentX = startX;
        int currentY = startY;

        @Nullable List<FormattedCharSequence> desc = context.view.desc;
        if (desc != null) {
            /**
             * {@link AmmoTooltipMask}第一项前不加行间距，后面的不管
             */
//            yOffset += textLineSpacing;

            // 子弹描述
            for (int i = 0; i < desc.size(); i++) {
                // 单行子弹描述
                FormattedCharSequence sequence = desc.get(i);
                context.drawText(sequence,
                        currentX, currentY,
                        _defaultDescriptionColor.getRGB(),
                        hasTextShadow,
                        0,
                        packedLightCoords);
                currentY += textLineHeight;
            }
        }
    }
}

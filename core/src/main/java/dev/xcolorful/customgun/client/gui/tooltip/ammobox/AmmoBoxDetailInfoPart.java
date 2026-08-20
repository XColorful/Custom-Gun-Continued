/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.gui.tooltip.ammobox;

import dev.xcolorful.customgun.client.api.item.ammobox.AmmoBoxTooltipMask;
import dev.xcolorful.customgun.client.config.RenderConfig;
import dev.xcolorful.customgun.client.gui.tooltip.AbstractTooltipPart;
import dev.xcolorful.customgun.client.resource.assets.info.GunpackInfo;
import dev.xcolorful.customgun.core.api.item.ammo.AmmoCategory;
import dev.xcolorful.customgun.core.api.minecraft.Color64;
import dev.xcolorful.customgun.core.resource.data.index.AmmoIndex;
import dev.xcolorful.customgun.core.resource.instance.data.AmmoIndexInstance;
import dev.xcolorful.customgun.core.util.ComponentUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public final class AmmoBoxDetailInfoPart extends AbstractTooltipPart implements AmmoBoxTooltipPart {
    public static final AmmoBoxDetailInfoPart INSTANCE = new AmmoBoxDetailInfoPart();
    private AmmoBoxDetailInfoPart() {}

    private static final Color64 _defaultCategoryColor = Color64.fromChatFormatting(ChatFormatting.BLUE);
    private static final Color64 _defaultPackColor = Color64.fromChatFormatting(ChatFormatting.BLUE);
    private static final Color64 _defaultLocationColor = _defaultCategoryColor;
    private static final boolean hasTextShadow = true;

    @Override
    public void build(ClientAmmoBoxTooltip.Context context) {
        Font font = Minecraft.getInstance().font;

        var ammoLocation = context.ammoBoxTooltip.ammoLocation();

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
    public int measureHeight(ClientAmmoBoxTooltip.Context context) {
        if (!context.visibleParts.contains(AmmoBoxTooltipMask.ENCHANTMENT_INFO)) return 0;

        if (!context.showAmmo) return 0;

        int height = 0;

        if (context.showCategory && context.view.category != null) height += textLineSpacing + textLineHeight;
        if (context.showPackInfo && context.view.packInfo != null) height += textLineSpacing + textLineHeight;
        if (context.showPojoLocation && context.view.pojoLocation != null) height += textLineSpacing + textLineHeight;

        return height;
    }

    @Override
    public void renderText(ClientAmmoBoxTooltip.Context context,
                           Font font, int pX, int pY,
                           Matrix4f matrix4f,
                           MultiBufferSource.BufferSource bufferSource) {
        int currentX = pX;
        int currentY = pY;

        if (context.showAmmo && context.showCategory && context.view.category != null) {
            currentY += textLineSpacing;

            // 子弹类型
            font.drawInBatch(context.view.category,
                    currentX, currentY,
                    _defaultCategoryColor.getRGB(),
                    hasTextShadow,
                    matrix4f,
                    bufferSource,
                    Font.DisplayMode.NORMAL,
                    0,
                    packedLightCoords);
            currentY += textLineHeight;
        }

        if (context.showAmmo && context.showPackInfo && context.view.packInfo != null) {
            currentY += textLineSpacing;

            // 枪包信息
            font.drawInBatch(context.view.packInfo,
                    currentX, currentY,
                    _defaultPackColor.getRGB(),
                    hasTextShadow,
                    matrix4f,
                    bufferSource,
                    Font.DisplayMode.NORMAL,
                    0, packedLightCoords);
            currentY += textLineHeight;
        }

        if (context.showAmmo && context.showPojoLocation && context.view.pojoLocation != null) {
            currentY += textLineSpacing;

            // 资源位置
            font.drawInBatch(context.view.pojoLocation,
                    currentX, currentY,
                    _defaultLocationColor.getRGB(),
                    hasTextShadow,
                    matrix4f,
                    bufferSource,
                    Font.DisplayMode.NORMAL,
                    0, packedLightCoords);
            currentY += textLineHeight;
        }
    }

    @Override
    public void renderImage(ClientAmmoBoxTooltip.Context context,
                            Font font, int pX, int pY,
                            GuiGraphics guiGraphics) {
        // mixin注入点
    }
}

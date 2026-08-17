/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.gui.tooltip.attachment;

import dev.xcolorful.customgun.client.api.item.attachment.AttachmentTooltipMask;
import dev.xcolorful.customgun.client.api.resource.ClientResourceApi;
import dev.xcolorful.customgun.client.config.RenderConfig;
import dev.xcolorful.customgun.client.gui.tooltip.AbstractTooltipPart;
import dev.xcolorful.customgun.client.resource.assets.info.GunpackInfo;
import dev.xcolorful.customgun.core.api.item.attachment.AttachmentCategory;
import dev.xcolorful.customgun.core.api.minecraft.Color64;
import dev.xcolorful.customgun.core.api.resource.ResourceApi;
import dev.xcolorful.customgun.core.resource.data.index.AttachmentIndex;
import dev.xcolorful.customgun.core.resource.instance.data.AttachmentIndexInstance;
import dev.xcolorful.customgun.core.util.ComponentUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

/*
原版tab分类
枪包名
资源位置
 */
public final class AttachmentDetailInfoPart extends AbstractTooltipPart implements AttachmentTooltipPart {
    public static final AttachmentDetailInfoPart INSTANCE = new AttachmentDetailInfoPart();
    private AttachmentDetailInfoPart() {}

    private static final Color64 _defaultCategoryColor = Color64.fromChatFormatting(ChatFormatting.BLUE);
    private static final Color64 _defaultPackColor = Color64.fromChatFormatting(ChatFormatting.BLUE);
    private static final Color64 _defaultLocationColor = _defaultCategoryColor;
    private static final boolean hasTextShadow = true;

    @Override
    public void build(ClientAttachmentTooltip.Context context) {
        Font font = Minecraft.getInstance().font;

        var attachmentLocation = context.attachmentTooltip.attachmentLocation();

        Component category; {
            AttachmentCategory attachmentCategory; {
                @Nullable AttachmentIndexInstance attachmentIndexInstance = context.attachmentIndexInstance;
                if (attachmentIndexInstance != null) {
                    AttachmentIndex attachmentIndex = attachmentIndexInstance.getPojo();
                    attachmentCategory = attachmentIndex.getAttachmentCategory();
                } else {
                    attachmentCategory = AttachmentCategory.NONE;
                }
            }

            category = attachmentCategory.getCategoryLang().copy();
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

        context.view.pojoLocation = Component.literal(attachmentLocation.toString());
        context.showPojoLocation = context.showPackInfo && RenderConfig.APPEND_RESOURCE_LOCATION_IN_TOOLTIP.get();
    }

    @Override
    public int measureHeight(ClientAttachmentTooltip.Context context) {
        if (!context.visibleParts.contains(AttachmentTooltipMask.DETAIL_INFO)) return 0;

        int height = 0;

        if (context.showCategory && context.view.category != null) height += textLineSpacing + textLineHeight;
        if (context.showPackInfo && context.view.packInfo != null) height += textLineSpacing + textLineHeight;
        if (context.showPojoLocation && context.view.pojoLocation != null) height += textLineSpacing + textLineHeight;

        return height;
    }

    @Override
    public void renderText(ClientAttachmentTooltip.Context context,
                           Font font, int pX, int pY,
                           Matrix4f matrix4f,
                           MultiBufferSource.BufferSource bufferSource) {
        int xOffset = pX;
        int yOffset = pY;

        if (context.showCategory && context.view.category != null) {
            yOffset += textLineSpacing;

            // 配件类型
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
    public void renderImage(ClientAttachmentTooltip.Context context,
                            Font font, int pX, int pY,
                            GuiGraphics guiGraphics) {
        // mixin注入点
    }
}

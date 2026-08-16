/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.gui.tooltip.gun;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.client.api.item.gun.GunTooltipMask;
import dev.xcolorful.customgun.client.api.resource.ClientResourceApi;
import dev.xcolorful.customgun.client.gui.tooltip.AbstractTooltipPart;
import dev.xcolorful.customgun.client.resource.assets.GunpackInfoManager;
import dev.xcolorful.customgun.client.resource.assets.info.GunpackInfo;
import dev.xcolorful.customgun.core.api.minecraft.Color64;
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
public final class GunPackInfoPart extends AbstractTooltipPart implements GunTooltipPart {
    public static final GunPackInfoPart INSTANCE = new GunPackInfoPart();
    private GunPackInfoPart() {}

    private static final int _height = textLineSpacing + textLineHeight;
    private static final Color64 _defaultCategoryColor = Color64.fromChatFormatting(ChatFormatting.BLUE);
    private static final boolean hasTextShadow = true;

    @Override
    public void build(ClientGunTooltip.Context context) {
        Font font = Minecraft.getInstance().font;

        var gunLocation = context.gunTooltip.gunLocation();
        @Nullable var gunpackInfoLocation = CustomGun.getMcRegistry().createResourceLocation(gunLocation.getNamespace() + ":" + GunpackInfoManager.POJO_LOCATION_NAME);
        @Nullable GunpackInfo gunpackInfo = ClientResourceApi.getGunpackInfo(gunpackInfoLocation);

        Component packInfo; {
            if (gunpackInfo != null) {
                packInfo = Component.translatable(gunpackInfo.getNameLang())
                        .withStyle(ChatFormatting.ITALIC);
            } else {
                packInfo = ComponentUtils.unknownTranslatableKey();
            }
        }
        context.view.packInfo = packInfo;
        context.widenMaxWidth(font.width(context.view.packInfo));
    }

    @Override
    public int measureHeight(ClientGunTooltip.Context context) {
        if (!context.visibleParts.contains(GunTooltipMask.PACK_INFO)) return 0;

        return _height;
    }

    @Override
    public void renderText(ClientGunTooltip.Context context,
                           Font font, int pX, int pY,
                           Matrix4f matrix4f,
                           MultiBufferSource.BufferSource bufferSource) {
        int xOffset = pX; // 方便mixin修改
        int yOffset = pY;

        if (context.view.packInfo != null) {
            yOffset += textLineSpacing;

            // 枪包信息
            font.drawInBatch(context.view.packInfo,
                    xOffset, yOffset,
                    _defaultCategoryColor.getRGB(),
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

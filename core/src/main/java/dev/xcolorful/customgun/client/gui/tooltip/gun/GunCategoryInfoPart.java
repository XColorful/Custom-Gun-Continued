package dev.xcolorful.customgun.client.gui.tooltip.gun;

import dev.xcolorful.customgun.client.api.item.gun.GunTooltipMask;
import dev.xcolorful.customgun.client.gui.tooltip.AbstractTooltipPart;
import dev.xcolorful.customgun.core.api.item.gun.GunCategory;
import dev.xcolorful.customgun.core.api.minecraft.Color64;
import dev.xcolorful.customgun.core.api.resource.ResourceApi;
import dev.xcolorful.customgun.core.resource.data.index.GunIndex;
import dev.xcolorful.customgun.core.resource.instance.data.GunIndexInstance;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

/**
 * 原版物品tooltip底下显示的分类
 */
public final class GunCategoryInfoPart extends AbstractTooltipPart implements GunTooltipPart {
    public static final GunCategoryInfoPart INSTANCE = new GunCategoryInfoPart();
    private GunCategoryInfoPart() {}

    private static final int _height = textLineSpacing + textLineHeight;
    private static final Color64 _defaultCategoryColor = Color64.fromChatFormatting(ChatFormatting.BLUE);
    private static final boolean hasTextShadow = true;

    @Override
    public void build(ClientGunTooltip.Context context) {
        Font font = Minecraft.getInstance().font;

        var gunLocation = context.gunTooltip.gunLocation();
        @Nullable GunIndexInstance gunIndexInstance = ResourceApi.getGunIndexInstance(gunLocation);

        Component gunCategory; {
            GunCategory category;
            if (gunIndexInstance != null) {
                GunIndex gunIndex = gunIndexInstance.getPojo();
                category = gunIndex.getGunCategory();
            } else {
                category = GunCategory.CUSTOM;
            }

            gunCategory = category.getCategoryLang().copy();
            context.view.gunCategory = gunCategory;
            context.widenMaxWidth(font.width(gunCategory));
        }
    }

    @Override
    public int measureHeight(ClientGunTooltip.Context context) {
        if (!context.visibleParts.contains(GunTooltipMask.CATEGORY_INFO)) return 0;

        return _height;
    }

    @Override
    public void renderText(ClientGunTooltip.Context context,
                           Font font, int pX, int pY,
                           Matrix4f matrix4f,
                           MultiBufferSource.BufferSource bufferSource) {
        int xOffset = pX;
        int yOffset = pY;

        if (context.view.gunCategory != null) {
            yOffset += textLineSpacing;

            // 枪械类型
            font.drawInBatch(context.view.gunCategory,
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
    }
}

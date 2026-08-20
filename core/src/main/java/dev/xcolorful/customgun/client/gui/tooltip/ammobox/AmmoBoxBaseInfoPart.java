package dev.xcolorful.customgun.client.gui.tooltip.ammobox;

import dev.xcolorful.customgun.client.api.item.ammobox.AmmoBoxTooltipMask;
import dev.xcolorful.customgun.client.gui.tooltip.AbstractTooltipPart;
import dev.xcolorful.customgun.core.api.item.IAmmoBox;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import org.joml.Matrix4f;

/**
 * 扩展模组可以显示{@link IAmmoBox#getBoxLevel}
 */
public final class AmmoBoxBaseInfoPart extends AbstractTooltipPart implements AmmoBoxTooltipPart {
    public static final AmmoBoxBaseInfoPart INSTANCE = new AmmoBoxBaseInfoPart();
    private AmmoBoxBaseInfoPart() {}

    @Override
    public void build(ClientAmmoBoxTooltip.Context context) {
    }

    @Override
    public int measureHeight(ClientAmmoBoxTooltip.Context context) {
        if (!context.visibleParts.contains(AmmoBoxTooltipMask.BASE_INFO)) return 0;

        return 0;
    }

    @Override
    public void renderText(ClientAmmoBoxTooltip.Context context,
                           Font font,
                           int pX, int pY,
                           Matrix4f matrix4f,
                           MultiBufferSource.BufferSource bufferSource) {
        // mixin注入点
    }

    @Override
    public void renderImage(ClientAmmoBoxTooltip.Context context,
                            Font font,
                            int pX,
                            int pY,
                            GuiGraphics guiGraphics) {
        // mixin注入点
    }
}

package dev.xcolorful.customgun.client.gui.tooltip.ammobox;

import dev.xcolorful.customgun.client.api.item.ammobox.AmmoBoxTooltipMask;
import dev.xcolorful.customgun.client.gui.tooltip.AbstractTooltipPart;
import net.minecraft.client.gui.Font;

/**
 * 放扩展模组，可以添加使用说明
 */
public final class AmmoBoxGuideTipPart extends AbstractTooltipPart implements AmmoBoxTooltipPart {
    public static final AmmoBoxGuideTipPart INSTANCE = new AmmoBoxGuideTipPart();
    private AmmoBoxGuideTipPart() {}

    @Override
    public void build(ClientAmmoBoxTooltip.Context context, Font font) {
    }

    @Override
    public int measureHeight(ClientAmmoBoxTooltip.Context context) {
        if (!context.visibleParts.contains(AmmoBoxTooltipMask.GUIDE_TIP)) return 0;

        return 0;
    }
}

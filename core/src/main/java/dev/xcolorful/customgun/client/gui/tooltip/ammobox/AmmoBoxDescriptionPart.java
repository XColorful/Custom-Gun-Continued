package dev.xcolorful.customgun.client.gui.tooltip.ammobox;

import dev.xcolorful.customgun.client.api.item.ammobox.AmmoBoxTooltipMask;
import dev.xcolorful.customgun.client.gui.tooltip.AbstractTooltipPart;
import net.minecraft.client.gui.Font;

/*
描述内容，描述内容
，描述内容。
 */
public final class AmmoBoxDescriptionPart extends AbstractTooltipPart implements AmmoBoxTooltipPart {
    public static final AmmoBoxDescriptionPart INSTANCE = new AmmoBoxDescriptionPart();
    private AmmoBoxDescriptionPart() {}

    @Override
    public void build(ClientAmmoBoxTooltip.Context context, Font font) {
    }

    @Override
    public int measureHeight(ClientAmmoBoxTooltip.Context context) {
        if (!context.visibleParts.contains(AmmoBoxTooltipMask.DESCRIPTION)) return 0;

        return 0;
    }
}

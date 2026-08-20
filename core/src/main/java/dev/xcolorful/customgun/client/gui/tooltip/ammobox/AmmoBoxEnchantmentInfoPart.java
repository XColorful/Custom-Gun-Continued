package dev.xcolorful.customgun.client.gui.tooltip.ammobox;

import dev.xcolorful.customgun.client.api.item.ammobox.AmmoBoxTooltipMask;
import dev.xcolorful.customgun.client.gui.tooltip.AbstractTooltipPart;
import dev.xcolorful.customgun.core.api.item.IAmmo;

/**
 * 放扩展模组，可以显示{@link IAmmo#hasInfiniteFeed}、{@link IAmmo#isAlmightyAmmo}
 */
public final class AmmoBoxEnchantmentInfoPart extends AbstractTooltipPart implements AmmoBoxTooltipPart {
    public static final AmmoBoxEnchantmentInfoPart INSTANCE = new AmmoBoxEnchantmentInfoPart();
    private AmmoBoxEnchantmentInfoPart() {}

    @Override
    public void build(ClientAmmoBoxTooltip.Context context) {
    }

    @Override
    public int measureHeight(ClientAmmoBoxTooltip.Context context) {
        if (!context.visibleParts.contains(AmmoBoxTooltipMask.ENCHANTMENT_INFO)) return 0;

        return 0;
    }
}

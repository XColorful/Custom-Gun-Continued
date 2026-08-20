package dev.xcolorful.customgun.client.gui.tooltip.ammo;

import dev.xcolorful.customgun.client.api.item.ammo.AmmoTooltipMask;
import dev.xcolorful.customgun.client.gui.tooltip.AbstractTooltipPart;
import dev.xcolorful.customgun.core.api.item.IAmmo;import net.minecraft.client.gui.Font;

/**
 * 放扩展模组，可以显示{@link IAmmo#hasInfiniteFeed}、{@link IAmmo#isAlmightyAmmo}
 */
public final class AmmoEnchantmentInfoPart extends AbstractTooltipPart implements AmmoTooltipPart {
    public static final AmmoEnchantmentInfoPart INSTANCE = new AmmoEnchantmentInfoPart();
    private AmmoEnchantmentInfoPart() {}

    @Override
    public void build(ClientAmmoTooltip.Context context, Font font) {
    }

    @Override
    public int measureHeight(ClientAmmoTooltip.Context context) {
        if (!context.visibleParts.contains(AmmoTooltipMask.ENCHANTMENT_INFO)) return 0;

        return 0;
    }
}

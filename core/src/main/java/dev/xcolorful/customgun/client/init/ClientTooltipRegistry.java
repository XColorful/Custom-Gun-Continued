/*
 * 跟 BattleRoyale 同构
 */

package dev.xcolorful.customgun.client.init;

import dev.xcolorful.customgun.client.gui.tooltip.ammobox.ClientAmmoBoxTooltip;
import dev.xcolorful.customgun.client.gui.tooltip.attachment.ClientAttachmentTooltip;
import dev.xcolorful.customgun.client.gui.tooltip.gun.ClientGunTooltip;
import dev.xcolorful.customgun.core.gui.tooltip.ammobox.AmmoBoxTooltip;
import dev.xcolorful.customgun.core.gui.tooltip.attachment.AttachmentTooltip;
import dev.xcolorful.customgun.core.gui.tooltip.gun.GunTooltip;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

import java.util.function.Function;

public class ClientTooltipRegistry {

    private static ClientTooltipRegistry INSTANCE = new ClientTooltipRegistry();
    public static ClientTooltipRegistry get() {
        return INSTANCE;
    }
    private ClientTooltipRegistry() {}

    @FunctionalInterface
    public interface TooltipRegistrar {
        <T extends TooltipComponent> void register(
                Class<T> clazz,
                Function<T, ClientTooltipComponent> factory
        );
    }
    public void registerTooltips(TooltipRegistrar registrar) {
        registrar.register(GunTooltip.class, ClientGunTooltip::new);
        registrar.register(AttachmentTooltip.class, ClientAttachmentTooltip::new);
        registrar.register(AmmoBoxTooltip.class, ClientAmmoBoxTooltip::new);
    }
}

/*
 * 跟 BattleRoyale 同构
 */

package xiao.customgun.client.init;

import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import xiao.customgun.client.gui.tooltip.ammobox.ClientAmmoBoxTooltip;
import xiao.customgun.client.gui.tooltip.attachment.ClientAttachmentTooltip;
import xiao.customgun.client.gui.tooltip.gun.ClientGunTooltip;
import xiao.customgun.core.gui.tooltip.ammobox.AmmoBoxTooltip;
import xiao.customgun.core.gui.tooltip.attachment.AttachmentTooltip;
import xiao.customgun.core.gui.tooltip.gun.GunTooltip;

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

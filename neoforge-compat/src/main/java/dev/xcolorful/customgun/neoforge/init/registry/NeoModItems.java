package dev.xcolorful.customgun.neoforge.init.registry;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.core.init.registry.ModItems;
import dev.xcolorful.customgun.core.item.ammo.AmmoItem;
import dev.xcolorful.customgun.core.item.attachment.AttachmentItem;
import dev.xcolorful.customgun.core.item.gun.GunItem;
import dev.xcolorful.customgun.neoforge.item.ammo.NeoAmmoItem;
import dev.xcolorful.customgun.neoforge.item.attachment.NeoAttachmentItem;
import dev.xcolorful.customgun.neoforge.item.gun.NeoGunItem;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 处理需要hack的{@link ModItems}注册类型
 */
@EventBusSubscriber(value = Dist.CLIENT, modid = CustomGun.MOD_ID)
public class NeoModItems {

    private static final Map<Class<? extends Item>, Supplier<? extends Item>> NEO_ITEM_MAP = new HashMap<>();
    private static <C extends Item, F extends C> void _put(Class<C> coreClass, Supplier<F> forgeSupplier) {
        NEO_ITEM_MAP.put(coreClass, forgeSupplier);
    }
    static {
        _put(GunItem.class, NeoGunItem::new);
        _put(AttachmentItem.class, NeoAttachmentItem::new);
        _put(AmmoItem.class, NeoAmmoItem::new);
    }

    @SuppressWarnings("unchecked")
    public static <I extends Item> Supplier<? extends I> getNeoSupplier(Class<I> clazz) {
        Supplier<? extends Item> supplier = NEO_ITEM_MAP.get(clazz);
        if (supplier != null) {
            return (Supplier<? extends I>) supplier;
        }

        throw new IllegalArgumentException("No NeoForge item mapping registered for core class: " + clazz.getName());
    }

    // --------1.21.1--------
    /**
     * 手动添加{@link #NEO_ITEM_MAP}新增的{@link #_put}
     */
    @ApiStatus.AvailableSince("1.21.1")
    @SubscribeEvent
    public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        if (NEO_ITEM_MAP.size() != 3) {
            throw new IllegalStateException("NEO_ITEM_MAP size mismatch! Extensions registration is missing.");
        }
        if (!(ModItems.GUN.get() instanceof NeoGunItem neoGunItem)) throw new IllegalArgumentException("ModItems.GUN.get() is not NeoGunItem!");
        NeoGunItem.registerClientExtension(extensions -> event.registerItem(extensions, neoGunItem));
        if (!(ModItems.ATTACHMENT.get() instanceof NeoAttachmentItem neoAttachmentItem)) throw new IllegalArgumentException("ModItems.ATTACHMENT.get() is not NeoAttachmentItem!");
        NeoAttachmentItem.registerClientExtension(extensions -> event.registerItem(extensions, neoAttachmentItem));
        if (!(ModItems.AMMO.get() instanceof NeoAmmoItem neoAmmoItem)) throw new IllegalArgumentException("ModItems.AMMO.get() is not NeoAmmoItem!");
        NeoAmmoItem.registerClientExtension(extensions -> event.registerItem(extensions, neoAmmoItem));
    }
}

package dev.xcolorful.customgun.forge.init.registry;

import dev.xcolorful.customgun.core.init.registry.ModItems;
import dev.xcolorful.customgun.core.item.ammo.AmmoItem;
import dev.xcolorful.customgun.core.item.attachment.AttachmentItem;
import dev.xcolorful.customgun.core.item.gun.GunItem;
import dev.xcolorful.customgun.forge.item.ammo.ForgeAmmoItem;
import dev.xcolorful.customgun.forge.item.attachment.ForgeAttachmentItem;
import dev.xcolorful.customgun.forge.item.gun.ForgeGunItem;
import net.minecraft.world.item.Item;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 处理需要hack的{@link ModItems}注册类型
 */
public class ForgeModItems {

    private static final Map<Class<? extends Item>, Supplier<? extends Item>> FORGE_ITEM_MAP = new HashMap<>();
    private static <C extends Item, F extends C> void _put(Class<C> coreClass, Supplier<F> forgeSupplier) {
        FORGE_ITEM_MAP.put(coreClass, forgeSupplier);
    }
    static {
        _put(GunItem.class, ForgeGunItem::new);
        _put(AttachmentItem.class, ForgeAttachmentItem::new);
        _put(AmmoItem.class, ForgeAmmoItem::new);
    }

    @SuppressWarnings("unchecked")
    public static <I extends Item> Supplier<? extends I> getForgeSupplier(Class<I> clazz) {
        Supplier<? extends Item> supplier = FORGE_ITEM_MAP.get(clazz);
        if (supplier != null) {
            return (Supplier<? extends I>) supplier;
        }

        throw new IllegalArgumentException("No Forge item mapping registered for core class: " + clazz.getName());
    }
}

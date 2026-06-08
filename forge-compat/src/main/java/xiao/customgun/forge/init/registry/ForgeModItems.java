package xiao.customgun.forge.init.registry;

import net.minecraft.world.item.Item;
import xiao.customgun.core.init.registry.ModItems;
import xiao.customgun.core.item.ammo.AmmoItem;
import xiao.customgun.core.item.attachment.AttachmentItem;
import xiao.customgun.core.item.gun.GunItem;
import xiao.customgun.forge.item.ammo.ForgeAmmoItem;
import xiao.customgun.forge.item.attachment.ForgeAttachmentItem;
import xiao.customgun.forge.item.gun.ForgeGunItem;

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
    public static <V extends Item> Supplier<? extends V> getForgeSupplier(Class<V> clazz) {
        if (FORGE_ITEM_MAP.containsKey(clazz)) {
            return (Supplier<? extends V>) FORGE_ITEM_MAP.get(clazz);
        }

        throw new IllegalArgumentException("No Forge item mapping registered for core class: " + clazz.getName());
    }
}

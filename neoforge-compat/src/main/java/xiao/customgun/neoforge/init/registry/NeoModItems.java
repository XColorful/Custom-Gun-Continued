package xiao.customgun.neoforge.init.registry;

import net.minecraft.world.item.Item;
import xiao.customgun.core.init.registry.ModItems;
import xiao.customgun.core.item.ammo.AmmoItem;
import xiao.customgun.core.item.attachment.AttachmentItem;
import xiao.customgun.core.item.gun.GunItem;
import xiao.customgun.neoforge.item.ammo.NeoAmmoItem;
import xiao.customgun.neoforge.item.attachment.NeoAttachmentItem;
import xiao.customgun.neoforge.item.gun.NeoGunItem;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 处理需要hack的{@link ModItems}注册类型
 */
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
    public static <V extends Item> Supplier<? extends V> getNeoSupplier(Class<V> clazz) {
        if (NEO_ITEM_MAP.containsKey(clazz)) {
            return (Supplier<? extends V>) NEO_ITEM_MAP.get(clazz);
        }

        throw new IllegalArgumentException("No NeoForge item mapping registered for core class: " + clazz.getName());
    }
}

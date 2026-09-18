package dev.xcolorful.customgun.core.api.item.ammo;

import dev.xcolorful.customgun.core.api.item.IAmmo;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public interface IAmmoGetter {

    static @Nullable IAmmo fromItemStack(@Nullable ItemStack ammoItem) {
        if (ammoItem == null) return null;
        return ammoItem.getItem() instanceof IAmmo iAmmo ? iAmmo : null;
    }

    // --------Deprecated--------

    @Deprecated static @Nullable IAmmo getIAmmoOrNull(@Nullable ItemStack ammoItem) {
        return fromItemStack(ammoItem);
    }
}

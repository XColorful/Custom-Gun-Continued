package dev.xcolorful.customgun.forge.mixin.item;

import dev.xcolorful.customgun.core.api.item.IAmmo;
import dev.xcolorful.customgun.core.api.item.ammo.IAmmoGetter;
import dev.xcolorful.customgun.core.item.ammo.AmmoItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(AmmoItem.class)
public abstract class AmmoItemMixin extends Item {

    public AmmoItemMixin(Properties properties) {
        super(properties);
    }

    // --------IForgeItem--------

    /**
     * @deprecated 1.21.1forge移至 {@link dev.xcolorful.customgun.forge.mixin.item.ItemStackMixin}
     */
    @Deprecated(since = "1.21.1")
//    @Override
    public int getMaxStackSize(ItemStack ammoItem) {
        @Nullable IAmmo iAmmo = IAmmoGetter.fromItemStack(ammoItem);
        if (iAmmo == null) return 1;

        return iAmmo.getAmmoMaxStackSize(ammoItem);
    }
}

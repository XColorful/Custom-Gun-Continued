package dev.xcolorful.customgun.neoforge.mixin.item;

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

    // --------IItemExtension--------

    @Override
    public int getMaxStackSize(ItemStack ammoItem) {
        @Nullable IAmmo iAmmo = IAmmoGetter.fromItemStack(ammoItem);
        if (iAmmo == null) return super.getMaxStackSize(ammoItem);

        return iAmmo.getAmmoMaxStackSize(ammoItem);
    }
}

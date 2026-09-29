package dev.xcolorful.customgun.core.api.item;

import dev.xcolorful.customgun.core.api.item.ammo.IAmmoDataAccess;
import dev.xcolorful.customgun.core.api.item.ammo.IAmmoGetter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface IAmmo extends IAmmoDataAccess, IAmmoGetter,
        IPojoItem {

    // --------IPojoItem--------

    @Override
    default @NotNull ResourceLocation getPojoLocation(ItemStack ammoItem) {
        return this.getAmmoLocation(ammoItem);
    }
    @Override
    default void setPojoLocation(ItemStack ammoItem, ResourceLocation ammoLocation) {
        this.setAmmoLocation(ammoItem, ammoLocation);
    }

    @Override
    default @NotNull ResourceLocation getPojoDisplayLocation(ItemStack ammoItem) {
        return this.getAmmoLocation(ammoItem);
    }
    @Override
    default void setPojoDisplayLocation(ItemStack ammoItem, ResourceLocation ammoDisplayLocation) {
//        this.setAmmoLocation(ammoItem, ammoDisplayLocation);
    }
}

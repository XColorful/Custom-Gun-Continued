package dev.xcolorful.customgun.core.api.item.pojo;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface IPojoItemDataAccess {

    @NotNull Identifier getPojoLocation(ItemStack pojoItem);
    void setPojoLocation(ItemStack pojoItem, Identifier pojoLocation);

    @NotNull Identifier getPojoDisplayLocation(ItemStack pojoItem);
    void setPojoDisplayLocation(ItemStack pojoItem, Identifier displayLocation);
}

package dev.xcolorful.customgun.core.api.item.pojo;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface IPojoItemDataAccess {

    @NotNull ResourceLocation getPojoLocation(ItemStack pojoItem);
    void setPojoLocation(ItemStack pojoItem, ResourceLocation pojoLocation);
}

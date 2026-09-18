package dev.xcolorful.customgun.core.api.script.context;

import dev.xcolorful.customgun.core.api.item.IGun;
import net.minecraft.world.item.ItemStack;

@Deprecated
public record _AbstractGunItem(IGun iGun, ItemStack gunItem) {
}

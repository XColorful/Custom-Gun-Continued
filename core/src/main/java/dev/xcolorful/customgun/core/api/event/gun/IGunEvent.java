package dev.xcolorful.customgun.core.api.event.gun;

import dev.xcolorful.customgun.core.api.item.IGun;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IGunEvent {

    @Nullable IGun getIGun();
    @NotNull ItemStack getGunItem();
}

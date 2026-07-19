package xiao.customgun.core.api.event.gun;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.api.item.IGun;

public interface IGunEvent {

    @Nullable IGun getIGun();
    @NotNull ItemStack getGunItem();
}

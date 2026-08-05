package dev.xcolorful.customgun.core.api.event.gun;

import dev.xcolorful.customgun.core.api.event.CustomEvent;
import dev.xcolorful.customgun.core.api.item.IGun;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 枪械{@link IGun} 事件
 */
public abstract class GunEvent extends CustomEvent implements IGunEvent {

    protected @Nullable IGun iGun;
    protected @NotNull ItemStack gunItem;

    protected GunEvent(@Nullable IGun iGun, @NotNull ItemStack gunItem) {
        this.iGun = iGun;
        this.gunItem = gunItem;
    }

    public @Nullable IGun getIGun() {
        return this.iGun;
    }
    public @NotNull ItemStack getGunItem() {
        return this.gunItem;
    }
}

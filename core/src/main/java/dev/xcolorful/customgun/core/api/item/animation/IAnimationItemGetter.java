package dev.xcolorful.customgun.core.api.item.animation;

import dev.xcolorful.customgun.core.api.item.IAnimationItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public interface IAnimationItemGetter {

    static @Nullable IAnimationItem fromItemStack(@Nullable ItemStack animationItem) {
        if (animationItem == null) return null;
        return animationItem.getItem() instanceof IAnimationItem iAnimationItem ? iAnimationItem : null;
    }
    static @Nullable IAnimationItem fromMainHand(@Nullable LivingEntity livingEntity) {
        if (livingEntity == null) return null;
        return livingEntity.getMainHandItem().getItem() instanceof IAnimationItem iAnimationItem ? iAnimationItem : null;
    }
}

package xiao.customgun.core.api.item.pojo;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.api.item.IPojoItem;

public interface IPojoItemGetter {

    static @Nullable IPojoItem fromItemStack(@Nullable ItemStack pojoItem) {
        if (pojoItem == null) return null;
        return pojoItem.getItem() instanceof IPojoItem iPojoItem ? iPojoItem : null;
    }
    static @Nullable IPojoItem fromMainHand(@Nullable LivingEntity livingEntity) {
        if (livingEntity == null) return null;
        return livingEntity.getMainHandItem().getItem() instanceof IPojoItem iPojoItem ? iPojoItem : null;
    }
}

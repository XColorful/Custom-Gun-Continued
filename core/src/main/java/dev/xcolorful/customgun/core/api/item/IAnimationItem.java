package dev.xcolorful.customgun.core.api.item;

import dev.xcolorful.customgun.core.api.item.pojo.IPojoItemGetter;
import net.minecraft.world.item.ItemStack;

public interface IAnimationItem extends IPojoItem {

    /**
     * 返回物品是否需要重新初始化状态机或属性
     * @param oldItem 物品1
     * @param newItem 物品2
     * @return 是否需要重新初始化
     */
    default boolean switchItemNeedReset(ItemStack oldItem, ItemStack newItem) {
        if (oldItem.isEmpty() || newItem.isEmpty()) {
            return oldItem.isEmpty() != newItem.isEmpty();
        }
        IPojoItem iPojoItem1 = IPojoItemGetter.fromItemStack(oldItem);
        IPojoItem iPojoItem2 = IPojoItemGetter.fromItemStack(newItem);
        if (iPojoItem1 != null && iPojoItem2 != null) {
            return !iPojoItem1.getPojoLocation(oldItem).equals(iPojoItem2.getPojoLocation(oldItem))
                    || !iPojoItem1.getPojoDisplayLocation(oldItem).equals(iPojoItem2.getPojoDisplayLocation(oldItem));
        }
        return !ItemStack.matches(oldItem, newItem);
    }

    // --------Deprecated--------

    @Deprecated default boolean isSame(ItemStack oldItem, ItemStack newItem) {
        return !this.switchItemNeedReset(oldItem, newItem);
    }
}

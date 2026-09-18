package dev.xcolorful.customgun.core.api.item;

import net.minecraft.world.item.ItemStack;

public interface IAnimationItem {

    /**
     * 返回物品是否需要重新初始化状态机或属性
     * @param oldItem 物品1
     * @param newItem 物品2
     * @return 是否需要重新初始化
     */
    boolean switchItemNeedReset(ItemStack oldItem, ItemStack newItem);

    // --------Deprecated--------

    @Deprecated default boolean isSame(ItemStack oldItem, ItemStack newItem) {
        return !this.switchItemNeedReset(oldItem, newItem);
    }
}

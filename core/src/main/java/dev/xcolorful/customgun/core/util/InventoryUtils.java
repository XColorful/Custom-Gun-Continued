package dev.xcolorful.customgun.core.util;

import net.minecraft.world.entity.player.Inventory;

public class InventoryUtils {

    /**
     * @return 当前选中的物品槽的索引
     */
    public static int getSelectedSlot(Inventory inventory) {
        return inventory.selected; // inventory.getSelectedSlot();
    }
}

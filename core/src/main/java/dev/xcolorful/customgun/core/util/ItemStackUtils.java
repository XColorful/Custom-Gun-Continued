package dev.xcolorful.customgun.core.util;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class ItemStackUtils {

    public static void hurtAndBreak(ItemStack itemStack, int damageAmount, LivingEntity owner, EquipmentSlot equipmentSlot) {
        // [1.20.1, 1.21.1)
        itemStack.hurtAndBreak(damageAmount, owner, (livingEntity) -> {
            livingEntity.broadcastBreakEvent(equipmentSlot);
        });

        // [1.21.1, )
//        itemStack.hurtAndBreak(damageAmount, owner, equipmentSlot);
    }
}

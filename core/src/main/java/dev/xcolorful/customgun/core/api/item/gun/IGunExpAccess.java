package dev.xcolorful.customgun.core.api.item.gun;

import net.minecraft.world.item.ItemStack;

/**
 * 枪械等级
 */
public interface IGunExpAccess {

    int getGunExp(ItemStack gunItem);
    void setGunExp(ItemStack gunItem, int exp);
    int calculateLevel(ItemStack gunItem, int exp);
    int calculateExp(ItemStack gunItem, int level);
    int getCurrentLevelExp(ItemStack gunItem);
    int getExpToNextLevel(ItemStack gunItem);
    int getMaxLevel(ItemStack gunItem);
}

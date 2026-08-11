package dev.xcolorful.customgun.client.api.minecraft.item;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * 封装 IClientItemExtensions
 */
public interface IClientItemExtensionProvider {

    @Deprecated Object of(ItemStack itemStack);
    @Deprecated Object of(Item item);

    BlockEntityWithoutLevelRenderer getBEWLR(ItemStack itemStack);
}

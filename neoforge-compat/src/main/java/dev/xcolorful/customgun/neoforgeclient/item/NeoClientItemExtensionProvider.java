package dev.xcolorful.customgun.neoforgeclient.item;

import dev.xcolorful.customgun.client.api.minecraft.item.IClientItemExtensionProvider;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

public class NeoClientItemExtensionProvider implements IClientItemExtensionProvider {

    @Override public Object of(ItemStack itemStack) {
        return IClientItemExtensions.of(itemStack);
    }

    @Override public Object of(Item item) {
        return IClientItemExtensions.of(item);
    }

    @Override public BlockEntityWithoutLevelRenderer getBEWLR(ItemStack itemStack) {
        return IClientItemExtensions.of(itemStack)
                .getCustomRenderer();
    }
}

package dev.xcolorful.customgun.forgeclient.item;

import dev.xcolorful.customgun.client.api.minecraft.item.IClientItemExtensionProvider;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

public class ForgeClientItemExtensionProvider implements IClientItemExtensionProvider {

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

package dev.xcolorful.customgun.neoforgeclient.item;

import dev.xcolorful.customgun.client.api.minecraft.item.IClientItemExtensionProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.Nullable;

public class NeoClientItemExtensionProvider implements IClientItemExtensionProvider {

    @Override public Object of(ItemStack itemStack) {
        return IClientItemExtensions.of(itemStack);
    }

    @Override public Object of(Item item) {
        return IClientItemExtensions.of(item);
    }

    @Override public @Nullable Object getBEWLR(ItemStack itemStack) {
        return IClientItemExtensions.of(itemStack)
                .getCustomRenderer();
    }
}

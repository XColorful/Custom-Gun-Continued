package dev.xcolorful.customgun.neoforgeclient.item;

import dev.xcolorful.customgun.client.CustomGunClient;
import dev.xcolorful.customgun.client.api.minecraft.item.IClientItemExtensionProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.Nullable;

public class NeoClientItemExtensionProvider implements IClientItemExtensionProvider {
    public static final NeoClientItemExtensionProvider INSTANCE = new NeoClientItemExtensionProvider();

    /**
     * 手动传NeoForge类为旧实现方式，现直接调用 {@link CustomGunClient#getClientItemExtensionProvider()} 即可
     */
    @SuppressWarnings("DeprecatedIsStillUsed")
    @Deprecated(forRemoval = false)
    public NeoClientItemExtensionProvider() {
    }

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

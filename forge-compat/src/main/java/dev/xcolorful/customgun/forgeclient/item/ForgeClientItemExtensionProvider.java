package dev.xcolorful.customgun.forgeclient.item;

import dev.xcolorful.customgun.client.CustomGunClient;
import dev.xcolorful.customgun.client.api.minecraft.item.IClientItemExtensionProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.Nullable;

public class ForgeClientItemExtensionProvider implements IClientItemExtensionProvider {
    public static final ForgeClientItemExtensionProvider INSTANCE = new ForgeClientItemExtensionProvider();

    /**
     * 手动传Forge类为旧实现方式，现直接调用 {@link CustomGunClient#getClientItemExtensionProvider()} 即可
     */
    @SuppressWarnings("DeprecatedIsStillUsed")
    @Deprecated(forRemoval = false)
    public ForgeClientItemExtensionProvider() {
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

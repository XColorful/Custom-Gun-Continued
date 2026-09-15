package dev.xcolorful.customgun.neoforgeclient.mixin.item;

import dev.xcolorful.customgun.client.api.item.IItemBEWLR;
import dev.xcolorful.customgun.client.renderer.item.AmmoItemRenderer;
import dev.xcolorful.customgun.core.item.ammo.AmmoItem;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.spongepowered.asm.mixin.Mixin;

import java.util.function.Consumer;

@Mixin(AmmoItem.class)
public abstract class AmmoItemMixin extends Item implements IItemBEWLR {

    public AmmoItemRenderer cgc$renderer;

    public AmmoItemMixin(Properties properties) {
        super(properties);
    }

    // --------IClientItemExtensions-------

    @OnlyIn(Dist.CLIENT)
    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Deprecated(since = "1.21.4")
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return AmmoItemMixin.this.cgc$getBEWLR();
            }
        });
    }

    // --------IItemBEWLR--------

    @Override
    public AmmoItemRenderer cgc$getBEWLR() {
        if (this.cgc$renderer == null) this.cgc$renderer = new AmmoItemRenderer();

        return this.cgc$renderer;
    }
}

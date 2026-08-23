package dev.xcolorful.customgun.forgeclient.mixin.item;

import dev.xcolorful.customgun.client.api.item.IItemBEWLR;
import dev.xcolorful.customgun.client.renderer.item.AmmoItemRenderer;
import dev.xcolorful.customgun.core.item.ammo.AmmoItem;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(AmmoItem.class)
public abstract class AmmoItemMixin extends Item implements IItemBEWLR {

    public AmmoItemRenderer cgc$renderer;

    public AmmoItemMixin(Properties properties) {
        super(properties);
    }

    // --------IItemBEWLR--------

    @Override
    public AmmoItemRenderer cgc$getBEWLR() {
        if (this.cgc$renderer == null) this.cgc$renderer = new AmmoItemRenderer();

        return this.cgc$renderer;
    }
}

package dev.xcolorful.customgun.forgeclient.mixin.item;

import dev.xcolorful.customgun.client.api.item.IAnimateGeoItem;
import dev.xcolorful.customgun.client.api.item.IItemBEWLR;
import dev.xcolorful.customgun.client.renderer.item.GunItemRenderer;
import dev.xcolorful.customgun.core.item.gun.GunItem;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(GunItem.class)
public abstract class GunItemMixin extends Item implements IAnimateGeoItem, IItemBEWLR {

    public GunItemRenderer cgc$renderer;

    public GunItemMixin(Properties properties) {
        super(properties);
    }

    // --------IAnimateGeoItem--------

    @Override
    public GunItemRenderer cgc$getCustomRenderer() {
        if (this.cgc$renderer == null) this.cgc$renderer = new GunItemRenderer();

        return this.cgc$renderer;
    }

    // --------IItemBEWLR--------

    @Override
    public GunItemRenderer cgc$getBEWLR() {
        return this.cgc$getCustomRenderer();
    }
}

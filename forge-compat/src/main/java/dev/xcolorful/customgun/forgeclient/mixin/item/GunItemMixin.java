package dev.xcolorful.customgun.forgeclient.mixin.item;

import dev.xcolorful.customgun.client.animation.statemachine.GunAnimStateContext;
import dev.xcolorful.customgun.client.api.item.IAnimateGeoItem;
import dev.xcolorful.customgun.client.model.GunModelObject;
import dev.xcolorful.customgun.client.renderer.item.GunItemRenderer;
import dev.xcolorful.customgun.client.api.renderer.item.IAnimateGeoItemRenderer;
import dev.xcolorful.customgun.core.item.gun.GunItem;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(GunItem.class)
public abstract class GunItemMixin extends Item implements IAnimateGeoItem {

    public IAnimateGeoItemRenderer<GunModelObject, GunAnimStateContext> cgc$renderer;

    public GunItemMixin(Properties properties) {
        super(properties);
    }

    @Override
    public IAnimateGeoItemRenderer<GunModelObject, GunAnimStateContext> cgc$getCustomRenderer() {
        if (this.cgc$renderer == null) this.cgc$renderer = new GunItemRenderer();

        return this.cgc$renderer;
    }
}

package dev.xcolorful.customgun.client.api.item;

import dev.xcolorful.customgun.client.api.renderer.item.IAnimateGeoItemRenderer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public interface IAnimateGeoItem {

    static @Nullable IAnimateGeoItem cgc$fromItemStack(ItemStack geoItem) {
        return geoItem.getItem() instanceof IAnimateGeoItem animateGeoItem ? animateGeoItem : null;
    }
    static @Nullable IAnimateGeoItem cgc$fromItem(Item geoItem) {
        return geoItem instanceof IAnimateGeoItem animateGeoItem ? animateGeoItem : null;
    }
    static @Nullable IAnimateGeoItemRenderer<?, ?> cgc$getCustomRenderer(ItemStack geoItem) {
        return geoItem.getItem() instanceof IAnimateGeoItem animateGeoItem ? animateGeoItem.cgc$getCustomRenderer() : null;
    }

    IAnimateGeoItemRenderer<?, ?> cgc$getCustomRenderer();
}

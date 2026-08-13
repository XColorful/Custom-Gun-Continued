package dev.xcolorful.customgun.client.api.minecraft.item;

import dev.xcolorful.customgun.client.api.item.IAnimateGeoItem;
import dev.xcolorful.customgun.client.api.renderer.item.IAnimateGeoItemRenderer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * 封装 IClientItemExtensions
 */
public interface IClientItemExtensionProvider {

    @Deprecated Object of(ItemStack itemStack);
    @Deprecated Object of(Item item);

    /**
     * 自1.21.4起，已经没有BEWLR了
     * <ul>
     *     <li>本模组改成在平台层Mixin{@link IAnimateGeoItem}接口</li>
     *     <li>使用{@link IAnimateGeoItem#cgc$getCustomRenderer(ItemStack)}获取自定义渲染器{@link IAnimateGeoItemRenderer}</li>
     * </ul>
     */
    @Deprecated(since = "1.21.4")
    @Nullable Object getBEWLR(ItemStack itemStack);
}

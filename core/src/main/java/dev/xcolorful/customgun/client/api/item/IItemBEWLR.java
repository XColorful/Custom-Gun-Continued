package dev.xcolorful.customgun.client.api.item;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.client.compat.minecraft.BlockEntityWithoutLevelRenderer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

public interface IItemBEWLR {

    /**
     * 1.21.4起，"assets/{@link CustomGun#MOD_ID}/items/*.json"里的{@code type}字段
     */
    @ApiStatus.AvailableSince("1.21.4")
    @NotNull ResourceLocation REGISTRY_LOCATION = CustomGun.getMcRegistry().createResourceLocation(String.format("%s:%s", CustomGun.MOD_ID, "item_bewlr"));

    /**
     * @since 1.21.4 返回值改为{@link dev.xcolorful.customgun.client.compat.minecraft.BlockEntityWithoutLevelRenderer}
     */
    BlockEntityWithoutLevelRenderer cgc$getBEWLR();
}

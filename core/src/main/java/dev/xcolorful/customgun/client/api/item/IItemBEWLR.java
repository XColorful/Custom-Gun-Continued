package dev.xcolorful.customgun.client.api.item;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;

public interface IItemBEWLR {

    /**
     * @since 1.21.4 返回值改为{@link dev.xcolorful.customgun.client.compat.minecraft.BlockEntityWithoutLevelRenderer}
     */
    BlockEntityWithoutLevelRenderer cgc$getBEWLR();
}

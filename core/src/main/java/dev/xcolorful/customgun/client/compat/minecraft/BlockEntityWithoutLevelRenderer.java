package dev.xcolorful.customgun.client.compat.minecraft;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.xcolorful.customgun.client.api.renderer.item.IBlockEntityWithoutLevelRenderer;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * 自1.21.4起，已经没有BEWLR了
 */
public class BlockEntityWithoutLevelRenderer implements IBlockEntityWithoutLevelRenderer {
    protected final BlockEntityRenderDispatcher blockEntityRenderDispatcher;
    protected final EntityModelSet entityModelSet;

    public BlockEntityWithoutLevelRenderer(BlockEntityRenderDispatcher blockEntityRenderDispatcher, EntityModelSet entityModelSet) {
        this.blockEntityRenderDispatcher = blockEntityRenderDispatcher;
        this.entityModelSet = entityModelSet;
    }

    public void renderByItem(ItemStack stack,
                             ItemDisplayContext transformType,
                             PoseStack poseStack,
                             MultiBufferSource buffer,
                             int packedLight, int packedOverlay) {
    }
}

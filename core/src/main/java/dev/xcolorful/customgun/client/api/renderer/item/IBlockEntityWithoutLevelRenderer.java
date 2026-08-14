package dev.xcolorful.customgun.client.api.renderer.item;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public interface IBlockEntityWithoutLevelRenderer {

    void renderByItem(ItemStack stack,
                      ItemDisplayContext transformType,
                      PoseStack poseStack,
                      SubmitNodeCollector buffer,
                      int packedLight, int packedOverlay);
}

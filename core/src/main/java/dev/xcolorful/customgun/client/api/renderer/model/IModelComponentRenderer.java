package dev.xcolorful.customgun.client.api.renderer.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.world.item.ItemDisplayContext;

public interface IModelComponentRenderer {

    void render(PoseStack poseStack,
                VertexConsumer vertexBuffer,
                ItemDisplayContext transformType,
                int light, int overlay);
}

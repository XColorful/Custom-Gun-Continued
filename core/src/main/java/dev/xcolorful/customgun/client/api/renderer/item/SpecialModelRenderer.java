package dev.xcolorful.customgun.client.api.renderer.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nullable;

/**
 * 为了跨版本提前知道原版1.21.4的移植方式，添加此类作为占位符
 */
@Deprecated(since = "1.21.4", forRemoval = false)
@ApiStatus.AvailableSince("1.21.4")
public interface SpecialModelRenderer<T> {

    void render(@Nullable T patterns,
                ItemDisplayContext displayContext,
                PoseStack poseStack,
                MultiBufferSource bufferSource,
                int packedLight, int packedOverlay,
                boolean hasFoilType
    );

    @Nullable T extractArgument(ItemStack stack);

    interface Unbaked {

        @Nullable SpecialModelRenderer<?> bake(EntityModelSet modelSet);

        MapCodec<? extends Unbaked> type();
    }
}

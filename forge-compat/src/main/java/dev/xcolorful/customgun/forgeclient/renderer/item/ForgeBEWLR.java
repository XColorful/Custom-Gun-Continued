package dev.xcolorful.customgun.forgeclient.renderer.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import dev.xcolorful.customgun.client.api.item.IItemBEWLR;
import dev.xcolorful.customgun.client.api.renderer.item.SpecialModelRenderer;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/*
为了跨版本提前知道1.21.4neoforge的移植方式，添加此类作为占位符
 */
/**
 * <ul>
 *     自 1.21.4 起，原版的 {@code BlockEntityWithoutLevelRenderer} 被移除
 *     <li>物品自定义渲染改由 {@code net.minecraft.client.renderer.special.SpecialModelRenderer} 承担</li>
 *     <li>本类作为 1.21.4 的 special model 渲染器，把调用桥接回{@link IItemBEWLR#cgc$getBEWLR()}</li>
 *     <li>从而复用{@code renderByItem}</li>
 * </ul>
 */
@ApiStatus.AvailableSince("1.21.4")
@OnlyIn(Dist.CLIENT)
public class ForgeBEWLR implements SpecialModelRenderer<ItemStack> {

    public static final ForgeBEWLR INSTANCE = new ForgeBEWLR();

    private ForgeBEWLR() {}

    @Override
    public void render(@Nullable ItemStack itemStack,
                       ItemDisplayContext displayContext,
                       PoseStack poseStack,
                       MultiBufferSource bufferSource,
                       int packedLight, int packedOverlay,
                       boolean hasFoilType) {
        if (itemStack == null || !(itemStack.getItem() instanceof IItemBEWLR itemBEWLR)) return;

        var bewlr = itemBEWLR.cgc$getBEWLR(); // 用var就跟ResourceLocation是一样的手法 (把import给隐身，省一个移植修改)，除此之外没别的用意
        bewlr.renderByItem(itemStack,
                displayContext,
                poseStack,
                bufferSource,
                packedLight, packedOverlay);
    }

    @Override
    public @Nullable ItemStack extractArgument(ItemStack itemStack) {
        return itemStack.getItem() instanceof IItemBEWLR ? itemStack : null;
    }

    public record BewlrUnbaked() implements SpecialModelRenderer.Unbaked {
        public static final MapCodec<BewlrUnbaked> MAP_CODEC = MapCodec.unit(new BewlrUnbaked());

        @Override
        public SpecialModelRenderer<?> bake(EntityModelSet modelSet) {
            return INSTANCE;
        }

        @Override public MapCodec<BewlrUnbaked> type() {
            return MAP_CODEC;
        }

    }
}

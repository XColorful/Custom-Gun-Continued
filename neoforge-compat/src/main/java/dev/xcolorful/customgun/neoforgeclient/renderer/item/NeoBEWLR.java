package dev.xcolorful.customgun.neoforgeclient.renderer.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import dev.xcolorful.customgun.client.api.item.IItemBEWLR;
import dev.xcolorful.customgun.client.api.renderer.item.ItemDisplayContextTracker;
import dev.xcolorful.customgun.client.api.renderer.item._SpecialModelRenderer;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/*
// 为了跨版本提前知道1.21.4neoforge的移植方式，添加此类作为占位符
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
public class NeoBEWLR implements
        SpecialModelRenderer<ItemStack>,
        _SpecialModelRenderer<ItemStack> {

    public static final NeoBEWLR INSTANCE = new NeoBEWLR();

    private NeoBEWLR() {}

    @Override
    public void render(@Nullable ItemStack itemStack,
                       ItemDisplayContext displayContext,
                       PoseStack poseStack,
                       MultiBufferSource bufferSource,
                       int packedLight, int packedOverlay,
                       boolean hasFoilType) {
        if (itemStack == null || !(itemStack.getItem() instanceof IItemBEWLR itemBEWLR)) return;

        ItemDisplayContext itemDisplayContext = ItemDisplayContextTracker.current();
        if (true) itemDisplayContext = displayContext; // 26.1移除

        /*
        1.21.10 起 SpecialModelRenderer 改为 submit(SubmitNodeCollector)，不再由原版在 submit 后 endBatch
        renderByItem 里 GUI 槽位贴图会直接写入 bufferSource 而不 flush，需要在这里补上 flush
        3D 模型渲染走全局 bufferSource 并自行 endBatch
         */
//        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource(); // [1.21.10, 26.2)

        var bewlr = itemBEWLR.cgc$getBEWLR(); // 用var就跟ResourceLocation是一样的手法 (把import给隐身，省一个移植修改)，除此之外没别的用意
        bewlr.renderByItem(itemStack,
                itemDisplayContext,
                poseStack,
                bufferSource,
                packedLight, packedOverlay);

//        if (!OculusCompat.endBatch(bufferSource)) { // [1.21.10, 26.2)
//            bufferSource.endBatch();
//        }
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

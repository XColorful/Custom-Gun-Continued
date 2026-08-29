package dev.xcolorful.customgun.neoforgeclient.renderer.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import dev.xcolorful.customgun.client.api.item.IItemBEWLR;
import dev.xcolorful.customgun.client.api.renderer.item.ItemDisplayContextTracker;
import dev.xcolorful.customgun.client.api.renderer.item._SpecialModelRenderer;
import dev.xcolorful.customgun.client.compat.oculus.OculusCompat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.Set;

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
    public void submit(@Nullable ItemStack itemStack,
                       ItemDisplayContext displayContext,
                       PoseStack poseStack,
                       SubmitNodeCollector nodeCollector,
                       int packedLight, int packedOverlay,
                       boolean hasFoil, int outlineColor) {
        if (itemStack == null || !(itemStack.getItem() instanceof IItemBEWLR itemBEWLR)) return;

        ItemDisplayContext itemDisplayContext = ItemDisplayContextTracker.current();
        if (true) itemDisplayContext = displayContext; // 26.1移除

        var bewlr = itemBEWLR.cgc$getBEWLR(); // 用var就跟ResourceLocation是一样的手法 (把import给隐身，省一个移植修改)，除此之外没别的用意
        // 1.21.10 起 SpecialModelRenderer 改为 submit(SubmitNodeCollector)，不再由原版在 submit 后 endBatch。
        // renderByItem 里 GUI 槽位贴图会直接写入 bufferSource 而不 flush，需要在这里补上 flush；3D 模型渲染走全局 bufferSource 并自行 endBatch。
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        bewlr.renderByItem(itemStack,
                itemDisplayContext,
                poseStack,
                bufferSource,
                packedLight, packedOverlay);
        if (!OculusCompat.endBatch(bufferSource)) {
            bufferSource.endBatch();
        }
    }

    @Override
    public void getExtents(Set<Vector3f> output) {
        // extents 为空会让 getModelBoundingBox() 得到无限大 AABB，ItemEntityRenderer 会据此把物品平移到无穷远，导致掉落物只看得见影子
        // 默认给一个 1×1×1 格的近似包围盒（item 单位，与 getExtentsForGui 的 /16 约定一致）
        output.add(new Vector3f(0.0F, 0.0F, 0.0F));
        output.add(new Vector3f(0.0F, 0.0F, 1.0F));
        output.add(new Vector3f(0.0F, 1.0F, 0.0F));
        output.add(new Vector3f(0.0F, 1.0F, 1.0F));
        output.add(new Vector3f(1.0F, 0.0F, 0.0F));
        output.add(new Vector3f(1.0F, 0.0F, 1.0F));
        output.add(new Vector3f(1.0F, 1.0F, 0.0F));
        output.add(new Vector3f(1.0F, 1.0F, 1.0F));
    }
    /**
     * 给物品模型提供包围盒
     * <ul>
     *     <li>{@link net.minecraft.client.renderer.entity.ItemEntityRenderer}（掉落物悬浮高度/平铺）</li>
     *     <li>{@code net.minecraft.client.gui.render.state.GuiItemRenderState}（仅当 assets/{@link CustomGun#MOD_ID}/items/*.json 里 oversized_in_gui=true 时才用）</li>
     * </ul>
     * @param output
     */
    @ApiStatus.AvailableSince("1.21.11")
    public void getExtents(Consumer<Vector3fc> output) {
        // extents 为空会让 getModelBoundingBox() 得到无限大 AABB，ItemEntityRenderer 会据此把物品平移到无穷远，导致掉落物只看得见影子
        // 默认给一个 1×1×1 格的近似包围盒（item 单位，与 getExtentsForGui 的 /16 约定一致）
        output.add(new Vector3f(0.0F, 0.0F, 0.0F));
        output.add(new Vector3f(0.0F, 0.0F, 1.0F));
        output.add(new Vector3f(0.0F, 1.0F, 0.0F));
        output.add(new Vector3f(0.0F, 1.0F, 1.0F));
        output.add(new Vector3f(1.0F, 0.0F, 0.0F));
        output.add(new Vector3f(1.0F, 0.0F, 1.0F));
        output.add(new Vector3f(1.0F, 1.0F, 0.0F));
        output.add(new Vector3f(1.0F, 1.0F, 1.0F));
    }

    @Override
    public @Nullable ItemStack extractArgument(ItemStack itemStack) {
        return itemStack.getItem() instanceof IItemBEWLR ? itemStack : null;
    }

    public record BewlrUnbaked() implements SpecialModelRenderer.Unbaked {
        public static final MapCodec<BewlrUnbaked> MAP_CODEC = MapCodec.unit(new BewlrUnbaked());

        @Override
        public @Nullable SpecialModelRenderer<?> bake(SpecialModelRenderer.BakingContext context) {
            return INSTANCE;
        }

        @Override public MapCodec<BewlrUnbaked> type() {
            return MAP_CODEC;
        }
    }
}

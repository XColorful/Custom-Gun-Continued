package dev.xcolorful.customgun.neoforgeclient.renderer.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.client.api.item.IItemBEWLR;
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
import org.joml.Vector3f;

import java.util.Set;
import java.util.function.Consumer;

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

        ItemDisplayContext itemDisplayContext = dev.xcolorful.customgun.client.api.renderer.item.ItemDisplayContextTracker.current();
        if (true) itemDisplayContext = displayContext; // 26.1移除

        var bewlr = itemBEWLR.cgc$getBEWLR(); // 用var就跟ResourceLocation是一样的手法 (把import给隐身，省一个移植修改)，除此之外没别的用意
        bewlr.renderByItem(itemStack,
                itemDisplayContext,
                poseStack,
                bufferSource,
                packedLight, packedOverlay);
    }

//  @Deprecated(since = "1.21.11")
    @ApiStatus.AvailableSince("1.21.6")
    public void getExtents(Set<Vector3f> output) {
        this.getExtents(output::add);
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
    public void getExtents(Consumer<Vector3f> output) {
        // extents 为空会让 getModelBoundingBox() 得到无限大 AABB，ItemEntityRenderer 会据此把物品平移到无穷远，导致掉落物只看得见影子
        // 默认给一个 1×1×1 格的近似包围盒（item 单位，与 getExtentsForGui 的 /16 约定一致）
        output.accept(new Vector3f(0.0F, 0.0F, 0.0F));
        output.accept(new Vector3f(0.0F, 0.0F, 1.0F));
        output.accept(new Vector3f(0.0F, 1.0F, 0.0F));
        output.accept(new Vector3f(0.0F, 1.0F, 1.0F));
        output.accept(new Vector3f(1.0F, 0.0F, 0.0F));
        output.accept(new Vector3f(1.0F, 0.0F, 1.0F));
        output.accept(new Vector3f(1.0F, 1.0F, 0.0F));
        output.accept(new Vector3f(1.0F, 1.0F, 1.0F));
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

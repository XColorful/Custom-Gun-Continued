package dev.xcolorful.customgun.client.api.renderer.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import dev.xcolorful.customgun.CustomGun;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import javax.annotation.Nullable;
import java.util.Set;
import java.util.function.Consumer;

/**
 * 为了跨版本提前知道原版1.21.4的移植方式，添加此类作为占位符
 * 同时复用default方法
 */
@Deprecated(since = "1.21.4", forRemoval = false)
@ApiStatus.AvailableSince("1.21.4")
public interface _SpecialModelRenderer<T> {

    void submit(@Nullable T patterns,
                ItemDisplayContext displayContext,
                PoseStack poseStack,
                SubmitNodeCollector nodeCollector,
                int packedLight, int packedOverlay,
                boolean hasFoilType,
                int outlineColor
    );

//  @Deprecated(since = "1.21.11")
    @ApiStatus.AvailableSince("1.21.6")
    default void getExtents(Set<Vector3f> output) {
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
    default void getExtents(Consumer<Vector3fc> output) {
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

    @Nullable T extractArgument(ItemStack stack);

    @Deprecated(since = "1.21.10")
    interface Unbaked {

        @Nullable
        _SpecialModelRenderer<?> bake(EntityModelSet modelSet);

        MapCodec<? extends Unbaked> type();
    }
}

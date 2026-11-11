package dev.xcolorful.customgun.client.model.bake;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.client.compat.iris.IrisCompat;
import dev.xcolorful.customgun.client.init.registry.ClientRenderRegistry;
import dev.xcolorful.customgun.client.util.ClientRenderProfiler;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.world.item.ItemDisplayContext;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.function.Consumer;

/**
 * B1-a 静态烘焙的入口
 * <ul>
 *     <li>节点姿态不变的场景（展示框 / 地面 / 第三人称）用烘焙网格替换逐顶点遍历</li>
 *     <li>第一人称不在本阶段范围内，仍走即时管线</li>
 *     <li>光照与雾由「<b>实例矩阵参与着色</b>」保证，见 {@link #resolveEntityShader()}</li>
 * </ul>
 * 状态段（functional 回调与可见性）仍然每帧执行，见 {@code BedrockPart#renderStatus}。
 */
@ApiStatus.Internal
public final class StaticBake {

    private StaticBake() {}

    /**
     * 烘焙路径总开关。开关用于实机对照；关闭时行为与重构前完全一致。
     */
    public static boolean ENABLED = true;

    /**
     * 只有非第一人称的展示类场景参与烘焙。
     * <p>
     * GUI 槽位不走模型渲染，不会进入这里。
     */
    public static boolean eligible(ItemDisplayContext transformType) {
        return ENABLED && !transformType.firstPerson();
    }

    /**
     * 本次绘制该用的实体着色器；必须在 {@link RenderType#setupRenderState()} 之后调用
     * <ul>
     *     <li><b>开光影包</b>：Oculus/Iris 在 {@code GameRenderer.getRendertype*Shader()} 里把该 RenderType 的
     *     shader 换成光影包 program（按 shadow / hand / block-entity / entities 阶段分支），
     *     那份 shader 的位置与法线都由 {@code ModelViewMat} 驱动，直接沿用即可</li>
     *     <li><b>无光影</b>：用 CGC 自己的烘焙着色器；平台没有注册它时返回 {@code null}，
     *     调用方退回即时管线</li>
     * </ul>
     */
    public static @Nullable ShaderInstance resolveEntityShader() {
        if (IrisCompat.isUsingRenderPack()) {
            return RenderSystem.getShader();
        }
        @Nullable ShaderInstance shader = ClientRenderRegistry.BakedEntityRenderState.getBakedEntityShader();
        if (shader == null && !warnedNoShader) {
            warnedNoShader = true;
            CustomGun.LOGGER.warn("Core shader {} is unavailable, static bake falls back to the immediate pipeline",
                    ClientRenderRegistry.BakedEntityRenderState.BAKED_ENTITY_SHADER);
        }
        return shader;
    }

    private static boolean warnedNoShader;

    /**
     * 取缓存或烘焙，然后用模型视图矩阵绘制。
     *
     * @param owner       网格所属的模型对象（同一把枪的所有展示框共享）
     * @param subKey      同一模型内的子网格标识（如瞄具的某条节点路径），整模型传 {@code null}
     * @param pose        烘焙空间 → 视图空间的变换；必须与 {@code emitter} 写入的模型空间一致
     * @param fingerprint 状态段采集到的节点状态指纹，变化即失效
     * @param emitter     在模型空间写顶点的回调
     * @return {@code false} 表示没有可用着色器，调用方应退回即时管线（需丢弃状态段登记的委托）
     */
    public static boolean drawOrBake(@Nullable Object owner,
                                     @Nullable Object subKey,
                                     Matrix4f pose,
                                     RenderType renderType,
                                     int light, int overlay,
                                     float red, float green, float blue, float alpha,
                                     long fingerprint,
                                     Consumer<VertexConsumer> emitter) {
        boolean shaderPack = IrisCompat.isUsingRenderPack();
        StaticBakeCache.Key key = new StaticBakeCache.Key(owner, subKey, renderType, light, overlay, fingerprint,
                shaderPack, red, green, blue, alpha);

        // F12 采集期间强制重建，用于人工验收「缓存失效」与测量烘焙成本
        @Nullable BakedMesh mesh = ClientRenderProfiler.enabled ? null : StaticBakeCache.get(key);
        if (mesh == null) {
            final long profBake = ClientRenderProfiler.begin();
            mesh = BakedMesh.bake(renderType.mode(), renderType.format(), emitter);
            ClientRenderProfiler.end(ClientRenderProfiler.BAKE, profBake);
            ClientRenderProfiler.countBake();
            StaticBakeCache.put(key, mesh);
        }

        // 光影包刚切换时，顶点布局与当前着色器不匹配：本帧退回即时管线，下一帧会重新烘焙
        if (mesh.bytes() > 0 && mesh.extendedFormat() != shaderPack) return false;

        ClientRenderProfiler.countRenderType(renderType);
        final long profDraw = ClientRenderProfiler.begin();
        boolean drawn = mesh.draw(renderType, pose);
        ClientRenderProfiler.end(ClientRenderProfiler.DRAW, profDraw);
        if (drawn) ClientRenderProfiler.countDraw();
        return drawn;
    }
}

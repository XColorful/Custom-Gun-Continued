package dev.xcolorful.customgun.client.renderer.model;

import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.xcolorful.customgun.client.init.registry.ClientRenderRegistry;
import dev.xcolorful.customgun.client.util.ClientRenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.TextRenderable;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;

import java.util.*;

/**
 * 模型文字的最终覆盖层
 * <ul>
 *     文字不再走 {@code submitText}：
 *     <li>提交进手部 pass 的文字会被 Iris 派到光影包的手部程序，而文字顶点格式没有 Normal，光照语义对不上，表现为发黑与拖影</li>
 *     <li>改为在手部几何的 flush 阶段排队（{@link #queue}），等这一遍真的画完之后，用快照的手部投影与模型视图配合 {@link ClientRenderRegistry.ModelTextRenderState} 自己的管线重画（{@link #flush}）</li>
 *     <li>两个刷新点都调用 {@link #flush}：level 渲染之后（光影下 Iris 已把手部搬进{@code LevelRenderer#render}）与 {@code renderLevel} 返回前（无光影时手部在之后才画）</li>
 *     <li>队列为空时开销就是一次判空</li>
 *     <li>深度仍按主深度缓冲生效：目镜黑盘挡在文字前面，开镜孔里才可见</li>
 * </ul>
 */
@ApiStatus.AvailableSince("26.2")
public final class TextRenderOverlay {

    private static final List<QueuedText> QUEUE = new ArrayList<>();

    private static @Nullable Matrix4f handModelView;
    private static @Nullable GpuBufferSlice handProjection;
    private static @Nullable ProjectionType handProjectionType;

    private TextRenderOverlay() {
    }

    /**
     * 把一条文字排进覆盖层
     * <ul>
     *     <li>调用点在模型几何的 flush 回调里</li>
     *     <li>此刻 {@code RenderSystem} 里挂的就是手部那一遍的矩阵</li>
     * </ul>
     */
    public static void queue(Matrix4f pose,
                             Matrix3f normal,
                             float x, float y,
                             String text,
                             boolean shadow,
                             int packedLight,
                             int color) {
        if (handModelView == null) {
            handModelView = RenderSystem.getModelViewMatrixCopy();
            handProjection = RenderSystem.getProjectionMatrixBuffer();
            handProjectionType = RenderSystem.getProjectionType();
        }
        QUEUE.add(new QueuedText(new Matrix4f(pose), new Matrix3f(normal), x, y, text, shadow, packedLight, color));
    }

    /**
     * 把排队中的文字画到最终画面上
     */
    public static void flush() {
        List<QueuedText> texts = List.copyOf(QUEUE);
        QUEUE.clear();

        Matrix4f modelView = handModelView;
        GpuBufferSlice projection = handProjection;
        ProjectionType projectionType = handProjectionType;
        handModelView = null;
        handProjection = null;
        handProjectionType = null;

        if (texts.isEmpty() || modelView == null) return;

        GpuBufferSlice previousProjection = RenderSystem.getProjectionMatrixBuffer();
        ProjectionType previousProjectionType = RenderSystem.getProjectionType();
        Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushMatrix();
        modelViewStack.set(modelView);
        RenderSystem.setProjectionMatrix(projection, projectionType);
        try {
            SubmitNodeStorage storage = new SubmitNodeStorage();
            Font font = Minecraft.getInstance().font;

            for (int i = 0; i < texts.size(); i++) {
                QueuedText queued = texts.get(i);

                PoseStack poseStack = new PoseStack();
                poseStack.last().pose().set(queued.pose());
                poseStack.last().normal().set(queued.normal());

                Font.PreparedText prepared = font.prepareText(queued.text(),
                        queued.x(),
                        queued.y(),
                        queued.color(),
                        queued.shadow(),
                        0);

                Map<RenderType, List<TextRenderable>> groups = new LinkedHashMap<>();
                prepared.visit(new Font.GlyphVisitor() {
                    @Override
                    public void acceptRenderable(TextRenderable renderable) {
                        groups.computeIfAbsent(renderable.renderType(Font.DisplayMode.NORMAL),
                                key -> new ArrayList<>()).add(renderable);
                    }
                });

                for (Map.Entry<RenderType, List<TextRenderable>> entry : groups.entrySet()) {
                    RenderType renderType = ClientRenderRegistry.ModelTextRenderState.getModelText(entry.getKey());
                    List<TextRenderable> renderables = entry.getValue();

                    storage.submitCustomGeometry(poseStack, renderType, (pose, consumer) -> {
                        for (int j = 0; j < renderables.size(); j++) {
                            renderables.get(j).render(pose.pose(), consumer, queued.packedLight(), false);
                        }
                    });
                }
            }

            Minecraft mc = Minecraft.getInstance();
            {
                // [26.2, 26.3)
                mc.gameRenderer.featureRenderDispatcher().renderAllFeatures(storage);

                // [26.3, )
                /**
                 * <ul>
                 *     <li>26.3 的 FeatureRenderDispatcher 不再自开 pass</li>
                 *     <li>renderAllFeatures 变成 static (RenderPass, PreparedFrame)，输出目标在 createRenderPass 时显式指定</li>
                 *     <li>序列与 Iris 26.3 的 HandRenderer 一致：prepareFrame → createRenderPass → renderAllFeatures → frame.close</li>
                 * </ul>
                 */
//                RenderTarget main = ClientRenderUtils.getMainRenderTarget(mc);
//                if (main == null || main.getColorTextureView() == null) return;
//
//                FeatureRenderDispatcher dispatcher = mc.gameRenderer.featureRenderDispatcher();
//                FeatureRenderDispatcher.PreparedFrame frame = dispatcher.prepareFrame(storage);
//                try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(
//                        () -> "CustomGun model text overlay",
//                        main.getColorTextureView(), Optional.empty(),
//                        main.getDepthTextureView(), OptionalDouble.empty())) {
//                    FeatureRenderDispatcher.renderAllFeatures(renderPass, frame);
//                } finally {
//                    frame.close();
//                }
            }
        } finally {
            modelViewStack.popMatrix();
            RenderSystem.setProjectionMatrix(previousProjection, previousProjectionType);
        }
    }

    private record QueuedText(Matrix4f pose,
                              Matrix3f normal,
                              float x, float y,
                              String text,
                              boolean shadow,
                              int packedLight,
                              int color) {
    }
}

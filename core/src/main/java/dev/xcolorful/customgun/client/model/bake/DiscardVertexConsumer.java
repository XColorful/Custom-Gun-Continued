package dev.xcolorful.customgun.client.model.bake;

import com.mojang.blaze3d.vertex.VertexConsumer;
import org.jetbrains.annotations.ApiStatus;

/**
 * 只接收调用、不写顶点的 {@link VertexConsumer}
 * <ul>
 *     <li>状态段用它执行 functional 回调：回调需要拿到 consumer 参数，但状态段不产出烘焙网格</li>
 *     <li>记录 {@code endVertex()}/{@code addVertex()} 次数，用于发现「回调直接发射了几何」的情况——那类回调无法烘焙，调用方必须退回即时管线</li>
 * </ul>
 */
@ApiStatus.Internal
public final class DiscardVertexConsumer implements VertexConsumer {

    public static final DiscardVertexConsumer INSTANCE = new DiscardVertexConsumer();

    private long vertices;

    private DiscardVertexConsumer() {}

    /**
     * 累计被丢弃的顶点数；调用方取前后差值判断状态段是否发射过几何
     */
    public long writtenVertices() {
        return this.vertices;
    }

    @Deprecated(since = "1.21.1")
    @Override
    public VertexConsumer vertex(double x, double y, double z) {
        return this;
    }
    @ApiStatus.AvailableSince("1.21.1")
//    @Override
    public VertexConsumer addVertex(float x, float y, float z) {
        this.vertices++; return this;
    }

    @Deprecated(since = "1.21.1")
    @Override
    public VertexConsumer color(int red, int green, int blue, int alpha) {
        return this;
    }
    @ApiStatus.AvailableSince("1.21.1")
//    @Override
    public VertexConsumer setColor(int red, int green, int blue, int alpha) {
        return this;
    }

    @Deprecated(since = "1.21.1")
    @Override
    public VertexConsumer uv(float u, float v) {
        return this;
    }
    @ApiStatus.AvailableSince("1.21.1")
//    @Override
    public VertexConsumer setUv(float u, float v) {
        return this;
    }

    @Deprecated(since = "1.21.1")
    @Override
    public VertexConsumer overlayCoords(int u, int v) {
        return this;
    }
    @ApiStatus.AvailableSince("1.21.1")
//    @Override
    public VertexConsumer setUv1(int u, int v) {
        return this;
    }

    @Deprecated(since = "1.21.1")
    @Override
    public VertexConsumer uv2(int u, int v) {
        return this;
    }
    @ApiStatus.AvailableSince("1.21.1")
//    @Override
    public VertexConsumer setUv2(int u, int v) {
        return this;
    }

    @Deprecated(since = "1.21.1")
    @Override
    public VertexConsumer normal(float x, float y, float z) {
        return this;
    }
    @ApiStatus.AvailableSince("1.21.1")
//    @Override
    public VertexConsumer setNormal(float x, float y, float z) {
        return this;
    }

    @Deprecated(since = "1.21.1")
    @Override
    public void endVertex() {
        this.vertices++;
    }

    @Deprecated(since = "1.21.1")
    @Override
    public void defaultColor(int red, int green, int blue, int alpha) {
    }

    @Deprecated(since = "1.21.1")
    @Override
    public void unsetDefaultColor() {
    }
}

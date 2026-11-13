package dev.xcolorful.customgun.client.model.bake;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.xcolorful.customgun.client.model.ModelObject;
import dev.xcolorful.customgun.client.resource._AssetsInstanceManager;
import net.minecraft.client.renderer.RenderType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * 烘焙网格的全局 LRU 缓存
 * <ul>
 *     <li>键包含模型身份、子网格、纹理（{@code RenderType} 实例）、光照、叠加与节点状态指纹；<b>不含世界矩阵</b></li>
 *     <li>同款枪的多个展示框共享同一个 {@code ModelObject}，因此共享同一份网格</li>
 *     <li>光照仍是键：同一份整模网格会被多个展示实例同时使用（各自 {@code packedLight} 不同），
 *         按光照保留多份才是这里的正确形态；光照解耦只做在第一人称分块路径（见 {@code ModelObject.Cgc$Chunk}）</li>
 *     <li>资源重载时由 {@link _AssetsInstanceManager#reload} 清空，避免持有旧模型与显存</li>
 * </ul>
 */
@ApiStatus.Internal
public final class StaticBakeCache {

    /**
     * 条目上限按「一把枪 = 枪体 + 若干配件 + 瞄具子网格」估算：满配配置不同的枪会各自成键，
     * 32 项在 multi 场景会反复淘汰重烘；真正的内存上限由 {@link #MAX_BYTES} 兜底。
     */
    private static final int MAX_ENTRIES = 128;
    private static final long MAX_BYTES = 32L << 20;

    private static final LinkedHashMap<Key, BakedMesh> MESHES = new LinkedHashMap<>(16, 0.75F, true);
    private static long bytes;

    /**
     * 持有第一人称分块网格的模型（B1-b）：分块网格挂在模型上管理生命周期，这里只保留身份引用，
     * 以便资源重载时统一释放显存
     */
    private static final Set<ModelObject> MODEL_CHUNKS = Collections.newSetFromMap(new IdentityHashMap<>());

    private StaticBakeCache() {}

    /**
     * @param owner       所属模型对象（身份比较）
     * @param subKey      子网格标识（身份/结构比较），整模型为 {@code null}
     * @param fingerprint 节点状态指纹
     * @param shaderPack  是否开着光影包：光影包会把 {@code NEW_ENTITY} 顶点格式扩展成
     *                    {@code IrisVertexFormats.ENTITY}，两种格式的 VBO 不能复用
     * @param red/…/alpha 顶点颜色，进键以免颜色变化复用旧颜色
     */
    public record Key(@Nullable Object owner,
                      @Nullable Object subKey,
                      RenderType renderType,
                      int light, int overlay,
                      long fingerprint,
                      boolean shaderPack,
                      float red, float green, float blue, float alpha) {}

    public static @Nullable BakedMesh get(Key key) {
        return MESHES.get(key);
    }

    public static void put(Key key, BakedMesh mesh) {
        @Nullable BakedMesh previous = MESHES.put(key, mesh);
        if (previous != null) {
            bytes -= previous.bytes();
            close(previous);
        }
        bytes += mesh.bytes();
        evict();
    }

    private static void evict() {
        // 至少保留一个：单个网格就超过预算时也不能把它淘汰掉，否则调用方拿到已关闭的网格、画面直接消失
        while ((MESHES.size() > MAX_ENTRIES || bytes > MAX_BYTES) && MESHES.size() > 1) {
            Iterator<Map.Entry<Key, BakedMesh>> iterator = MESHES.entrySet().iterator();
            if (!iterator.hasNext()) return;
            Map.Entry<Key, BakedMesh> eldest = iterator.next();
            iterator.remove();
            bytes -= eldest.getValue().bytes();
            close(eldest.getValue());
        }
    }

    /**
     * 登记一个创建了分块网格的模型，资源重载时统一释放
     */
    public static void registerModel(ModelObject model) {
        MODEL_CHUNKS.add(model);
    }

    /**
     * 资源重载：旧模型对象会被整体替换，网格必须一并释放
     */
    public static void clearAll() {
        for (BakedMesh mesh : MESHES.values()) {
            close(mesh);
        }
        MESHES.clear();
        bytes = 0;

        for (ModelObject model : MODEL_CHUNKS) {
            if (RenderSystem.isOnRenderThread()) {
                model.cgc$clearChunks();
            } else {
                RenderSystem.recordRenderCall(model::cgc$clearChunks);
            }
        }
        MODEL_CHUNKS.clear();
    }

    private static void close(BakedMesh mesh) {
        if (RenderSystem.isOnRenderThread()) {
            mesh.close();
        } else {
            RenderSystem.recordRenderCall(mesh::close);
        }
    }
}

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
 *     <li>有两套键：按光照建键的整模网格（会被多个展示实例共享），以及不含光照的单实例网格
 *         （第一人称分块与瞄具子网格，光照变化时原地改写 uv2）</li>
 *     <li>同款枪的多个展示框共享同一个 {@code ModelObject}，因此共享同一份网格</li>
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
    /**
     * 光照解耦的单实例网格（第一人称）：键不含光照，一个键保留少量光照变体
     */
    private static final LinkedHashMap<PatchKey, Variants> PATCHABLE = new LinkedHashMap<>(16, 0.75F, true);
    private static long bytes;

    /**
     * 持有第一人称分块网格的模型（B1-b）：分块网格挂在模型上管理生命周期，这里只保留身份引用，
     * 以便资源重载时统一释放显存
     */
    private static final Set<ModelObject> MODEL_CHUNKS = Collections.newSetFromMap(new IdentityHashMap<>());

    private StaticBakeCache() {}

    /**
     * 按光照建键的整模网格键（展示框 / 地面 / 第三人称：同一份网格被多个实例共享，各自 {@code packedLight} 不同）
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

    /**
     * 光照解耦的单实例网格键（第一人称分块 / 瞄具子网格）：<b>不含 {@code light}</b>，光照变化走原地改写
     * <br>
     * 只能用于「同一帧只有一份实例」的路径；多实例共享的整模网格必须用 {@link Key}
     */
    public record PatchKey(@Nullable Object owner,
                           @Nullable Object subKey,
                           RenderType renderType,
                           int overlay,
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

    public static @Nullable BakedMesh getPatchable(PatchKey key, int light) {
        @Nullable Variants variants = PATCHABLE.get(key);
        return variants == null ? null : variants.get(light);
    }

    /**
     * 该键是否已经有网格：区分「首次烘焙」与「光照变体未满」
     */
    public static boolean hasPatchable(PatchKey key) {
        return PATCHABLE.containsKey(key);
    }

    /**
     * 满员时取出一条最久未用的变体，供调用方改写光照；取出后必须 {@link #putPatchable} 回去或关闭
     */
    public static @Nullable BakedMesh takeReusablePatchable(PatchKey key) {
        @Nullable Variants variants = PATCHABLE.get(key);
        if (variants == null) return null;
        @Nullable BakedMesh mesh = variants.takeReusable();
        if (mesh != null) bytes -= mesh.bytes();
        return mesh;
    }

    public static void putPatchable(PatchKey key, int light, BakedMesh mesh) {
        Variants variants = PATCHABLE.get(key);
        if (variants == null) {
            variants = new Variants();
            PATCHABLE.put(key, variants);
        }
        int before = variants.bytes();
        variants.put(light, mesh);
        bytes += variants.bytes() - before;
        evict();
    }

    private static void evict() {
        // 至少保留一个：单个网格就超过预算时也不能把它淘汰掉，否则调用方拿到已关闭的网格、画面直接消失
        while ((MESHES.size() + PATCHABLE.size() > MAX_ENTRIES || bytes > MAX_BYTES)
                && MESHES.size() + PATCHABLE.size() > 1) {
            if (!evictEldest()) return;
        }
    }

    private static boolean evictEldest() {
        Iterator<Map.Entry<Key, BakedMesh>> meshes = MESHES.entrySet().iterator();
        if (meshes.hasNext()) {
            Map.Entry<Key, BakedMesh> eldest = meshes.next();
            meshes.remove();
            bytes -= eldest.getValue().bytes();
            close(eldest.getValue());
            return true;
        }
        Iterator<Map.Entry<PatchKey, Variants>> variants = PATCHABLE.entrySet().iterator();
        if (variants.hasNext()) {
            Map.Entry<PatchKey, Variants> eldest = variants.next();
            variants.remove();
            bytes -= eldest.getValue().bytes();
            close(eldest.getValue());
            return true;
        }
        return false;
    }

    /**
     * 同一条几何在不同光照下的网格变体；满员后改写最久未用的那一份，正在使用的那份不动
     */
    private static final class Variants {

        /**
         * 变体上限：主 pass 与光影阴影 pass 各一份
         * <li>不能只留一份：两个 pass 的光照不同，每帧来回切换会退化成每帧整段重传</li>
         * <li>不放大余量：满员才开始改写，上限越大，光照变化前要白白多烘焙几次</li>
         */
        private static final int MAX_VARIANTS = 2;

        /**
         * accessOrder = true：迭代顺序即最久未用顺序
         */
        private final Map<Integer, BakedMesh> meshes = new LinkedHashMap<>(2, 0.75F, true);

        BakedMesh get(int light) {
            return this.meshes.get(light);
        }

        @Nullable BakedMesh takeReusable() {
            if (this.meshes.size() < MAX_VARIANTS) return null;
            Iterator<Map.Entry<Integer, BakedMesh>> iterator = this.meshes.entrySet().iterator();
            if (!iterator.hasNext()) return null;
            Map.Entry<Integer, BakedMesh> eldest = iterator.next();
            iterator.remove();
            return eldest.getValue();
        }

        void put(int light, BakedMesh mesh) {
            @Nullable BakedMesh previous = this.meshes.put(light, mesh);
            if (previous != null && previous != mesh) previous.close();
        }

        int bytes() {
            int total = 0;
            for (BakedMesh mesh : this.meshes.values()) {
                total += mesh.bytes();
            }
            return total;
        }

        void close() {
            for (BakedMesh mesh : this.meshes.values()) {
                mesh.close();
            }
            this.meshes.clear();
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
        for (Variants variants : PATCHABLE.values()) {
            close(variants);
        }
        PATCHABLE.clear();
        bytes = 0;
        StaticBatch.clearAll();

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

    private static void close(Variants variants) {
        if (RenderSystem.isOnRenderThread()) {
            variants.close();
        } else {
            RenderSystem.recordRenderCall(variants::close);
        }
    }
}

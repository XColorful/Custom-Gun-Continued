package dev.xcolorful.customgun.client.util;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.core.api.event.EventType;
import dev.xcolorful.customgun.core.api.event.IEvent;
import dev.xcolorful.customgun.core.api.event.IEventHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;

/**
 * 渲染链路埋点
 * <ul>
 *     <li>按 F12 开始采集，再按一次停止并立即统计、追加写入文件</li>
 *     <li>未采集时热路径成本：每个埋点一次 {@link #enabled} 的 volatile 读 + 一次分支</li>
 *     <li>帧边界取 {@link EventType#RENDER_FRAME_EVENT}（Forge 上即 {@code TickEvent.RenderTickEvent} 的 END，整帧渲染结束）</li>
 *     <li>探针之间可以嵌套（例如提交包含模型渲染，模型渲染又包含 flush），统计时各算各的，不能直接相加</li>
 * </ul>
 * 埋点位置与含义见 ./docs/architecture/client/render/chain/Home.md 的「测量与数据登记」
 */
@ApiStatus.Internal
public final class ClientRenderProfiler implements IEventHandler {

    // --------探针--------

    /** 第一人称整次提交（状态更新 → 模型渲染 → 枪口缓存） */
    public static final int FP_SUBMIT = 0;
    /** 非 GUI 的 renderByItem 整次提交（展示框 / 掉落物 / 第三人称） */
    public static final int WORLD_SUBMIT = 1;
    /** 状态机 update() */
    public static final int STATE_UPDATE = 2;
    /** 每次提交前的附件 / 枪械数据刷新 */
    public static final int DATA_REFRESH = 3;
    /** renderScope（模版窗口准备） */
    public static final int SCOPE = 4;
    /** ModelObject.render 整体（含嵌套的配件模型） */
    public static final int MODEL_TOTAL = 5;
    /** ModelObject.render 的节点快照采集与还原 */
    public static final int SNAPSHOT = 6;
    /** ModelObject.render 的 shouldRender 遍历与顶点生成 */
    public static final int TRAVERSE = 7;
    /** 所有 endBatch 的耗时合计 */
    public static final int FLUSH = 8;
    /** ModelObject.render 的 delegate 执行 */
    public static final int DELEGATE = 9;
    /** 第一人称枪口位置缓存 */
    public static final int MUZZLE_CACHE = 10;
    /** 瞄具模版缓冲清空 */
    public static final int STENCIL_CLEAR = 11;
    /** 第一人称手部事件入口（含早退判定） */
    public static final int ENTRY = 12;
    /** 第三人称逐帧 visualUpdate */
    public static final int VISUAL_UPDATE = 13;
    /** 状态机初始化（切枪时） */
    public static final int TRY_INIT = 14;
    /** 功能性节点回调登记 */
    public static final int FUNCTIONAL_NODES = 15;
    /** GUI 槽位图标 */
    public static final int SLOT_ICON = 16;
    /** 世界相机角度事件 */
    public static final int CAMERA_LEVEL = 17;
    /** 手部相机动画事件 */
    public static final int CAMERA_HAND = 18;
    /** FOV 事件 */
    public static final int FOV = 19;
    /** endBatch 内的缓冲上传（VertexBuffer.upload） */
    public static final int UPLOAD = 20;
    /** endBatch 内的 draw（VertexBuffer.drawWithShader） */
    public static final int DRAW = 21;
    /** 渲染重构B1-a 静态烘焙（首次或失效后的重建） */
    public static final int BAKE = 22;

    public static final int PROBE_COUNT = 23;
    private static final String[] PROBE_NAMES = {
            "First-person submit", "World-item submit", "State update", "Data refresh", "Scope render",
            "Model render total", "Node snapshot", "Geometry traverse", "endBatch flush",
            "Delegate render", "Muzzle cache", "Stencil clear",
            "Hand event entry", "Third-person visual update", "State machine init",
            "Functional nodes", "GUI slot icon", "Camera angles", "Hand camera", "FOV",
            "endBatch upload", "endBatch draw", "Static bake",
    };

    // --------计数--------

    private static final int COUNTER_FP_SUBMIT = 0;
    private static final int COUNTER_WORLD_SUBMIT = 1;
    private static final int COUNTER_FLUSH = 2;
    private static final int COUNTER_CUBES = 3;
    private static final int COUNTER_NODES = 4;
    private static final int COUNTER_BATCHES = 5;
    private static final int COUNTER_STENCIL_WINDOWS = 6;
    private static final int COUNTER_LOD_HITS = 7;
    private static final int COUNTER_DRAWS = 8;
    private static final int COUNTER_UPLOAD_BYTES = 9;
    private static final int COUNTER_ALLOC_BYTES = 10;
    private static final int COUNTER_ANIM_CHANNELS = 11;
    private static final int COUNTER_STATE_CHANGES = 12;
    private static final int COUNTER_BAKE_REBUILDS = 13;
    private static final int COUNTER_RENDER_TYPES = 14;
    private static final int COUNTER_DISTINCT_NODES = 15;
    private static final int COUNTER_DIRTY_NODES = 16;
    private static final int COUNTER_DIRTY_SUBTREE = 17;
    private static final int COUNTER_COUNT = 18;
    private static final String[] COUNTER_NAMES = {
            "First-person submissions", "World-item submissions", "endBatch calls", "Cubes submitted",
            "Nodes captured", "Geometry batches", "Stencil windows", "LOD hits",
            "Draw calls", "Upload bytes", "Allocated bytes", "Animation channels", "State changes",
            "Bake rebuilds", "Distinct RenderTypes", "Distinct nodes", "Dirty nodes",
            "Dirty subtree nodes",
    };

    /**
     * GPU 查询与渲染线程分配采样周期（帧）：这两项本身有固定开销，逐帧采集会污染被测项
     */
    public static final int SAMPLE_EVERY = 4;

    /**
     * 唯一的热路径开关
     */
    public static volatile boolean enabled = false;

    private static final int CAPACITY = 100_000;
    private static final ClientRenderProfiler INSTANCE = new ClientRenderProfiler();

    /** 仅在开始采集时分配一次，之后复用 */
    private static long[] frameTimes;
    private static long[][] probeSums;
    private static int[][] probeHits;
    private static int[][] counterSums;

    private static final long[] frameProbe = new long[PROBE_COUNT];
    private static final int[] frameProbeHits = new int[PROBE_COUNT];
    private static final int[] frameCounter = new int[COUNTER_COUNT];
    /** 本帧出现过的 RenderType（身份比较），帧末取数量即 B2 的 draw 下限 */
    private static final IdentityHashMap<RenderType, Boolean> frameRenderTypes = new IdentityHashMap<>();

    private static boolean collecting = false;
    private static boolean truncated = false;
    private static int frameCount = 0;
    private static long lastFrameEnd = 0L;

    /** 每帧 GPU 忙时（ns）；0 表示该帧没有采到（非采样帧、首个边界或查询未完成） */
    private static long[] gpuNanos;
    private static long frameGpuNanos = 0L;

    /** 每帧 CGC 提交窗口内的 GPU 忙时（ns），是 {@link #gpuNanos} 的子集 */
    private static long[] cgcGpuNanos;
    private static long frameCgcGpuNanos = 0L;

    /**
     * 帧序号，仅供「按帧去重」的探针使用（节点脏计数、每帧一次的状态指纹）
     * <ul>
     *     <li>只在采集窗口内递增，未采集时不变化</li>
     *     <li>每个采集周期用新种子重置，避免上一周期的标记值被误判为「本帧已访问」</li>
     * </ul>
     */
    private static long frameOrdinal = 0L;

    /** 渲染线程分配量，用于解释 1% low 抖动；HotSpot 上通过 com.sun.management 提供 */
    private static com.sun.management.ThreadMXBean allocationBean;
    private static long allocationThreadId = -1L;
    private static long lastAllocatedBytes = -1L;

    /** 状态机初始化只在切枪时发生一次，通常落在采集窗口之外，因此独立累计，直到写文件才清空 */
    private static int initCount = 0;
    private static long initLastNanos = 0L;
    private static long initMaxNanos = 0L;
    private static long initLastEpochMillis = 0L;

    private ClientRenderProfiler() {}

    public static ClientRenderProfiler get() {
        return INSTANCE;
    }

    // --------IEventHandler--------

    @Override public String getEventHandlerName() {
        return this.getClass().getName();
    }

    @Override public void handleEvent(EventType eventType, IEvent event) {
        if (eventType == EventType.RENDER_FRAME_EVENT) {
            onFrameEnd();
        } else {
            onReceiveWrongEvent(eventType);
        }
    }

    // --------热路径--------

    /**
     * 埋点起点。未采集时只有一次 volatile 读。
     *
     * @return 0 表示未采集，调用方原样传给 {@link #end(int, long)}
     */
    public static long begin() {
        return enabled ? System.nanoTime() : 0L;
    }

    public static void end(int probe, long startNanos) {
        if (startNanos == 0L) return;
        long elapsed = System.nanoTime() - startNanos;
        if (!collecting) return;
        // 命中次数与耗时分开记：极短区间的时间差可能为 0，不能拿耗时判断是否命中
        frameProbe[probe] += elapsed;
        frameProbeHits[probe]++;
    }

    public static void count(int counter) {
        if (!collecting) return;
        frameCounter[counter]++;
    }

    public static void countFlush() {
        count(COUNTER_FLUSH);
    }

    public static void countSubmission(boolean firstPerson) {
        count(firstPerson ? COUNTER_FP_SUBMIT : COUNTER_WORLD_SUBMIT);
    }

    public static void countCube() {
        count(COUNTER_CUBES);
    }

    public static void countBy(int counter, int amount) {
        if (!collecting) return;
        frameCounter[counter] += amount;
    }

    public static void countNodes(int amount) {
        countBy(COUNTER_NODES, amount);
    }

    public static void countBatch() {
        count(COUNTER_BATCHES);
    }

    public static void countStencilWindow() {
        count(COUNTER_STENCIL_WINDOWS);
    }

    public static void countLod() {
        count(COUNTER_LOD_HITS);
    }

    /** 一次 VertexBuffer.drawWithShader */
    public static void countDraw() {
        count(COUNTER_DRAWS);
    }

    /** 一次 draw 对应的顶点 + 索引字节数 */
    public static void countUploadBytes(int bytes) {
        countBy(COUNTER_UPLOAD_BYTES, bytes);
    }

    /** 一个动画通道在本帧执行了一次插值与监听器 */
    public static void countAnimationChannel() {
        count(COUNTER_ANIM_CHANNELS);
    }

    /** 一次提交的节点状态指纹与上次不同（缓存会失效） */
    public static void countStateChange() {
        count(COUNTER_STATE_CHANGES);
    }

    /** 一次静态网格烘焙（首帧或状态变化后） */
    public static void countBake() {
        count(COUNTER_BAKE_REBUILDS);
    }

    /** 记录本帧使用过的一个 RenderType 实例 */
    public static void countRenderType(RenderType renderType) {
        if (!collecting) return;
        frameRenderTypes.put(renderType, Boolean.TRUE);
    }

    /**
     * 本帧第一次访问该节点（同一节点被多个展示框 / 光影阴影 pass 重复渲染时只算一次）
     * 分母取它而不是 {@link #COUNTER_NODES}（按提交累加），否则多实例场景会把脏节点比例稀释成 1/枪数
     */
    public static void countDistinctNode() {
        count(COUNTER_DISTINCT_NODES);
    }

    /** 节点的变换状态与上一帧不同；B1-b 的「脏子树」就是它所在的子树 */
    public static void countDirtyNode() {
        count(COUNTER_DIRTY_NODES);
    }

    /**
     * 脏子树的节点数：以每个脏节点为根、去掉被祖先覆盖的部分后累加
     * 这是 B1-b 重烘焙的实际规模，比 {@link #COUNTER_DIRTY_NODES} 更接近成本
     */
    public static void countDirtySubtree(int nodes) {
        countBy(COUNTER_DIRTY_SUBTREE, nodes);
    }

    /** 当前帧序号；探针按「每帧一次」去重时用作标记值 */
    public static long frameStamp() {
        return frameOrdinal;
    }

    /** CGC 提交窗口开始：把 GPU 查询切成 CGC 段 */
    public static void beginCgcGpu() {
        if (enabled) GpuFrameTimer.beginCgc();
    }

    /** CGC 提交窗口结束：恢复整帧段 */
    public static void endCgcGpu() {
        if (enabled) GpuFrameTimer.endCgc();
    }

    /** GPU 计时查询返回的一帧忙时（ns），由 {@link GpuFrameTimer} 调用 */
    public static void recordGpuFrame(long nanos) {
        frameGpuNanos += nanos;
    }

    /** GPU 计时查询返回的 CGC 提交窗口忙时（ns），由 {@link GpuFrameTimer} 调用 */
    public static void recordGpuCgc(long nanos) {
        frameCgcGpuNanos += nanos;
    }

    /**
     * 采样状态机初始化耗时。与 {@link #begin()} 不同，这里不做开关判断：
     * 切枪发生在采集之前，必须始终记录，写文件后才重新开始累计。
     */
    public static void recordInit(long nanos) {
        initCount++;
        initLastNanos = nanos;
        initLastEpochMillis = System.currentTimeMillis();
        if (nanos > initMaxNanos) initMaxNanos = nanos;
    }

    // --------采集周期--------

    /**
     * 帧边界：RENDER_FRAME_EVENT 在整帧渲染完成时触发。
     * 结算上一帧，再清空本帧累加器。
     */
    private static void onFrameEnd() {
        if (!enabled) return;

        frameOrdinal++;
        boolean sampled = Math.floorMod(frameOrdinal, SAMPLE_EVERY) == 0;

        GpuFrameTimer.onFrameBoundary(sampled);
        if (sampled) sampleAllocation();

        long now = System.nanoTime();
        if (collecting) {
            frameCounter[COUNTER_RENDER_TYPES] = frameRenderTypes.size();
            if (lastFrameEnd != 0L) {
                if (frameCount < CAPACITY) {
                    int index = frameCount++;
                    frameTimes[index] = now - lastFrameEnd;
                    for (int i = 0; i < PROBE_COUNT; i++) {
                        probeSums[i][index] = frameProbe[i];
                        probeHits[i][index] = frameProbeHits[i];
                    }
                    for (int i = 0; i < COUNTER_COUNT; i++) {
                        counterSums[i][index] = frameCounter[i];
                    }
                    gpuNanos[index] = frameGpuNanos;
                    cgcGpuNanos[index] = frameCgcGpuNanos;
                } else {
                    truncated = true;
                }
            }
            lastFrameEnd = now;
            resetFrame();
        }
    }

    private static void sampleAllocation() {
        if (allocationBean == null) return;
        long now = allocationBean.getThreadAllocatedBytes(allocationThreadId);
        if (lastAllocatedBytes >= 0 && now >= lastAllocatedBytes) {
            long delta = now - lastAllocatedBytes;
            frameCounter[COUNTER_ALLOC_BYTES] += (int) Math.min(delta, Integer.MAX_VALUE);
        }
        lastAllocatedBytes = now;
    }

    private static void resetFrame() {
        Arrays.fill(frameProbe, 0L);
        Arrays.fill(frameProbeHits, 0);
        Arrays.fill(frameCounter, 0);
        frameRenderTypes.clear();
        frameGpuNanos = 0L;
        frameCgcGpuNanos = 0L;
    }

    public static void start() {
        ensureBuffers();
        frameCount = 0;
        truncated = false;
        lastFrameEnd = 0L;
        // 新周期换一组标记值，上一周期残留在节点上的帧序号不再匹配
        frameOrdinal = System.nanoTime();
        GpuFrameTimer.reset();
        resetFrame();
        initAllocationSampler();
        collecting = true;
        enabled = true;
//        message("渲染链路采集开始，再次按下调试键停止"); // 显示文本会降一点FPS，而且文本会驻留几秒
    }

    public static void stopAndWrite() {
        enabled = false;
        collecting = false;

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy, HH:mm:ss"));
        if (frameCount == 0) {
            message("渲染链路采集：没有采到完整帧，未写入文件");
            return;
        }

        Path path = Paths.get("./customgun-render-profile.txt");
        List<String> lines = buildReport(timestamp);
        try {
            Files.write(path, lines, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND, StandardOpenOption.WRITE);
            initCount = 0;
            initLastNanos = 0L;
            initMaxNanos = 0L;
            initLastEpochMillis = 0L;
            message(String.format(Locale.ROOT, "渲染链路采集完成：%d 帧，已追加写入 %s", frameCount, path.toAbsolutePath()));
        } catch (IOException e) {
            CustomGun.LOGGER.error("Failed to write render profile", e);
            message("渲染链路采集完成，但写入文件失败：" + e.getMessage());
        }
    }

    private static void ensureBuffers() {
        if (frameTimes != null) return;

        frameTimes = new long[CAPACITY];
        probeSums = new long[PROBE_COUNT][CAPACITY];
        probeHits = new int[PROBE_COUNT][CAPACITY];
        counterSums = new int[COUNTER_COUNT][CAPACITY];
        gpuNanos = new long[CAPACITY];
        cgcGpuNanos = new long[CAPACITY];
    }

    private static void initAllocationSampler() {
        ThreadMXBean bean = ManagementFactory.getThreadMXBean();
        if (bean instanceof com.sun.management.ThreadMXBean sun && sun.isThreadAllocatedMemorySupported()) {
            allocationBean = sun;
            allocationThreadId = Thread.currentThread().getId();
            lastAllocatedBytes = sun.getThreadAllocatedBytes(allocationThreadId);
        } else {
            allocationBean = null;
            allocationThreadId = -1L;
            lastAllocatedBytes = -1L;
        }
    }

    // --------统计与输出--------

    private static List<String> buildReport(String timestamp) {
        long totalNanos = 0L;
        long[] sortedFrames = new long[frameCount];
        for (int i = 0; i < frameCount; i++) {
            totalNanos += frameTimes[i];
            sortedFrames[i] = frameTimes[i];
        }
        Arrays.sort(sortedFrames);
        double totalSeconds = totalNanos / 1_000_000_000.0;

        List<String> lines = new ArrayList<>();
        lines.add(String.format(Locale.ROOT,
                "%s customgun render profile completed, %d frames rendered in %.3f s",
                timestamp, frameCount, totalSeconds));

        double avgFrameMs = totalNanos / 1_000_000.0 / frameCount;
        double minFrameMs = sortedFrames[0] / 1_000_000.0;
        double maxFrameMs = sortedFrames[frameCount - 1] / 1_000_000.0;
        double p99FrameMs = sortedFrames[percentileIndex(frameCount, 0.99)] / 1_000_000.0;

        lines.add(line("Average frame time", String.format(Locale.ROOT, "%8.3f ms (%6.1f FPS)", avgFrameMs, avgFrameMs > 0 ? 1000.0 / avgFrameMs : 0.0)));
        lines.add(line("Minimum frame time", String.format(Locale.ROOT, "%8.3f ms", minFrameMs)));
        lines.add(line("Maximum frame time", String.format(Locale.ROOT, "%8.3f ms", maxFrameMs)));
        lines.add(line("1% high frame time", String.format(Locale.ROOT, "%8.3f ms", p99FrameMs)));

        lines.add(gpuBusyLine("GPU frame busy", gpuNanos, totalNanos));
        lines.add(gpuBusyLine("CGC GPU busy", cgcGpuNanos, totalNanos));

        long cgcTotal = 0L;
        for (int i = 0; i < frameCount; i++) {
            cgcTotal += probeSums[FP_SUBMIT][i] + probeSums[WORLD_SUBMIT][i];
        }
        double cgcAvgMs = cgcTotal / 1_000_000.0 / frameCount;
        double cgcShare = totalNanos > 0 ? (double) cgcTotal / totalNanos * 100.0 : 0.0;
        lines.add(line("CGC render per frame", String.format(Locale.ROOT, "%8.3f ms (%5.1f%% of frame)", cgcAvgMs, cgcShare)));

        for (int i = 0; i < COUNTER_COUNT; i++) {
            lines.add(line(COUNTER_NAMES[i], String.format(Locale.ROOT, "%8.2f / frame", countAverage(i))));
        }

        { // 脏节点 / 脏子树比例：B1-b 的「脏子树重写」能覆盖多少遍历
            double distinct = countAverage(COUNTER_DISTINCT_NODES);
            if (distinct > 0.0) {
                double dirty = countAverage(COUNTER_DIRTY_NODES);
                double dirtySubtree = countAverage(COUNTER_DIRTY_SUBTREE);
                lines.add(counterStatLine("Dirty nodes (per frame)", COUNTER_DIRTY_NODES));
                lines.add(counterStatLine("Dirty subtree nodes (per frame)", COUNTER_DIRTY_SUBTREE));
                lines.add(line("Dirty node share", String.format(Locale.ROOT,
                        "%8.1f %% (%5.1f / %5.1f nodes per frame)", dirty * 100.0 / distinct, dirty, distinct)));
                lines.add(line("Dirty subtree share", String.format(Locale.ROOT,
                        "%8.1f %% (%5.1f / %5.1f nodes per frame)", dirtySubtree * 100.0 / distinct, dirtySubtree, distinct)));
            }
        }

        lines.add("");

        for (int p = 0; p < PROBE_COUNT; p++) {
            long total = 0L;
            long min = Long.MAX_VALUE;
            long max = 0L;
            long[] active = new long[frameCount];
            int activeFrames = 0;
            for (int i = 0; i < frameCount; i++) {
                if (probeHits[p][i] <= 0) continue;
                long value = probeSums[p][i];
                total += value;
                if (value < min) min = value;
                if (value > max) max = value;
                active[activeFrames++] = value;
            }
            if (activeFrames == 0) {
                lines.add(line(PROBE_NAMES[p], "        -"));
                continue;
            }
            long[] sorted = Arrays.copyOf(active, activeFrames);
            Arrays.sort(sorted);
            double avgMs = total / 1_000_000.0 / activeFrames;
            lines.add(line(PROBE_NAMES[p], String.format(Locale.ROOT,
                    "%8.3f ms (min %7.3f / max %7.3f / p99 %7.3f, %5.1f%% of frames)",
                    avgMs, min / 1_000_000.0, max / 1_000_000.0,
                    sorted[percentileIndex(activeFrames, 0.99)] / 1_000_000.0,
                    activeFrames * 100.0 / frameCount)));
        }

        if (truncated) {
            lines.add(line("! truncated", "frame buffer full, later frames dropped"));
        }
        if (initCount > 0) {
            String initAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(initLastEpochMillis), ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            lines.add(line("State machine init (since last report)", String.format(Locale.ROOT,
                    "%8.3f ms (last, %d calls, max %7.3f, at %s)",
                    initLastNanos / 1_000_000.0, initCount, initMaxNanos / 1_000_000.0, initAt)));
        }
        lines.add("");
        return lines;
    }

    private static int percentileIndex(int count, double percentile) {
        int index = (int) Math.ceil(count * percentile) - 1;
        return Math.max(0, Math.min(count - 1, index));
    }

    /** GPU 忙时统计：只统计拿到结果的帧（采样帧中查询就绪的部分） */
    private static String gpuBusyLine(String label, long[] values, long totalNanos) {
        long total = 0L;
        long min = Long.MAX_VALUE;
        long max = 0L;
        long[] active = new long[frameCount];
        int activeFrames = 0;
        for (int i = 0; i < frameCount; i++) {
            long value = values[i];
            if (value <= 0L) continue;
            total += value;
            if (value < min) min = value;
            if (value > max) max = value;
            active[activeFrames++] = value;
        }
        if (activeFrames == 0) {
            return line(label, "        -");
        }
        long[] sorted = Arrays.copyOf(active, activeFrames);
        Arrays.sort(sorted);
        return line(label, String.format(Locale.ROOT,
                "%8.3f ms (min %7.3f / max %7.3f / p99 %7.3f, %5.1f%% of frames, %5.1f%% of frame)",
                total / 1_000_000.0 / activeFrames, min / 1_000_000.0, max / 1_000_000.0,
                sorted[percentileIndex(activeFrames, 0.99)] / 1_000_000.0,
                activeFrames * 100.0 / frameCount,
                totalNanos > 0L ? total * 100.0 / totalNanos : 0.0));
    }

    /** 计数器按帧统计：均值取全部帧，min / max / p99 只取非零帧（与探针口径一致） */
    private static String counterStatLine(String label, int counter) {
        long total = 0L;
        int active = 0;
        long min = Long.MAX_VALUE;
        long max = 0L;
        long[] values = new long[frameCount];
        for (int i = 0; i < frameCount; i++) {
            long value = counterSums[counter][i];
            total += value;
            if (value <= 0L) continue;
            if (value < min) min = value;
            if (value > max) max = value;
            values[active++] = value;
        }
        if (active == 0) return line(label, "        -");
        long[] sorted = Arrays.copyOf(values, active);
        Arrays.sort(sorted);
        return line(label, String.format(Locale.ROOT,
                "%8.2f / frame (min %7.2f / max %7.2f / p99 %7.2f, %5.1f%% of frames)",
                total / (double) frameCount, (double) min, (double) max,
                (double) sorted[percentileIndex(active, 0.99)], active * 100.0 / frameCount));
    }

    private static double countAverage(int counter) {
        long total = 0L;
        for (int i = 0; i < frameCount; i++) {
            total += counterSums[counter][i];
        }
        return (double) total / frameCount;
    }

    private static String line(String label, String value) {
        return String.format(Locale.ROOT, "%38s : %s", label, value);
    }

    private static void message(String text) {
        CustomGun.LOGGER.info("{}", text);
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            mc.player.displayClientMessage(Component.literal("[CGC] " + text), false);
        }
    }
}

package dev.xcolorful.customgun.client.util;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.core.api.event.EventType;
import dev.xcolorful.customgun.core.api.event.IEvent;
import dev.xcolorful.customgun.core.api.event.IEventHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus;

import java.io.IOException;
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
import java.util.List;
import java.util.Locale;

/**
 * 渲染链路埋点（26.2+ 延迟渲染版）。
 * <ul>
 *     <li>按 F12 开始采集，再按一次停止并立即统计、追加写入文件</li>
 *     <li>未采集时热路径成本：每个埋点一次 {@link #enabled} 的 volatile 读 + 一次分支</li>
 *     <li>帧边界取 {@link EventType#RENDER_FRAME_EVENT}（整帧渲染结束）</li>
 *     <li>与 1.20.1 版的差异：26.2+ 把提交与绘制拆成 submit / flush 两段。
 *     {@code *-submit} 类探针只统计 render 线程上的提交开销，真正的顶点生成发生在
 *     {@code Model flush}（{@code submitCustomGeometry} 的回调）里，因此
 *     {@code CGC render per frame} = 提交 + flush 回调</li>
 *     <li>探针之间可以嵌套（提交包含模型提交，模型 flush 包含遍历与 delegate），统计时各算各的</li>
 * </ul>
 * 埋点位置与含义见 ./docs/architecture/client/render/chain/Home.md 的「测量与数据登记」。
 */
@ApiStatus.Internal
public final class ClientRenderProfiler implements IEventHandler {

    // --------探针--------

    /** 第一人称整次提交（状态更新 → 模型提交 → 枪口缓存） */
    public static final int FP_SUBMIT = 0;
    /** 非 GUI 的 renderByItem 整次提交（展示框 / 掉落物 / 第三人称） */
    public static final int WORLD_SUBMIT = 1;
    /** 状态机 update() */
    public static final int STATE_UPDATE = 2;
    /** 每次提交前的附件 / 枪械数据刷新 */
    public static final int DATA_REFRESH = 3;
    /** renderScope（模版窗口准备） */
    public static final int SCOPE = 4;
    /** ModelObject.render 的提交阶段（节点快照采集 + 提交几何） */
    public static final int MODEL_SUBMIT = 5;
    /** ModelObject.render 的节点快照采集与还原（flush 回调内） */
    public static final int SNAPSHOT = 6;
    /** ModelObject.render 的 shouldRender 遍历与顶点生成（flush 回调内） */
    public static final int TRAVERSE = 7;
    /** ModelObject.render 的 flush 回调整体（延迟渲染的实际绘制） */
    public static final int MODEL_FLUSH = 8;
    /** ModelObject.render 的 delegate 执行（flush 回调内） */
    public static final int DELEGATE = 9;
    /** 第一人称枪口位置缓存 */
    public static final int MUZZLE_CACHE = 10;
    /** 瞄具模版缓冲清空（26.2 起为空实现，用于确认该成本已消失） */
    public static final int STENCIL_CLEAR = 11;
    /** 第一人称手部事件入口（含早退判定） */
    public static final int ENTRY = 12;
    /** 第三人称逐帧 visualUpdate */
    public static final int VISUAL_UPDATE = 13;
    /** 状态机初始化（切枪时） */
    public static final int TRY_INIT = 14;
    /** 功能性节点回调登记 */
    public static final int FUNCTIONAL_NODES = 15;
    /** GUI 槽位图标（提交阶段） */
    public static final int SLOT_ICON = 16;
    /** 世界相机角度事件 */
    public static final int CAMERA_LEVEL = 17;
    /** 手部相机动画事件 */
    public static final int CAMERA_HAND = 18;
    /** FOV 事件 */
    public static final int FOV = 19;

    public static final int PROBE_COUNT = 20;
    private static final String[] PROBE_NAMES = {
            "First-person submit", "World-item submit", "State update", "Data refresh", "Scope render",
            "Model submit", "Node snapshot", "Geometry traverse", "Model flush",
            "Delegate render", "Muzzle cache", "Stencil clear",
            "Hand event entry", "Third-person visual update", "State machine init",
            "Functional nodes", "GUI slot icon", "Camera angles", "Hand camera", "FOV",
    };

    // --------计数--------

    private static final int COUNTER_FP_SUBMIT = 0;
    private static final int COUNTER_WORLD_SUBMIT = 1;
    private static final int COUNTER_SUBMITS = 2;
    private static final int COUNTER_CUBES = 3;
    private static final int COUNTER_NODES = 4;
    private static final int COUNTER_FLUSH_CALLBACKS = 5;
    private static final int COUNTER_STENCIL_WINDOWS = 6;
    private static final int COUNTER_LOD_HITS = 7;
    private static final int COUNTER_COUNT = 8;
    private static final String[] COUNTER_NAMES = {
            "First-person submissions", "World-item submissions", "Geometry submits", "Cubes submitted",
            "Nodes captured", "Flush callbacks", "Stencil windows", "LOD hits",
    };

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

    private static boolean collecting = false;
    private static boolean truncated = false;
    private static int frameCount = 0;
    private static long lastFrameEnd = 0L;

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

    public static void countBy(int counter, int amount) {
        if (!collecting) return;
        frameCounter[counter] += amount;
    }

    public static void countSubmission(boolean firstPerson) {
        count(firstPerson ? COUNTER_FP_SUBMIT : COUNTER_WORLD_SUBMIT);
    }

    public static void countCube() {
        count(COUNTER_CUBES);
    }

    public static void countNodes(int amount) {
        countBy(COUNTER_NODES, amount);
    }

    /** 提交阶段调用：每调用一次 {@code submitCustomGeometry} 记一次 */
    public static void countSubmit() {
        count(COUNTER_SUBMITS);
    }

    /** flush 阶段调用：回调真正执行时记一次 */
    public static void countFlushCallback() {
        count(COUNTER_FLUSH_CALLBACKS);
    }

    public static void countStencilWindow() {
        count(COUNTER_STENCIL_WINDOWS);
    }

    public static void countLod() {
        count(COUNTER_LOD_HITS);
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

        long now = System.nanoTime();
        if (collecting) {
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
                } else {
                    truncated = true;
                }
            }
            lastFrameEnd = now;
            resetFrame();
        }
    }

    private static void resetFrame() {
        Arrays.fill(frameProbe, 0L);
        Arrays.fill(frameProbeHits, 0);
        Arrays.fill(frameCounter, 0);
    }

    public static void start() {
        ensureBuffers();
        frameCount = 0;
        truncated = false;
        lastFrameEnd = 0L;
        resetFrame();
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

        // 26.2+：提交与绘制分离，CGC 的总开销 = 提交阶段 + flush 回调
        long cgcTotal = 0L;
        for (int i = 0; i < frameCount; i++) {
            cgcTotal += probeSums[FP_SUBMIT][i] + probeSums[WORLD_SUBMIT][i] + probeSums[MODEL_FLUSH][i];
        }
        double cgcAvgMs = cgcTotal / 1_000_000.0 / frameCount;
        double cgcShare = totalNanos > 0 ? (double) cgcTotal / totalNanos * 100.0 : 0.0;
        lines.add(line("CGC render per frame", String.format(Locale.ROOT, "%8.3f ms (%5.1f%% of frame)", cgcAvgMs, cgcShare)));

        for (int i = 0; i < COUNTER_COUNT; i++) {
            lines.add(line(COUNTER_NAMES[i], String.format(Locale.ROOT, "%8.2f / frame", countAverage(i))));
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
            mc.player.sendSystemMessage(Component.literal("[CGC] " + text)); // [26.3, ) displayClientMessage 已移除
        }
    }
}

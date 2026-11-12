package dev.xcolorful.customgun.client.util;

import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.ApiStatus;
import org.lwjgl.opengl.ARBTimerQuery;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL32C;

/**
 * 帧级 GPU 忙时采样（在 F12 采集窗口内生效）
 * <ul>
 *     <li>整帧与 CGC 提交共用一组 {@code GL_TIME_ELAPSED} 查询：两者不能嵌套，因此按「段」切分——交开始时结束整帧段并开启 CGC 段，提交结束时反过来</li>
 *     <li>查询对象来自固定池，避免每帧 {@code glGenQueries} / {@code glDeleteQueries} 成为探针自身的开销</li>
 *     <li>只在每 {@link ClientRenderProfiler#SAMPLE_EVERY} 帧采样一次；未采样的帧不开启任何查询</li>
 *     <li>查询结果延迟读取，未就绪的查询留到下一帧继续轮询，不阻塞渲染线程</li>
 *     <li>原版在 F3 打开或性能录制时会自己使用 {@code GL_TIME_ELAPSED}，此时本类跳过采集</li>
 * </ul>
 * 口径：覆盖两次帧事件之间的 GL 命令，不含 {@code updateDisplay} 的 present 等待
 * CGC 段只覆盖提交窗口内发出的命令
 */
@ApiStatus.Internal
public final class GpuFrameTimer {

    private static final int TIME_ELAPSED = 35007;
    private static final int QUERY_RESULT = 34918;
    private static final int QUERY_RESULT_AVAILABLE = 34919;

    private static final int TAG_FRAME = 0;
    private static final int TAG_CGC = 1;

    /**
     * 池大小需覆盖「结果延迟帧数 × 每帧最多段数（CGC 每枪两次切换）」
     */
    private static final int POOL_SIZE = 256;

    private static final int[] all = new int[POOL_SIZE];
    private static int allCount;
    private static final int[] free = new int[POOL_SIZE];
    private static int freeCount;
    private static final int[] pendingQuery = new int[POOL_SIZE];
    private static final byte[] pendingTag = new byte[POOL_SIZE];
    private static int pendingCount;

    private static int activeQuery;
    private static int activeTag;
    private static boolean initialized;
    private static boolean supported = true;

    private GpuFrameTimer() {}

    /**
     * 帧边界：结算上一帧，再决定本帧是否开启新查询
     * @param sampled 本帧是否属于采样帧；非采样帧不开启查询，CGC 段也随之停用
     */
    public static void onFrameBoundary(boolean sampled) {
        if (!supported) return;

        if (!initialized) {
            initialized = true;
            supported = GL.getCapabilities().GL_ARB_timer_query;
            if (!supported) return;
        }

        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc != null && mc.options.renderDebug) {
                endActive(true);
                drain(true);
                return;
            }

            endActive(false); // 上一帧最后一个整帧段
            drain(false);
            if (sampled) begin(TAG_FRAME);
        } catch (Throwable t) {
            supported = false;
            activeQuery = 0;
            pendingCount = 0;
        }
    }

    /** CGC 提交开始：结束整帧段，开启 CGC 段；没有整帧段（未采样 / F3 / 不支持）时不做任何事 */
    public static void beginCgc() {
        if (!supported || activeQuery == 0 || activeTag != TAG_FRAME) return;
        endActive(false);
        begin(TAG_CGC);
    }

    /** CGC 提交结束：结束 CGC 段，恢复整帧段 */
    public static void endCgc() {
        if (!supported || activeQuery == 0 || activeTag != TAG_CGC) return;
        endActive(false);
        begin(TAG_FRAME);
    }

    private static void begin(int tag) {
        int query = acquire();
        if (query == 0) return;
        GL32C.glBeginQuery(TIME_ELAPSED, query);
        activeQuery = query;
        activeTag = tag;
    }

    private static void endActive(boolean discard) {
        if (activeQuery == 0) return;
        GL32C.glEndQuery(TIME_ELAPSED);
        if (discard) {
            release(activeQuery);
        } else if (pendingCount < pendingQuery.length) {
            pendingQuery[pendingCount] = activeQuery;
            pendingTag[pendingCount] = (byte) activeTag;
            pendingCount++;
        } else {
            release(activeQuery);
        }
        activeQuery = 0;
    }

    private static void drain(boolean discard) {
        int keep = 0;
        for (int i = 0; i < pendingCount; i++) {
            int query = pendingQuery[i];
            if (discard) {
                release(query);
                continue;
            }
            boolean ready = GL32C.glGetQueryObjecti(query, QUERY_RESULT_AVAILABLE) == 1;
            if (!ready) {
                pendingQuery[keep] = query;
                pendingTag[keep] = pendingTag[i];
                keep++;
                continue;
            }
            long nanos = ARBTimerQuery.glGetQueryObjecti64(query, QUERY_RESULT);
            if (pendingTag[i] == TAG_CGC) {
                ClientRenderProfiler.recordGpuCgc(nanos);
            }
            // 整帧忙时仍是「整帧口径」：包含 CGC 段
            ClientRenderProfiler.recordGpuFrame(nanos);
            release(query);
        }
        pendingCount = keep;
    }

    private static int acquire() {
        if (freeCount > 0) return free[--freeCount];
        if (allCount >= POOL_SIZE) return 0;
        int query = GL32C.glGenQueries();
        all[allCount++] = query;
        return query;
    }

    private static void release(int query) {
        if (freeCount < POOL_SIZE) free[freeCount++] = query;
    }

    public static void reset() {
        if (!supported) return;
        try {
            endActive(true);
            drain(true);
            for (int i = 0; i < allCount; i++) GL32C.glDeleteQueries(all[i]);
            allCount = 0;
            freeCount = 0;
        } catch (Throwable t) {
            supported = false;
            activeQuery = 0;
            pendingCount = 0;
            allCount = 0;
            freeCount = 0;
        }
    }
}

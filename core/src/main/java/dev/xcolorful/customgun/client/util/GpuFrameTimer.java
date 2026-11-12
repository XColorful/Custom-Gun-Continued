package dev.xcolorful.customgun.client.util;

import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.ApiStatus;
import org.lwjgl.opengl.ARBTimerQuery;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL32C;

/**
 * 帧级 GPU 忙时采样（在 F12 采集窗口内生效）
 * <ul>
 *     <li>在每帧结束时结束上一帧的 {@code GL_TIME_ELAPSED} 查询并开启下一帧的查询</li>
 *     <li>查询结果延迟读取，未就绪的查询留到下一帧继续轮询，不阻塞渲染线程</li>
 *     <li>原版在 F3 打开或性能录制时会自己使用 {@code GL_TIME_ELAPSED}，同一时刻不能嵌套，此时本类跳过采集</li>
 * </ul>
 * 口径：覆盖两次帧事件之间的 GL 命令，不含 {@code updateDisplay} 的 present 等待
 */
@ApiStatus.Internal
public final class GpuFrameTimer {

    private static final int TIME_ELAPSED = 35007;
    private static final int QUERY_RESULT = 34918;
    private static final int QUERY_RESULT_AVAILABLE = 34919;

    private static final int[] pending = new int[4];
    private static int pendingCount;
    private static int activeQuery;
    private static boolean initialized;
    private static boolean supported = true;

    private GpuFrameTimer() {}

    public static void onFrameBoundary() {
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

            endActive(false);
            drain(false);
            activeQuery = GL32C.glGenQueries();
            GL32C.glBeginQuery(TIME_ELAPSED, activeQuery);
        } catch (Throwable t) {
            supported = false;
            activeQuery = 0;
            pendingCount = 0;
        }
    }

    private static void endActive(boolean discard) {
        if (activeQuery == 0) return;
        GL32C.glEndQuery(TIME_ELAPSED);
        if (discard) {
            GL32C.glDeleteQueries(activeQuery);
        } else if (pendingCount < pending.length) {
            pending[pendingCount++] = activeQuery;
        } else {
            GL32C.glDeleteQueries(activeQuery);
        }
        activeQuery = 0;
    }

    private static void drain(boolean discard) {
        int keep = 0;
        for (int i = 0; i < pendingCount; i++) {
            int query = pending[i];
            boolean ready = GL32C.glGetQueryObjecti(query, QUERY_RESULT_AVAILABLE) == 1;
            if (ready && !discard) {
                ClientRenderProfiler.recordGpuFrame(ARBTimerQuery.glGetQueryObjecti64(query, QUERY_RESULT));
                GL32C.glDeleteQueries(query);
            } else if (discard) {
                GL32C.glDeleteQueries(query);
            } else {
                pending[keep++] = query;
            }
        }
        pendingCount = keep;
    }

    public static void reset() {
        if (!supported) return;
        try {
            endActive(true);
            drain(true);
        } catch (Throwable t) {
            supported = false;
            activeQuery = 0;
            pendingCount = 0;
        }
    }
}

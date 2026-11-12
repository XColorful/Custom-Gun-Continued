package dev.xcolorful.customgun.client.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.commands.CommandEncoder;
import com.mojang.renderpearl.api.commands.GpuQueryPool;
import org.jetbrains.annotations.ApiStatus;

import java.util.OptionalLong;

/**
 * 帧级 GPU 忙时采样（26.2+ 延迟渲染版本）
 * <ul>
 *     <li>用 {@code GpuQueryPool} 写两个时间戳，覆盖两次帧事件之间提交的 GPU 命令</li>
 *     <li>结果延迟读取，未就绪的槽位留到后续帧继续轮询，不阻塞渲染线程</li>
 *     <li>与原版 F3 的 {@code TimerQuery} 使用各自独立的查询池，互不干扰</li>
 * </ul>
 */
@ApiStatus.Internal
public final class GpuFrameTimer {

    private static final int ROTATIONS = 3;

    private static GpuQueryPool pool;
    private static CommandEncoder encoder;
    private static final boolean[] awaiting = new boolean[ROTATIONS];
    private static int index;
    private static boolean started;
    private static boolean supported = true;

    private GpuFrameTimer() {}

    public static void onFrameBoundary() {
        if (!supported) return;
        try {
            if (pool == null) {
                pool = RenderSystem.getDevice().createTimestampQueryPool(ROTATIONS * 2);
            }
            if (started) {
                encoder.writeTimestamp(pool, index * 2 + 1);
                awaiting[index] = true;
                encoder = null;
                started = false;
            }
            drain();
            index = (index + 1) % ROTATIONS;
            if (awaiting[index]) return;
            encoder = RenderSystem.getDevice().createCommandEncoder();
            encoder.writeTimestamp(pool, index * 2);
            started = true;
        } catch (Throwable t) {
            supported = false;
            encoder = null;
            started = false;
        }
    }

    private static void drain() {
        double period = RenderSystem.getDevice().getDeviceInfo().timestampPeriod();
        for (int i = 0; i < ROTATIONS; i++) {
            if (!awaiting[i]) continue;
            OptionalLong[] values = pool.getValues(i * 2, 2);
            if (values.length == 2 && values[0].isPresent() && values[1].isPresent()) {
                long delta = values[1].getAsLong() - values[0].getAsLong();
                ClientRenderProfiler.recordGpuFrame((long) (delta * period));
                awaiting[i] = false;
            }
        }
    }

    public static void reset() {
        if (started && encoder != null) {
            try {
                encoder.writeTimestamp(pool, index * 2 + 1);
                awaiting[index] = true;
            } catch (Throwable ignored) {
            }
        }
        encoder = null;
        started = false;
    }
}

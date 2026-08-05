package dev.xcolorful.customgun.core.api.event;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.core.event.EventDispatcher;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.BooleanSupplier;

/**
 * 针对多线程注册、单线程调度的场景优化
 * <br>
 * 区别于{@link EventDispatcher}针对读多写少，避免任务注册时频繁上锁
 */
public final class CycledEvent {

    /**
     * 反复执行的task，返回false则退出循环
     */
    private final BooleanSupplier task; // 不用Supplier<Boolean>可以省一点装箱开销
    private final long delayMs;
    private final long intervalMs;
    private final int maxCycles;
    private final long createTimeMs;
    private boolean started = false;
    private long lastExecuteTimeMs;
    private int executedCycles = 0;

    private CycledEvent(BooleanSupplier task, long delayMs, long intervalMs, int maxCycles) {
        this.task = task;
        this.delayMs = delayMs;
        this.intervalMs = intervalMs;
        this.maxCycles = maxCycles;
        this.createTimeMs = System.currentTimeMillis();
        this.lastExecuteTimeMs = this.createTimeMs - intervalMs;
    }
    /**
     * 挂载到{@link IServerTickEvent}执行
     * <br>
     * 当 delayMs 大于 0 的时候，调用该方法是线程安全的
     * @param delayMs 执行延迟，小于等于0则立即执行一次，但注意对原版的操作不是线程安全的
     */
    public static void create(BooleanSupplier task, long delayMs, long intervalMs, int maxCycles) {
        if (delayMs <= 0) { // 立即执行
            if (!task.getAsBoolean() // 执行了一次，返回false
                    || maxCycles <= 1) { // 达到最大次数
                return;
            } else {
                CycledEvent cycledEvent = new CycledEvent(task, delayMs, intervalMs, maxCycles);
                cycledEvent.executedCycles = 1;
                Handler.INSTANCE.register(cycledEvent);
                return;
            }
        } else {
            Handler.INSTANCE.register(new CycledEvent(task, delayMs, intervalMs, maxCycles));
            return;
        }
    }

    // --------调度--------

    @ApiStatus.Internal
    public static final class Handler implements IEventHandler {
        public static final Handler INSTANCE = new Handler();
        private Handler() {}

        static { // 首次调用时自动监听
            CustomGun.getEventRegister().register(INSTANCE, EventType.SERVER_TICK_EVENT, EventPriority.NORMAL, true);
        }

        @Override public String getEventHandlerName() {
            return "CycledEvent.Handler";
        }
        @Override
        public void handleEvent(EventType eventType, IEvent event) {
            if (eventType == EventType.SERVER_TICK_EVENT) {
                onServerTick();
            } else {
                onReceiveWrongEvent(eventType);
            }
        }

        private final ConcurrentLinkedQueue<CycledEvent> pendingRegistration = new ConcurrentLinkedQueue<>();
        private ArrayList<CycledEvent> cycledEvents = new ArrayList<>();
        private ArrayList<CycledEvent> nextCycledEvents = new ArrayList<>();

        private void register(CycledEvent cycledEvent) {
            pendingRegistration.add(cycledEvent);
        }

        /**
         * 实现方式:
         * <ul>
         *     <li>双列表交换: 避免链式结构节点开销和列表删除时的数据移动</li>
         *     <li>使用两个ArrayList轮换保存当前任务和下一tick任务，避免遍历时修改列表</li>
         * </ul>
         * 适用于短生命周期、高频注册的循环任务，例如实体更新、弹道检测等场景:
         * <ul>
         *     <li>(主要用途) 极端情况在50个玩家600RPM的情况下，设一个子弹在空中飞8ticks，则同时存在约4000个循环任务</li>
         *     <li>其余使用场景不会像子弹这样高频</li>
         * </ul>
         */
        private void onServerTick() {
            CycledEvent pending;
            while ((pending = pendingRegistration.poll()) != null) {
                cycledEvents.add(pending);
            }

            final long currentTimeMillis = System.currentTimeMillis();
            nextCycledEvents.clear();
            int size = cycledEvents.size();
            for (int i = 0; i < size; i++) {
                CycledEvent cycledEvent = cycledEvents.get(i);

                // 初始延迟
                if (!cycledEvent.started) {
                    if (currentTimeMillis - cycledEvent.createTimeMs < cycledEvent.delayMs) {
                        nextCycledEvents.add(cycledEvent);
                        continue;
                    }
                    cycledEvent.started = true;
                }

                // 满足条件就持续执行，防止因卡顿而堆积的task被推迟
                boolean keep = true;
                while (currentTimeMillis - cycledEvent.lastExecuteTimeMs >= cycledEvent.intervalMs) {
                    boolean shouldContinue = cycledEvent.task.getAsBoolean();
                    if (!shouldContinue || ++cycledEvent.executedCycles >= cycledEvent.maxCycles) { // (返回值)task自己结束 || 超过最大限制
                        keep = false;
                        break;
                    }
                    cycledEvent.lastExecuteTimeMs += cycledEvent.intervalMs;
                }
                if (keep) {
                    nextCycledEvents.add(cycledEvent);
                }
            }

            ArrayList<CycledEvent> temp = cycledEvents;
            cycledEvents = nextCycledEvents;
            nextCycledEvents = temp;
        }
    }
}

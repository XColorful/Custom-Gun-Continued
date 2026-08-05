package dev.xcolorful.customgun.core.api.common;

import java.io.Serializable;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

/**
 * 等价于 net.minecraftforge.fml.DistExecutor
 */
public interface ISideExecutor {

    void executeOn(McSide dist, Supplier<Runnable> toRun);

    /**
     * 调用方需手动类隔离，在开发环境下会触发 Forge 的类安全校验
     */
    void executeOnIsolated(McSide dist, Supplier<SideRunnable> toRun);

    @FunctionalInterface
    interface SideRunnable extends Runnable, Serializable {}

    McLogicalSide getLogicalSide();

    /**
     * @deprecated [1.20.1-1.21.4]可用，1.21.6没找到
     */
    @Deprecated(since = "1.21.6")
    Executor getMainThreadExecutor(McLogicalSide side);
}

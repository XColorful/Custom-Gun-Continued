package xiao.customgun.core.api.common;

import java.io.Serializable;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

/**
 * 等价于 net.minecraftforge.fml.DistExecutor
 */
public interface ISideExecutor {

    void unsafeRunWhenOn(McSide dist, Supplier<Runnable> toRun);

    void safeRunWhenOn(McSide dist, Supplier<SideRunnable> toRun);

    /**
     * 对标 Forge 的 SafeRunnable
     */
    @FunctionalInterface
    interface SideRunnable extends Runnable, Serializable {}

    McLogicalSide getLogicalSide();

    /**
     * @deprecated [1.20.1-1.21.4]可用，1.21.6没找到
     */
    @Deprecated(since = "1.21.6")
    Executor getMainThreadExecutor(McLogicalSide side);
}

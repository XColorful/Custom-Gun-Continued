package xiao.customgun.forge.common;

import net.minecraftforge.common.util.LogicalSidedProvider;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.util.thread.EffectiveSide;
import xiao.customgun.core.api.common.ISideExecutor;
import xiao.customgun.core.api.common.McLogicalSide;
import xiao.customgun.core.api.common.McSide;

import java.util.concurrent.Executor;
import java.util.function.Supplier;

public class ForgeSideExecutor implements ISideExecutor {

    @Override
    public void unsafeRunWhenOn(McSide dist, Supplier<Runnable> toRun) {
        DistExecutor.unsafeRunWhenOn(McSideHelper.convert(dist), toRun);
    }

    @Override
    public void safeRunWhenOn(McSide dist, Supplier<SideRunnable> toRun) {
        DistExecutor.safeRunWhenOn(McSideHelper.convert(dist), () -> {
            SideRunnable coreRunnable = toRun.get();
            return (DistExecutor.SafeRunnable) coreRunnable::run;
        });
    }

    @Override
    public McLogicalSide getLogicalSide() {
        return McSideHelper.convert(EffectiveSide.get());
    }

    @Override
    public Executor getMainThreadExecutor(McLogicalSide side) {
        return LogicalSidedProvider.WORKQUEUE.get(McSideHelper.convert(side));
    }
}

package dev.xcolorful.customgun.forge.common;

import dev.xcolorful.customgun.core.api.common.ISideExecutor;
import dev.xcolorful.customgun.core.api.common.McLogicalSide;
import dev.xcolorful.customgun.core.api.common.McSide;
import net.minecraftforge.common.util.LogicalSidedProvider;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.util.thread.EffectiveSide;

import java.util.concurrent.Executor;
import java.util.function.Supplier;

public class ForgeSideExecutor implements ISideExecutor {

    @Override
    public void executeOn(McSide dist, Supplier<Runnable> toRun) {
        DistExecutor.unsafeRunWhenOn(McSideHelper.convert(dist), toRun);
    }

    @Override
    public void executeOnIsolated(McSide dist, Supplier<SideRunnable> toRun) {
        DistExecutor.safeRunWhenOn(McSideHelper.convert(dist), () -> (DistExecutor.SafeRunnable) () -> toRun.get().run());
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

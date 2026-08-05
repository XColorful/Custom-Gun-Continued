package dev.xcolorful.customgun.neoforge.common;

import dev.xcolorful.customgun.core.api.common.ISideExecutor;
import dev.xcolorful.customgun.core.api.common.McLogicalSide;
import dev.xcolorful.customgun.core.api.common.McSide;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.util.thread.EffectiveSide;
import net.neoforged.neoforge.common.util.LogicalSidedProvider;

import java.util.concurrent.Executor;
import java.util.function.Supplier;

public class NeoSideExecutor implements ISideExecutor {

    @Override
    public void executeOn(McSide dist, Supplier<Runnable> toRun) {
        if (FMLEnvironment.dist == McSideHelper.convert(dist)) {
            toRun.get().run();
        }
    }

    @Override
    public void executeOnIsolated(McSide dist, Supplier<ISideExecutor.SideRunnable> toRun) {
        if (FMLEnvironment.dist == McSideHelper.convert(dist)) {
            toRun.get().run();
        }
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

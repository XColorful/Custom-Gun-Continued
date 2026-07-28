/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.projectile.impact;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.api.event.*;
import xiao.customgun.core.api.event.projectile.ProjectileHitBlockEvent;
import xiao.customgun.core.config.AmmoConfig;

public class _WorldImpactHandler implements ICustomEventHandler {
    private static class _WorldImpactHandlerHolder {
        private static final _WorldImpactHandler INSTANCE = new _WorldImpactHandler();
    }
    public static _WorldImpactHandler get() {
        return _WorldImpactHandlerHolder.INSTANCE;
    }
    protected _WorldImpactHandler() {}
    @Override public String getEventHandlerName() {
        return this.getClass().getName();
    }
    @Override
    public void handleEvent(CustomEventType eventType, ICustomEvent event) {
        if (eventType == CustomEventType.PROJECTILE_HIT_BLOCK_EVENT) {
            this.onHitBlock((ProjectileHitBlockEvent) event);
        } else {
            onReceiveWrongEvent(eventType);
        }
    }

    private void onHitBlock(ProjectileHitBlockEvent event) {
        @Nullable Level level = event.getLevel();
        if (level == null) return;

        Block victimBlock = event.getBulletVictimBlock();
        if (victimBlock instanceof BellBlock bellBlock) {
            // 敲钟
            this.onHitBell(level, bellBlock, event.getBlockHitResult());
        } else if (AmmoConfig.DESTROY_GLASS.get()) {
            // 破坏玻璃
            BlockPos blockPos = event.getBlockHitResult().getBlockPos();
            if (this.isGlass(level, blockPos, victimBlock)) {
                this.onHitGlass(level, event.getBlockHitResult().getBlockPos());
            }
        }
    }
    private void onHitBell(Level level, BellBlock bellBlock, BlockHitResult hitResult) {
        bellBlock.attemptToRing(level, hitResult.getBlockPos(), hitResult.getDirection());
    }
    /**
     * 玻璃板是铁栏杆{@link IronBarsBlock}
     * @see net.minecraft.world.level.block.Blocks#GLASS_PANE
     */
    private boolean isGlass(Level level, BlockPos blockPos, Block block) {
        return block instanceof AbstractGlassBlock
                || (block instanceof IronBarsBlock && level.getBlockState(blockPos).instrument() == NoteBlockInstrument.HAT);
    }
    private void onHitGlass(Level level, BlockPos blockPos) {
        level.destroyBlock(blockPos, false);
    }
}

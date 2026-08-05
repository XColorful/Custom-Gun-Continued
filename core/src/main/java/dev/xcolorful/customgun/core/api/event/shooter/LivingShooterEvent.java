package dev.xcolorful.customgun.core.api.event.shooter;

import dev.xcolorful.customgun.core.api.common.ILogicalSideOnly;
import dev.xcolorful.customgun.core.api.common.McLogicalSide;
import dev.xcolorful.customgun.core.api.entity.ILivingShooter;
import dev.xcolorful.customgun.core.api.event.CustomEvent;
import dev.xcolorful.customgun.core.api.minecraft.CommandLevel;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

/**
 * 射手生物{@link ILivingShooter} 事件
 */
public abstract class LivingShooterEvent extends CustomEvent implements ILogicalSideOnly, ILivingShooterEvent {

    protected final McLogicalSide logicalSide;

    protected @Nullable ILivingShooter iLivingShooter;
    protected @Nullable LivingEntity livingShooter;

    public LivingShooterEvent(McLogicalSide logicalSide,
                              @Nullable ILivingShooter iLivingShooter, @Nullable LivingEntity livingShooter) {
        this.logicalSide = logicalSide;
        this.iLivingShooter = iLivingShooter;
        this.livingShooter = livingShooter;
    }

    @Override
    public McLogicalSide getLogicalSide() {
        return this.logicalSide;
    }

    public @Nullable ILivingShooter getILivingShooter() {
        return this.iLivingShooter;
    }
    public @Nullable LivingEntity getLivingShooter() {
        return this.livingShooter;
    }

    @Override
    public CommandSourceStack createCommandSourceStack(@Nullable CommandSource source) {
        if (this.logicalSide.isClient()) return null;
        if (!(this.livingShooter.level() instanceof ServerLevel serverLevel)) return null;

        return new CommandSourceStack(
                source != null ? source : CommandSource.NULL,
                this.livingShooter.position(),
                this.livingShooter.getRotationVector(),
                serverLevel,
                CommandLevel.permission(4),
                this.getTextName(),
                this.getDisplayName(),
                serverLevel.getServer(),
                this.livingShooter
        );
    }
}

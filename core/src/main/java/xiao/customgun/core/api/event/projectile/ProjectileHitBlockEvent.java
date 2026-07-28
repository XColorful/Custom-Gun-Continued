package xiao.customgun.core.api.event.projectile;

import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.CustomGun;
import xiao.customgun.core.api.block.IBulletVictimBlock;
import xiao.customgun.core.api.entity.IGunProjectile;
import xiao.customgun.core.api.event.CustomEventType;
import xiao.customgun.core.api.event.ICustomEvent;
import xiao.customgun.core.api.event.ICustomEventHandler;
import xiao.customgun.core.api.minecraft.CommandLevel;
import xiao.customgun.core.event.EventDispatcher;
import xiao.customgun.core.util.Vec3Utils;

/**
 * 仅服务端触发
 * <br>
 * 枪射物{@link IGunProjectile} 命中 方块{@link Block} 事件
 */
public final class ProjectileHitBlockEvent extends GunProjectileEvent implements IBulletVictimBlockEvent {

    private final @NotNull BlockHitResult blockHitResult;
    private final @Nullable IBulletVictimBlock ibulletVictimBlock;
    private final Block bulletVictimBlock;

    public ProjectileHitBlockEvent(@NotNull IGunProjectile iGunProjectile, @NotNull Entity gunProjectile, // 纯服务端事件，默认不为null
                                   @NotNull BlockHitResult blockHitResult,
                                   @Nullable IBulletVictimBlock ibulletVictimBlock, Block bulletVictimBlock) {
        super(iGunProjectile, gunProjectile);
        this.blockHitResult = blockHitResult;
        this.ibulletVictimBlock = ibulletVictimBlock;
        this.bulletVictimBlock = bulletVictimBlock;
    }
    @Override public CustomEventType getEventType() {
        return CustomEventType.PROJECTILE_HIT_BLOCK_EVENT;
    }

    public @NotNull BlockHitResult getBlockHitResult() {
        return this.blockHitResult;
    }
    public @Nullable IBulletVictimBlock getIBulletVictimBlock() {
        return this.ibulletVictimBlock;
    }
    public Block getBulletVictimBlock() {
        return this.bulletVictimBlock;
    }

    @Override
    public @Nullable CommandSourceStack createCommandSourceStack(@Nullable CommandSource source) {
        if (!(this.gunProjectile.level() instanceof ServerLevel serverLevel)) return null;
        return new CommandSourceStack(
                source != null ? source : CommandSource.NULL,
                Vec3Utils.getCenter(blockHitResult.getBlockPos()),
                Vec2.ZERO,
                serverLevel,
                CommandLevel.permission(4),
                this.getTextName(),
                this.getDisplayName(),
                serverLevel.getServer(),
                this.gunProjectile
        );
    }

    @Override public String getTextName() {
        return this.bulletVictimBlock.getName().getString();
    }
    @Override public Component getDisplayName() {
        return this.bulletVictimBlock.getName();
    }

    private static final EventDispatcher<ICustomEventHandler, ICustomEvent, CustomEventType> _EVENT_DISPATCHER = CustomGun.getEventPoster().getEventDispatcher(ProjectileHitBlockEvent.class);
    @Override public @NotNull EventDispatcher<ICustomEventHandler, ICustomEvent, CustomEventType> getEventDispatcher() {
        return _EVENT_DISPATCHER;
    }
}

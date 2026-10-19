package dev.xcolorful.customgun.core.network.message.shooter;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.core.api.entity.shooter.ILivingShooterGetter;
import dev.xcolorful.customgun.core.api.network.message.IMessage;
import dev.xcolorful.customgun.core.developer.PlannedRefactor;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Consumer;

/**
 * 客户端本地不能连续切换，必须先等服务端收到信息（会有延迟）；
 * 会影响丝滑度，但未来需要加开镜散步判定 // TODO
 * @param fromScopeViewIndex 客户端本地数据，可能是垃圾信息或非法数据
 * @param toScopeViewIndex 客户端本地期望的合法数据，目前服务端暂无校验方式 ({@link PlannedRefactor#MOVE_SCOPE_VIEW_INDEX_TO_CORE})
 */
public record C2SMessageShooterZoom(int fromScopeViewIndex, int toScopeViewIndex)
        implements IMessage<C2SMessageShooterZoom> {

    @Override
    public void encode(FriendlyByteBuf buffer) {
        var message = this;
        buffer.writeInt(message.fromScopeViewIndex);
        buffer.writeInt(message.toScopeViewIndex);
    }

    public static C2SMessageShooterZoom decode(FriendlyByteBuf buffer) {
        return new C2SMessageShooterZoom(buffer.readInt(), buffer.readInt());
    }

    @Override
    public void handle(Consumer<Runnable> handler, NetworkContext context) {
        if (CustomGun.getSideExecutor().getLogicalSide().isServer()) {
            handler.accept(() -> {
                if (!(context.sender() instanceof ServerPlayer player)) {
                    return;
                }

                ILivingShooterGetter.cgc$fromLivingEntity(player).cgc$zoom(fromScopeViewIndex, toScopeViewIndex);
            });
        }
    }
}
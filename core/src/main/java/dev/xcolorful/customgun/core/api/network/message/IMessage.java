package dev.xcolorful.customgun.core.api.network.message;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.core.api.network.MessageDirection;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * T 必须保持无界：改成 {@code T extends IMessage<T>} 会把方法擦除签名从 Object 改为 IMessage，
 * 使旧扩展 mod 的桥方法不再覆盖本接口，导致消息收发被静默跳过
 */
public interface IMessage<T> {

    /**
     * 过渡期旧签名，供已发布的扩展 mod 继续实现；CGC 本体只实现无参重载
     */
    @Deprecated(forRemoval = true)
    default void encode(T message, FriendlyByteBuf buffer) {
        throw new UnsupportedOperationException(
                getClass().getName() + " does not implement encode(FriendlyByteBuf)");
    }
    @SuppressWarnings("unchecked")
    default void encode(FriendlyByteBuf buffer) {
        encode((T) this, buffer);
    }

    /**
     * 过渡期旧签名，供已发布的扩展 mod 继续实现；CGC 本体只实现无参重载
     */
    @Deprecated(forRemoval = true)
    default void handle(T message, Consumer<Runnable> handler, NetworkContext context) {
        throw new UnsupportedOperationException(
                getClass().getName() + " does not implement handle(Consumer, NetworkContext)");
    }
    @SuppressWarnings("unchecked")
    default void handle(Consumer<Runnable> handler, NetworkContext context) {
        handle((T) this, handler, context);
    }

    /**
     * @param sender 高版本可以获取 LocalPlayer
     * 但为了兼容性(取交集)，客户端统一从 Minecraft 获取 LocalPlayer
     * @param setHandledFunc 握手包专用，非握手包为 null
     */
    record NetworkContext(
            Connection connection,
            MessageDirection direction,
            @Nullable Player sender,
            Consumer<IMessage<?>> replyConsumer,
            @Nullable Runnable setHandledFunc
    ) {
        public void reply(IMessage<?> replyMsg) {
            replyConsumer.accept(replyMsg);
        }
        public void setHandled() {
            if (setHandledFunc != null) {
                setHandledFunc.run();
            } else {
                CustomGun.LOGGER.warn("Non-handshake message should not set handled manually");
            }
        }
    }
}

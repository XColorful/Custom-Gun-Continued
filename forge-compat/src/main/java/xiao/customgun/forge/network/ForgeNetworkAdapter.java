/*
 * 改成跟 BattleRoyale 同构的写法
 */

package xiao.customgun.forge.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.*;
import net.minecraftforge.network.simple.SimpleChannel;
import xiao.customgun.CustomGun;
import xiao.customgun.core.api.network.INetworkAdapter;
import xiao.customgun.core.api.network.MessageDirection;
import xiao.customgun.core.api.network.message.IMessage;
import xiao.customgun.core.network.LoginIndexHolder;
import xiao.customgun.core.network.NetworkHandler;
import xiao.customgun.forge.CustomGunForge;

import java.util.Optional;
import java.util.function.Function;

public class ForgeNetworkAdapter implements INetworkAdapter {

    private final SimpleChannel HANDSHAKE_CHANNEL;
    private final SimpleChannel CHANNEL;

    public ForgeNetworkAdapter() {
        this.HANDSHAKE_CHANNEL = NetworkRegistry.newSimpleChannel(
                CustomGunForge.mcRegistry.createResourceLocation(String.format("%s:handshake", CustomGun.MOD_ID)),
                () -> NetworkHandler.PROTOCOL_VERSION,
                NetworkHandler.getProtocolAcceptancePredicate(), // 服务端 -> 客户端
                NetworkHandler.getProtocolAcceptancePredicate() // 客户端 -> 服务端
        );
        this.CHANNEL = NetworkRegistry.newSimpleChannel(
                CustomGunForge.mcRegistry.createResourceLocation(String.format("%s:network", CustomGun.MOD_ID)),
                () -> NetworkHandler.PROTOCOL_VERSION,
                NetworkHandler.getProtocolAcceptancePredicate(), // 服务端 -> 客户端
                NetworkHandler.getProtocolAcceptancePredicate() // 客户端 -> 服务端
        );
    }

    @Override
    public <T extends IMessage<T>> void registerMessage(int id, Class<T> clazz, Function<FriendlyByteBuf, T> decoder, MessageDirection direction) {
        NetworkDirection forgeDirection = MessageDirectionHelper.convert(direction);

        this.CHANNEL.registerMessage(
                id,
                clazz,
                (messageInstance, buffer) -> messageInstance.encode(messageInstance, buffer),
                decoder,
                (message, contextSupplier) -> {
                    NetworkEvent.Context context = contextSupplier.get();
                    IMessage.NetworkContext netContext = new IMessage.NetworkContext(
                            context.getNetworkManager(),
                            MessageDirectionHelper.convert(context.getDirection()),
                            context.getSender(),
                            (replyMsg) -> CHANNEL.reply(replyMsg, context),
                            null
                    );
                    message.handle(message, context::enqueueWork, netContext);
                    context.setPacketHandled(true);
                },
                Optional.of(forgeDirection)
        );
    }

    @Override
    public <T extends LoginIndexHolder & IMessage<T>> void registerHandshakeAcknowledge(int id, Class<T> clazz, Function<FriendlyByteBuf, T> decoder) {
        this.HANDSHAKE_CHANNEL.messageBuilder(clazz, id)
                .loginIndex(LoginIndexHolder::getLoginIndex, LoginIndexHolder::setLoginIndex)
                .encoder((messageInstance, buffer) -> messageInstance.encode(messageInstance, buffer))
                .decoder(decoder)
                .consumerNetworkThread(HandshakeHandler.indexFirst((handler, messageInstance, contextSupplier) -> {
                    NetworkEvent.Context context = contextSupplier.get();
                    IMessage.NetworkContext netContext = new IMessage.NetworkContext(
                            context.getNetworkManager(),
                            MessageDirectionHelper.convert(context.getDirection()),
                            context.getSender(),
                            (replyMsg) -> HANDSHAKE_CHANNEL.reply(replyMsg, context),
                            () -> context.setPacketHandled(true)
                    );
                    messageInstance.handle(messageInstance, context::enqueueWork, netContext);
                }))
                .add();
    }
    @Override
    public <T extends LoginIndexHolder & IMessage<T>> void registerHandshakeMessage(int id, Class<T> clazz, Function<FriendlyByteBuf, T> decoder) {
        this.HANDSHAKE_CHANNEL.messageBuilder(clazz, id)
                .loginIndex(LoginIndexHolder::getLoginIndex, LoginIndexHolder::setLoginIndex)
                .encoder((messageInstance, buffer) -> messageInstance.encode(messageInstance, buffer))
                .decoder(decoder)
                .consumerNetworkThread((message, contextSupplier) -> {
                    NetworkEvent.Context context = contextSupplier.get();
                    IMessage.NetworkContext netContext = new IMessage.NetworkContext(
                            context.getNetworkManager(),
                            MessageDirectionHelper.convert(context.getDirection()),
                            context.getSender(),
                            (replyMsg) -> HANDSHAKE_CHANNEL.reply(replyMsg, context),
                            () -> context.setPacketHandled(true)
                    );
                    message.handle(message, context::enqueueWork, netContext);
                })
                .markAsLoginPacket()
                .add();
    }

    @Override
    public void sendToAll(IMessage<?> message) {
        this.CHANNEL.send(PacketDistributor.ALL.noArg(), message);
    }

    @Override
    public void sendToPlayer(ServerPlayer player, IMessage<?> message) {
        this.CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player),
                message
        );
    }

    @Override
    public void sendToTrackingEntityAndSelf(Entity centerEntity, IMessage<?> message) {
        this.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> centerEntity), message);
    }

    @Override
    public void sendToTrackingEntity(Entity centerEntity, IMessage<?> message) {
        this.CHANNEL.send(PacketDistributor.TRACKING_ENTITY.with(() -> centerEntity), message);
    }

    @Override
    public void sendToServer(IMessage<?> message) {
        this.CHANNEL.sendToServer(message);
    }
}

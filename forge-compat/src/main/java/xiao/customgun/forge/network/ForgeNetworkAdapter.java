/*
 * 改成跟 BattleRoyale 同构的写法
 */

package xiao.customgun.forge.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.*;
import xiao.customgun.CustomGun;
import xiao.customgun.core.api.network.INetworkAdapter;
import xiao.customgun.core.api.network.MessageDirection;
import xiao.customgun.core.api.network.message.IMessage;
import xiao.customgun.core.network.LoginIndexHolder;
import xiao.customgun.core.network.NetworkHandler;
import xiao.customgun.forge.CustomGunForge;

import java.util.function.Function;

public class ForgeNetworkAdapter implements INetworkAdapter {

    private final SimpleChannel HANDSHAKE_CHANNEL;
    private final SimpleChannel CHANNEL;

    public ForgeNetworkAdapter() {
        int protocolVersion = NetworkHandler.PROTOCOL_VERSION;
        Channel.VersionTest acceptedVersions = Channel.VersionTest.exact(protocolVersion);
        this.HANDSHAKE_CHANNEL = ChannelBuilder
                .named(CustomGunForge.mcRegistry.createResourceLocation(String.format("%s:handshake", CustomGun.MOD_ID)))
                .networkProtocolVersion(protocolVersion) // 协议版本必须是 int
                .clientAcceptedVersions(acceptedVersions) // 客户端接受版本
                .serverAcceptedVersions(acceptedVersions) // 服务端接受版本
                .simpleChannel(); // 创建 SimpleChannel 实例
        this.CHANNEL = ChannelBuilder
                .named(CustomGunForge.mcRegistry.createResourceLocation(String.format("%s:network", CustomGun.MOD_ID)))
                .networkProtocolVersion(protocolVersion)
                .clientAcceptedVersions(acceptedVersions)
                .serverAcceptedVersions(acceptedVersions)
                .simpleChannel();
    }

    @Override
    public <T extends IMessage<T>> void registerMessage(int id, Class<T> clazz, Function<FriendlyByteBuf, T> decoder, MessageDirection direction) {
        NetworkDirection forgeDirection = MessageDirectionHelper.convert(direction);

        this.CHANNEL.<T>messageBuilder(clazz, id, forgeDirection)
                .encoder((messageInstance, buffer) -> messageInstance.encode(messageInstance, buffer))
                .decoder(decoder)
                .consumerMainThread((messageInstance, context) -> {
                    IMessage.NetworkContext netContext = new IMessage.NetworkContext(
                            context.getConnection(),
                            MessageDirectionHelper.convert(context.getDirection()),
                            context.getSender(),
                            (replyMsg) -> CHANNEL.reply(replyMsg, context),
                            null
                    );
                    messageInstance.handle(messageInstance, context::enqueueWork, netContext);
                })
                .add();
    }

    @Override
    public <T extends LoginIndexHolder & IMessage<T>> void registerHandshakeAcknowledge(int id, Class<T> clazz, Function<FriendlyByteBuf, T> decoder) {
        this.HANDSHAKE_CHANNEL.messageBuilder(clazz, id, NetworkDirection.LOGIN_TO_SERVER)
//                .loginIndex(LoginIndexHolder::getLoginIndex, LoginIndexHolder::setLoginIndex) // 去掉后也能进单人游戏/多人游戏
                .encoder((messageInstance, buffer) -> messageInstance.encode(messageInstance, buffer))
                .decoder(decoder)
                .consumerNetworkThread((messageInstance, context) -> { // 去掉了HandshakeHandler.indexFirst，Context不再Supplier
                    IMessage.NetworkContext netContext = new IMessage.NetworkContext(
                            context.getConnection(),
                            MessageDirectionHelper.convert(context.getDirection()),
                            context.getSender(),
                            (replyMsg) -> HANDSHAKE_CHANNEL.reply(replyMsg, context),
                            () -> context.setPacketHandled(true)
                    );
                    messageInstance.handle(messageInstance, context::enqueueWork, netContext);
                })
                .add();
    }
    @Override
    public <T extends LoginIndexHolder & IMessage<T>> void registerHandshakeMessage(int id, Class<T> clazz, Function<FriendlyByteBuf, T> decoder) {
        this.HANDSHAKE_CHANNEL.messageBuilder(clazz, id, NetworkDirection.LOGIN_TO_CLIENT)
//                .loginIndex(LoginIndexHolder::getLoginIndex, LoginIndexHolder::setLoginIndex)
                .encoder((messageInstance, buffer) -> messageInstance.encode(messageInstance, buffer))
                .decoder(decoder)
                .consumerNetworkThread((messageInstance, context) -> {
                    IMessage.NetworkContext netContext = new IMessage.NetworkContext(
                            context.getConnection(),
                            MessageDirectionHelper.convert(context.getDirection()),
                            context.getSender(),
                            (replyMsg) -> HANDSHAKE_CHANNEL.send(replyMsg, context.getConnection()),
                            () -> context.setPacketHandled(true)
                    );
                    messageInstance.handle(messageInstance, context::enqueueWork, netContext);
                })
                .add();
    }

    @Override
    public void sendToAll(IMessage<?> message) {
        this.CHANNEL.send(message, PacketDistributor.ALL.noArg());
    }

    @Override
    public void sendToPlayer(ServerPlayer player, IMessage<?> message) {
        this.CHANNEL.send(message, PacketDistributor.PLAYER.with(player));
    }

    @Override
    public void sendToTrackingEntityAndSelf(Entity centerEntity, IMessage<?> message) {
        this.CHANNEL.send(message, PacketDistributor.TRACKING_ENTITY_AND_SELF.with(centerEntity));
    }

    @Override
    public void sendToTrackingEntity(Entity centerEntity, IMessage<?> message) {
        this.CHANNEL.send(message, PacketDistributor.TRACKING_ENTITY.with(centerEntity));
    }

    @Override
    public void sendToServer(IMessage<?> message) {
        this.CHANNEL.send(message, PacketDistributor.SERVER.noArg());
    }
}

package dev.xcolorful.customgun.forge.network;

import dev.xcolorful.customgun.core.api.minecraft.IMcRegistry;
import dev.xcolorful.customgun.core.api.network.INetworkAdapter;
import dev.xcolorful.customgun.core.api.network.MessageDirection;
import dev.xcolorful.customgun.core.api.network.message.IMessage;
import dev.xcolorful.customgun.core.network.LoginIndexHolder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.*;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;
import java.util.function.Predicate;

public class ForgeNetworkAdapter implements INetworkAdapter {

    private final int protocolVersion;
    private final String protocolVersionString;
    private boolean isProtocolAccepted(String removeVersion) {
        return removeVersion.equals(protocolVersionString);
    }
    private Predicate<String> getProtocolAcceptancePredicate() {
        return this::isProtocolAccepted;
    }

    private final SimpleChannel HANDSHAKE_CHANNEL;
    private final SimpleChannel CHANNEL;

    public ForgeNetworkAdapter(@NotNull IMcRegistry mcRegistry,
                               String modId, int protocolVersion) {
        this.protocolVersion = protocolVersion;
        this.protocolVersionString = String.valueOf(protocolVersion);

        Channel.VersionTest acceptedVersions = Channel.VersionTest.exact(protocolVersion);
        this.HANDSHAKE_CHANNEL = ChannelBuilder
                .named(mcRegistry.createResourceLocation(String.format("%s:handshake", modId)))
                .networkProtocolVersion(protocolVersion) // 协议版本必须是 int
                .clientAcceptedVersions(acceptedVersions) // 客户端接受版本
                .serverAcceptedVersions(acceptedVersions) // 服务端接受版本
                .simpleChannel(); // 创建 SimpleChannel 实例
        this.CHANNEL = ChannelBuilder
                .named(mcRegistry.createResourceLocation(String.format("%s:network", modId)))
                .networkProtocolVersion(protocolVersion)
                .clientAcceptedVersions(acceptedVersions)
                .serverAcceptedVersions(acceptedVersions)
                .simpleChannel();
    }

    @Override
    public <T extends IMessage<T>> void registerMessage(int id, Class<T> clazz, Function<FriendlyByteBuf, T> decoder, MessageDirection direction) {
        NetworkDirection<?> forgeDirection = MessageDirectionHelper.convert(direction);

        this.CHANNEL.messageBuilder(clazz, id, forgeDirection)
                .encoder((message, buffer) -> message.encode(message, buffer))
                .decoder(decoder::apply) // 为啥这里要 apply 才过编译? 抽风了?
                .consumerMainThread((messageInstance, context) -> {
                    IMessage.NetworkContext netContext = new IMessage.NetworkContext(
                            context.getConnection(),
                            context.isClientSide() ? MessageDirection.SERVER_TO_CLIENT : MessageDirection.CLIENT_TO_SERVER,
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
                .encoder((message, buffer) -> message.encode(message, buffer))
                .decoder(decoder)
                .consumerNetworkThread((message, context) -> {
                    IMessage.NetworkContext netContext = new IMessage.NetworkContext(
                            context.getConnection(),
                            context.isClientSide() ? MessageDirection.SERVER_TO_CLIENT : MessageDirection.CLIENT_TO_SERVER,
                            context.getSender(),
                            (replyMsg) -> HANDSHAKE_CHANNEL.reply(replyMsg, context),
                            () -> context.setPacketHandled(true)
                    );
                    message.handle(message, context::enqueueWork, netContext);
                })
                .add();
    }

    @Override
    public <T extends LoginIndexHolder & IMessage<T>> void registerHandshakeMessage(int id, Class<T> clazz, Function<FriendlyByteBuf, T> decoder) {
        this.HANDSHAKE_CHANNEL.messageBuilder(clazz, id, NetworkDirection.LOGIN_TO_CLIENT)
                .encoder((message, buffer) -> message.encode(message, buffer))
                .decoder(decoder)
                .consumerNetworkThread((message, context) -> {
                    IMessage.NetworkContext netContext = new IMessage.NetworkContext(
                            context.getConnection(),
                            context.isClientSide() ? MessageDirection.SERVER_TO_CLIENT : MessageDirection.CLIENT_TO_SERVER,
                            context.getSender(),
                            (replyMsg) -> HANDSHAKE_CHANNEL.send(replyMsg, context.getConnection()),
                            () -> context.setPacketHandled(true)
                    );
                    message.handle(message, context::enqueueWork, netContext);
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

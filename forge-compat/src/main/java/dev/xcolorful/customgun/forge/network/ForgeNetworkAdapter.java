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
import net.minecraftforge.network.simple.SimpleChannel;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
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

        this.HANDSHAKE_CHANNEL = NetworkRegistry.newSimpleChannel(
                mcRegistry.createResourceLocation(String.format("%s:handshake", modId)),
                () -> this.protocolVersionString,
                this.getProtocolAcceptancePredicate(), // 服务端 -> 客户端
                this.getProtocolAcceptancePredicate() // 客户端 -> 服务端
        );
        this.CHANNEL = NetworkRegistry.newSimpleChannel(
                mcRegistry.createResourceLocation(String.format("%s:network", modId)),
                () -> this.protocolVersionString,
                this.getProtocolAcceptancePredicate(), // 服务端 -> 客户端
                this.getProtocolAcceptancePredicate() // 客户端 -> 服务端
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

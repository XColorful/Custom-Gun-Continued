package dev.xcolorful.customgun.neoforge.network;

import dev.xcolorful.customgun.core.api.minecraft.IMcRegistry;
import dev.xcolorful.customgun.core.api.network.INetworkAdapter;
import dev.xcolorful.customgun.core.api.network.MessageDirection;
import dev.xcolorful.customgun.core.api.network.message.IMessage;
import dev.xcolorful.customgun.core.network.LoginIndexHolder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.NetworkRegistry;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.PlayNetworkDirection;
import net.neoforged.neoforge.network.simple.SimpleChannel;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;
import java.util.function.Predicate;

public class NeoNetworkAdapter implements INetworkAdapter {

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

    public NeoNetworkAdapter(IEventBus modEventBus, @NotNull IMcRegistry mcRegistry,
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

        PlayNetworkDirection neoDirection = MessageDirectionHelper.convert(direction);

        this.CHANNEL.<T>messageBuilder(clazz, id, neoDirection)
                .encoder((messageInstance, buffer) -> messageInstance.encode(messageInstance, buffer))
                .decoder(decoder::apply)
                .consumerMainThread((messageInstance, context) -> {
                    IMessage.NetworkContext netContext = new IMessage.NetworkContext(
                            context.getNetworkManager(),
                            MessageDirectionHelper.convertDynamic(context.getDirection()),
                            context.getSender(),
                            (replyMsg) -> this.CHANNEL.reply(replyMsg, context),
                            null
                    );
                    messageInstance.handle(messageInstance, context::enqueueWork, netContext);
                })
                .add();
    }

    @Override
    public <T extends LoginIndexHolder & IMessage<T>> void registerHandshakeAcknowledge(int id, Class<T> clazz, Function<FriendlyByteBuf, T> decoder) {
        this.HANDSHAKE_CHANNEL.messageBuilder(clazz, id)
                .loginIndex(LoginIndexHolder::getLoginIndex, LoginIndexHolder::setLoginIndex)
                .encoder((messageInstance, buffer) -> messageInstance.encode(messageInstance, buffer))
                .decoder(decoder::apply)
                .consumerNetworkThread((messageInstance, context) -> {
                    IMessage.NetworkContext netContext = new IMessage.NetworkContext(
                            context.getNetworkManager(),
                            MessageDirectionHelper.convertDynamic(context.getDirection()),
                            context.getSender(),
                            (replyMsg) -> this.HANDSHAKE_CHANNEL.reply(replyMsg, context),
                            () -> context.setPacketHandled(true)
                    );
                    messageInstance.handle(messageInstance, context::enqueueWork, netContext);
                })
                .add();
    }

    @Override
    public <T extends LoginIndexHolder & IMessage<T>> void registerHandshakeMessage(int id, Class<T> clazz, Function<FriendlyByteBuf, T> decoder) {
        this.HANDSHAKE_CHANNEL.messageBuilder(clazz, id)
                .loginIndex(LoginIndexHolder::getLoginIndex, LoginIndexHolder::setLoginIndex)
                .encoder((messageInstance, buffer) -> messageInstance.encode(messageInstance, buffer))
                .decoder(decoder::apply)
                .consumerNetworkThread((messageInstance, context) -> {
                    IMessage.NetworkContext netContext = new IMessage.NetworkContext(
                            context.getNetworkManager(),
                            MessageDirectionHelper.convertDynamic(context.getDirection()),
                            context.getSender(),
                            (replyMsg) -> this.HANDSHAKE_CHANNEL.reply(replyMsg, context),
                            () -> context.setPacketHandled(true)
                    );
                    messageInstance.handle(messageInstance, context::enqueueWork, netContext);
                })
                .add();
    }

    @Override
    public void sendToAll(IMessage<?> message) {
        this.CHANNEL.send(PacketDistributor.ALL.noArg(), message);
    }

    @Override
    public void sendToPlayer(ServerPlayer player, IMessage<?> message) {
        this.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), message);
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
        this.CHANNEL.send(PacketDistributor.SERVER.noArg(), message);
    }
}
/*
 * 改成跟 BattleRoyale 同构的写法
 */

package dev.xcolorful.customgun.neoforge.network;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.core.api.network.INetworkAdapter;
import dev.xcolorful.customgun.core.api.network.MessageDirection;
import dev.xcolorful.customgun.core.api.network.message.IMessage;
import dev.xcolorful.customgun.core.network.LoginIndexHolder;
import dev.xcolorful.customgun.core.network.NetworkHandler;
import dev.xcolorful.customgun.neoforge.CustomGunNeoforge;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.NetworkRegistry;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.PlayNetworkDirection;
import net.neoforged.neoforge.network.simple.SimpleChannel;

import java.util.function.Function;

public class NeoNetworkAdapter implements INetworkAdapter {

    private final SimpleChannel HANDSHAKE_CHANNEL;
    private final SimpleChannel CHANNEL;
    private final String protocolVersionString = String.valueOf(NetworkHandler.PROTOCOL_VERSION);

    public NeoNetworkAdapter() {
        ResourceLocation handshakeName = CustomGunNeoforge.mcRegistry.createResourceLocation(String.format("%s:handshake", CustomGun.MOD_ID));
        ResourceLocation networkName = CustomGunNeoforge.mcRegistry.createResourceLocation(String.format("%s:network", CustomGun.MOD_ID));

        this.HANDSHAKE_CHANNEL = NetworkRegistry.newSimpleChannel(
                handshakeName,
                () -> this.protocolVersionString,
                this.protocolVersionString::equals,
                this.protocolVersionString::equals
        );
        this.CHANNEL = NetworkRegistry.newSimpleChannel(
                networkName,
                () -> this.protocolVersionString,
                this.protocolVersionString::equals,
                this.protocolVersionString::equals
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
/*
 * 改成跟 BattleRoyale 同构的写法
 */

package dev.xcolorful.customgun.neoforge.network;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.core.api.network.INetworkAdapter;
import dev.xcolorful.customgun.core.api.network.MessageDirection;
import dev.xcolorful.customgun.core.api.network.message.IMessage;
import dev.xcolorful.customgun.core.network.LoginIndexHolder;
import dev.xcolorful.customgun.neoforge.CustomGunNeoforge;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@EventBusSubscriber(modid = CustomGun.MOD_ID)
public class NeoNetworkAdapter implements INetworkAdapter {
    public static NeoNetworkAdapter INSTANCE = new NeoNetworkAdapter();

    private record RegisteredPacket<T extends IMessage<T>>(
            Class<T> messageType,
            ResourceLocation id,
            Function<FriendlyByteBuf, T> decoder,
            MessageDirection direction,
            boolean isHandshake
    ) {}

    private record NeoPayload<T extends IMessage<T>>(ResourceLocation id, T message) implements CustomPacketPayload {

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return new Type<>(id);
        }

        public void write(FriendlyByteBuf buffer) {
            this.message.encode(this.message, buffer);
        }
    }

    private final List<RegisteredPacket<?>> registeredPackets = new ArrayList<>();
    private final String modId = CustomGun.MOD_ID;

    public NeoNetworkAdapter() {
    }

    @Override
    public <T extends IMessage<T>> void registerMessage(int id, Class<T> clazz, Function<FriendlyByteBuf, T> decoder, MessageDirection direction) {
        String path = clazz.getSimpleName().toLowerCase();
        ResourceLocation packetId = CustomGunNeoforge.mcRegistry.createResourceLocation(String.format("%s:%s", modId, path));
        registeredPackets.add(new RegisteredPacket<>(clazz, packetId, decoder, direction, false));
    }

    @Override
    public <T extends LoginIndexHolder & IMessage<T>> void registerHandshakeAcknowledge(int id, Class<T> clazz, Function<FriendlyByteBuf, T> decoder) {
        String path = clazz.getSimpleName().toLowerCase();
        ResourceLocation packetId = CustomGunNeoforge.mcRegistry.createResourceLocation(String.format("%s:%s", modId, path));
        registeredPackets.add(new RegisteredPacket<>(clazz, packetId, decoder, MessageDirection.CLIENT_TO_SERVER, true));
    }

    @Override
    public <T extends LoginIndexHolder & IMessage<T>> void registerHandshakeMessage(int id, Class<T> clazz, Function<FriendlyByteBuf, T> decoder) {
        String path = clazz.getSimpleName().toLowerCase();
        ResourceLocation packetId = CustomGunNeoforge.mcRegistry.createResourceLocation(String.format("%s:%s", modId, path));
        registeredPackets.add(new RegisteredPacket<>(clazz, packetId, decoder, MessageDirection.SERVER_TO_CLIENT, true));
    }

    @Override
    public void sendToAll(IMessage<?> message) {
        this.registeredPackets.stream()
                .filter(rp -> rp.messageType().isInstance(message))
                .findFirst()
                .ifPresent(packetInfo -> {
                    this.sendInternal((RegisteredPacket) packetInfo, message);
                });
    }
    private <T extends IMessage<T>> void sendInternal(RegisteredPacket<T> packetInfo, IMessage<?> message) {
        @SuppressWarnings("unchecked")
        T castedMessage = (T) message;

        CustomPacketPayload payload = new NeoPayload<>(packetInfo.id(), castedMessage);
        PacketDistributor.sendToAllPlayers(payload);
    }

    @Override
    public void sendToPlayer(ServerPlayer player, IMessage<?> message) {
        this.registeredPackets.stream()
                .filter(rp -> rp.messageType().isInstance(message))
                .findFirst()
                .ifPresent(packetInfo -> this.sendToPlayerInternal(player, (RegisteredPacket) packetInfo, message));
    }
    private <T extends IMessage<T>> void sendToPlayerInternal(ServerPlayer player, RegisteredPacket<T> packetInfo, IMessage<?> message) {
        @SuppressWarnings("unchecked")
        T castedMessage = (T) message;
        CustomPacketPayload payload = new NeoPayload<>(packetInfo.id(), castedMessage);
        PacketDistributor.sendToPlayer(player, payload);
    }

    @Override
    public void sendToTrackingEntityAndSelf(Entity centerEntity, IMessage<?> message) {
        this.registeredPackets.stream()
                .filter(rp -> rp.messageType().isInstance(message))
                .findFirst()
                .ifPresent(packetInfo -> this.sendToTrackingInternal(centerEntity, (RegisteredPacket) packetInfo, message, true));
    }

    @Override
    public void sendToTrackingEntity(Entity centerEntity, IMessage<?> message) {
        this.registeredPackets.stream()
                .filter(rp -> rp.messageType().isInstance(message))
                .findFirst()
                .ifPresent(packetInfo -> this.sendToTrackingInternal(centerEntity, (RegisteredPacket) packetInfo, message, false));
    }

    private <T extends IMessage<T>> void sendToTrackingInternal(Entity entity, RegisteredPacket<T> packetInfo, IMessage<?> message, boolean andSelf) {
        @SuppressWarnings("unchecked")
        T castedMessage = (T) message;
        CustomPacketPayload payload = new NeoPayload<>(packetInfo.id(), castedMessage);
        if (andSelf) PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, payload);
        else PacketDistributor.sendToPlayersTrackingEntity(entity, payload);
    }

    @Override
    public void sendToServer(IMessage<?> message) {
        this.registeredPackets.stream()
                .filter(rp -> rp.messageType().isInstance(message))
                .findFirst()
                .ifPresent(packetInfo -> this.sendToServerInternal((RegisteredPacket) packetInfo, message));
    }

    private <T extends IMessage<T>> void sendToServerInternal(RegisteredPacket<T> packetInfo, IMessage<?> message) {
        @SuppressWarnings("unchecked")
        T castedMessage = (T) message;
        CustomPacketPayload payload = new NeoPayload<>(packetInfo.id(), castedMessage);
        PacketDistributor.sendToServer(payload);
    }

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(INSTANCE.modId);

        for (RegisteredPacket<?> rp : INSTANCE.registeredPackets) {
            INSTANCE.registerPacketInternal(registrar, rp);
        }
    }

    private <T extends IMessage<T>> void registerPacketInternal(PayloadRegistrar registrar, RegisteredPacket<T> rp) {
        ResourceLocation id = rp.id;

        StreamCodec<FriendlyByteBuf, NeoPayload<T>> codec = StreamCodec.of(
                (buf, payload) -> payload.write(buf),
                (buf) -> new NeoPayload<>(id, rp.decoder.apply(buf))
        );

        IPayloadHandler<NeoPayload<T>> handler = (payload, context) -> {
            final T message = payload.message();

            Connection connection = null;
            {
                Player player = context.player();
                if (player instanceof ServerPlayer sp) {
                    connection = sp.connection.getConnection();
                } else if (FMLLoader.getDist().isClient()) {
                    connection = _LocalPlayer.getConnection(player);
                }
            }

            IMessage.NetworkContext netContext = new IMessage.NetworkContext(
                    connection,
                    rp.direction,
                    context.player(),
                    (replyMsg) -> {
                        this.registeredPackets.stream()
                                .filter(r -> r.messageType().isInstance(replyMsg))
                                .findFirst()
                                .ifPresent(info -> {
                                    @SuppressWarnings("unchecked")
                                    NeoPayload<T> replyPayload = new NeoPayload<>(info.id(), (T) replyMsg);
                                    context.reply(replyPayload);
                                });
                    },
                    rp.isHandshake ? () -> {} : null
            );

            context.enqueueWork(() -> {
                message.handle(message, Runnable::run, netContext);
            });
        };

        CustomPacketPayload.Type<NeoPayload<T>> payloadType = new CustomPacketPayload.Type<>(id);
        if (rp.isHandshake) {
            if (rp.direction == MessageDirection.SERVER_TO_CLIENT) {
                registrar.configurationToClient(payloadType, codec, handler);
            } else {
                registrar.configurationToServer(payloadType, codec, handler);
            }
        } else {
            if (rp.direction == MessageDirection.SERVER_TO_CLIENT) {
                registrar.playToClient(payloadType, codec, handler);
            } else {
                registrar.playToServer(payloadType, codec, handler);
            }
        }
    }

    private static class _LocalPlayer {
        private static Connection getConnection(Player player) {
            if (player instanceof net.minecraft.client.player.LocalPlayer lp) {
                return lp.connection.getConnection();
            }
            return null;
        };
    }
}
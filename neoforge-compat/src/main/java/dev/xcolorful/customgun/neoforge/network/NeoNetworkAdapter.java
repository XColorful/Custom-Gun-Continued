package dev.xcolorful.customgun.neoforge.network;

import dev.xcolorful.customgun.core.api.minecraft.IMcRegistry;
import dev.xcolorful.customgun.core.api.network.INetworkAdapter;
import dev.xcolorful.customgun.core.api.network.MessageDirection;
import dev.xcolorful.customgun.core.api.network.message.IMessage;
import dev.xcolorful.customgun.core.network.LoginIndexHolder;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ConfigurationTask;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.configuration.ICustomConfigurationTask;
import net.neoforged.neoforge.network.event.RegisterConfigurationTasksEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.registration.HandlerThread;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class NeoNetworkAdapter implements INetworkAdapter {
    private final int protocolVersion;
    private final String protocolVersionString;
    private boolean isProtocolAccepted(String removeVersion) {
        return removeVersion.equals(protocolVersionString);
    }
    private Predicate<String> getProtocolAcceptancePredicate() {
        return this::isProtocolAccepted;
    }

    private record RegisteredPacket<T extends IMessage<T>>(
            Class<T> messageType,
            ResourceLocation id,
            Function<FriendlyByteBuf, T> decoder,
            MessageDirection direction,
            boolean isHandshake
            , Supplier<CustomPacketPayload> handshakePayloadFactory
    ) {
        public RegisteredPacket(Class<T> messageType,
                                ResourceLocation id,
                                Function<FriendlyByteBuf, T> decoder,
                                MessageDirection direction,
                                boolean isHandshake) {
            this(messageType, id, decoder, direction, isHandshake, null);
        }
    }

    private record NeoPayload<T extends IMessage<T>>(ResourceLocation id, T message) implements CustomPacketPayload {

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return new Type<>(id);
        }

        public void write(FriendlyByteBuf buffer) {
            this.message.encode(buffer);
        }
    }

    private final List<RegisteredPacket<?>> registeredPackets = new ArrayList<>();
    private final String modId;
    private final IMcRegistry mcRegistry;
    private ConfigurationTask.Type handshakeTaskType;

    public NeoNetworkAdapter(IEventBus modEventBus, @NotNull IMcRegistry mcRegistry,
                             String modId, int protocolVersion) {
        this.protocolVersion = protocolVersion;
        this.protocolVersionString = String.valueOf(protocolVersion);

        this.modId = modId;
        this.mcRegistry = mcRegistry;
        modEventBus.register(this);
    }

    @Override
    public <T extends IMessage<T>> void registerMessage(int id, Class<T> clazz, Function<FriendlyByteBuf, T> decoder, MessageDirection direction) {
        String path = clazz.getSimpleName().toLowerCase();
        var packetId = this.mcRegistry.createResourceLocation(String.format("%s:%s", modId, path));
        registeredPackets.add(new RegisteredPacket<>(clazz, packetId, decoder, direction, false));
    }

    @Override
    public <T extends LoginIndexHolder & IMessage<T>> void registerHandshakeAcknowledge(int id, Class<T> clazz, Function<FriendlyByteBuf, T> decoder) {
        this.registeredPackets.stream()
                .filter(rp -> rp.isHandshake() && rp.direction() == MessageDirection.CLIENT_TO_SERVER)
                .findFirst()
                .ifPresent(existing -> {
                    throw new IllegalStateException(String.format(
                            "Multiple handshake acknowledge messages registered: %s and %s; only one is supported",
                            existing.messageType().getName(), clazz.getName()));
                });
        String path = clazz.getSimpleName().toLowerCase();
        var packetId = this.mcRegistry.createResourceLocation(String.format("%s:%s", modId, path));
        registeredPackets.add(new RegisteredPacket<>(clazz, packetId, decoder, MessageDirection.CLIENT_TO_SERVER, true));
    }

    @Override
    public <T extends LoginIndexHolder & IMessage<T>> void registerHandshakeMessage(int id, Class<T> clazz, Function<FriendlyByteBuf, T> decoder) {
        this.registeredPackets.stream()
                .filter(rp -> rp.isHandshake() && rp.direction() == MessageDirection.SERVER_TO_CLIENT)
                .findFirst()
                .ifPresent(existing -> {
                    throw new IllegalStateException(String.format(
                            "Multiple handshake messages registered: %s and %s; only one is supported",
                            existing.messageType().getName(), clazz.getName()));
                });
        String path = clazz.getSimpleName().toLowerCase();
        var packetId = this.mcRegistry.createResourceLocation(String.format("%s:%s", modId, path));

        Supplier<CustomPacketPayload> payloadFactory = null;
        try {
            clazz.getDeclaredConstructor();
            payloadFactory = () -> {
                try {
                    return new NeoPayload<>(packetId, clazz.getDeclaredConstructor().newInstance());
                } catch (ReflectiveOperationException e) {
                    throw new IllegalStateException("Failed to instantiate handshake message " + clazz.getName(), e);
                }
            };
            // 任务标识取第一个可发送的握手消息，仍然只由 modid + 注册类名组成
            if (this.handshakeTaskType == null) {
                this.handshakeTaskType = new ConfigurationTask.Type(packetId);
            }
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Handshake message " + clazz.getName() + " has no no-arg constructor", e);
        }

        registeredPackets.add(new RegisteredPacket<>(clazz, packetId, decoder, MessageDirection.SERVER_TO_CLIENT, true, payloadFactory));
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
        ClientPacketDistributor.sendToServer(payload);
    }

    @SubscribeEvent
    public void register(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(this.modId).executesOn(HandlerThread.NETWORK);

        for (RegisteredPacket<?> rp : this.registeredPackets) {
            this.registerPacketInternal(registrar, rp);
        }
    }

    @ApiStatus.AvailableSince("1.21.1")
    @SubscribeEvent
    public void registerConfigurationTasks(RegisterConfigurationTasksEvent event) {
        boolean hasAck = this.registeredPackets.stream()
                .anyMatch(rp -> rp.isHandshake() && rp.direction() == MessageDirection.CLIENT_TO_SERVER);
        List<Supplier<CustomPacketPayload>> payloadFactories = this.registeredPackets.stream()
                .filter(rp -> rp.isHandshake() && rp.direction() == MessageDirection.SERVER_TO_CLIENT)
                .map(RegisteredPacket::handshakePayloadFactory)
                .filter(Objects::nonNull)
                .toList();
        if (!hasAck || this.handshakeTaskType == null || payloadFactories.isEmpty()) {
            return;
        }
        final ConfigurationTask.Type taskType = this.handshakeTaskType;
        event.register(new ICustomConfigurationTask() {
            @Override
            public ConfigurationTask.Type type() {
                return taskType;
            }

            @Override
            public void run(Consumer<CustomPacketPayload> sender) {
                payloadFactories.forEach(factory -> sender.accept(factory.get()));
            }
        });
    }

    private <T extends IMessage<T>> void registerPacketInternal(PayloadRegistrar registrar, RegisteredPacket<T> rp) {
        ResourceLocation id = rp.id;

        StreamCodec<FriendlyByteBuf, NeoPayload<T>> codec = StreamCodec.of(
                (buf, payload) -> payload.write(buf),
                (buf) -> new NeoPayload<>(id, rp.decoder.apply(buf))
        );

        IPayloadHandler<NeoPayload<T>> handler = (payload, context) -> {
            final T message = payload.message();

            Runnable setHandledFunc = null;
            if (rp.isHandshake) {
                if (rp.direction == MessageDirection.CLIENT_TO_SERVER && this.handshakeTaskType != null) {
                    setHandledFunc = () -> context.enqueueWork(() -> context.finishCurrentTask(this.handshakeTaskType));
                } else {
                    setHandledFunc = () -> {};
                }
            }

            Player sender = null;
            if (!rp.isHandshake && rp.direction == MessageDirection.CLIENT_TO_SERVER) {
                sender = context.player();
            }

            IMessage.NetworkContext netContext = new IMessage.NetworkContext(
                    context.connection(),
                    rp.direction,
                    sender,
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
                    setHandledFunc
            );

            message.handle(context::enqueueWork, netContext);
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

    /**
     * 仅1.20.4neoforge使用，1.21.1起不再需要
     */
    @Deprecated(since = "1.21.1")
    private static class _LocalPlayer {
        private static Connection getConnection(Player player) {
            if (player instanceof net.minecraft.client.player.LocalPlayer lp) {
                return lp.connection.getConnection();
            }
            return null;
        };
    }
}
/*
 * 改成跟 BattleRoyale 同构的写法
 */

package xiao.customgun.neoforge.network;

import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.registration.IPayloadRegistrar;
import xiao.customgun.CustomGun;
import xiao.customgun.core.api.network.INetworkAdapter;
import xiao.customgun.core.api.network.MessageDirection;
import xiao.customgun.core.api.network.message.IMessage;
import xiao.customgun.core.network.LoginIndexHolder;
import xiao.customgun.neoforge.CustomGunNeoforge;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Mod.EventBusSubscriber(modid = CustomGun.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class NeoNetworkAdapter implements INetworkAdapter {
    public static final NeoNetworkAdapter INSTANCE = new NeoNetworkAdapter();

    private record RegisteredPacket<T extends IMessage<T>>(
            Class<T> messageType,
            ResourceLocation id,
            Function<FriendlyByteBuf, T> decoder,
            MessageDirection direction,
            boolean isHandshake
    ) {}

    private record NeoPayload<T extends IMessage<T>>(ResourceLocation id, T message) implements CustomPacketPayload {

        @Override
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
        PacketDistributor.ALL.noArg().send(payload);
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
        PacketDistributor.PLAYER.with(player).send(payload);
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
        if (andSelf) PacketDistributor.TRACKING_ENTITY_AND_SELF.with(entity).send(payload);
        else PacketDistributor.TRACKING_ENTITY.with(entity).send(payload);
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
        PacketDistributor.SERVER.noArg().send(payload);
    }

    @SubscribeEvent
    public static void register(RegisterPayloadHandlerEvent event) {
        final IPayloadRegistrar registrar = event.registrar(INSTANCE.modId);

        for (RegisteredPacket<?> rp : INSTANCE.registeredPackets) {
            INSTANCE.registerPacketInternal(registrar, rp);
        }
    }

    private <T extends IMessage<T>> void registerPacketInternal(IPayloadRegistrar registrar, RegisteredPacket<T> rp) {
        ResourceLocation id = rp.id;
        FriendlyByteBuf.Reader<NeoPayload<T>> decoder = (buffer) -> new NeoPayload<>(id, rp.decoder.apply(buffer));
        IPayloadHandler<NeoPayload<T>> handler = (payload, context) -> {
            final T message = payload.message();

            Connection connection = null;
            if (context.player().isPresent()) {
                Player player = context.player().get();
                if (player instanceof ServerPlayer sp) {
                    connection = sp.connection.connection;
                } else if (FMLLoader.getDist().isClient()) {
                    connection = _LocalPlayer.getConnection(player);
                }
            }
            IMessage.NetworkContext netContext = new IMessage.NetworkContext(
                    connection,
                    rp.direction,
                    context.player().orElse(null),
                    (replyMsg) -> {
                        this.registeredPackets.stream()
                                .filter(r -> r.messageType().isInstance(replyMsg))
                                .findFirst()
                                .ifPresent(info -> {
                                    CustomPacketPayload replyPayload = new NeoPayload<>(info.id(), (T) replyMsg);
                                    context.replyHandler().send(replyPayload);
                                });
                    },
                    rp.isHandshake ? () -> {} : null
            );

            context.workHandler().execute(() -> {
                message.handle(message, Runnable::run, netContext);
            });
        };

        if (rp.isHandshake) {
            // ↓貌似不会自动发message
            registrar.configuration(id, decoder, handler::handle);
        } else {
            registrar.common(id, decoder, handler);
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
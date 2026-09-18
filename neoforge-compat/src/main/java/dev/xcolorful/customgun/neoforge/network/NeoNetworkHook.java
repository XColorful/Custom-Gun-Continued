package dev.xcolorful.customgun.neoforge.network;

import dev.xcolorful.customgun.core.api.network.INetworkHook;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;

import java.util.function.Consumer;

public class NeoNetworkHook implements INetworkHook {

    @Override
    public void openScreen(ServerPlayer player, MenuProvider containerSupplier, Consumer<FriendlyByteBuf> extraDataWriter) {
        Consumer<RegistryFriendlyByteBuf> registryAdapter = (RegistryFriendlyByteBuf registryBuf) -> {
            extraDataWriter.accept((FriendlyByteBuf) registryBuf);
        };

        player.openMenu(containerSupplier, registryAdapter);
    }
}
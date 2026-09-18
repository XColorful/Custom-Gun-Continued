package dev.xcolorful.customgun.neoforge.network;

import dev.xcolorful.customgun.core.api.network.INetworkHook;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;

import java.util.function.Consumer;

public class NeoNetworkHook implements INetworkHook {

    @Override
    public void openScreen(ServerPlayer player, MenuProvider containerSupplier, Consumer<FriendlyByteBuf> extraDataWriter) {
        player.openMenu(containerSupplier, extraDataWriter);
    }
}
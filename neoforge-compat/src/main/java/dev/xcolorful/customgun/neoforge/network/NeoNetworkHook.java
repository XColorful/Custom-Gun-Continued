package dev.xcolorful.customgun.neoforge.network;

import dev.xcolorful.customgun.core.api.network.INetworkHook;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;

import java.util.function.Consumer;

public class NeoNetworkHook implements INetworkHook {
    public static final NeoNetworkHook INSTANCE = new NeoNetworkHook();

    /**
     * 手动传NeoForge类为旧实现方式，现直接调用 {@link dev.xcolorful.customgun.core.network.NetworkHook#get()} 即可
     */
    @SuppressWarnings("DeprecatedIsStillUsed")
    @Deprecated(forRemoval = false)
    public NeoNetworkHook() {
    }

    @Override
    public void openScreen(ServerPlayer player, MenuProvider containerSupplier, Consumer<FriendlyByteBuf> extraDataWriter) {
        player.openMenu(containerSupplier, extraDataWriter);
    }
}
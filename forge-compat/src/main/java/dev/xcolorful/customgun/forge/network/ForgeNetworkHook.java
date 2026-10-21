package dev.xcolorful.customgun.forge.network;

import dev.xcolorful.customgun.core.api.network.INetworkHook;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraftforge.network.NetworkHooks;

import java.util.function.Consumer;

public class ForgeNetworkHook implements INetworkHook {
    public static final ForgeNetworkHook INSTANCE = new ForgeNetworkHook();

    /**
     * 手动传Forge类为旧实现方式，现直接调用 {@link dev.xcolorful.customgun.core.network.NetworkHook#get()} 即可
     */
    @SuppressWarnings("DeprecatedIsStillUsed")
    @Deprecated(forRemoval = false)
    public ForgeNetworkHook() {
    }

    @Override
    public void openScreen(ServerPlayer player, MenuProvider containerSupplier, Consumer<FriendlyByteBuf> extraDataWriter) {
        NetworkHooks.openScreen(player, containerSupplier, extraDataWriter);
    }
}

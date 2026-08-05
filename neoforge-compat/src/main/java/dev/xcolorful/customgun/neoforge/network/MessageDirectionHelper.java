package dev.xcolorful.customgun.neoforge.network;

import dev.xcolorful.customgun.core.api.network.MessageDirection;
import net.minecraft.network.protocol.PacketFlow;

public class MessageDirectionHelper {

    public static MessageDirection convert(PacketFlow direction) {
        return direction == PacketFlow.CLIENTBOUND
                ? MessageDirection.SERVER_TO_CLIENT
                : MessageDirection.CLIENT_TO_SERVER;
    }

    public static PacketFlow convert(MessageDirection direction) {
        return direction == MessageDirection.SERVER_TO_CLIENT
                ? PacketFlow.CLIENTBOUND
                : PacketFlow.SERVERBOUND;
    }
}

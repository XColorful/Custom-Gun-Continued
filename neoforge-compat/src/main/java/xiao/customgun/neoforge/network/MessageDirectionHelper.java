package xiao.customgun.neoforge.network;

import net.minecraft.network.protocol.PacketFlow;
import xiao.customgun.core.api.network.MessageDirection;

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

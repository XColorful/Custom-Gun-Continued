package dev.xcolorful.customgun.neoforge.network;

import dev.xcolorful.customgun.core.api.network.MessageDirection;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.network.INetworkDirection;
import net.neoforged.neoforge.network.PlayNetworkDirection;

public class MessageDirectionHelper {

    public static MessageDirection convert(PlayNetworkDirection direction) {
        return direction == PlayNetworkDirection.PLAY_TO_CLIENT
                ? MessageDirection.SERVER_TO_CLIENT
                : MessageDirection.CLIENT_TO_SERVER;
    }

    public static PlayNetworkDirection convert(MessageDirection direction) {
        return direction == MessageDirection.SERVER_TO_CLIENT
                ? PlayNetworkDirection.PLAY_TO_CLIENT
                : PlayNetworkDirection.PLAY_TO_SERVER;
    }

    public static MessageDirection convertDynamic(INetworkDirection<?> direction) {
        return direction.getReceptionSide() == LogicalSide.CLIENT
                ? MessageDirection.SERVER_TO_CLIENT
                : MessageDirection.CLIENT_TO_SERVER;
    }

    public static INetworkDirection<?> convertDynamic(MessageDirection direction) {
        return convert(direction);
    }
}

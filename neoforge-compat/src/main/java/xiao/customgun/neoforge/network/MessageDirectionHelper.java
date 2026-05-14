package xiao.customgun.neoforge.network;

import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.network.INetworkDirection;
import net.neoforged.neoforge.network.PlayNetworkDirection;
import xiao.customgun.core.api.network.MessageDirection;

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

package dev.xcolorful.customgun.client.network.message.shooter;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.client.api.entity.LocalShooterProperty;
import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

@ApiStatus.Internal
public class _S2CMessageShooterBaseTimestamp {
    private static final Marker MARKER = MarkerFactory.getMarker("SYNC_BASE_TIMESTAMP");

    public static void updateBaseTimestamp(long timestamp) {
        LocalShooterProperty.clientBaseTimestamp = timestamp;
        CustomGun.LOGGER.debug(MARKER, "Update client base timestamp: {}", LocalShooterProperty.clientBaseTimestamp);
    }
}

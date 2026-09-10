package dev.xcolorful.customgun.client.network.message.shooter;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.client.util.ClientWorldUtils;
import dev.xcolorful.customgun.core.api.common.McLogicalSide;
import dev.xcolorful.customgun.core.api.entity.ILivingShooter;
import dev.xcolorful.customgun.core.api.entity.shooter.ILivingShooterGetter;
import dev.xcolorful.customgun.core.api.event.shooter.ShooterReloadFeedEvent;
import dev.xcolorful.customgun.core.api.item.IGun;
import dev.xcolorful.customgun.core.api.item.gun.IGunGetter;
import dev.xcolorful.customgun.core.network.message.shooter.S2CMessageShooterReloadFeed;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Internal
public class _S2CMessageShooterReloadFeed {

    public static void doClientEvent(S2CMessageShooterReloadFeed message) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;


        @Nullable LivingEntity livingShooter = ClientWorldUtils.getLivingEntityById(level, message.shooterId());
        @Nullable ILivingShooter iLivingShooter = ILivingShooterGetter.cgc$fromEntity(livingShooter);
        ItemStack gunItem = message.gunItem();
        @Nullable IGun iGun = IGunGetter.fromItemStack(gunItem);

        ShooterReloadFeedEvent event = new ShooterReloadFeedEvent(McLogicalSide.CLIENT,
                iLivingShooter, livingShooter,
                iGun, gunItem);
        CustomGun.getEventPoster().postCustomEvent(event);
    }
}

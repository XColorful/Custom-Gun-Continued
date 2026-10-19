package dev.xcolorful.customgun.core.mixin.entity;

import dev.xcolorful.customgun.core.api.entity.shooter.ILivingShooterConfig;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Player.class)
public class PlayerMixin implements ILivingShooterConfig {

    // --------ILivingShooterConfig--------

    @Override
    public boolean cgc$enableDefaultLivingShooterTick() {
        return true;
    }
}

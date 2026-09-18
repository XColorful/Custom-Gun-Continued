package dev.xcolorful.customgun.core.api.entity.victim;

import dev.xcolorful.customgun.core.api.entity.IBulletVictimEntity;
import dev.xcolorful.customgun.core.mixin.entity.LivingEntityMixin;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

public interface IBulletVictimEntityGetter {

    static @Nullable IBulletVictimEntity fromEntity(Entity entity) {
        return entity instanceof IBulletVictimEntity iBulletVictimEntity ? iBulletVictimEntity : null;
    }

    /**
     * {@link LivingEntityMixin} mixin到LivingEntity实现该接口
     */
    static IBulletVictimEntity fromLivingEntity(LivingEntity livingEntity) {
        return (IBulletVictimEntity) livingEntity;
    }
}

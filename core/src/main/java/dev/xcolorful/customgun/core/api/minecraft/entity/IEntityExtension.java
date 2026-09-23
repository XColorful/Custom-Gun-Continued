package dev.xcolorful.customgun.core.api.minecraft.entity;

import dev.xcolorful.customgun.core.mixin.entity.EntityMixin;
import dev.xcolorful.customgun.core.util.EntityUtils;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.ApiStatus;

/**
 * 注入到{@link net.minecraft.world.entity.Entity}
 * 对vanilla{@link EntityUtils#getMoveDist}版本差异的替代
 * 临时使用的类，以后可能改为更合适的方式或命名
 */
@ApiStatus.Internal
public interface IEntityExtension {

    /**
     * {@link EntityMixin} mixin到Entity实现该接口
     */
    static IEntityExtension cgc$getEntityExtension(Entity entity) {
        return (IEntityExtension) entity;
    }

    float cgc$getHorizontalMoveDist();
    float cgc$getHorizontalMoveDistOld();
}

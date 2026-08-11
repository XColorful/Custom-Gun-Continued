/*
 * 跟 BattleRoyale 同构
 */

package dev.xcolorful.customgun.client.init;

import dev.xcolorful.customgun.client.renderer.entity.GunProjectileRenderer;
import dev.xcolorful.customgun.core.init.registry.ModEntities;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

public class ClientEntityRendererRegistry {

    private static final ClientEntityRendererRegistry INSTANCE = new ClientEntityRendererRegistry();
    public static ClientEntityRendererRegistry get() {
        return INSTANCE;
    }
    private ClientEntityRendererRegistry() {}

    @FunctionalInterface
    public interface EntityRendererRegistrar {
        <T extends Entity> void register(
                EntityType<? extends T> entityType,
                EntityRendererProvider<T> entityRendererProvider
        );
    }

    public void registerEntityRenderers(EntityRendererRegistrar registrar) {
        registrar.register(ModEntities.GUN_PROJECTILE.get(), GunProjectileRenderer::new);
    }
}

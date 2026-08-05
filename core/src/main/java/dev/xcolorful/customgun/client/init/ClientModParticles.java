package dev.xcolorful.customgun.client.init;

import dev.xcolorful.customgun.client.particle.BulletHoleParticle;
import dev.xcolorful.customgun.core.init.registry.ModParticles;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;

public class ClientModParticles {

    private static final ClientModParticles INSTANCE = new ClientModParticles();

    public static ClientModParticles get() {
        return INSTANCE;
    }

    @FunctionalInterface
    public interface ParticleRegisterer {
        <T extends ParticleOptions> void register(ParticleType<T> type, ParticleProvider<T> provider);
    }

    public void onRegisterParticleProviders(ParticleRegisterer registerer) {
        registerer.register(ModParticles.BULLET_HOLE_PARTICLE_TYPE, BulletHoleParticle.PROVIDER);
    }
}

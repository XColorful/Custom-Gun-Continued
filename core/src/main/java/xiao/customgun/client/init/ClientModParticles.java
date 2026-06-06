package xiao.customgun.client.init;

import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import xiao.customgun.client.particle.BulletHoleParticle;
import xiao.customgun.core.init.registry.ModParticles;
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

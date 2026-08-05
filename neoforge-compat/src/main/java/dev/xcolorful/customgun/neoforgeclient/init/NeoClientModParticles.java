/*
 * 跟 BattleRoyale 同构
 */

package dev.xcolorful.customgun.neoforgeclient.init;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.client.init.ClientModParticles;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

@EventBusSubscriber(value = Dist.CLIENT, modid = CustomGun.MOD_ID)
public class NeoClientModParticles {

    public static ClientModParticles CLIENT_MOD_PARTICLES = ClientModParticles.get();

    @SubscribeEvent
    public static void onRegisterParticleFactory(RegisterParticleProvidersEvent event) {
        CLIENT_MOD_PARTICLES.onRegisterParticleProviders(event::registerSpecial);
    }
}

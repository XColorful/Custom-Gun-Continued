/*
 * 跟 BattleRoyale 同构
 */

package xiao.customgun.neoforgeclient.init;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import xiao.customgun.CustomGun;
import xiao.customgun.client.init.ClientModParticles;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = CustomGun.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class NeoClientModParticles {

    public static ClientModParticles CLIENT_MOD_PARTICLES = ClientModParticles.get();

    @SubscribeEvent
    public static void onRegisterParticleFactory(RegisterParticleProvidersEvent event) {
        CLIENT_MOD_PARTICLES.onRegisterParticleProviders(event::registerSpecial);
    }
}

/*
 * 跟 BattleRoyale 同构
 */

package xiao.customgun.forgeclient.init;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xiao.customgun.CustomGun;
import xiao.customgun.client.init.ClientModParticles;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = CustomGun.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ForgeClientModParticles {

    public static ClientModParticles CLIENT_MOD_PARTICLES = ClientModParticles.get();

    @SubscribeEvent
    public static void onRegisterParticleFactory(RegisterParticleProvidersEvent event) {
        CLIENT_MOD_PARTICLES.onRegisterParticleProviders(event::registerSpecial);
    }
}

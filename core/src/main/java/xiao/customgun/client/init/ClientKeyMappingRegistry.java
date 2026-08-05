/*
 * 跟 BattleRoyale 同构
 */

package xiao.customgun.client.init;

import net.minecraft.client.KeyMapping;
import org.jetbrains.annotations.ApiStatus;
import xiao.customgun.client.init.registry.ClientInputCategory;
import xiao.customgun.client.input.player.InteractKey;
import xiao.customgun.client.input.player.RefitKey;
import xiao.customgun.client.input.shooter.*;

import java.util.function.Consumer;

public class ClientKeyMappingRegistry {

    private static final ClientKeyMappingRegistry INSTANCE = new ClientKeyMappingRegistry();
    public static ClientKeyMappingRegistry get() {
        return INSTANCE;
    }
    private ClientKeyMappingRegistry() {}

    public void registerKeyMappings(Consumer<KeyMapping> register) {
        // player
        register.accept(InteractKey.get().getKeyMapping().get());
        register.accept(RefitKey.get().getKeyMapping().get());
        // shooter
        register.accept(AimKey.get().getKeyMapping().get());
        register.accept(InspectKey.get().getKeyMapping().get());
        register.accept(MeleeKey.get().getKeyMapping().get());
        register.accept(ProneKey.get().getKeyMapping().get());
        register.accept(ReloadKey.get().getKeyMapping().get());
        register.accept(ShootKey.get().getKeyMapping().get());
        register.accept(SwitchFireModeKey.get().getKeyMapping().get());
        register.accept(ZoomKey.get().getKeyMapping().get());
    }
    @ApiStatus.AvailableSince("1.21.10")
    public void registerInputCategories(Consumer<String> register) { // Consumer<KeyMapping.Category> register
        register.accept(ClientInputCategory.PLAYER);
        register.accept(ClientInputCategory.SHOOTER);
    }
}

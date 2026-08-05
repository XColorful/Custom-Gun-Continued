/*
 * 跟 BattleRoyale 同构
 */

package dev.xcolorful.customgun.client.init;

import dev.xcolorful.customgun.client.init.registry.ClientInputCategory;
import dev.xcolorful.customgun.client.input.player.InteractKey;
import dev.xcolorful.customgun.client.input.player.RefitKey;
import dev.xcolorful.customgun.client.input.shooter.*;
import net.minecraft.client.KeyMapping;
import org.jetbrains.annotations.ApiStatus;

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
    public void registerInputCategories(Consumer<KeyMapping.Category> register) {
        register.accept(ClientInputCategory.PLAYER);
        register.accept(ClientInputCategory.SHOOTER);
    }
}

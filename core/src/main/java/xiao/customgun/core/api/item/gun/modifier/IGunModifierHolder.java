package xiao.customgun.core.api.item.gun.modifier;

import org.jetbrains.annotations.NotNull;

public interface IGunModifierHolder extends IGunModifierType {

    @NotNull IGunModifier<?, ?, ?> getGunModifier();
}

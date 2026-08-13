package dev.xcolorful.customgun.client.api.minecraft.texture;

import dev.xcolorful.customgun.CustomGun;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public enum CustomTexture {
    WHITE(String.format("%s:%s", CustomGun.MOD_ID, "texture/white.png")),
    WHITE_8x8(String.format("%s:%s", CustomGun.MOD_ID, "texture/white_8x8.png")),
    // entity
    GUN_PROJECTILE(String.format("%s:%s", CustomGun.MOD_ID, "texture/entity/gun_projectile.png"));

    public final Identifier location;
    CustomTexture(String location) {
        this.location = CustomGun.getMcRegistry().createResourceLocation(location);
    }

    public @NotNull Identifier getLocation() {
        return this.location;
    }
}

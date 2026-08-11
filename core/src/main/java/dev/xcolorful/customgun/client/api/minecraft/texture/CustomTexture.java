package dev.xcolorful.customgun.client.api.minecraft.texture;

import dev.xcolorful.customgun.CustomGun;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public enum CustomTexture {
    WHITE(String.format("%s:%s", CustomGun.MOD_ID, "texture/white.png")),
    WHITE_8x8(String.format("%s:%s", CustomGun.MOD_ID, "texture/white_8x8.png")),
    // entity
    GUN_PROJECTILE(String.format("%s:%s", CustomGun.MOD_ID, "texture/entity/gun_projectile.png"));

    public final ResourceLocation location;
    CustomTexture(String location) {
        this.location = CustomGun.getMcRegistry().createResourceLocation(location);
    }

    public @NotNull ResourceLocation getLocation() {
        return this.location;
    }
}

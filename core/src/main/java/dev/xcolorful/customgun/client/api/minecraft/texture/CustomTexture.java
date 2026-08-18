package dev.xcolorful.customgun.client.api.minecraft.texture;

import dev.xcolorful.customgun.CustomGun;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public enum CustomTexture {
    WHITE_256x256(String.format("%s:%s", CustomGun.MOD_ID, "textures/white_256x256.png")),
    WHITE_32x32(String.format("%s:%s", CustomGun.MOD_ID, "textures/white_32x32.png")),
    WHITE_18x18(String.format("%s:%s", CustomGun.MOD_ID, "textures/white_18x18.png")),
    WHITE_8x8(String.format("%s:%s", CustomGun.MOD_ID, "textures/white_8x8.png")),
    // entity
    GUN_PROJECTILE(String.format("%s:%s", CustomGun.MOD_ID, "textures/entity/gun_projectile.png"))
    ;

    public final ResourceLocation location;
    CustomTexture(String location) {
        this.location = CustomGun.getMcRegistry().createResourceLocation(location);
    }

    public @NotNull ResourceLocation getLocation() {
        return this.location;
    }
}

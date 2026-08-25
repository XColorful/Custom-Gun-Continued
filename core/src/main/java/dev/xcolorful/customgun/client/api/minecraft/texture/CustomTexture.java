package dev.xcolorful.customgun.client.api.minecraft.texture;

import dev.xcolorful.customgun.CustomGun;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public enum CustomTexture {
    WHITE_256x256(String.format("%s:%s", CustomGun.MOD_ID, "textures/white_256x256.png"),
            256, 256),
    WHITE_32x32(String.format("%s:%s", CustomGun.MOD_ID, "textures/white_32x32.png"),
            32, 32),
    WHITE_18x18(String.format("%s:%s", CustomGun.MOD_ID, "textures/white_18x18.png"),
            18, 18),
    WHITE_8x8(String.format("%s:%s", CustomGun.MOD_ID, "textures/white_8x8.png"),
            8, 8),
    BLANK_128x128(String.format("%s:%s", CustomGun.MOD_ID, "textures/blank_128x128.png"),
            128, 128),
    // entity
    GUN_PROJECTILE(String.format("%s:%s", CustomGun.MOD_ID, "textures/entity/gun_projectile.png"),
            16, 16),
    // gui
    SLOT(String.format("%s:%s", CustomGun.MOD_ID, "textures/gui/slot.png"),
            18, 18),
    SLOT_SELECTED(String.format("%s:%s", CustomGun.MOD_ID, "textures/gui/slot_selected.png"),
            18, 18),
    UNLOAD(String.format("%s:%s", CustomGun.MOD_ID, "textures/gui/unload.png"),
            32, 16),
    ATTACHMENT_CATEGORIES(String.format("%s:%s", CustomGun.MOD_ID, "textures/gui/attachment_categories.png"),
            32 * 7, 32),
    TURN_PAGE(String.format("%s:%s", CustomGun.MOD_ID, "textures/gui/turn_page.png"),
            32, 32),
    CROSSHAIR(String.format("%s:%s", CustomGun.MOD_ID, "textures/gui/crosshair.png"),
            128, 128),
    ;

    public final ResourceLocation location;
    public final int width;
    public final int height;
    CustomTexture(String location, int width, int height) {
        this.location = CustomGun.getMcRegistry().createResourceLocation(location);
        this.width = width;
        this.height = height;
    }

    public @NotNull ResourceLocation getLocation() {
        return this.location;
    }
    public int getWidth() {
        return this.width;
    }
    public int getHeight() {
        return this.height;
    }
}

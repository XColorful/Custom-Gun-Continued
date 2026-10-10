package dev.xcolorful.customgun.client.text.placeholder.shooter;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.client.api.entity.ILocalShooter;
import dev.xcolorful.customgun.client.api.entity.shooter.ILocalShooterGetter;
import dev.xcolorful.customgun.core.api.text.placeholder.IPlaceholderParser;
import dev.xcolorful.customgun.core.api.text.placeholder.shooter.LocalShooterNameParserTag;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class LocalShooterNameParser implements IPlaceholderParser {
    public static final LocalShooterNameParser INSTANCE = new LocalShooterNameParser(LocalShooterNameParserTag.PLACEHOLDER_KEY);
    public static final LocalShooterNameParser INSTANCE_OLD1 = new LocalShooterNameParser(LocalShooterNameParserTag.PLACEHOLDER_KEY_OLD1) {
        @Override
        public @NotNull String parsePlaceholderKey(ItemStack gunItem) {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player == null) return "";

            return player.getName().getString();
        }
    };

    private final String placeholderKey;

    protected LocalShooterNameParser(String placeholderKey) {
        this.placeholderKey = placeholderKey;
    }

    public static void init() {
        CustomGun.getPlaceholderManager().register(INSTANCE);
        CustomGun.getPlaceholderManager().register(INSTANCE_OLD1);
    }

    // --------IPlaceholderParser--------

    @Override
    public String getPlaceholderKey() {
        return this.placeholderKey;
    }

    @Override
    public @NotNull String parsePlaceholderKey(ItemStack gunItem) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return "";

        ILocalShooter iLocalShooter = ILocalShooterGetter.fromLocalPlayer(player);
        return iLocalShooter.cgc$getLocalShooterName();
    }
}

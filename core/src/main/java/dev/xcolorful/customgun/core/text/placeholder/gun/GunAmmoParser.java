package dev.xcolorful.customgun.core.text.placeholder.gun;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.client.gui.tooltip.gun.GunStateInfoPart;
import dev.xcolorful.customgun.core.api.item.IGun;
import dev.xcolorful.customgun.core.api.item.gun.BoltType;
import dev.xcolorful.customgun.core.api.item.gun.IGunGetter;
import dev.xcolorful.customgun.core.api.resource.ResourceApi;
import dev.xcolorful.customgun.core.api.text.placeholder.IPlaceholderParser;
import dev.xcolorful.customgun.core.api.text.placeholder.gun.GunAmmoParserTag;
import dev.xcolorful.customgun.core.resource.data.data.GunData;
import dev.xcolorful.customgun.core.resource.instance.data.GunIndexInstance;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GunAmmoParser implements IPlaceholderParser {
    public static final GunAmmoParser INSTANCE = new GunAmmoParser(GunAmmoParserTag.PLACEHOLDER_KEY);
    public static final GunAmmoParser INSTANCE_OLD1 = new GunAmmoParser(GunAmmoParserTag.PLACEHOLDER_KEY_OLD1);

    private final String placeholderKey;

    protected GunAmmoParser(String placeholderKey) {
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

    /**
     * 同 {@link GunStateInfoPart#build}
     */
    @Override
    public @NotNull String parsePlaceholderKey(ItemStack gunItem) {
        @Nullable IGun iGun = IGunGetter.fromItemStack(gunItem);
        if (iGun == null) return "";

        // 当前枪内子弹
        int currentAmmoCount; {
            var gunLocation = iGun.getGunLocation(gunItem);
            @Nullable GunIndexInstance gunIndexInstance = ResourceApi.getGunIndexInstance(gunLocation);
            if (gunIndexInstance != null) {
                GunData gunData = gunIndexInstance.getGunData();
                BoltType boltType = gunData.getBoltType();
                currentAmmoCount = iGun.getMagAmmoCountWithBarrel(gunItem, boltType);
            } else {
                currentAmmoCount = iGun.getMagAmmoCount(gunItem);
            }
        }

        return String.valueOf(currentAmmoCount);
    }
}

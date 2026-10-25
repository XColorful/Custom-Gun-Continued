package dev.xcolorful.customgun.core.api.minecraft.tab;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.core.api.item.GunProperty;
import dev.xcolorful.customgun.core.api.item.builder.GunBuilder;
import dev.xcolorful.customgun.core.api.item.gun.FireModeType;
import dev.xcolorful.customgun.core.api.item.gun.GunCategory;
import dev.xcolorful.customgun.core.api.minecraft.IMcRegistry;
import dev.xcolorful.customgun.core.api.resource.ResourceApi;
import dev.xcolorful.customgun.core.init.registry.ModItems;
import dev.xcolorful.customgun.core.resource.data.data.GunData;
import dev.xcolorful.customgun.core.resource.data.index.GunIndex;
import dev.xcolorful.customgun.core.resource.instance.data.GunIndexInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class GunTab {

    public static Comparator<Map.Entry<ResourceLocation, GunIndexInstance>> indexSort() {
        return Comparator.comparingInt(entry -> entry.getValue().getPojo().getSlotSort());
    }

    public static List<ItemStack> buildGunItems(GunCategory gunCategory) {
        List<ItemStack> gunItems = new ArrayList<>();
        ResourceApi.getAllGunIndexInstance().stream().sorted(indexSort()).forEach(entry -> {
            @NotNull GunIndexInstance gunIndexInstance = entry.getValue();

            GunIndex gunIndex = gunIndexInstance.getPojo();
            if (gunIndex.getHideInGame()) {
                return;
            }

            if (gunIndex.getGunCategory() == gunCategory) {
                GunData gunData = gunIndexInstance.getGunData();
                ItemStack gunItem = buildGunItem(entry.getKey(),
                        gunData.getFireModeTypes().get(0),
                        gunData.getDefaultMagSize());
                gunItems.add(gunItem);
            }
        });
        return gunItems;
    }

    public static ItemStack buildGunItem(ResourceLocation gunLocation,
                                         FireModeType fireModeType,
                                         int magSize) {
        return GunBuilder.create(ModItems.GUN.get())
                // 枪械ResourceLocation
                .setProperty(GunProperty.GUN_LOCATION,
                        ResourceLocation.class,
                        gunLocation)
                // 开火模式
                .setProperty(GunProperty.FIRE_MODE_TYPE,
                        FireModeType.class,
                        /**
                         * {@link GunIndexInstance#isPojoValid}保证列表不为空
                         */
                        fireModeType)
                // 装满弹匣
                .setProperty(GunProperty.MAG_AMMO,
                        Integer.class,
                        magSize)
                .build();
    }

    public static @NotNull ResourceLocation PISTOL_ICON_LOCATION;
    public static @NotNull ResourceLocation AR_ICON_LOCATION;
    static {
        IMcRegistry mcRegistry = CustomGun.getMcRegistry();
        PISTOL_ICON_LOCATION = mcRegistry.createResourceLocation(String.format("%s:%s", CustomGun.TAB_ID, "pistol_icon"));
        AR_ICON_LOCATION = mcRegistry.createResourceLocation(String.format("%s:%s", CustomGun.TAB_ID, "ar_icon"));
    }

    public static ItemStack buildPistolTabItem() {
        // mixin注入点
        return buildGunItem(PISTOL_ICON_LOCATION,
                FireModeType.SEMI,
                0);
    }
    public static ItemStack buildArTabItem() {
        // mixin注入点
        return buildGunItem(AR_ICON_LOCATION,
                FireModeType.AUTO,
                0);
    }
    public static ItemStack buildDmrTabItem() {
        // mixin注入点
        return ItemStack.EMPTY;
    }
    public static ItemStack buildSrTabItem() {
        // mixin注入点
        return ItemStack.EMPTY;
    }
    public static ItemStack buildShotgunTabItem() {
        // mixin注入点
        return ItemStack.EMPTY;
    }
    public static ItemStack buildSmgTabItem() {
        // mixin注入点
        return ItemStack.EMPTY;
    }
    public static ItemStack buildMgTabItem() {
        // mixin注入点
        return ItemStack.EMPTY;
    }
    public static ItemStack buildRpgTabItem() {
        // mixin注入点
        return ItemStack.EMPTY;
    }
    public static ItemStack buildCustomTabItem() {
        // mixin注入点
        return ItemStack.EMPTY;
    }
}

package dev.xcolorful.customgun.client.gui.overlay.gunhud;

import dev.xcolorful.customgun.client.api.resource.ClientResourceApi;
import dev.xcolorful.customgun.client.gui.tooltip.gun.GunStateInfoPart;
import dev.xcolorful.customgun.client.resource.assets.display.GunDisplay;
import dev.xcolorful.customgun.client.resource.instance.assets.GunDisplayInstance;
import dev.xcolorful.customgun.core.api.entity.ILivingShooter;
import dev.xcolorful.customgun.core.api.item.IGun;
import dev.xcolorful.customgun.core.api.item.gun.AmmoCountType;
import dev.xcolorful.customgun.core.api.item.gun.BoltType;
import dev.xcolorful.customgun.core.api.item.gun.FireModeType;
import dev.xcolorful.customgun.core.api.resource.ResourceApi;
import dev.xcolorful.customgun.core.gun.action._DefaultGunAction;
import dev.xcolorful.customgun.core.resource.data.data.GunData;
import dev.xcolorful.customgun.core.resource.instance.data.GunIndexInstance;
import dev.xcolorful.customgun.core.util.ComponentUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class _GunHudBuilder {

    /**
     * 同 {@link GunStateInfoPart#build}
     */
    protected static @NotNull Component getMessage(@NotNull LocalPlayer localPlayer,
                                                   @NotNull IGun iGun, ItemStack gunItem) {

        // 当前枪内子弹
        int currentAmmoCount; {
            var gunLocation = iGun.getGunLocation(gunItem);
            @Nullable GunIndexInstance gunIndexInstance = ResourceApi.getGunIndexInstance(gunLocation);
            if (gunIndexInstance == null) return ComponentUtils.unknownTranslatableKey();

            GunData gunData = gunIndexInstance.getGunData();
            BoltType boltType = gunData.getBoltType();
            currentAmmoCount = iGun.getMagAmmoCountWithBarrel(gunItem, boltType);
        }

        // 弹匣大小
        int magAmmoLimit = iGun.getMagAmmoLimit(gunItem);

        // 备弹数量
        int reserveAmmoCount;
        ChatFormatting reserveAmmoColor;
        boolean forceShowReserveAmmo = false;
        if (iGun.useDummyAmmo(gunItem)) {
            /**
             * 跟{@link _DefaultGunAction#consumeFeedFromPlayer}对齐
             */
            // 虚拟备弹作为优先指定的备弹源
            reserveAmmoCount = iGun.getDummyAmmoCount(gunItem);
            reserveAmmoColor = ChatFormatting.DARK_AQUA;
            forceShowReserveAmmo = true; // 虚拟备弹强制显示备弹
        } else {
            // 背包直读 / 从背包读取
            reserveAmmoCount = iGun.getInventoryAmmoCount(localPlayer, gunItem);
            reserveAmmoColor = ChatFormatting.GRAY;
            if (iGun.useInventoryAmmo(gunItem)) forceShowReserveAmmo = true; // 背包直读强制显示备弹
        }

        @Nullable GunDisplayInstance gunDisplayInstance = ClientResourceApi.getGunDisplayInstance(gunItem);
        AmmoCountType ammoCountType; {
            if (gunDisplayInstance != null) {
                GunDisplay gunDisplay = gunDisplayInstance.getPojo();
                ammoCountType = gunDisplay.getAmmoCountType();
            } else {
                ammoCountType = AmmoCountType.NORMAL;
            }
        }

        Component baseMessage = _buildBaseMessage(ammoCountType, currentAmmoCount, magAmmoLimit, reserveAmmoCount, reserveAmmoColor, forceShowReserveAmmo);

        // 开火模式
        FireModeType fireModeType = iGun.getFireModeType(gunItem);
        return _buildMessage(baseMessage, fireModeType);
    }

    private static @NotNull Component _buildBaseMessage(AmmoCountType ammoCountType,
                                                        int currentAmmoCount, int magAmmoLimit, int reserveAmmoCount,
                                                        ChatFormatting reserveAmmoColor, boolean forceShowReserveAmmo) {
        MutableComponent message;
        return switch (ammoCountType) {
            case NORMAL -> {
                /*
                有备弹: "{当前子弹} {备弹}"
                无备弹: "{当前子弹}"
                 */

                // 当前子弹
                message = Component.literal(String.valueOf(currentAmmoCount))
                        .withStyle(currentAmmoCount > 0 ? (currentAmmoCount >= magAmmoLimit ? ChatFormatting.AQUA : ChatFormatting.WHITE)
                                : ChatFormatting.RED);
                // 备弹
                if (reserveAmmoCount > 0 || forceShowReserveAmmo) {
                    message.append(Component.literal(" ")
                            )
                            .append(Component.literal(String.valueOf(reserveAmmoCount))
                                    .withStyle(reserveAmmoColor)
                            );
                }

                yield message;
            }
            case PERCENT -> {
                /*
                有备弹: "{当前子弹%} {备弹}"
                无备弹: "{当前子弹%}"
                 */
                if (magAmmoLimit == 0) magAmmoLimit = 1;

                // 当前子弹
                message = Component.literal(String.format("%.1f%%", 100f * currentAmmoCount / magAmmoLimit));
                // 备弹
                if (reserveAmmoCount > 0) {
                    message.append(Component.literal(" ")
                            )
                            .append(Component.literal(String.format("%.1f%%", 100f * reserveAmmoCount / magAmmoLimit))
                                    .withStyle(ChatFormatting.GRAY)
                            );
                }

                yield message;
            }
            // 增加类型使此处强制编译不通过
        };
    }
    private static @NotNull Component _buildMessage(@NotNull Component message, FireModeType fireModeType) {
        return switch (fireModeType) {
            case AUTO -> {
                /*
                "{消息}"
                 */
                yield message;
            }
            case BURST -> {
                /*
                "<<< {消息} >>>"
                 */
                yield Component.literal("<<< ").withStyle(ChatFormatting.DARK_GRAY)
                        .append(message)
                        .append(Component.literal(" >>>").withStyle(ChatFormatting.DARK_GRAY));
            }
            case SEMI -> {
                /*
                "| {消息} |"
                 */
                yield Component.literal("| ").withStyle(ChatFormatting.DARK_GRAY)
                        .append(message)
                        .append(Component.literal(" |").withStyle(ChatFormatting.DARK_GRAY));
            }
            case DEFAULT -> {
                /*
                "x {消息} x"
                 */
                yield Component.literal("x ").withStyle(ChatFormatting.DARK_GRAY)
                        .append(message)
                        .append(Component.literal(" x").withStyle(ChatFormatting.DARK_GRAY));
            }
            // 增加类型使此处强制编译不通过
        };
    }
}

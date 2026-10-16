package dev.xcolorful.customgun.client.compat.playerrevive;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.core.compat.playerrevive.PlayerReviveCompat;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.lang.reflect.Method;

public class PlayerReviveClientCompat {

    /**
     * (客户端) 是否为倒地状态
     * <ul>
     *     <li>逻辑服务端侧要用{@link PlayerReviveCompat}</li>
     * </ul>
     */
    public static boolean isBleeding(LocalPlayer localPlayer) {
        // mixin注入点
        if (enableBuiltinCompat) return _isBleeding(localPlayer);
        return false;
    }

    // --------Config--------

    public static boolean shouldDisableShoot() {
        // mixin注入点
        if (enableBuiltinCompat) return _shouldDisableShoot();
        return false;
    }
    public static boolean shouldDisableReload() {
        // mixin注入点
        if (enableBuiltinCompat) return _shouldDisableReload();
        return false;
    }
    public static boolean shouldDisableSwitchFireMode() {
        // mixin注入点
        if (enableBuiltinCompat) return _shouldDisableSwitchFireMode();
        return false;
    }
    public static boolean shouldDisableMelee() {
        // mixin注入点
        if (enableBuiltinCompat) return _shouldDisableMelee();
        return false;
    }

    // --------Built-in Compat--------

    public static boolean enableBuiltinCompat = false;

    // 反射字段
    private static Method isBleeding;

    public static void init() {
        if (!CustomGun.getMcRegistry().isModLoaded(PlayerReviveCompat.MOD_ID)) return;

        try {
            // 客户端倒地状态存在PlayerRevive.BLEEDING能力上，由ReviveUpdatePacket同步
            Class<?> playerReviveServerClass = Class.forName("team.creative.playerrevive.server.PlayerReviveServer");

            isBleeding = playerReviveServerClass.getMethod("isBleeding", Player.class);

            enableBuiltinCompat = true;
        } catch (ReflectiveOperationException e) {
            CustomGun.LOGGER.debug("Failed to initialize PlayerRevive client compatibility", e);
        }
    }

    private static boolean _isBleeding(LivingEntity livingEntity) {
        return (boolean) invoke(isBleeding, null, livingEntity);
    }

    private static boolean _shouldDisableShoot() {
        return true;
    }
    private static boolean _shouldDisableReload() {
        return true;
    }
    private static boolean _shouldDisableSwitchFireMode() {
        return true;
    }
    private static boolean _shouldDisableMelee() {
        return true;
    }

    private static Object invoke(Method method, Object target, Object... args) {
        try {
            return method.invoke(target, args);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to invoke PlayerRevive API", e);
        }
    }
}

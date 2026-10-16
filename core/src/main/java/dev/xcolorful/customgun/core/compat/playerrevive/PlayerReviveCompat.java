package dev.xcolorful.customgun.core.compat.playerrevive;

import dev.xcolorful.customgun.CustomGun;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PlayerReviveCompat {

    public static final String MOD_ID = "playerrevive";

    /**
     * (服务端) 是否为倒地状态
     * <ul>
     *     <li>逻辑客户端侧要用{@link dev.xcolorful.customgun.client.compat.playerrevive.PlayerReviveClientCompat}</li>
     * </ul>
     */
    public static boolean isBleeding(LivingEntity livingEntity) {
        // mixin注入点
        if (enableBuiltinCompat) return _isBleeding(livingEntity);
        return false;
    }

    public static void revive(LivingEntity livingEntity) {
        // mixin注入点
        if (enableBuiltinCompat) _revive(livingEntity);
    }

    public static void kill(LivingEntity livingEntity) {
        // mixin注入点
        if (enableBuiltinCompat) _kill(livingEntity);
    }

    public static @Nullable List<Player> getRevivingPlayers(LivingEntity livingEntity) {
        // mixin注入点
        if (enableBuiltinCompat) return _getRevivingPlayers(livingEntity);
        return null;
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

    public static void init() {
        if (!CustomGun.getMcRegistry().isModLoaded(PlayerReviveCompat.MOD_ID)) return;

        try {

            enableBuiltinCompat = true;
        } catch (ReflectiveOperationException e) {
            CustomGun.LOGGER.debug("Failed to initialize PlayerRevive client compatibility", e);
        }
    }

    private static boolean _isBleeding(LivingEntity livingEntity) {
        return false;
    }

    private static void _revive(LivingEntity livingEntity) {
    }

    private static void _kill(LivingEntity livingEntity) {
    }

    private static @Nullable List<Player> _getRevivingPlayers(LivingEntity livingEntity) {
        return null;
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
}

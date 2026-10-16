package dev.xcolorful.customgun.core.compat.playerrevive;

import dev.xcolorful.customgun.CustomGun;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
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
    private static Method isBleeding;
    private static Method getBleeding;
    private static Method revive;
    private static Method kill;
    private static Method revivingPlayers;

    public static void init() {
        if (!CustomGun.getMcRegistry().isModLoaded(PlayerReviveCompat.MOD_ID)) return;

        try {
            Class<?> playerReviveServerClass = Class.forName("team.creative.playerrevive.server.PlayerReviveServer");
            Class<?> iBleedingClass = Class.forName("team.creative.playerrevive.api.IBleeding");

            isBleeding = playerReviveServerClass.getMethod("isBleeding", Player.class);
            getBleeding = playerReviveServerClass.getMethod("getBleeding", Player.class);
            revive = playerReviveServerClass.getMethod("revive", Player.class);
            kill = playerReviveServerClass.getMethod("kill", Player.class);

            revivingPlayers = iBleedingClass.getMethod("revivingPlayers");

            enableBuiltinCompat = true;
        } catch (ReflectiveOperationException e) {
            CustomGun.LOGGER.debug("Failed to initialize PlayerRevive compatibility", e);
        }
    }

    private static boolean _isBleeding(LivingEntity livingEntity) {
        if (!(livingEntity instanceof Player player)) return false;
        return (boolean) invoke(isBleeding, null, player);
    }

    private static void _revive(LivingEntity livingEntity) {
        if (!(livingEntity instanceof Player player)) return;
        invoke(revive, null, player);
    }

    private static void _kill(LivingEntity livingEntity) {
        if (!(livingEntity instanceof Player player)) return;
        invoke(kill, null, player);
    }

    private static @Nullable List<Player> _getRevivingPlayers(LivingEntity livingEntity) {
        if (!(livingEntity instanceof Player player)) return null;
        Object bleeding = invoke(getBleeding, null, player);
        if (bleeding == null) return null;
        @SuppressWarnings("unchecked")
        List<Player> players = (List<Player>) invoke(revivingPlayers, bleeding);
        return players;
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

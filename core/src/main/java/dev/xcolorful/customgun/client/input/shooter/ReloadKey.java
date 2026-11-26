package dev.xcolorful.customgun.client.input.shooter;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.client.api.entity.shooter.ILocalShooterGetter;
import dev.xcolorful.customgun.client.api.event.IClientPlayerTickEvent;
import dev.xcolorful.customgun.client.api.event.IInputKeyEvent;
import dev.xcolorful.customgun.client.api.event.IMouseButtonEvent;
import dev.xcolorful.customgun.client.api.input.*;
import dev.xcolorful.customgun.client.api.minecraft.input.CustomInputKey;
import dev.xcolorful.customgun.client.config.KeyConfig;
import dev.xcolorful.customgun.client.init.registry.ClientInputCategory;
import dev.xcolorful.customgun.client.input.InputKey;
import dev.xcolorful.customgun.client.util.ClientInputUtils;
import dev.xcolorful.customgun.core.api.event.*;
import dev.xcolorful.customgun.core.api.item.IGun;
import dev.xcolorful.customgun.core.api.item.gun.BoltType;
import dev.xcolorful.customgun.core.api.item.gun.IGunGetter;
import dev.xcolorful.customgun.core.api.resource.ResourceApi;
import dev.xcolorful.customgun.core.resource.instance.data.GunIndexInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ReloadKey extends InputKey implements IEventHandler {

    private static final class ReloadKeyHolder {
        private static final ReloadKey INSTANCE = new ReloadKey();
    }

    public static ReloadKey get() {
        return ReloadKeyHolder.INSTANCE;
    }

    private ReloadKey() {
        super(CustomInputKey.RELOAD);
    }
    @Override protected IKeyMapping createKeyMapping(IKeyMapping.Creator creator) {
        return creator.create(this.key.getCategoryLang().getString(),
                IKeyConflictContext.Type.IN_GAME,
                IKeyModifier.Type.NONE,
                ClientInputUtils.KeyType.keyboard(),
                ClientInputUtils.KeyCode._KEY_R(),
                ClientInputCategory.SHOOTER);
    }

    public static final String _MANAGER_NAME = String.format("%s:%s", CustomGun.MOD_ID, ReloadKey.class.getSimpleName());
    @Override public String getManagerName() {
        return _MANAGER_NAME;
    }

    @Override
    public boolean registerEventHandler() {
        ICustomEventRegister customEventRegister = CustomGun.getEventRegister();
        customEventRegister.register(this, EventType.PREPARE_CLIENT_PLAYER_TICK_EVENT, EventPriority.NORMAL, false);
        return true;
    }
    @Override
    public boolean unregisterEventHandler() {
        ICustomEventRegister customEventRegister = CustomGun.getEventRegister();
        customEventRegister.unregister(this, EventType.PREPARE_CLIENT_PLAYER_TICK_EVENT, EventPriority.NORMAL, false);
        return true;
    }

    @Override public String getEventHandlerName() {
        return this.getClass().getName();
    }
    @Override
    public void handleEvent(EventType eventType, IEvent event) {
        switch (eventType) {
            case PREPARE_CLIENT_PLAYER_TICK_EVENT -> tickReload((IClientPlayerTickEvent) event);
            default -> onReceiveWrongEvent(eventType);
        }
    }

    // --------IInputHandler--------

    @Override
    public void onKeyInput(IInputKeyManager inputKeyManager, IInputKeyEvent event) {
        this.onReloadKeyInput(event.getAction());
    }
    @Override
    public void onMouseInput(IInputKeyManager inputKeyManager, IMouseButtonEvent event) {
        this.onReloadKeyInput(event.getAction());
    }
    private void onReloadKeyInput(int action) {
        if (InputAction.of(action) != InputAction.PRESS) return;

        if (!ClientInputUtils.isGameplayFocused()) return; // 不在焦点

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || player.isSpectator()) return; // 旁观模式

        ItemStack gunItem = player.getMainHandItem();
        @Nullable IGun iGun = IGunGetter.fromItemStack(gunItem);
        if (iGun == null) return; // 主手没枪

        if (iGun.useInventoryAmmo(gunItem)) return; // 背包直读 -> 不需要装弹

        ILocalShooterGetter.fromLocalPlayer(player).cgc$reload();
    }

    @ApiStatus.Internal
    public static boolean needReload = false;
    private void tickReload(IClientPlayerTickEvent event) {
        LocalPlayer player = Minecraft.getInstance().player;

        if (needReload) {
            if (KeyConfig.SHOOT_TO_RELOAD.get() && player != null) this.doAutoReload(player);
            needReload = false;
            return;
        }

        this.autoReload(player);
    }

    public static int AUTO_RELOAD_FREQUENCY = 5;
    private void autoReload(LocalPlayer player) {
        if (!KeyConfig.AUTO_RELOAD.get()) return;

        if (player == null || player.isSpectator() // 旁观模式
                || player.tickCount % AUTO_RELOAD_FREQUENCY != 0) return;

        this.doAutoReload(player);
    }

    /**
     * 仅限空仓时自动装弹，即枪管和弹匣都没子弹
     */
    private void doAutoReload(@NotNull LocalPlayer player) {
        ItemStack gunItem = player.getMainHandItem();
        @Nullable IGun iGun = IGunGetter.fromItemStack(gunItem);
        if (iGun == null) return; // 主手没枪

        if (iGun.useInventoryAmmo(gunItem)) return; // 背包直读 -> 不需要装弹

        { // 枪管有子弹就不装弹
            var gunLocation = iGun.getGunLocation(gunItem);
            @Nullable GunIndexInstance gunIndexInstance = ResourceApi.getGunIndexInstance(gunLocation);
            if (gunIndexInstance == null) return;

            BoltType boltType = gunIndexInstance.getGunData().getBoltType();
            /*
            只要枪管或弹匣有子弹就取消
            否则狙击枪（半自动）打完一发时枪管没子弹，点击左键触发装弹而弹匣还有剩
             */
            boolean hasAmmo = iGun.getMagAmmoCountWithBarrel(gunItem, boltType) > 0;
            if (hasAmmo) return;
        }

        ILocalShooterGetter.fromLocalPlayer(player).cgc$reload();
    }

    // --------Deprecated--------

    /**
     * Controllable联动的写法要改, 至少肯定不是写在这里
     */
    @Deprecated(forRemoval = true)
    public static boolean onReloadControllerPress(boolean isPress) {
        return false;
    }
}

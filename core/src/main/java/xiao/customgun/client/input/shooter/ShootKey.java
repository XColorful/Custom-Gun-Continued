/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.input.shooter;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import xiao.customgun.CustomGun;
import xiao.customgun.client.api.entity.ILocalShooter;
import xiao.customgun.client.api.entity.shooter.ILocalShooterGetter;
import xiao.customgun.client.api.event.IClientTickEvent;
import xiao.customgun.client.api.event.IInputKeyEvent;
import xiao.customgun.client.api.event.IMouseButtonEvent;
import xiao.customgun.client.api.event.IPrepareClientTickEvent;
import xiao.customgun.client.api.input.IInputKeyManager;
import xiao.customgun.client.api.input.IKeyConflictContext;
import xiao.customgun.client.api.input.IKeyMapping;
import xiao.customgun.client.api.input.IKeyModifier;
import xiao.customgun.client.api.minecraft.input.CustomInputKey;
import xiao.customgun.client.entity.shooter.LocalShooterSprint;
import xiao.customgun.client.init.registry.ClientInputCategory;
import xiao.customgun.client.input.InputKey;
import xiao.customgun.client.sound.SoundPlayManager;
import xiao.customgun.client.util.ClientInputUtils;
import xiao.customgun.core.api.entity.ShootResult;
import xiao.customgun.core.api.event.*;
import xiao.customgun.core.api.item.IGun;
import xiao.customgun.core.api.item.gun.IGunGetter;
import xiao.customgun.core.api.resource.ResourceApi;
import xiao.customgun.core.resource.data.data.GunData;
import xiao.customgun.core.resource.instance.data.GunIndexInstance;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class ShootKey extends InputKey implements IEventHandler {

    private static final class ShootKeyHolder {
        private static final ShootKey INSTANCE = new ShootKey();
    }

    public static ShootKey get() {
        return ShootKeyHolder.INSTANCE;
    }

    private ShootKey() {
        super(CustomInputKey.SHOOT);
    }
    @Override protected IKeyMapping createKeyMapping(IKeyMapping.Creator creator) {
        return creator.create(this.key.getCategoryLang().getString(),
                IKeyConflictContext.Type.IN_GAME,
                IKeyModifier.Type.NONE,
                InputConstants.Type.MOUSE,
                GLFW.GLFW_MOUSE_BUTTON_LEFT,
                ClientInputCategory.SHOOTER);
    }

    public static final String _MANAGER_NAME = String.format("%s:%s", CustomGun.MOD_ID, ShootKey.class.getSimpleName());
    @Override public String getManagerName() {
        return _MANAGER_NAME;
    }

    @Override
    public boolean registerEventHandler() {
        ICustomEventRegister customEventRegister = CustomGun.getEventRegister();
        customEventRegister.register(this, EventType.PREPARE_CLIENT_TICK_EVENT, EventPriority.NORMAL, false);
        customEventRegister.register(this, EventType.CLIENT_TICK_EVENT, EventPriority.NORMAL, false);
        return true;
    }
    @Override
    public boolean unregisterEventHandler() {
        ICustomEventRegister customEventRegister = CustomGun.getEventRegister();
        customEventRegister.unregister(this, EventType.PREPARE_CLIENT_TICK_EVENT, EventPriority.NORMAL, false);
        customEventRegister.unregister(this, EventType.CLIENT_TICK_EVENT, EventPriority.NORMAL, false);
        this.inputQueue.clear();
        return true;
    }

    @Override public String getEventHandlerName() {
        return this.getClass().getName();
    }
    @Override
    public void handleEvent(EventType eventType, IEvent event) {
        switch (eventType) {
            case PREPARE_CLIENT_TICK_EVENT -> onShootPreInput((IPrepareClientTickEvent) event);
            case CLIENT_TICK_EVENT -> tickShoot((IClientTickEvent) event);
            default -> onReceiveWrongEvent(eventType);
        }
    }

    // --------IInputHandler--------

    @Override
    public void onKeyInput(IInputKeyManager inputKeyManager, IInputKeyEvent event) {
        this.onShootKeyInput(event.getAction());
    }
    @Override
    public void onMouseInput(IInputKeyManager inputKeyManager, IMouseButtonEvent event) {
        this.onShootKeyInput(event.getAction());
    }
    private void onShootKeyInput(int action) {
        switch (action) {
            case GLFW.GLFW_PRESS -> this.inputQueue.offer(ShootInput.PRESS);
            case GLFW.GLFW_RELEASE -> this.inputQueue.offer(ShootInput.RELEASE);
        }
    }

    private enum ShootInput { PRESS, RELEASE }
    private final Queue<ShootInput> inputQueue = new ConcurrentLinkedQueue<>();
    private boolean hasPressedShoot = false; // 本tick是否出现过按压
    private boolean isShootReleased = false; // 本tick最后是否为释放状态 (会被最新press覆盖)

    /**
     * 清空上一tick信息 + 消费输入队列
     */
    private void onShootPreInput(IPrepareClientTickEvent event) {
        this.hasPressedShoot = false;
        this.isShootReleased = false;

        // 消费输入队列
        ShootInput input;
        while ((input = this.inputQueue.poll()) != null) {
            switch (input) {
                case PRESS -> {
                    this.hasPressedShoot = true;
                    this.isShootReleased = false; // 最新的press 覆盖 未在checkShoot里使用的release ("press -> release -> press" 视为没释放)
                }
                case RELEASE -> this.isShootReleased = true;
            }
        }

    }
    private boolean lastShootSuccess = false;
    private void tickShoot(IClientTickEvent event) {
        this.lastShootSuccess = checkAndDoShoot();

    }
    /**
     * 只读 this 状态
     * 返回是否成功
     */
    private boolean checkAndDoShoot() {
        if (!ClientInputUtils.isGameplayFocused()) return false; // 不在焦点

        LocalShooterSprint.forceDisableSprint = false;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || player.isSpectator()) return false; // 旁观模式

        ItemStack gunItem = player.getMainHandItem();
        IGun iGun = IGunGetter.fromItemStack(gunItem);
        if (iGun == null) return false; // 主手没枪

        // --------shoot--------

        boolean currentShootDown = this.keyMapping.get().isDown() || externalKeyDown; // 当前是否按下开火键
        boolean shootReleased = this.isShootReleased && !currentShootDown;
        boolean shootPressed = this.hasPressedShoot || currentShootDown;

        if (shootPressed) // 按住开火键就禁止奔跑
            LocalShooterSprint.forceDisableSprint = true;
        else {
            SoundPlayManager.get().resetDryFireSound(); // 没按开火,后面没开火成功允许触发音效
            // 不提前返回, 需要减少蓄力进度
        }

        boolean allowContinuousShoot = switch (iGun.getFireModeType(gunItem)) { // 全自动 / 自动连发 -> 允许连续射击
            case AUTO -> true;
            case BURST -> {
                var gunLocation = iGun.getGunLocation(gunItem);
                @Nullable GunIndexInstance gunIndexInstance = ResourceApi.getGunIndexInstance(gunLocation);
                if (gunIndexInstance == null) yield false;
                GunData gunData = gunIndexInstance.getGunData();
                yield gunData.getBurstData().getContinuousShoot();
            }
            default -> false;
        };
        allowContinuousShoot |= shootReleased; // 如果这一tick先按住再释放，则允许下一tick再按住，即允许连续射击(连狙速点)

        // 开火判断: "按下攻击键 + (允许按住开火 or 上次没成功)"
        boolean doShoot = shootPressed && (allowContinuousShoot || !this.lastShootSuccess); // 上次没成功即至少间隔1tick

        // 检查是否蓄力满/不需要充能 -> 开火充能就绪
        ILocalShooter localShooter = ILocalShooterGetter.fromLocalPlayer(player);
        boolean shouldShoot = localShooter.cgc$doCharge_isChargeEnough(doShoot); // 包含了蓄力进度tick
        if (!shouldShoot) return false;

        // ----开火充能就绪----

        LocalShooterSprint.forceDisableSprint = true;

        // 不允许连续射击模式 + 上次开火成功 -> 阻止继续蓄力(不开火)
        if (!allowContinuousShoot && this.lastShootSuccess) return false;

        // 执行开火
        if (localShooter.cgc$localShoot() == ShootResult.SUCCESS) {
            this.lastShootSuccess = true;
            this.onShootSuccess();
            return true;
        } else {
            return false;
        }
    }
    @ApiStatus.Internal
    public void onShootSuccess() {
        // mixin注入点
    }

    private boolean externalKeyDown = false;
    public void setExternalKeyDown(boolean value) {
        this.externalKeyDown = value;
    }

    // --------Deprecated--------

    /**
     * Controllable联动的写法要改, 至少肯定不是写在这里
     */
    @Deprecated(forRemoval = true)
    public static boolean shootControllerTick(boolean isShootDown) {
        return false;
    }
}

package dev.xcolorful.customgun.client.input.player;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.client.api.entity.shooter.ILocalShooterGetter;
import dev.xcolorful.customgun.client.api.event.IInputKeyEvent;
import dev.xcolorful.customgun.client.api.event.IMouseButtonEvent;
import dev.xcolorful.customgun.client.api.input.*;
import dev.xcolorful.customgun.client.api.minecraft.input.CustomInputKey;
import dev.xcolorful.customgun.client.init.registry.ClientInputCategory;
import dev.xcolorful.customgun.client.input.InputKey;
import dev.xcolorful.customgun.client.util.ClientInputUtils;
import dev.xcolorful.customgun.client.util.ClientRenderProfiler;
import dev.xcolorful.customgun.core.api.event.EventType;
import dev.xcolorful.customgun.core.api.event.ICustomEventRegister;
import dev.xcolorful.customgun.core.api.event.IEvent;
import dev.xcolorful.customgun.core.api.event.IEventHandler;
import dev.xcolorful.customgun.core.entity.shooter.LivingShooterReload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.jetbrains.annotations.ApiStatus;

/**
 * 仅开发时测试用
 */
@ApiStatus.Internal
public final class DebugKey extends InputKey {

    private static final class DebugKeyHolder {
        private static final DebugKey INSTANCE = new DebugKey();
    }
    public static DebugKey get() {
        return DebugKeyHolder.INSTANCE;
    }
    private DebugKey() {
        super(CustomInputKey.DEBUG);
    }
    @Override protected IKeyMapping createKeyMapping(IKeyMapping.Creator creator) {
        return creator.create(this.key.getCategoryLang().getString(),
                IKeyConflictContext.Type.IN_GAME,
                IKeyModifier.Type.NONE,
                ClientInputUtils.KeyType.keyboard(),
                ClientInputUtils.KeyCode._KEY_F12(),
                ClientInputCategory.PLAYER);
    }

    public static final String _MANAGER_NAME = String.format("%s:%s", CustomGun.MOD_ID, DebugKey.class.getSimpleName());
    @Override public String getManagerName() {
        return _MANAGER_NAME;
    }

    @Override
    public boolean registerEventHandler() {
        ICustomEventRegister customEventRegister = CustomGun.getEventRegister();
        return true;
    }
    @Override
    public boolean unregisterEventHandler() {
        ICustomEventRegister customEventRegister = CustomGun.getEventRegister();
        return true;
    }

    // --------IInputHandler--------

    @Override
    public void onKeyInput(IInputKeyManager inputKeyManager, IInputKeyEvent event) {
        this.onDebugKeyInput(event.getAction());
    }
    @Override
    public void onMouseInput(IInputKeyManager inputKeyManager, IMouseButtonEvent event) {
        this.onDebugKeyInput(event.getAction());
    }
    private void onDebugKeyInput(int action) {
        if (InputAction.of(action) != InputAction.PRESS) return;

        if (ClientRenderProfiler.enabled) {
            DebugKeyReloadTest.INSTANCE.stop();
            ClientRenderProfiler.stopAndWrite();
        } else {
            DebugKeyReloadTest.INSTANCE.start();
            ClientRenderProfiler.start();
        }
    }

    /**
     * 是否有调试逻辑 (决定是否注册调试键)
     */
    public static final boolean enableDebugKey = true;

    public static class DebugKeyReloadTest implements IEventHandler {
        public static final DebugKeyReloadTest INSTANCE = new DebugKeyReloadTest();
        @Override public String getEventHandlerName() {
            return this.getClass().getName();
        }
        @Override
        public void handleEvent(EventType eventType, IEvent event) {
            if (eventType == EventType.CLIENT_TICK_EVENT) {
                tick();
            } else {
                onReceiveWrongEvent(eventType);
            }
        }
        private void start() {
            LivingShooterReload.clientReloadTest = true;
            this.tickCount = 0;
            CustomGun.getEventRegister().register(INSTANCE, EventType.CLIENT_TICK_EVENT);
        }
        private void stop() {
            LivingShooterReload.clientReloadTest = false;
            this.tickCount = 0;
            CustomGun.getEventRegister().unregister(INSTANCE, EventType.CLIENT_TICK_EVENT);
        }
        private int tickCount = 0;
        private void tick() {
            if (this.tickCount % 20 * 3 == 0 && this.tickCount < 20 * 9) { // 每3秒自动换弹，最多三次
                LocalPlayer localPlayer = Minecraft.getInstance().player;
                ILocalShooterGetter.fromLocalPlayer(localPlayer).cgc$reload();
            }
            this.tickCount++;
            if (this.tickCount > 20 * 10) { // 10秒
                if (ClientRenderProfiler.enabled) DebugKey.get().onDebugKeyInput(InputAction.PRESS.keyCode()); // 自动点击结束
            }
        }
    }
}

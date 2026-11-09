package dev.xcolorful.customgun.client.input.player;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.client.api.event.IInputKeyEvent;
import dev.xcolorful.customgun.client.api.event.IMouseButtonEvent;
import dev.xcolorful.customgun.client.api.input.IInputKeyManager;
import dev.xcolorful.customgun.client.api.input.IKeyConflictContext;
import dev.xcolorful.customgun.client.api.input.IKeyMapping;
import dev.xcolorful.customgun.client.api.input.IKeyModifier;
import dev.xcolorful.customgun.client.api.minecraft.input.CustomInputKey;
import dev.xcolorful.customgun.client.init.registry.ClientInputCategory;
import dev.xcolorful.customgun.client.input.InputKey;
import dev.xcolorful.customgun.client.util.ClientInputUtils;
import dev.xcolorful.customgun.core.api.event.ICustomEventRegister;
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
    }

    /**
     * 是否有调试逻辑 (决定是否注册调试键)
     */
    public static final boolean enableDebugKey = true;
}

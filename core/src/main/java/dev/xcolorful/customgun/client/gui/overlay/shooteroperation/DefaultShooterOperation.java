package dev.xcolorful.customgun.client.gui.overlay.shooteroperation;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.client.CustomGunClient;
import dev.xcolorful.customgun.client.api.event.IRenderGuiEvent;
import dev.xcolorful.customgun.client.api.gui.overlay.BuiltinOverlayType;
import dev.xcolorful.customgun.client.api.gui.overlay.IOverlaySubManager;
import dev.xcolorful.customgun.client.api.gui.screen.IScreen;
import dev.xcolorful.customgun.client.config.RenderConfig;
import dev.xcolorful.customgun.client.util.ClientGuiUtils;
import dev.xcolorful.customgun.client.util.ClientInputUtils;
import dev.xcolorful.customgun.core.api.event.EventType;
import dev.xcolorful.customgun.core.api.event.ICustomEventRegister;
import dev.xcolorful.customgun.core.api.event.IEvent;
import dev.xcolorful.customgun.core.api.event.IEventHandler;
import dev.xcolorful.customgun.core.api.minecraft.Color64;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public class DefaultShooterOperation implements IOverlaySubManager, IEventHandler {
    public static final DefaultShooterOperation INSTANCE = new DefaultShooterOperation();

    protected DefaultShooterOperation() {}

    public static void init() {
        CustomGunClient.getOverlayManager().register(INSTANCE);
    }

    @Override public String getEventHandlerName() {
        return this.getClass().getName();
    }
    @Override
    public void handleEvent(EventType eventType, IEvent event) {
        if (eventType == EventType.RENDER_GUI_EVENT) {
            onRenderGui((IRenderGuiEvent) event);
        } else {
            onReceiveWrongEvent(eventType);
        }
    }

    /**
     * @return 是否处于不应该渲染overlay的状态
     */
    private boolean _shouldForceDisableOverlay(Minecraft mc) {
        if ( // 状态检查
                // 按F1 (不显示GUI)
                ClientGuiUtils.isGuiHidden(mc)
                // 当前不是第一人称
                || !mc.options.getCameraType().isFirstPerson()
                // 不在游戏内
                || !ClientInputUtils.isInGameWorld()
                // 旁观模式
                || mc.player == null || mc.player.isSpectator()
        ) return true;

        return false;
    }

    private void onRenderGui(IRenderGuiEvent event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer localPlayer = mc.player;
        if (localPlayer == null) return;

        // 未启用功能
        if (!this.isEnabled()) return;

        if ( // 状态检查
                // 当前没有要显示的消息
                this.currentMessage == null
                // 不需要渲染的状态
                || _shouldForceDisableOverlay(mc)
        ) return;

        // screen检查
        boolean hideOverlay = false;
        @Nullable Screen screen = ClientGuiUtils.getCurrentScreen(mc); {
            if (screen != null) {
                // 当前有screen

                @Nullable IScreen<?> iScreen = screen instanceof IScreen<?> _iScreen ? _iScreen : null;
                if (iScreen == null) {
                    // 不是本模组能检查的screen接口，不接管逻辑
                    return;
                } else {
                    hideOverlay = iScreen.hideOverlay();
                }
            }
        }
        if (hideOverlay) return;

        this._renderOperationMessage(event);
    }

    /**
     * 当前显示的消息
     */
    private @Nullable Component currentMessage;
    /**
     * 优先级，数字越大越优先
     */
    private int currentPriority = 0;

    private final _PlayerInteract playerInteractTrigger = new _PlayerInteract();

    /**
     * 原版{@code Gui#renderCrosshair}攻击冷却条是在屏幕中心往下 9 (1920x1080p下36像素)
     */
    private static final int Y_OFFSET = 9;
    /**
     * 在准心下方显示射手操作消息
     */
    private void _renderOperationMessage(IRenderGuiEvent event) {
        var graphics = event.getGuiGraphics();

        { // 显示操作消息
            Font font = Minecraft.getInstance().font;
            int xOffset = graphics.guiWidth() / 2;
            int yOffset = graphics.guiHeight() / 2 + Y_OFFSET;
            ClientGuiUtils.Graphics.drawCenteredText(graphics,
                    font,
                    this.currentMessage,
                    xOffset, yOffset,
                    Color64._FFFFFF.getRGB());
        }
    }

    @ApiStatus.Internal
    public void setOperationMessage(@Nullable Component message, int priority) {
        if (message != null) {
            // 当前没有message 或 高优先级
            if (this.currentMessage == null || this.currentPriority <= priority) {
                this.currentMessage = message;
                this.currentPriority = priority;
            }
        } else {
            // priority对应的时候可以设置null
            if (this.currentPriority == 0 || this.currentPriority == priority) {
                this.currentMessage = null;
                this.currentPriority = 0;
            }
        }
    }

    // --------IOverlaySubManager--------

    public static final String _MANAGER_NAME = String.format("%s:%s", CustomGun.MOD_ID, DefaultShooterOperation.class.getSimpleName());
    @Override public String getManagerName() {
        return _MANAGER_NAME;
    }

    @Override public String getOverlayName() {
        return BuiltinOverlayType.SHOOTER_OPERATION.getCategoryName();
    }

    @Override public boolean registerOverlayEvent() {
        ICustomEventRegister eventRegister = CustomGun.getEventRegister();
        eventRegister.register(INSTANCE, EventType.RENDER_GUI_EVENT);
        this.playerInteractTrigger.register(eventRegister);
        return true;
    }
    @Override public boolean unregisterOverlayEvent() {
        ICustomEventRegister eventRegister = CustomGun.getEventRegister();
        eventRegister.unregister(INSTANCE, EventType.RENDER_GUI_EVENT);
        this.playerInteractTrigger.unregister(eventRegister);
        return true;
    }

    @Override public boolean isEnabled() {
        return RenderConfig.ENABLE_SHOOTER_OPERATION_HUD.get();
    }
    @Override public void setEnabled(boolean enabled) {
        RenderConfig.ENABLE_SHOOTER_OPERATION_HUD.set(enabled);
    }
}

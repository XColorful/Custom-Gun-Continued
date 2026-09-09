package dev.xcolorful.customgun.client.gui.overlay.gunhud;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.client.CustomGunClient;
import dev.xcolorful.customgun.client.api.event.IClientTickEvent;
import dev.xcolorful.customgun.client.api.event.IPrepareRenderGuiEvent;
import dev.xcolorful.customgun.client.api.gui.overlay.BuiltinOverlayType;
import dev.xcolorful.customgun.client.api.gui.overlay.IOverlaySubManager;
import dev.xcolorful.customgun.client.config.RenderConfig;
import dev.xcolorful.customgun.client.util.ClientGuiUtils;
import dev.xcolorful.customgun.core.api.event.*;
import dev.xcolorful.customgun.core.api.item.IGun;
import dev.xcolorful.customgun.core.api.item.gun.IGunGetter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * 用action bar来显示 {当前子弹} {备弹(如有)}
 * <ul>
 *     <li>每次设置{@link ClientGuiUtils#setActionBarMessage}固定60ticks，40tick开始淡出，持续保活则至少要每39ticks刷新一次</li>
 *     <li>备选刷新: 周期显示, 转视角, 移动</li>
 * </ul>
 */
public class DefaultGunHud implements IOverlaySubManager, IEventHandler {
    public static final DefaultGunHud INSTANCE = new DefaultGunHud();

    protected DefaultGunHud() {}

    public static void init() {
        CustomGunClient.getOverlayManager().register(INSTANCE);
    }

    @Override public String getEventHandlerName() {
        return this.getClass().getName();
    }
    @Override
    public void handleEvent(EventType eventType, IEvent event) {
        switch (eventType) {
            case CLIENT_TICK_EVENT -> {
                onClientTick((IClientTickEvent) event);
            }
            case PREPARE_RENDER_GUI_EVENT -> {
                onPrepareRenderGui((IPrepareRenderGuiEvent) event);
            }
            default -> {
                onReceiveWrongEvent(eventType);
            }
        }
    }

    private void onClientTick(IClientTickEvent event) {
        this._check_tickHud(event);
        this.forceRefresh = false;
    }

    private void onPrepareRenderGui(IPrepareRenderGuiEvent event) {
        if (this.state.isMessageConsumed()) return;

        this._setActionBarMessage(Minecraft.getInstance().gui, this.state.getPendingMessage());
    }

    private boolean shouldKeepDisplay() {
        return RenderConfig.KEEP_DISPLAY_GUN_HUD.get();
    }
    private boolean shouldForceDisplay() {
        return RenderConfig.FORCE_DISPLAY_GUN_HUD.get();
    }

    private boolean isEnabledBefore = false;
    private boolean forceRefresh = false;
    private final _GunHudState state = new _GunHudState();
    private final _GunHudTrigger trigger = _GunHudTrigger.INSTANCE;

    /**
     * 检查状态并tick 枪械 HUD
     */
    private void _check_tickHud(IClientTickEvent event) {
        boolean isEnabled = this.isEnabled(); {
            // 检查是否启用
            if (!isEnabled) {
                if (isEnabledBefore) this.state.reset();
                this.isEnabledBefore = false;
                return;
            } else {
                this.isEnabledBefore = true;
            }
        }

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer localPlayer = mc.player;
        if (localPlayer == null) {
            this.state.reset();
            return;
        }

        // 检查手持枪械
        ItemStack gunItem = localPlayer.getMainHandItem();
        @Nullable IGun iGun = IGunGetter.fromItemStack(gunItem);
        if (iGun == null) {
            this.state.reset();
            return;
        }

        if ( // 刷新
                // 强制刷新
                this.forceRefresh
                // 配置持续显示
                || this.shouldKeepDisplay()) {
            this.state.setPendingMessage(_GunHudBuilder.getMessage(localPlayer, iGun, gunItem));
        }
    }

    private void _setActionBarMessage(Gui gui, Component message) {
        @Nullable Component currentMessage = ClientGuiUtils.getActionBarMessage(gui);
        // 强制覆盖 或 是可被覆盖的消息
        if (this.shouldForceDisplay() || this.state.isOverwritableMessage(currentMessage)) {
            ClientGuiUtils.setActionBarMessage(gui, message, false);
        }
        this.state.setConsumed();
    }

    protected void setForceRefresh() {
        this.forceRefresh = true;
    }

    // --------IOverlaySubManager--------

    public static final String _MANAGER_NAME = String.format("%s:%s", CustomGun.MOD_ID, DefaultGunHud.class.getSimpleName());
    @Override public String getManagerName() {
        return _MANAGER_NAME;
    }

    @Override public String getOverlayName() {
        return BuiltinOverlayType.GUN_HUD.getCategoryName();
    }

    @Override public boolean registerOverlayEvent() {
        ICustomEventRegister eventRegister = CustomGun.getEventRegister();
        eventRegister.register(INSTANCE, EventType.CLIENT_TICK_EVENT, EventPriority.LOWEST, true);
        eventRegister.register(INSTANCE, EventType.PREPARE_RENDER_GUI_EVENT);
        this.trigger.register(eventRegister);
        return true;
    }
    @Override public boolean unregisterOverlayEvent() {
        ICustomEventRegister eventRegister = CustomGun.getEventRegister();
        eventRegister.unregister(INSTANCE, EventType.CLIENT_TICK_EVENT, EventPriority.LOWEST, true);
        eventRegister.unregister(INSTANCE, EventType.PREPARE_RENDER_GUI_EVENT);
        this.trigger.unregister(eventRegister);
        return true;
    }

    @Override public boolean isEnabled() {
        return RenderConfig.ENABLE_GUN_HUD.get();
    }
    @Override public void setEnabled(boolean enabled) {
        RenderConfig.ENABLE_GUN_HUD.set(enabled);
    }
}

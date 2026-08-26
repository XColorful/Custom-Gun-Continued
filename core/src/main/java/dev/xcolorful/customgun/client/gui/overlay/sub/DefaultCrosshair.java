package dev.xcolorful.customgun.client.gui.overlay.sub;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.client.CustomGunClient;
import dev.xcolorful.customgun.client.api.entity.shooter.ILocalShooterGetter;
import dev.xcolorful.customgun.client.api.event.IPrepareRenderOverlayEvent;
import dev.xcolorful.customgun.client.api.gui.GuiSize;
import dev.xcolorful.customgun.client.api.gui.overlay.BuiltinOverlayType;
import dev.xcolorful.customgun.client.api.gui.overlay.IOverlaySubManager;
import dev.xcolorful.customgun.client.api.minecraft.texture.CustomTexture;
import dev.xcolorful.customgun.client.compat.shouldersurfing.ShoulderSurfingCompat;
import dev.xcolorful.customgun.client.config.RenderConfig;
import dev.xcolorful.customgun.client.util.ClientGuiUtils;
import dev.xcolorful.customgun.client.util.ClientInputUtils;
import dev.xcolorful.customgun.client.util.ClientRenderHelper;
import dev.xcolorful.customgun.core.api.event.EventType;
import dev.xcolorful.customgun.core.api.event.ICustomEventRegister;
import dev.xcolorful.customgun.core.api.event.IEvent;
import dev.xcolorful.customgun.core.api.event.IEventHandler;
import dev.xcolorful.customgun.core.api.item.gun.IGunGetter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.jetbrains.annotations.ApiStatus;

/**
 * 模组内置的准心渲染
 * 作为给扩展模组的{@link IOverlaySubManager}示范
 */
public class DefaultCrosshair implements IOverlaySubManager, IEventHandler {
    public static final DefaultCrosshair INSTANCE = new DefaultCrosshair();

    protected DefaultCrosshair() {}

    public static void init() {
        CustomGunClient.getOverlayManager().register(INSTANCE);
    }

    @Override public String getEventHandlerName() {
        return this.getClass().getName();
    }
    @Override
    public void handleEvent(EventType eventType, IEvent event) {
        if (eventType == EventType.PREPARE_RENDER_OVERLAY_EVENT) {
            onPrepareRenderOverlay((IPrepareRenderOverlayEvent) event);
        } else {
            onReceiveWrongEvent(eventType);
        }
    }

    /**
     * @return 是否处于不应该渲染overlay的状态
     */
    private boolean _shouldForceDisableOverlay() {
        Minecraft mc = Minecraft.getInstance();
        if ( // 状态检查
                // 按F1 (不显示GUI)
                ClientGuiUtils.isGuiHidden(mc)
                // 当前不是第一人称
                || !mc.options.getCameraType().isFirstPerson()
                // 当前有打开任意screen
                || ClientGuiUtils.getCurrentScreen(mc) != null
                // 不在游戏内
                || !ClientInputUtils.isInGameWorld()
                // 旁观模式
                || mc.player == null || mc.player.isSpectator()
        ) return true;

        return false;
    }

    /**
     * 区别于{@link GuiSize.Vanilla#CROSSHAIR_HEIGHT}，使用偶数来画在屏幕正中心
     */
    @ApiStatus.Internal
    public static final int CROSSHAIR_SIZE = 64 / GuiSize._sizeToPixelRatio; // 在1920x1080p屏幕中间绘制64x64像素
    private void onPrepareRenderOverlay(IPrepareRenderOverlayEvent event) {
        LocalPlayer localPlayer = Minecraft.getInstance().player;
        if (localPlayer == null) return;

        if ( // 状态检查
                // 不需要渲染的状态
                _shouldForceDisableOverlay()
                // 不是需要接管的overlay
                || !this.getOverlayName().equals(event.getRegistryLocation().getPath())
                // 不接管原版准心
                || !RenderConfig.REPLACE_VANILLA_CROSSHAIR.get()
        ) return;

        if ( // 特殊检查
                // 手持枪械检查
                IGunGetter.fromMainHand(localPlayer) == null
                // 让给越肩视角渲染
                || ShoulderSurfingCompat.showCrosshair()
        ) return;

        // --------接管原版渲染--------
        event.setCanceled(true);

        if ( // 隐藏渲染检查
                // 瞄准状态下隐藏
                ILocalShooterGetter.fromLocalPlayer(localPlayer).cgc$getRenderAimingProgress(event.getPartialTick()) > 0.001
        ) return;

        this._renderCrosshair(event);
    }
    /**
     * 绘制原版反色效果的准心
     */
    private void _renderCrosshair(IPrepareRenderOverlayEvent event) {
        var graphics = event.getGuiGraphics();

        ClientRenderHelper.GL._enableCrosshair(); {
            int textureWidth;
            int textureHeight; {
                CustomTexture texture = CustomTexture.CROSSHAIR;
                textureWidth = texture.getWidth();
                textureHeight = texture.getHeight();
            }

            { // 渲染准心
                int guiWidth = graphics.guiWidth();
                int guiHeight = graphics.guiHeight();

                // 屏幕宽度双数，准心材质长宽也是双数
                int startX = (guiWidth - CROSSHAIR_SIZE) / 2;
                int startY = (guiHeight - CROSSHAIR_SIZE) / 2;
                int width = CROSSHAIR_SIZE;
                int height = CROSSHAIR_SIZE;
                int uOffset = 0;
                int vOffset = 0;
                int uWidth = textureWidth;
                int vHeight = textureHeight;
                ClientGuiUtils.Graphics.blitCrosshairTexture(graphics,
                        RenderConfig.CROSSHAIR_TYPE.get().getTextureLocation(),
                        startX, startY,
                        width, height,
                        uOffset, vOffset,
                        uWidth, vHeight,
                        textureWidth, textureHeight);
            }
        }
        ClientRenderHelper.GL._disableCrosshair();
    }

    // --------IOverlaySubManager--------

    public static final String _MANAGER_NAME = String.format("%s:%s", CustomGun.MOD_ID, DefaultCrosshair.class.getSimpleName());
    @Override public String getManagerName() {
        return _MANAGER_NAME;
    }

    @Override public String getOverlayName() {
        return BuiltinOverlayType.CROSSHAIR.getCategoryName();
    }

    @Override public boolean registerOverlayEvent() {
        ICustomEventRegister eventRegister = CustomGun.getEventRegister();
        return eventRegister.register(INSTANCE, EventType.PREPARE_RENDER_OVERLAY_EVENT);
    }
    @Override public boolean unregisterOverlayEvent() {
        ICustomEventRegister eventRegister = CustomGun.getEventRegister();
        return eventRegister.unregister(INSTANCE, EventType.PREPARE_RENDER_OVERLAY_EVENT);
    }

    @Override public boolean isEnabled() {
        return RenderConfig.REPLACE_VANILLA_CROSSHAIR.get();
    }
    @Override public void setEnabled(boolean enabled) {
        RenderConfig.REPLACE_VANILLA_CROSSHAIR.set(enabled);
    }
}

package dev.xcolorful.customgun.client.input.shooter;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.client.api.entity.ILocalShooter;
import dev.xcolorful.customgun.client.api.entity.shooter.ILocalShooterGetter;
import dev.xcolorful.customgun.client.api.event.IClientTickEvent;
import dev.xcolorful.customgun.client.api.event.IInputKeyEvent;
import dev.xcolorful.customgun.client.api.event.IMouseButtonEvent;
import dev.xcolorful.customgun.client.api.event.IMouseScrollingEvent;
import dev.xcolorful.customgun.client.api.input.*;
import dev.xcolorful.customgun.client.api.minecraft.input.CustomInputKey;
import dev.xcolorful.customgun.client.api.resource.ClientResourceApi;
import dev.xcolorful.customgun.client.config.ZoomConfig;
import dev.xcolorful.customgun.client.init.registry.ClientInputCategory;
import dev.xcolorful.customgun.client.input.InputKey;
import dev.xcolorful.customgun.client.resource.assets.display.AttachmentDisplay;
import dev.xcolorful.customgun.client.resource.instance.data.ClientAttachmentIndexInstance;
import dev.xcolorful.customgun.client.util.ClientInputUtils;
import dev.xcolorful.customgun.core.api.event.EventPriority;
import dev.xcolorful.customgun.core.api.event.EventType;
import dev.xcolorful.customgun.core.api.event.ICustomEventRegister;
import dev.xcolorful.customgun.core.api.event.IEvent;
import dev.xcolorful.customgun.core.api.event.IEventHandler;
import dev.xcolorful.customgun.core.api.item.IGun;
import dev.xcolorful.customgun.core.api.item.attachment.AttachmentCategory;
import dev.xcolorful.customgun.core.api.item.attachment.AttachmentNBTAccessor;
import dev.xcolorful.customgun.core.api.item.gun.IGunGetter;
import dev.xcolorful.customgun.core.developer.PlannedRefactor;
import dev.xcolorful.customgun.core.entity.shooter.LivingShooterAim;
import dev.xcolorful.customgun.core.network.message.shooter.C2SMessageShooterZoom;
import dev.xcolorful.customgun.core.util.SendUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class ZoomKey extends InputKey implements IEventHandler {

    private static final class ZoomKeyHolder {
        private static final ZoomKey INSTANCE = new ZoomKey();
    }

    public static ZoomKey get() {
        return ZoomKeyHolder.INSTANCE;
    }

    private ZoomKey() {
        super(CustomInputKey.ZOOM);
    }
    @Override protected IKeyMapping createKeyMapping(IKeyMapping.Creator creator) {
        return creator.create(this.key.getCategoryLang().getString(),
                IKeyConflictContext.Type.IN_GAME,
                IKeyModifier.Type.NONE,
                ClientInputUtils.KeyType.keyboard(),
                ClientInputUtils.KeyCode._KEY_V(),
                ClientInputCategory.SHOOTER);
    }

    public static final String _MANAGER_NAME = String.format("%s:%s", CustomGun.MOD_ID, ZoomKey.class.getSimpleName());
    @Override public String getManagerName() {
        return _MANAGER_NAME;
    }

    @Override
    public boolean registerEventHandler() {
        ICustomEventRegister customEventRegister = CustomGun.getEventRegister();
        customEventRegister.register(this, EventType.MOUSE_SCROLLING_EVENT, EventPriority.HIGH, false);
        customEventRegister.register(this, EventType.CLIENT_TICK_EVENT, EventPriority.NORMAL, false);
        return true;
    }
    @Override
    public boolean unregisterEventHandler() {
        ICustomEventRegister customEventRegister = CustomGun.getEventRegister();
        customEventRegister.unregister(this, EventType.MOUSE_SCROLLING_EVENT, EventPriority.HIGH, false);
        customEventRegister.unregister(this, EventType.CLIENT_TICK_EVENT, EventPriority.NORMAL, false);
        this.mouseScrollingQueue.clear();
        this.stackedScroll = 0;
        return true;
    }

    @Override public String getEventHandlerName() {
        return this.getClass().getName();
    }
    @Override
    public void handleEvent(EventType eventType, IEvent event) {
        switch (eventType) {
            case MOUSE_SCROLLING_EVENT -> {
                onMouseScrolling((IMouseScrollingEvent) event);
            }
            case CLIENT_TICK_EVENT -> {
                tickScrollingZoom((IClientTickEvent) event);
            }
            default -> {
                onReceiveWrongEvent(eventType);
            }
        }
    }

    // --------IInputHandler--------

    @Override
    public void onKeyInput(IInputKeyManager inputKeyManager, IInputKeyEvent event) {
        this.onZoomKeyInput(event.getAction());
    }
    @Override
    public void onMouseInput(IInputKeyManager inputKeyManager, IMouseButtonEvent event) {
        this.onZoomKeyInput(event.getAction());
    }
    private void onZoomKeyInput(int action) {
        if (InputAction.of(action) != InputAction.PRESS) return;

        if (!ClientInputUtils.isGameplayFocused()) return; // 不在焦点

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null
                || player.isSpectator() // 旁观模式
        ) return;

        ILocalShooter localShooter = ILocalShooterGetter.fromLocalPlayer(player);
        if (!localShooter.cgc$isAim()) return; // 不在瞄准

        ItemStack gunItem = player.getMainHandItem();
        @Nullable IGun iGun = IGunGetter.fromItemStack(gunItem);
        if (iGun == null) return; // 主手没枪

        _doZoom(iGun, gunItem, +1, true);
    }

    private final Queue<Double> mouseScrollingQueue = new ConcurrentLinkedQueue<>();
    private double stackedScroll = 0;

    /**
     * @return 是否处于可用鼠标滚轮缩放的状态
     */
    private boolean _canScrollingZoom(@NotNull LocalPlayer player) {
        if (!ZoomConfig.ENABLE_SCROLLING_ZOOM.get()) return false;

        if (!ClientInputUtils.isGameplayFocused()) return false; // 不在焦点

        if (player.isSpectator()) return false; // 旁观模式

        ILocalShooter localShooter = ILocalShooterGetter.fromLocalPlayer(player);
        if (!localShooter.cgc$isAim()) return false; // 不在瞄准

        return true;
    }
    private void onMouseScrolling(IMouseScrollingEvent event) {
        double scrollDeltaY = event.getScrollDeltaY();
        if (scrollDeltaY == 0) return;

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || !_canScrollingZoom(player)) return;

        ItemStack gunItem = player.getMainHandItem();
        @Nullable IGun iGun = IGunGetter.fromItemStack(gunItem);
        if (iGun == null) return; // 主手没枪

        event.setCanceled(true); // 拦截滚轮，防止同时切换快捷栏物品

        this.mouseScrollingQueue.offer(scrollDeltaY);
    }
    private void tickScrollingZoom(IClientTickEvent event) {
        // 消费输入队列
        Double scrollDeltaY;
        while ((scrollDeltaY = this.mouseScrollingQueue.poll()) != null) {
            this.stackedScroll += scrollDeltaY;
        }

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || !_canScrollingZoom(player)) {
            this.stackedScroll = 0; // 离开缩放状态后不保留累计
            return;
        }

        ItemStack gunItem = player.getMainHandItem();
        @Nullable IGun iGun = IGunGetter.fromItemStack(gunItem);
        if (iGun == null) { // 主手没枪
            this.stackedScroll = 0;
            return;
        }

        if (Math.abs(this.stackedScroll) < threshold) return;

        int offset = (int) (this.stackedScroll / threshold);
        this.stackedScroll -= offset * threshold;
        _doZoom(iGun, gunItem, offset, ZoomConfig.SCROLLING_ZOOM_CYCLE.get());
    }
    private static double threshold = 1; // 临时用

    /**
     * 对应 {@link LivingShooterAim#zoom}
     * @param offset scopeViewIndex偏移量，{@code +1}则切换到下一个
     * @param cycle 是否循环切换
     */
    private void _doZoom(IGun iGun, ItemStack gunItem, int offset, boolean cycle) {
        final var SCOPE = AttachmentCategory.SCOPE;
        @Nullable CompoundTag scopeCustomDataTag = iGun.getAttachmentCustomDataTag(gunItem, SCOPE);
        if (scopeCustomDataTag == null) return;

        var scopeLocation = iGun.getAttachmentLocation(gunItem, SCOPE);
        @Nullable ClientAttachmentIndexInstance clientAttachmentIndexInstance = ClientResourceApi.getClientAttachmentIndexInstance(scopeLocation);
        if (clientAttachmentIndexInstance == null) return;

        AttachmentDisplay attachmentDisplay = clientAttachmentIndexInstance.getAttachmentDisplay();

        final int fromScopeViewIndex = AttachmentNBTAccessor.INSTANCE.getScopeViewIndex(scopeCustomDataTag);
        int scopeViewIndex = fromScopeViewIndex;
        if (scopeViewIndex < 0) {
            scopeViewIndex = 0;
        }

        if (PlannedRefactor.MOVE_SCOPE_VIEW_INDEX_TO_CORE) {} // 放在资源包，服务端就没法做校验了
        int @Nullable [] scopeViewIndexList = attachmentDisplay.getScopeViewIndex();
        if (scopeViewIndexList == null) return;

        /**
         * {@link ClientAttachmentIndexInstance#checkScopeViewIndex}不保证列表长度不为0
         */
        if (scopeViewIndexList.length == 0) return;

        int toScopeViewIndex; {
            toScopeViewIndex = scopeViewIndex + offset;
            if (cycle) { // 循环
                toScopeViewIndex = (toScopeViewIndex % scopeViewIndexList.length + scopeViewIndexList.length) % scopeViewIndexList.length;
            } else { // 不循环，限制到列表上下界
                toScopeViewIndex = Math.max(0, Math.min(toScopeViewIndex, scopeViewIndexList.length - 1));
            }
        }
        if (fromScopeViewIndex == toScopeViewIndex) return;

        SendUtils.sendMessageToServer(new C2SMessageShooterZoom(fromScopeViewIndex, toScopeViewIndex));
    }

    // --------Deprecated--------

    /**
     * Controllable联动的写法要改, 至少肯定不是写在这里
     */
    @Deprecated(forRemoval = true)
    public static boolean onZoomControllerPress(boolean isPress) {
        return false;
    }
}

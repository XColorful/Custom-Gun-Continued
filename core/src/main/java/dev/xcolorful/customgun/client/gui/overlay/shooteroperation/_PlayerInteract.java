package dev.xcolorful.customgun.client.gui.overlay.shooteroperation;

import dev.xcolorful.customgun.client.api.event.IClientTickEvent;
import dev.xcolorful.customgun.client.api.input.IKeyMapping;
import dev.xcolorful.customgun.client.config.sync.InteractFilterData;
import dev.xcolorful.customgun.client.input.InputKeyManager;
import dev.xcolorful.customgun.client.input.player.InteractKey;
import dev.xcolorful.customgun.client.util.ClientInputUtils;
import dev.xcolorful.customgun.core.api.event.*;
import dev.xcolorful.customgun.core.api.item.gun.IGunGetter;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @see InteractKey
 */
public class _PlayerInteract implements IEventHandler {
    protected static final _PlayerInteract INSTANCE = new _PlayerInteract();
    private static final int OPERATION_PRIORITY = 5;

    protected _PlayerInteract() {}

    protected void register(ICustomEventRegister eventRegister) {
        eventRegister.register(INSTANCE, EventType.CLIENT_TICK_EVENT, EventPriority.LOWEST, true);
    }
    protected void unregister(ICustomEventRegister eventRegister) {
        eventRegister.unregister(INSTANCE, EventType.CLIENT_TICK_EVENT, EventPriority.LOWEST, true);
    }

    @Override public String getEventHandlerName() {
        return this.getClass().getName();
    }
    @Override public void handleEvent(EventType eventType, IEvent event) {
        if (eventType == EventType.CLIENT_TICK_EVENT) {
            onClientTick((IClientTickEvent) event);
        } else {
            onReceiveWrongEvent(eventType);
        }
    }

    private void onClientTick(IClientTickEvent event) {
        @Nullable Component message = this._checkOperation() ? this._buildMessage() : null;
        DefaultShooterOperation.INSTANCE.setOperationMessage(message, OPERATION_PRIORITY);
    }

    /**
     * 跟 {@link InteractKey} 强耦合 (目前{@link InputKeyManager}没有 O(1) getter)
     */
    @ApiStatus.Internal
    private @NotNull Component _buildMessage() {
        /*
        [{按键}] {交互}
         */
        IKeyMapping iKeyMapping = InteractKey.get().getKeyMapping();
        Component inputKey = iKeyMapping.get().getTranslatedKeyMessage()
                .copy();
        return Component.literal("[").withStyle(ChatFormatting.WHITE)
                .append(Component.empty().append(inputKey).withStyle(ChatFormatting.AQUA))
                .append("] ").withStyle(ChatFormatting.WHITE)
                .append(Component.translatable("key.customgun.interact").withStyle(ChatFormatting.AQUA));
    }

    /**
     * @see InteractKey#onInteractKeyInput
     */
    private boolean _checkOperation() {
        if (!ClientInputUtils.isGameplayFocused()) return false; // 不在焦点

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (IGunGetter.fromMainHand(player) == null // 主手没枪
                || player.isSpectator() // 旁观模式
        ) return false;

        HitResult hitResult = mc.hitResult;
        if (hitResult == null) return false;

        // 方块交互
        if (hitResult instanceof BlockHitResult blockHitResult) {
            BlockPos blockPos = blockHitResult.getBlockPos();
            BlockState blockState = player.level().getBlockState(blockPos);
            if (InteractFilterData.canInteract(blockState)) {
                return true;
            }
        }
        // 实体交互
        else if (hitResult instanceof EntityHitResult entityHitResult) {
            Entity entity = entityHitResult.getEntity();
            if (InteractFilterData.canInteract(entity)) {
                return true;
            }
        }

        return false;
    }
}

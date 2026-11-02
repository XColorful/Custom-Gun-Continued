package dev.xcolorful.customgun.core.api.event.shooter;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.core.api.common.McLogicalSide;
import dev.xcolorful.customgun.core.api.entity.ILivingShooter;
import dev.xcolorful.customgun.core.api.event.CustomEventType;
import dev.xcolorful.customgun.core.api.event.ICustomEvent;
import dev.xcolorful.customgun.core.api.event.ICustomEventHandler;
import dev.xcolorful.customgun.core.api.event.gun.IGunEvent;
import dev.xcolorful.customgun.core.api.item.IGun;
import dev.xcolorful.customgun.core.event.EventDispatcher;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 射手生物{@link ILivingShooter} 切换瞄准状态事件
 */
public final class ShooterSwitchAimEvent extends LivingShooterEvent implements IGunEvent {

    private final @Nullable IGun iGun;
    private final @NotNull ItemStack gunItem;

    private boolean isAiming;

    public ShooterSwitchAimEvent(McLogicalSide logicalSide,
                                 @Nullable ILivingShooter iLivingShooter, @Nullable LivingEntity livingShooter,
                                 @Nullable IGun iGun, @NotNull ItemStack gunItem,
                                 boolean isAiming) {
        super(logicalSide, iLivingShooter, livingShooter);
        this.iGun = iGun;
        this.gunItem = gunItem;
        this.isAiming = isAiming;
    }
    @Override public CustomEventType getEventType() {
        return CustomEventType.SHOOTER_SWITCH_AIM_EVENT;
    }

    public @Nullable IGun getIGun() {
        return this.iGun;
    }
    public @NotNull ItemStack getGunItem() {
        return this.gunItem;
    }

    public boolean getIsAiming() {
        return this.isAiming;
    }
    public void setIsAiming(boolean isAiming) {
        this.isAiming = isAiming;
    }

    @Override public String getTextName() {
        return this.livingShooter != null ? this.livingShooter.getName().getString()
                : "ShooterSwitchAimEvent";
    }
    @Override public Component getDisplayName() {
        return this.livingShooter != null ? this.livingShooter.getDisplayName()
                : Component.literal(getTextName());
    }

    private static final EventDispatcher<ICustomEventHandler, ICustomEvent, CustomEventType> _EVENT_DISPATCHER = CustomGun.getEventPoster().getEventDispatcher(ShooterSwitchAimEvent.class);
    @Override public @NotNull EventDispatcher<ICustomEventHandler, ICustomEvent, CustomEventType> getEventDispatcher() {
        return _EVENT_DISPATCHER;
    }
}

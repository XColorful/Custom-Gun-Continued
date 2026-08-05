package dev.xcolorful.customgun.forge.event;

import dev.xcolorful.customgun.core.api.event.EventType;
import dev.xcolorful.customgun.core.api.event.ILivingKnockbackEvent;
import dev.xcolorful.customgun.core.api.minecraft.CommandLevel;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.eventbus.api.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ForgeLivingKnockbackEvent extends ForgeEvent implements ILivingKnockbackEvent {

    protected LivingKnockBackEvent livingKnockBackEvent;

    public ForgeLivingKnockbackEvent(Event event) {
        super(event);
        if (event instanceof LivingKnockBackEvent eventIn) {
            this.livingKnockBackEvent = eventIn;
        } else {
            throw new RuntimeException("Expected LivingKnockBackEvent but received: " + event.getClass().getName());
        }
    }
    @Override public EventType getType() {
        return EventType.LIVING_KNOCKBACK_EVENT;
    }

    @Override
    public LivingEntity getEntity() {
        return livingKnockBackEvent.getEntity();
    }

    @Override
    public float getKnockbackStrength() {
        return livingKnockBackEvent.getStrength();
    }
    @Override
    public double getRatioX() {
        return livingKnockBackEvent.getRatioX();
    }
    @Override
    public double getRatioZ() {
        return livingKnockBackEvent.getRatioZ();
    }

    @Override
    public float getOriginalKnockbackStrength() {
        return livingKnockBackEvent.getOriginalStrength();
    }
    @Override
    public double getOriginalRatioX() {
        return livingKnockBackEvent.getOriginalRatioX();
    }
    @Override
    public double getOriginalRatioZ() {
        return livingKnockBackEvent.getOriginalRatioZ();
    }

    @Override public void setKnockbackStrength(float strength) {
        livingKnockBackEvent.setStrength(strength);
    }
    @Override public void setRatioX(double ratioX) {
        livingKnockBackEvent.setRatioX(ratioX);
    }
    @Override public void setRatioZ(double ratioZ) {
        livingKnockBackEvent.setRatioZ(ratioZ);
    }

    @Override
    public @Nullable CommandSourceStack createCommandSourceStack(@Nullable CommandSource source) {
        @NotNull Entity entity = this.getEntity();
        Level level = entity.level();
        if (level != null && level.isClientSide()) return null;
        return new CommandSourceStack(
                source != null ? source : CommandSource.NULL,
                entity.position(),
                entity.getRotationVector(),
                (ServerLevel) level,
                CommandLevel.permission(4),
                this.getTextName(),
                this.getDisplayName(),
                level.getServer(),
                entity
        );
    }

    @Override public String getTextName() {
        return this.getEntity().getName().getString();
    }
    @Override public Component getDisplayName() {
        return this.getEntity().getDisplayName();
    }
}
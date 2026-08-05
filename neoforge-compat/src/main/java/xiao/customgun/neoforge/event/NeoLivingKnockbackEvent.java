package dev.xcolorful.customgun.neoforge.event;

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
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NeoLivingKnockbackEvent extends NeoEvent implements ILivingKnockbackEvent {

    protected LivingKnockBackEvent livingKnockbackEvent;

    public NeoLivingKnockbackEvent(Event event) {
        super(event);
        if (event instanceof LivingKnockBackEvent eventIn) {
            this.livingKnockbackEvent = eventIn;
        } else {
            throw new RuntimeException("Expected LivingKnockbackEvent but received: " + event.getClass().getName());
        }
    }
    @Override public EventType getType() {
        return EventType.LIVING_KNOCKBACK_EVENT;
    }

    @Override
    public LivingEntity getEntity() {
        return livingKnockbackEvent.getEntity();
    }

    @Override public float getKnockbackStrength() {
        return livingKnockbackEvent.getStrength();
    }
    @Override public double getRatioX() {
        return livingKnockbackEvent.getRatioX();
    }
    @Override public double getRatioZ() {
        return livingKnockbackEvent.getRatioZ();
    }

    @Override public float getOriginalKnockbackStrength() {
        return livingKnockbackEvent.getOriginalStrength();
    }
    @Override public double getOriginalRatioX() {
        return livingKnockbackEvent.getOriginalRatioX();
    }
    @Override public double getOriginalRatioZ() {
        return livingKnockbackEvent.getOriginalRatioZ();
    }

    @Override public void setKnockbackStrength(float strength) {
        livingKnockbackEvent.setStrength(strength);
    }
    @Override public void setRatioX(double ratioX) {
        livingKnockbackEvent.setRatioX(ratioX);
    }
    @Override public void setRatioZ(double ratioZ) {
        livingKnockbackEvent.setRatioZ(ratioZ);
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
package dev.xcolorful.customgun.forgeclient.input;

import com.mojang.blaze3d.platform.InputConstants;
import dev.xcolorful.customgun.client.api.input.IKeyConflictContext;
import dev.xcolorful.customgun.client.api.input.IKeyModifier;
import net.minecraftforge.client.settings.KeyModifier;
import org.jetbrains.annotations.Nullable;

public abstract class ForgeKeyModifier implements IKeyModifier {

    public static ForgeKeyModifier convert(Type type) {
        return switch (type) {
            case CONTROL -> Control.INSTANCE;
            case SHIFT -> Shift.INSTANCE;
            case ALT -> Alt.INSTANCE;
            case NONE -> None.INSTANCE;
        };
    }

    private final KeyModifier keyModifier;

    private ForgeKeyModifier(KeyModifier keyModifier) {
        this.keyModifier = keyModifier;
    }
    @Override public boolean matches(InputConstants.Key key) {
        return this.keyModifier.matches(key);
    }
    @Override public boolean isActive(@Nullable IKeyConflictContext conflictContext) {
        if (conflictContext == null) return keyModifier.isActive(null);
        return this.keyModifier.isActive((net.minecraftforge.client.settings.IKeyConflictContext) conflictContext.getKeyConflictContext());
    }
    @Override public KeyModifier getKeyModifier() {
        return this.keyModifier;
    }

    private static class Control extends ForgeKeyModifier {
        public static final Control INSTANCE = new Control();
        public Control() {
            super(KeyModifier.CONTROL);
        }
    }
    private static class Shift extends ForgeKeyModifier {
        public static final Shift INSTANCE = new Shift();
        public Shift() {
            super(KeyModifier.SHIFT);
        }
    }
    private static class Alt extends ForgeKeyModifier {
        public static final Alt INSTANCE = new Alt();
        public Alt() {
            super(KeyModifier.ALT);
        }
    }
    private static class None extends ForgeKeyModifier {
        public static final None INSTANCE = new None();
        public None() {
            super(KeyModifier.NONE);
        }
    }
}

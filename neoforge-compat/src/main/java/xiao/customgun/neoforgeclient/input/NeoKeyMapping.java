package xiao.customgun.neoforgeclient.input;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;
import xiao.customgun.client.api.input.IKeyConflictContext;
import xiao.customgun.client.api.input.IKeyMapping;
import xiao.customgun.client.api.input.IKeyModifier;

public class NeoKeyMapping implements IKeyMapping {

    private final KeyMapping keyMapping;

    private final String name;
    private final IKeyConflictContext keyConflictContext;
    private final IKeyModifier keyModifier;
    private final InputConstants.Type inputType;
    private final int keyCode;
    private final String category;

    public NeoKeyMapping(String name,
                        IKeyConflictContext keyConflictContext,
                        IKeyModifier keyModifier,
                        InputConstants.Type inputType,
                        int keyCode,
                        String category) {
        this.name = name;
        this.keyConflictContext = keyConflictContext;
        this.keyModifier = keyModifier;
        this.inputType = inputType;
        this.keyCode = keyCode;
        this.category = category;
        this.keyMapping = new KeyMapping(this.name,
                (KeyConflictContext) this.keyConflictContext.getKeyConflictContext(), (KeyModifier) this.keyModifier.getKeyModifier(),
                this.inputType, this.keyCode,
                this.category);
    }

    @Override
    public KeyMapping get() {return this.keyMapping;
    }

    @Override public String getName() {
        return this.name;
    }
    @Override public IKeyConflictContext getKeyConflictContext() {
        return this.keyConflictContext;
    }
    @Override public IKeyModifier getKeyModifier() {
        return this.keyModifier;
    }
    @Override public InputConstants.Type getInputType() {
        return this.inputType;
    }
    @Override public int getKeyCode() {
        return this.keyCode;
    }
    @Override public String getCategory() {
        return this.category;
    }

    public static class Creator implements IKeyMapping.Creator {

        @Override
        public NeoKeyMapping create(String name,
                                   IKeyConflictContext.Type contextType, IKeyModifier.Type modifierType,
                                   InputConstants.Type inputType, int keyCode,
                                   String category) {
            return new NeoKeyMapping(name,
                    NeoKeyConflictContext.convert(contextType), NeoKeyModifier.convert(modifierType),
                    inputType, keyCode,
                    category);
        }
    }
}

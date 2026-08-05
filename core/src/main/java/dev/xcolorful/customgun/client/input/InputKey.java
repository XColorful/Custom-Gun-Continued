package dev.xcolorful.customgun.client.input;

import dev.xcolorful.customgun.client.CustomGunClient;
import dev.xcolorful.customgun.client.api.input.IInputKeySubManager;
import dev.xcolorful.customgun.client.api.input.IKeyMapping;
import dev.xcolorful.customgun.client.api.minecraft.input.ICustomInputKey;


public abstract class InputKey implements IInputKeySubManager {

    protected final ICustomInputKey key;
    protected IKeyMapping keyMapping;

    public InputKey(ICustomInputKey key) {
        this.key = key;
        this.keyMapping = this.createKeyMapping(CustomGunClient.getKeyMappingCreator());
        this._registerToManager();
    }
    protected abstract IKeyMapping createKeyMapping(IKeyMapping.Creator creator);

    public ICustomInputKey getKey() {
        return this.key;
    }

    protected void _registerToManager() {
        CustomGunClient.getInputKeyManager().registerSubManager(this);
    }

    // --------IInputKeySubManager--------

    @Override
    public IKeyMapping getKeyMapping() {
        return this.keyMapping;
    }
}

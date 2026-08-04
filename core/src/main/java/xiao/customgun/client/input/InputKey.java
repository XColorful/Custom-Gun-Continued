package xiao.customgun.client.input;

import xiao.customgun.client.CustomGunClient;
import xiao.customgun.client.api.input.IInputKeySubManager;
import xiao.customgun.client.api.input.IKeyMapping;
import xiao.customgun.client.api.minecraft.input.ICustomInputKey;


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

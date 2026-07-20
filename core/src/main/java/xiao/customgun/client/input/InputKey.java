package xiao.customgun.client.input;

import xiao.customgun.client.CustomGunClient;
import xiao.customgun.client.api.input.IKeyMapping;
import xiao.customgun.client.api.minecraft.input.CustomInputKey;

public abstract class InputKey {

    protected final CustomInputKey key;
    protected IKeyMapping keyMapping;

    public InputKey(CustomInputKey key) {
        this.key = key;
        this.keyMapping = this.createKeyMapping(CustomGunClient.getKeyMappingCreator());
    }
    protected abstract IKeyMapping createKeyMapping(IKeyMapping.Creator creator);

    public CustomInputKey getKey() {
        return this.key;
    }
    public IKeyMapping getKeyMapping() {
        return this.keyMapping;
    }
}

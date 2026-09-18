package dev.xcolorful.customgun.neoforge.init.registry;

import dev.xcolorful.customgun.core.api.init.registry.IRegistryObject;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredHolder;

public class NeoRegistryObject<R, T extends R> implements IRegistryObject<T> {

    private final DeferredHolder<R, T> registryObject;
    private final String id;

    public NeoRegistryObject(DeferredHolder<R, T> deferredHolder, String id) {
        this.registryObject = deferredHolder;
        this.id = id;
    }

    @Override
    public String getId() {
        return registryObject.getId().getPath();
    }

    @Override
    public T get() {
        return registryObject.get();
    }

    @Override
    public ResourceLocation getRegistryName() {
        return registryObject.getId();
    }
}
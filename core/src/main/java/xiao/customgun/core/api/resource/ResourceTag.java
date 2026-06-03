package xiao.customgun.core.api.resource;

import net.minecraft.resources.Identifier;
import xiao.customgun.CustomGun;

public interface ResourceTag {

    String getTagName();

    interface CategoryTag extends ResourceTag {
        String getCategoryName();
    }
    interface RegistryTag extends ResourceTag {
        String getRegistryName();
    }
    interface ConstantTag extends ResourceTag {
        String getConstantName();
    }

    Identifier NULL_LOCATION = CustomGun.getMcRegistry().createResourceLocation(CustomGun.MOD_ID + ":null");

    @FunctionalInterface
    interface TriConsumer<A, B, C> {
        void accept(A a, B b, C c);
    }
}

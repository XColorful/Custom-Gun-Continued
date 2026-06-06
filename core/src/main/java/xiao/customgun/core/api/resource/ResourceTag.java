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

    String nullLocation = CustomGun.MOD_ID + ":null";
    Identifier NULL_LOCATION = CustomGun.getMcRegistry().createResourceLocation(nullLocation);

    @FunctionalInterface
    interface TriConsumer<A, B, C> {
        void accept(A a, B b, C c);
    }
}

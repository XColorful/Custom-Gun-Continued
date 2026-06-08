package xiao.customgun.core.api.resource;

import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import xiao.customgun.CustomGun;

import java.util.Objects;

public interface ResourceTag {

    String getTagName();

    interface CategoryTag extends ResourceTag {
        String getCategoryName();
    }
    interface RegistryTag extends ResourceTag {
        String getRegistryName();
        Identifier getRegistryLocation();
    }
    interface ConstantTag extends ResourceTag {
        String getConstantName();
    }

    String nullLocation = CustomGun.MOD_ID + ":null";
    @NotNull Identifier NULL_LOCATION = Objects.requireNonNull(CustomGun.getMcRegistry().createResourceLocation(nullLocation));

    @FunctionalInterface
    interface TriConsumer<A, B, C> {
        void accept(A a, B b, C c);
    }
}

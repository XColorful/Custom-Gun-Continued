package dev.xcolorful.customgun.core.api.resource;

import dev.xcolorful.customgun.CustomGun;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

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
    interface IndexTag extends ResourceTag {
        int getIndex();
    }
    interface MaskTag extends ResourceTag {
        int getMask();
    }

    String nullLocation = CustomGun.MOD_ID + ":null";
    @NotNull Identifier NULL_LOCATION = Objects.requireNonNull(CustomGun.getMcRegistry().createResourceLocation(nullLocation));

    @FunctionalInterface
    interface TriConsumer<A, B, C> {
        void accept(A a, B b, C c);
    }
}

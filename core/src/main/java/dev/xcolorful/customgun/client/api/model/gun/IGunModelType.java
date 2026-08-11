package dev.xcolorful.customgun.client.api.model.gun;

import dev.xcolorful.customgun.client.model.GunModelObject;
import dev.xcolorful.customgun.client.resource.assets.model.BedrockModel;
import dev.xcolorful.customgun.core.api.resource.ResourceTag;
import org.jetbrains.annotations.Nullable;

public interface IGunModelType extends ResourceTag.CategoryTag {

    default String getName() {
        return this.getCategoryName();
    }

    @Nullable GunModelObject create(BedrockModel pojo);
}

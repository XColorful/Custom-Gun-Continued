package dev.xcolorful.customgun.client.api.item.gun;

import dev.xcolorful.customgun.core.api.resource.ResourceTag;

public interface IShooterAnimationCategory extends ResourceTag.CategoryTag {

    default String getName() {
        return this.getCategoryName();
    }
}

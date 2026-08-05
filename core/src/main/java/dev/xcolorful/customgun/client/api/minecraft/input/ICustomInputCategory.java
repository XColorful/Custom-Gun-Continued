package dev.xcolorful.customgun.client.api.minecraft.input;

import dev.xcolorful.customgun.core.api.resource.ResourceTag;
import net.minecraft.network.chat.Component;

public interface ICustomInputCategory extends ResourceTag.CategoryTag, ResourceTag.RegistryTag {

    Component getCategoryLang();
}

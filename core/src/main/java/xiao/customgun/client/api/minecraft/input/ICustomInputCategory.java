package xiao.customgun.client.api.minecraft.input;

import net.minecraft.network.chat.Component;
import xiao.customgun.core.api.resource.ResourceTag;

public interface ICustomInputCategory extends ResourceTag.CategoryTag, ResourceTag.RegistryTag {

    Component getCategoryLang();
}

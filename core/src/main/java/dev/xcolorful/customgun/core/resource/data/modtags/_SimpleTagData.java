package dev.xcolorful.customgun.core.resource.data.modtags;

import dev.xcolorful.customgun.core.api.resource.IResourcePojoExtension;
import dev.xcolorful.customgun.core.resource.ResourcePojo;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;

public abstract class _SimpleTagData<T extends _SimpleTagData<T>> extends ResourcePojo<T>
        implements IResourcePojoExtension.Appendable<T> {

    private List<String> tags;

    // --------Getter & Setter--------

    public final List<String> getTags() {
        return tags;
    }

    public final void setTags(List<String> tags) {
        this.tags = tags;
    }

    // --------IResourcePojoExtension--------

    @Override
    public void appendRaw(T pojo) {
        this.tags.addAll(pojo.getTags());
    }

    // --------Back compatibility--------
}
package dev.xcolorful.customgun.core.resource.data.modtags;

import dev.xcolorful.customgun.core.resource.ResourcePojo;

import java.util.List;

public abstract class _SimpleTagData<T extends _SimpleTagData<T>> extends ResourcePojo<T> {

    private List<String> tags;

    // --------Getter & Setter--------

    public final List<String> getTags() {
        return tags;
    }

    public final void setTags(List<String> tags) {
        this.tags = tags;
    }

    // --------Back compatibility--------
}
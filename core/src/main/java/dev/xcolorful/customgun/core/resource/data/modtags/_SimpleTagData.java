package dev.xcolorful.customgun.core.resource.data.modtags;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import dev.xcolorful.customgun.core.api.resource.IResourcePojoExtension;
import dev.xcolorful.customgun.core.resource.ResourcePojo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class _SimpleTagData<T extends _SimpleTagData<T>> extends ResourcePojo<T>
        implements IResourcePojoExtension.Appendable<T> {

    private List<String> tags;

    @Override
    protected T copyTo(final T pojo) {
        List<String> tags = new ArrayList<>(this.tags.size()); {
            for (int i = 0; i < this.tags.size(); i++) {
                tags.add(this.tags.get(i));
            }
        } pojo.setTags(tags);
        return pojo;
    }

    @Override
    protected boolean readCustomKey(JsonReader reader, String key) throws IOException {
        return super.readCustomKey(reader, key);
    }

    @Override
    protected void writeCustomValue(JsonWriter writer) {
        super.writeCustomValue(writer);
    }

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
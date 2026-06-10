package xiao.customgun.core.resource.instance.data;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.resource.data.modtags.AttachmentTagData;
import xiao.customgun.core.resource.instance.PojoInstance;

public class AttachmentTagDataInstance extends PojoInstance<AttachmentTagData> {

    private AttachmentTagDataInstance(@NotNull AttachmentTagData pojo) {
        super(pojo);
    }

    public static @Nullable AttachmentTagDataInstance fromPojo(AttachmentTagData pojo) {
        if (pojo == null) return null;
        AttachmentTagDataInstance instance = new AttachmentTagDataInstance(pojo);
        if (!instance.isPojoValid()) return null;
        else return instance;
    }

    @Override public boolean resetCache() {
        return true;
    }
    @Override protected boolean isPojoValid() {
        var pojo = this.getPojo();
        if (!pojo.isValid()) return false;
        if (!resetCache()) return false;

        return true;
    }
}

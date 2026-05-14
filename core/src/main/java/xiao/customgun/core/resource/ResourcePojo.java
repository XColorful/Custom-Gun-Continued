package xiao.customgun.core.resource;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public abstract class ResourcePojo<T extends ResourcePojo<T>> {

    private transient boolean validated = false;
    private transient boolean valid = false;

    public ResourcePojo() {
    }

    public final void validate() {
        this.validatePojo();
        this.validated = true;
    }

    /**
     * 流式反序列化
     */
    protected abstract T fromJsonReader(JsonReader reader) throws IOException;

    /**
     * 流式序列化
     */
    public abstract void toJson(JsonWriter writer) throws IOException;

    /**
     * 子类需满足 {@link ResourcePojo#isValid} 的要求
     */
    protected abstract void validatePojo();

    // --------Getter & Setter--------

    public final boolean isValidated() {
        return validated;
    }

    /**
     * 若为 true, 则可能返回 null 的 getter/setter 都需要加装饰器显式声明
     * 不覆盖 validatePojo 后被手动设置为 null 的场景, 应由修改方重新 validatePojo
     */
    public final boolean isValid() {
        return valid;
    }
    protected final void setValid(boolean valid) {
        this.valid = valid;
    }
}

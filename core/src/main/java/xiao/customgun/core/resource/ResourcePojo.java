package xiao.customgun.core.resource;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import xiao.customgun.core.developer.PlannedRefactor;
import xiao.customgun.core.resource.instance.PojoInstance;

import java.io.IOException;

/**
 * 只读 Pojo，允许多个目标共享
 * <p>
 * {@link PojoInstance}需要对允许修改的值单独复制一份cache
 */
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
     * <p>
     * 优化规范：Null检查用{@code |}每4-5个分一组，压缩分支预测开销并吃满单核ALU宽度；
     * 递归valid需连续调用以引导JIT内联，状态聚合用{@code &}每3-4个分一组以消除数据依赖
     * </p>
     * 预期是面对全数据合法的情况，否则短路{@code ||}可能更快
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

    // --------Back compatibility--------

    public static boolean ENABLE_BACK_COMPATIBILITY = PlannedRefactor.ENABLE_BACK_COMPATIBILITY;

    /**
     * 尝试填补非 @Nullable 字段，不包含完全的矫正
     * <p>
     * 全部new而不复用单例
     * @return Pojo自身，方便使用 this.foo == null ? new Foo().applyBackCompatibility() : this.foo
     */
    @SuppressWarnings("unchecked")
    public T applyBackCompatibility() {
        return (T) this;
    }
}

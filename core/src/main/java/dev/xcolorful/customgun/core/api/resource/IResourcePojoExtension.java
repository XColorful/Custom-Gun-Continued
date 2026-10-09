package dev.xcolorful.customgun.core.api.resource;

import dev.xcolorful.customgun.core.resource.ResourcePojo;

public interface IResourcePojoExtension<T extends ResourcePojo<T>> {

    /**
     * @return {@link ResourcePojo}
     */
    T asPojo();

    interface Appendable<T extends ResourcePojo<T>> extends IResourcePojoExtension<T> {
        /**
         * 将传入的 pojo 补充到当前 {@link #asPojo()}
         * <ul>
         *     <li>不检查 {@link ResourcePojo#isValid()}，调用时默认已满足</li>
         * </ul>
         */
        void appendRaw(T pojo);
    }
}

package xiao.customgun.core.api.item;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.resource.ResourcePojo;

import java.util.Collection;

/**
 * 无状态修饰工具
 * @param <T> 数据源
 * @param <K> 从数据源获取的modifier类型
 * @param <V> 修饰值类型
 */
public interface IItemModifier<T extends ResourcePojo<T>, K, V> {

    /**
     * 从Pojo获取原始modifier值
     */
    @Nullable K getModifier(@NotNull T pojo);

    V eval(Collection<K> modifiers, V base);
}

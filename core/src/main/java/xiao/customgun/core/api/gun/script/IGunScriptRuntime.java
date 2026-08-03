package xiao.customgun.core.api.gun.script;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import xiao.customgun.core.api.item.gun.modifier.GunModifierType;
import xiao.customgun.core.api.item.gun.modifier.IGunModifier;

public interface IGunScriptRuntime {

    /**
     * <font color="red">内部方法</font>，外部应使用{@link xiao.customgun.core.api.item.gun.modifier}包中各{@code I*Modifier}接口的{@code static evalByScript}作为入口
     * <br>
     * 调用前请手动确定{@link GunScriptApi#isCacheValid()}返回值以确保缓存有效，本方法内部不再检查
     *
     * @param <V> {@link IGunModifier}{@code <T, K, V>}
     * @param value 输入变量/当前计算值
     */
    @ApiStatus.Internal
    @NotNull <V> V evalByScript(ItemStack gunItem, GunScriptApi scriptApi, GunModifierType modifierType, @NotNull V value);
}

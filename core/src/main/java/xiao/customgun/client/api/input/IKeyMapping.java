package xiao.customgun.client.api.input;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;

/**
 * 封装 Forge 增加的 {@link KeyMapping} 构造函数
 */
public interface IKeyMapping {

    KeyMapping get();

    String getName();
    IKeyConflictContext getKeyConflictContext();
    IKeyModifier getKeyModifier();
    InputConstants.Type getInputType();
    int getKeyCode();
    String getCategory();


    interface Creator {
        IKeyMapping create(String name,
                           IKeyConflictContext.Type contextType, IKeyModifier.Type modifierType,
                           InputConstants.Type inputType, int keyCode,
                           String category);
    }
}

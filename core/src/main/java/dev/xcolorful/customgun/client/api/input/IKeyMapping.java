package dev.xcolorful.customgun.client.api.input;

import com.mojang.blaze3d.platform.InputConstants;
import dev.xcolorful.customgun.client.util.ClientInputUtils;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;

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
    KeyMapping.Category getCategory();

    default boolean matches(KeyEvent keyEvent) {
        return this.get().matches(keyEvent);
    }
    default boolean matchesMouse(MouseButtonEvent mouseButtonEvent) {
        return this.get().matchesMouse(mouseButtonEvent);
    }

    interface Creator {

        /**
         * @param inputType 推荐用 {@link ClientInputUtils.KeyType}
         * @param keyCode 推荐用 {@link ClientInputUtils.KeyCode}
         */
        IKeyMapping create(String name,
                           IKeyConflictContext.Type contextType, IKeyModifier.Type modifierType,
                           InputConstants.Type inputType, int keyCode,
                           KeyMapping.Category category);
    }
}

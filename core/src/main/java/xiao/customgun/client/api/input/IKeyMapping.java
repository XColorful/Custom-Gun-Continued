package xiao.customgun.client.api.input;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import xiao.customgun.client.api.event.IInputKeyEvent.KeyEvent;
import xiao.customgun.client.api.event.IMouseButtonEvent.MouseButtonEvent;

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

    default boolean matches(KeyEvent keyEvent) {
        return this.get().matches(keyEvent.key(), keyEvent.scancode());
    }
    default boolean matchesMouse(MouseButtonEvent mouseButtonEvent) {
        return this.get().matchesMouse(mouseButtonEvent.buttonInfo().button());
    }

    interface Creator {
        IKeyMapping create(String name,
                           IKeyConflictContext.Type contextType, IKeyModifier.Type modifierType,
                           InputConstants.Type inputType, int keyCode,
                           String category);
    }
}

package xiao.customgun.client.api.input;

import com.mojang.blaze3d.platform.InputConstants;
import org.jetbrains.annotations.Nullable;

/**
 * 封装 KeyModifier
 */
public interface IKeyModifier {

    boolean matches(InputConstants.Key key);
    boolean isActive(@Nullable IKeyConflictContext conflictContext);

    Object getKeyModifier();

    enum Type {
        CONTROL,
        SHIFT,
        ALT,
        NONE
    }
}

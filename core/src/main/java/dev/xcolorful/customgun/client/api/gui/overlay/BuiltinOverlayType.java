package dev.xcolorful.customgun.client.api.gui.overlay;

import dev.xcolorful.customgun.client.api.event.IPrepareRenderOverlayEvent;
import dev.xcolorful.customgun.core.api.gui.overlay.BuiltinOverlayTypeTag;
import dev.xcolorful.customgun.core.api.resource.ResourceTag;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public enum BuiltinOverlayType implements ResourceTag.CategoryTag {
    /**
     * 跟Forge对原版起的注册名一致，通常都应该叫crosshair，可用于在{@link IPrepareRenderOverlayEvent}进行拦截
     */
    CROSSHAIR(BuiltinOverlayTypeTag.CROSSHAIR),
    /**
     * 射手 HUD
     */
    SHOOTER_HUD(BuiltinOverlayTypeTag.SHOOTER_HUD),
    /**
     * 枪械 HUD
     */
    GUN_HUD(BuiltinOverlayTypeTag.GUN_HUD),
    /**
     * 枪射物 HUD (如子弹下坠/子弹扩散范围)
     */
    PROJECTILE_HUD(BuiltinOverlayTypeTag.PROJECTILE_HUD),
    /**
     * 射击反馈
     */
    SHOOT_FEEDBACK(BuiltinOverlayTypeTag.SHOOT_FEEDBACK),
    /**
     * 射手操作 (如交互提示)
     */
    SHOOTER_OPERATION(BuiltinOverlayTypeTag.SHOOTER_OPERATION),
    /**
     * 枪械状态 (如过热)
     */
    GUN_STATE(BuiltinOverlayTypeTag.GUN_STATE),
    /**
     * 新手教学/操作指引 HUD
     */
    GUIDE_TIP(BuiltinOverlayTypeTag.GUIDE_TIP),
    /**
     * 调试信息
     */
    DEBUG_INFO(BuiltinOverlayTypeTag.DEBUG_INFO),
    ;

    public final String typeName;
    BuiltinOverlayType(String name) {
        this.typeName = name;
    }
    @Override public String getTagName() {
        return this.typeName;
    }
    @Override public String getCategoryName() {
        return this.typeName;
    }

    private static final Map<String, BuiltinOverlayType> OVERLAY_TYPES = new HashMap<>();

    static {
        for (BuiltinOverlayType type : BuiltinOverlayType.values()) {
            OVERLAY_TYPES.put(type.getCategoryName(), type);
        }
    }

    public static @Nullable BuiltinOverlayType fromString(String name) {
        return name != null ? OVERLAY_TYPES.get(name) : null;
    }

    @Override
    public String toString() {
        return this.typeName;
    }
}

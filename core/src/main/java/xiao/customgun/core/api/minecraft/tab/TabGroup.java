package xiao.customgun.core.api.minecraft.tab;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.api.init.registry.IRegistryObject;
import xiao.customgun.core.api.resource.ResourceTag;
import xiao.customgun.core.init.registry.ModCreativeTabs;

import java.util.HashMap;
import java.util.Map;

import static xiao.customgun.core.init.registry.ModCreativeTabs.*;

/**
 * 需要在 {@link ModCreativeTabs} 之后访问
 * 弱枚举类型，即仍然使用 {@link Identifier} 以及 {@link TabGroup#getRegistryLocation()}
 */
public enum TabGroup implements ResourceTag.RegistryTag {
    AMMO(AMMO_TAB),
    ATTACHMENT_MUZZLE(ATTACHMENT_MUZZLE_TAB),
    ATTACHMENT_LASER(ATTACHMENT_LASER_TAB),
    ATTACHMENT_GRIP(ATTACHMENT_GRIP_TAB),
    ATTACHMENT_MAGAZINE(ATTACHMENT_MAGAZINE_TAB),
    ATTACHMENT_SCOPE(ATTACHMENT_SCOPE_TAB),
    ATTACHMENT_STOCK(ATTACHMENT_STOCK_TAB),
    GUN_SHOTGUN(GUN_SHOTGUN_TAB),
    GUN_PISTOL(GUN_PISTOL_TAB),
    GUN_RIFLE(GUN_RIFLE_TAB),
    GUN_SNIPER(GUN_SNIPER_TAB),
    GUN_MG(GUN_MG_TAB),
    GUN_SMG(GUN_SMG_TAB),
    GUN_RPG(GUN_RPG_TAB),
    GUN_CUSTOM(GUN_CUSTOM_TAB);

    public final String typeName;
    public final String registryName;
    public final Identifier registryLocation;
    public final IRegistryObject<CreativeModeTab> registryObject;
    TabGroup(IRegistryObject<CreativeModeTab> tabRegistryObject) {
        this.registryObject = tabRegistryObject;
        this.registryLocation = this.registryObject.getRegistryName();
        this.typeName = this.registryLocation.getPath();
        this.registryName = this.registryLocation.toString();
    }
    @Override public String getTagName() {
        return this.typeName;
    }
    @Override public String getRegistryName() {
        return this.registryName;
    }
    @Override public Identifier getRegistryLocation() {
        return this.registryLocation;
    }

    private static final Map<String, TabGroup> TAB_GROUPS = new HashMap<>();
    private static final Map<Identifier, TabGroup> TAB_GROUP_LOCATIONS = new HashMap<>();

    static {
        for (TabGroup group : values()) {
            TAB_GROUPS.put(group.typeName, group);
            TAB_GROUPS.put(group.registryName, group);
            TAB_GROUP_LOCATIONS.put(group.registryLocation, group);
        }
    }

    public static @Nullable TabGroup fromString(String name) {
        return name != null ? TAB_GROUPS.get(name) : null;
    }
    public static @Nullable TabGroup fromLocation(Identifier location) {
        return location != null ? TAB_GROUP_LOCATIONS.get(location) : null;
    }

    @Override
    public String toString() {
        return this.registryName;
    }
}

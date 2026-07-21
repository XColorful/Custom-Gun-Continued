package xiao.customgun.core.projectile;

import org.jetbrains.annotations.NotNull;
import xiao.customgun.CustomGun;
import xiao.customgun.core.api.common.McSide;
import xiao.customgun.core.api.projectile.IProjectileManager;
import xiao.customgun.core.api.projectile.ProjectileManagerGroup;
import xiao.customgun.core.projectile.effect.ProjectileEffectManager;
import xiao.customgun.core.projectile.impact.ProjectileImpactManager;
import xiao.customgun.core.projectile.physics.ProjectilePhysicsManager;
import xiao.customgun.core.projectile.process.ProjectileProcessManager;

import java.util.HashMap;
import java.util.Map;

public class ProjectileManager implements IProjectileManager {
    public static final ProjectileManager INSTANCE = new ProjectileManager();

    private final ProjectileManagerGroup defaultGroup = new ProjectileManagerGroup("default",
            ProjectileEffectManager.INSTANCE,
            ProjectileImpactManager.INSTANCE,
            ProjectilePhysicsManager.INSTANCE,
            ProjectileProcessManager.INSTANCE);
    private final Map<String, ProjectileManagerGroup> managerGroups;

    protected ProjectileManager() {
        this.managerGroups = new HashMap<>();
        this.registerRuntimeGroup(defaultGroup);
    }

    public static void init(McSide mcSide) {
    }

    public static final String _MANAGER_NAME = String.format("%s:%s", CustomGun.MOD_ID, ProjectileManager.class.getSimpleName());
    @Override public String getManagerName() {
        return _MANAGER_NAME;
    }

    // --------IProjectileMainManager--------

    @Override
    public boolean registerRuntimeGroup(ProjectileManagerGroup projectileManagerGroup) {
        return this.managerGroups.putIfAbsent(projectileManagerGroup.managerGroupTag(), projectileManagerGroup) == null;
    }
    @Override
    public @NotNull ProjectileManagerGroup getProjectileManagerGroup(String managerGroupTag) {
        ProjectileManagerGroup group = this.managerGroups.get(managerGroupTag);
        return group != null ? group : defaultGroup;
    }
}

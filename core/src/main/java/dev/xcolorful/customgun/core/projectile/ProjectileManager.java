package dev.xcolorful.customgun.core.projectile;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.core.api.common.McSide;
import dev.xcolorful.customgun.core.api.projectile.IProjectileManager;
import dev.xcolorful.customgun.core.api.projectile.ProjectileManagerGroup;
import dev.xcolorful.customgun.core.projectile.effect.ProjectileEffectManager;
import dev.xcolorful.customgun.core.projectile.impact.ProjectileImpactManager;
import dev.xcolorful.customgun.core.projectile.physics.ProjectilePhysicsManager;
import dev.xcolorful.customgun.core.projectile.process.ProjectileProcessManager;
import org.jetbrains.annotations.NotNull;

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

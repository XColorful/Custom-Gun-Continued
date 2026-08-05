package dev.xcolorful.customgun.core.api.projectile;

/*
从 IGunProjectileManager 到决定用 IProjectileManager 的过程
不亚于 AmmoBullet -> GunBullet -> GunProjectile 的过程
不能只看到过早设计的心智折磨, 也不能惧怕架构设计, 就像BattleRoyale GameManager的发展过程
 */
@Deprecated(forRemoval = false)
public interface IGunProjectileManager extends IProjectileMainManager {
}

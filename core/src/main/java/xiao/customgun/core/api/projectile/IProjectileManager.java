/*
 * Go to BattleRoyale GameManager 的设计
 */

package xiao.customgun.core.api.projectile;

/*
接口名不带Gun前缀是因为
1. 没有必要写一个全部Projectile通用的父接口, 就像GameManager/GunManager这类顶级分形, 完全统一的父接口太臃肿太无力
2. Projectile在CustomGun语境下只有跟Gun相关, 只不过为了跟原版Projectile区分而用GunProjectile
"即使以后IGunProjectileManager extends IProjectileManager也兼容"的设想不成立, 不需要这么考虑

起名字是一件头疼的事情 (悲
 */
public interface IProjectileManager extends IProjectileMainManager {
}

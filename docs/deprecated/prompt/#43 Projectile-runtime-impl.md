> #43 提示词

# 

```
现在需要检查并修复枪射物（GunProjectile）的移动问题：
- 是否应用了初始旋转和加速度
- 是否按正常运动轨迹和射线检测判定运行
- 当前射线检测是否会避开射手和枪射物本身
- 可以通过IDEA的mcp访问tacz源码
- tacz类到cgc的迁移映射在./docs-tacz/TaCZ Migration Mapping.md

- 测试枪械为SLR，gun data在游戏目录下.\saves\新的世界\datapacks\classic_battleroyale_gun\data\cbrg\data\guns\slr_data.json
- 超平坦世界，专用服务端simulation-distance=64，view-distance=64，本地客户端渲染距离20，模拟距离12
- 第一发玩家位于0 ~ 0，朝向0 0射击，正常应该能飞挺远
- 第二发玩家位于0 ~ 0，朝向0 30射击（朝地面），正常应该很快就碰到地面然后pierce减少discard

辅助快速查找：
- 枪射物在_DefaultGunFire#doGunFire里生成
- 枪射物tick为GunProjectile#tick，调用projectile.process.ProjectileProcessManager进行各子Manager调度

当前git暂存区有一些修改，当前代码状态下测试出的日志如下：
服务端日志：
[06Sep2026 14:03:40.672] [Server thread/INFO] [net.minecraft.server.MinecraftServer/]: [XiaoColorful: Teleported XiaoColorful to 0.500000, -60.000000, 0.500000]
[06Sep2026 14:03:42.617] [Server thread/INFO] [net.minecraft.server.MinecraftServer/]: [Not Secure] <XiaoColorful> tp @s 0 ~ 0 0 0
[06Sep2026 14:03:43.696] [Server thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG]constructInitData lifetimeTicks 40
[06Sep2026 14:03:43.696] [Server thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG]constructInitData pierce 1
[06Sep2026 14:03:43.704] [Server thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG]GunProjectile tick: spawnPos (0.5, -58.37999999523163, 0.5), position (0.5, -58.37999999523163, 0.5), deltaMovement = (0.11201393732168996, -0.17705855949226562, 83.99973870878621)
[06Sep2026 14:03:43.704] [Server thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG]GunProjectile processTick 1 position = (0.5, -58.37999999523163, 0.5), isDiscarded = false
[06Sep2026 14:03:43.704] [Server thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG]GunProjectile processTick finish, lifetimeTicks = 39, isDiscarded = false
[06Sep2026 14:03:43.704] [Server thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG]GunProjectile tick: finish spawnPos (0.5, -58.37999999523163, 0.5), position (0.61201393732169, -58.557058554723895, 84.49973870878621), isDiscarded = false
[06Sep2026 14:03:43.754] [Server thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG]GunProjectile tick: spawnPos (0.5, -58.37999999523163, 0.5), position (0.61201393732169, -58.557058554723895, 84.49973870878621), deltaMovement = (0.10829283509513858, -0.1956766747243244, 81.20926796726293)
[06Sep2026 14:03:43.754] [Server thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG]GunProjectile processTick 1 position = (0.61201393732169, -58.557058554723895, 84.49973870878621), isDiscarded = false
[06Sep2026 14:03:43.754] [Server thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG]GunProjectile processTick finish, lifetimeTicks = 38, isDiscarded = false
[06Sep2026 14:03:43.754] [Server thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG]GunProjectile tick: finish spawnPos (0.5, -58.37999999523163, 0.5), position (0.7203067724168285, -58.75273522944822, 165.70900667604914), isDiscarded = false
[06Sep2026 14:03:43.804] [Server thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG]GunProjectile tick: spawnPos (0.5, -58.37999999523163, 0.5), position (0.7203067724168285, -58.75273522944822, 165.70900667604914), deltaMovement = (0.10469534785893149, -0.21367629629656976, 78.51149664455919)
[06Sep2026 14:03:43.804] [Server thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG]GunProjectile processTick 1 position = (0.7203067724168285, -58.75273522944822, 165.70900667604914), isDiscarded = false
[06Sep2026 14:03:43.804] [Server thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG]GunProjectile processTick finish, lifetimeTicks = 37, isDiscarded = false
[06Sep2026 14:03:43.804] [Server thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG]GunProjectile tick: finish spawnPos (0.5, -58.37999999523163, 0.5), position (0.82500212027576, -58.96641152574479, 244.22050332060832), isDiscarded = false
[06Sep2026 14:03:43.853] [Server thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG]GunProjectile tick: spawnPos (0.5, -58.37999999523163, 0.5), position (0.82500212027576, -58.96641152574479, 244.22050332060832), deltaMovement = (0.1012173691239406, -0.23107797056412202, 75.90334526662008)
[06Sep2026 14:03:43.853] [Server thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG]GunProjectile processTick 1 position = (0.82500212027576, -58.96641152574479, 244.22050332060832), isDiscarded = false
[06Sep2026 14:03:43.854] [Server thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG]GunProjectile processTick finish, lifetimeTicks = 36, isDiscarded = false
[06Sep2026 14:03:43.854] [Server thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG]GunProjectile tick: finish spawnPos (0.5, -58.37999999523163, 0.5), position (0.9262194893997007, -59.19748949630891, 320.1238485872284), isDiscarded = false
[06Sep2026 14:03:43.904] [Server thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG]GunProjectile tick: spawnPos (0.5, -58.37999999523163, 0.5), position (0.9262194893997007, -59.19748949630891, 320.1238485872284), deltaMovement = (0.09785492881857838, -0.2479015613323259, 73.38183665949761)
[06Sep2026 14:03:43.904] [Server thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG]GunProjectile processTick 1 position = (0.9262194893997007, -59.19748949630891, 320.1238485872284), isDiscarded = false
[06Sep2026 14:03:43.905] [Server thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG]GunProjectile processTick finish, lifetimeTicks = 35, isDiscarded = false
[06Sep2026 14:03:43.905] [Server thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG]GunProjectile tick: finish spawnPos (0.5, -58.37999999523163, 0.5), position (1.024074418218279, -59.44539105764124, 393.505685246726), isDiscarded = false
[06Sep2026 14:03:43.953] [Server thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG]GunProjectile tick: spawnPos (0.5, -58.37999999523163, 0.5), position (1.024074418218279, -59.44539105764124, 393.505685246726), deltaMovement = (0.09460418875700811, -0.2641662725310494, 70.94409255094183)
[06Sep2026 14:03:43.953] [Server thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG]GunProjectile processTick 1 position = (1.024074418218279, -59.44539105764124, 393.505685246726), isDiscarded = false
[06Sep2026 14:03:43.954] [Server thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG]GunProjectile processTick finish, lifetimeTicks = 34, isDiscarded = false
[06Sep2026 14:03:43.954] [Server thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG]GunProjectile tick: finish spawnPos (0.5, -58.37999999523163, 0.5), position (1.1186786069752872, -59.70955733017229, 464.4497777976678), isDiscarded = false
[06Sep2026 14:03:44.006] [Server thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG]GunProjectile tick: spawnPos (0.5, -58.37999999523163, 0.5), position (1.1186786069752872, -59.70955733017229, 464.4497777976678), deltaMovement = (0.09146143825790015, -0.2798906701357424, 68.58733028488712)
[06Sep2026 14:03:44.007] [Server thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG]GunProjectile processTick 1 position = (1.1186786069752872, -59.70955733017229, 464.4497777976678), isDiscarded = false
[06Sep2026 14:03:44.007] [Server thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG]GunProjectile processTick finish, lifetimeTicks = 33, isDiscarded = false
[06Sep2026 14:03:44.007] [Server thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG]GunProjectile tick: finish spawnPos (0.5, -58.37999999523163, 0.5), position (1.2101400452331874, -59.98944800030803, 533.0371080825549), isDiscarded = false
[06Sep2026 14:03:48.494] [Server thread/INFO] [net.minecraft.server.MinecraftServer/]: [XiaoColorful: Teleported XiaoColorful to 0.500000, -60.000000, 0.500000]
[06Sep2026 14:03:51.677] [Server thread/INFO] [net.minecraft.server.MinecraftServer/]: [Not Secure] <XiaoColorful> tp @s 0 ~ 0 0 30
[06Sep2026 14:03:52.813] [Server thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG]constructInitData lifetimeTicks 40
[06Sep2026 14:03:52.813] [Server thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG]constructInitData pierce 1
[06Sep2026 14:03:52.861] [Server thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG]GunProjectile tick: spawnPos (0.5, -58.37999999523163, 0.5), position (0.5, -58.37999999523163, 0.5), deltaMovement = (-0.06098355538035359, 41.940144049499125, 72.78063340005525)
[06Sep2026 14:03:52.861] [Server thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG]GunProjectile processTick 1 position = (0.5, -58.37999999523163, 0.5), isDiscarded = false
[06Sep2026 14:03:52.861] [Server thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG]GunProjectile processTick finish, lifetimeTicks = 39, isDiscarded = false
[06Sep2026 14:03:52.861] [Server thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG]GunProjectile tick: finish spawnPos (0.5, -58.37999999523163, 0.5), position (0.4390164446196464, -16.439855945732504, 73.28063340005525), isDiscarded = false
[06Sep2026 14:03:52.915] [Server thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG]GunProjectile tick: spawnPos (0.5, -58.37999999523163, 0.5), position (0.4390164446196464, -16.439855945732504, 73.28063340005525), deltaMovement = (-0.058957682090522254, 40.522392753595575, 70.36286125963855)
[06Sep2026 14:03:52.915] [Server thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG]GunProjectile processTick 1 position = (0.4390164446196464, -16.439855945732504, 73.28063340005525), isDiscarded = false
[06Sep2026 14:03:52.915] [Server thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG]GunProjectile processTick finish, lifetimeTicks = 38, isDiscarded = false
[06Sep2026 14:03:52.915] [Server thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG]GunProjectile tick: finish spawnPos (0.5, -58.37999999523163, 0.5), position (0.38005876252912413, 24.08253680786307, 143.6434946596938), isDiscarded = false
[06Sep2026 14:03:52.962] [Server thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG]GunProjectile tick: spawnPos (0.5, -58.37999999523163, 0.5), position (0.38005876252912413, 24.08253680786307, 143.6434946596938), deltaMovement = (-0.05699910829742991, 39.151739145979974, 68.02540749307884)
[06Sep2026 14:03:52.963] [Server thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG]GunProjectile processTick 1 position = (0.38005876252912413, 24.08253680786307, 143.6434946596938), isDiscarded = false
[06Sep2026 14:03:52.963] [Server thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG]GunProjectile processTick finish, lifetimeTicks = 37, isDiscarded = false
[06Sep2026 14:03:52.963] [Server thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG]GunProjectile tick: finish spawnPos (0.5, -58.37999999523163, 0.5), position (0.32305965423169425, 63.234275953843046, 211.66890215277266), isDiscarded = false
[06Sep2026 14:03:53.015] [Server thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG]GunProjectile tick: spawnPos (0.5, -58.37999999523163, 0.5), position (0.32305965423169425, 63.234275953843046, 211.66890215277266), deltaMovement = (-0.055105598312258273, 37.82661864177169, 65.76560392454965)
[06Sep2026 14:03:53.015] [Server thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG]GunProjectile processTick 1 position = (0.32305965423169425, 63.234275953843046, 211.66890215277266), isDiscarded = false
[06Sep2026 14:03:53.015] [Server thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG]GunProjectile processTick finish, lifetimeTicks = 36, isDiscarded = false
[06Sep2026 14:03:53.015] [Server thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG]GunProjectile tick: finish spawnPos (0.5, -58.37999999523163, 0.5), position (0.267954055919436, 101.06089459561474, 277.43450607732234), isDiscarded = false
[06Sep2026 14:03:53.064] [Server thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG]GunProjectile tick: spawnPos (0.5, -58.37999999523163, 0.5), position (0.267954055919436, 101.06089459561474, 277.43450607732234), deltaMovement = (-0.053274990715756226, 36.54551863158905, 63.580871015007055)
[06Sep2026 14:03:53.064] [Server thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG]GunProjectile processTick 1 position = (0.267954055919436, 101.06089459561474, 277.43450607732234), isDiscarded = false
[06Sep2026 14:03:53.064] [Server thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG]GunProjectile processTick finish, lifetimeTicks = 35, isDiscarded = false
[06Sep2026 14:03:53.064] [Server thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG]GunProjectile tick: finish spawnPos (0.5, -58.37999999523163, 0.5), position (0.21467906520367977, 137.6064132272038, 341.0153770923294), isDiscarded = false
[06Sep2026 14:03:53.110] [Server thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG]GunProjectile tick: spawnPos (0.5, -58.37999999523163, 0.5), position (0.21467906520367977, 137.6064132272038, 341.0153770923294), deltaMovement = (-0.05150519589100527, 35.306976754923625, 61.46871491767642)
[06Sep2026 14:03:53.110] [Server thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG]GunProjectile processTick 1 position = (0.21467906520367977, 137.6064132272038, 341.0153770923294), isDiscarded = false
[06Sep2026 14:03:53.110] [Server thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG]GunProjectile processTick finish, lifetimeTicks = 34, isDiscarded = false
[06Sep2026 14:03:53.110] [Server thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG]GunProjectile tick: finish spawnPos (0.5, -58.37999999523163, 0.5), position (0.1631738693126745, 172.91338998212743, 402.48409201000584), isDiscarded = false
[06Sep2026 14:03:53.166] [Server thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG]GunProjectile tick: spawnPos (0.5, -58.37999999523163, 0.5), position (0.1631738693126745, 172.91338998212743, 402.48409201000584), deltaMovement = (-0.04979419363814658, 34.10957923087301, 59.4267246313558)
[06Sep2026 14:03:53.166] [Server thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG]GunProjectile processTick 1 position = (0.1631738693126745, 172.91338998212743, 402.48409201000584), isDiscarded = false
[06Sep2026 14:03:53.166] [Server thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG]GunProjectile processTick finish, lifetimeTicks = 33, isDiscarded = false
[06Sep2026 14:03:53.166] [Server thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG]GunProjectile tick: finish spawnPos (0.5, -58.37999999523163, 0.5), position (0.11337967567452792, 207.02296921300044, 461.91081664136163), isDiscarded = false
[06Sep2026 14:03:53.210] [Server thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG]GunProjectile tick: spawnPos (0.5, -58.37999999523163, 0.5), position (0.11337967567452792, 207.02296921300044, 461.91081664136163), deltaMovement = (-0.048140030868346693, 32.95195924432664, 57.45256924828657)
[06Sep2026 14:03:53.210] [Server thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG]GunProjectile processTick 1 position = (0.11337967567452792, 207.02296921300044, 461.91081664136163), isDiscarded = false
[06Sep2026 14:03:53.210] [Server thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG]GunProjectile processTick finish, lifetimeTicks = 32, isDiscarded = false
[06Sep2026 14:03:53.210] [Server thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG]GunProjectile tick: finish spawnPos (0.5, -58.37999999523163, 0.5), position (0.06523964480618122, 239.97492845732708, 519.3633858896482), isDiscarded = false

客户端日志：
[06Sep2026 14:03:42.781] [Render thread/INFO] [net.minecraft.client.gui.components.ChatComponent/]: [Not Secure] [CHAT] <XiaoColorful> tp @s 0 ~ 0 0 0
[06Sep2026 14:03:43.900] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG]GunProjectile tick: spawnPos (0.0, 0.0, 0.0), position (0.5, -58.37999999523163, 0.5), deltaMovement = (0.112, -0.177, 3.9)
[06Sep2026 14:03:43.901] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG]GunProjectile processTick 1 position = (0.5, -58.37999999523163, 0.5), isDiscarded = false
[06Sep2026 14:03:43.901] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG]GunProjectile processTick finish, lifetimeTicks = -1, isDiscarded = true
[06Sep2026 14:03:43.901] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG]GunProjectile tick: finish spawnPos (0.0, 0.0, 0.0), position (0.612, -58.55699999523163, 4.4), isDiscarded = true
[06Sep2026 14:03:48.651] [Render thread/INFO] [net.minecraft.client.gui.components.ChatComponent/]: [System] [CHAT] ??XiaoColorful???0.500000, -60.000000, 0.500000
[06Sep2026 14:03:51.829] [Render thread/INFO] [net.minecraft.client.gui.components.ChatComponent/]: [Not Secure] [CHAT] <XiaoColorful> tp @s 0 ~ 0 0 30
[06Sep2026 14:03:52.993] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG]GunProjectile tick: spawnPos (0.0, 0.0, 0.0), position (0.5, -58.37999999523163, 0.5), deltaMovement = (-0.060875, 3.9, 3.9)
[06Sep2026 14:03:52.993] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG]GunProjectile processTick 1 position = (0.5, -58.37999999523163, 0.5), isDiscarded = false
[06Sep2026 14:03:52.993] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG]GunProjectile processTick finish, lifetimeTicks = -1, isDiscarded = true
[06Sep2026 14:03:52.993] [Render thread/DEBUG] [dev.xcolorful.customgun.CustomGun/]: [CGC-DEBUG]GunProjectile tick: finish spawnPos (0.0, 0.0, 0.0), position (0.439125, -54.47999999523163, 4.4), isDiscarded = true
[06Sep2026 14:04:11.982] [Render thread/INFO] [net.minecraft.client.gui.components.ChatComponent/]: [Not Secure] [CHAT] <XiaoColorful> 2
```

# 

I'll investigate the projectile movement code. Let me start by reading the key files in parallel and locating the gun data.

I've found the core movement code. Let me now find `doBulletSpread`, read the gun data, check the ray trace utilities, and inspect the tacz source for comparison.

Now I have the tacz source. Let me verify the speed computation in tacz and check the projectile spawn position and config values.

I've found the root cause. Let me verify the Minecraft `Vec3` rotation sign convention definitively before applying the fix, using the project's own melee code as a reference anchor.

The triple confirmation is already solid: tacz's `rotateX(pitch).rotateY(-yaw)` (JOML) equals `Vec3.xRot(-pitch).yRot(-yaw)`, the project's own melee code uses `new Vec3(0,0,1).xRot(-pitch).yRot(-yaw)`, and the log confirms positive pitch currently produces upward velocity. Let me apply the fix.

Let me verify the edited file has no syntax/compile problems.

修复完成并已验证编译无误。下面是完整的检查与修复结论。

## 结论：核心移动 bug 已定位并修复

问题出在初始速度方向的旋转计算上，**pitch（俯仰）和 yaw（偏航）的角度符号都反了**。修复位于 [ProjectilePhysicsManager.java:176](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/core/projectile/physics/ProjectilePhysicsManager.java:176) 的 `shootFromRotation`。

### 依据（三处独立交叉验证）

1. **tacz 源码** [EntityKineticBullet.java:360](app://localhost/epitaxy/local_a126636b-b89c-4af3-98a9-d8944356af30) 的 `shoot` 用的是 `new Vector3d(x, y, 8).rotateX(pitch).rotateY(-yaw)`（JOML）。而 Minecraft 的 `Vec3.xRot(a)` 等价于 JOML 的 `rotateX(-a)`、`Vec3.yRot(a)` 等价于 `rotateY(a)`，所以 tacz 实际等价于 `Vec3.xRot(-pitch).yRot(-yaw)`。CGC 写的是 `.xRot(pitch).yRot(yaw)`，两个符号都少了负号。
2. **项目自身已有正确先例**：[`_DefaultGunAttack._doMelee`](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/core/gun/attack/_DefaultGunAttack.java:274) 里算视角向量就是 `new Vec3(0,0,1).xRot(-getXRot()).yRot(-getYRot())`。
3. **日志直接印证**：第二发「朝向 0 30」（低头 30°）服务端 y 速度是 **+41.94（向上飞）**，而正确应该是 **-42（向下）**。

修复内容（对两个角度取反）：

```java
Vec3 projectileDirection = new Vec3(spreadOffset.x, spreadOffset.y, SPREAD_FORWARD_DISTANCE)
        .xRot(-(xRot + yOffset) * Mth.DEG_TO_RAD)
        .yRot(-yRot * Mth.DEG_TO_RAD);
```

## 三个检查项的结果

**1. 初始旋转和加速度是否应用？** — 之前应用了，但方向反了（已修复）。初速度大小本身是对的：`840（slr_data 的 speed）× 2.0（GLOBAL_BULLET_SPEED_MODIFIER 默认值）/ 20 = 84 格/tick`，与日志一致。重力和阻力也正确（`gravity 0.0245`、`friction 0.03322` 与枪械数据吻合）。

**2. 是否按正常轨迹和射线检测运行？** — 移动管线（`physicTick 射线检测 → impactTick 命中判定 → physicMove 位移+重力阻力`）与 tacz 的 `tick`/`onBulletTick` 顺序一致；block 射线 + entity 射线都在。修复方向后轨迹即可正常：第一发会沿 +Z 平飞很远，第二发会快速下坠撞地。

**3. 射线检测是否避开射手和枪射物自身？** — **正确，无需改动**：

- block 射线用 `ClipContext.Block.COLLIDER`，只检测方块，不涉及实体。
- entity 射线通过 `level.getEntities(枪射物, …)` 把枪射物自身作为 `except` 排除；`createWithFilter` 再排除 owner（射手）及其载具/乘客。

## 另行发现（未改，属于网络同步层，不在本次「移动」范围）

客户端日志暴露了另一个独立问题，与服务器端移动逻辑无关：

- 客户端枪射物 `deltaMovement` 显示 z=3.9，而服务端是 84 —— 因为原版 `ClientboundAddEntityPacket` 会把初速度 **clamp 到 ±3.9**。
- 客户端 `lifetimeTicks = -1`、`isDiscarded = true` —— 枪射物状态（lifetime 等）没有同步，客户端首 tick 就 discard。

根因：CGC 的 `GunProjectile` **没有实现自定义 spawn 数据同步**（tacz 用 Forge 的 `IEntityAdditionalSpawnData` 同步全精度速度和 lifetime）。加上 [ProjectileEffectManager](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/core/projectile/effect/ProjectileEffectManager.java) 的 `moveEffect`/`impactEffect` 客户端逻辑目前为空，所以客户端曳光/弹孔也还渲染不出来。

这是「枪射物网络同步 + 客户端渲染」这条线的工作，建议单独开任务处理，避免和本次移动修复混在一起。需要的话我可以接着做这块。

你要不要在游戏里重新打两发验证一下修复效果？（第一发平飞、第二发下坠撞地）

# 

```
- 客户端spawn数据同步写在ForgeGunProjectile里，对于NetworkUtils里已经封装的类型，则必须优先使用NetworkUtils里的（涉及高版本差异封装），如果没有则直接使用FriendlyByteBuf的
- 完成同步后，目前觉得直接逐个设置服务端计算好的值，不再需要复用constructInitData（客户端创建实体的时候已经在无信息的情况下调用过一遍constructInitData），避免客户端计算的时候不一致
- 同时需要检查，用ForgeGunProjectile拦截注册的机制下是否能正常使用这个接口，否则我会考虑改成mixin到GunProjectile实现该接口
- 我在Projectile Manager里面一些地方设置了仅客户端/服务端，你检查拦截得是否合适
```

# 

I now understand the architecture. Let me verify the mixin configuration setup and check the Forge spawn-packet API before implementing.

I have the full picture now. Let me verify the exact Forge and Minecraft method signatures I need, to avoid compile errors.

Confirmed the exact signatures: `Entity.getAddEntityPacket()` returns `Packet<ClientGamePacketListener>`, and `NetworkHooks.getEntitySpawningPacket(Entity)` returns the same. Now let me read the gradle.properties mixin config lines, then implement.

I now have everything verified. Let me implement all changes: create the common Forge mixin config, the mixin class, update `gradle.properties`, and fix `ForgeGunProjectile` to avoid the method override conflict.

The `extends Projectile` approach fails because `Projectile` has no no-arg constructor. Let me switch to the established pattern in this codebase (no extends, cast to `GunProjectile`), matching `GunProjectileMixin`.

Let me verify the build compiles (including Mixin annotation processing) before reporting.

构建通过（仅有与本改动无关的弃用警告）。全部完成。下面是本轮结论。

## 结论：`ForgeGunProjectile` 拦截机制不足以实现 spawn 数据同步，已改用 Mixin 完成

### 关键检查结果（你让我核对的那一点）

`ForgeGunProjectile` 的拦截注册**只影响客户端侧的 EntityType 工厂**，覆盖不到服务端：

- 服务端在 [`_DefaultGunFire.doGunFire`](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/core/gun/attack/_DefaultGunFire.java:217) 里用 `GunProjectile.create(...)` → `new GunProjectile(...)` **直接 new 出普通的 `GunProjectile`**，完全不经过 EntityType 工厂。
- `ForgeGunProjectile.writeSpawnData` 只有在「服务端实体是 `ForgeGunProjectile` 实例」时才会被 Forge 调用。而服务端实体是普通 `GunProjectile`，所以它**永远不会被执行**。

因此 `IEntityAdditionalSpawnData` 必须在 `GunProjectile` 本身实现——这正是你说的 fallback：**Mixin 到 `GunProjectile`**。这样无论服务端 `new` 出来的是 `GunProjectile`，还是客户端工厂建出来的是 `ForgeGunProjectile`（它继承自 `GunProjectile`），两侧都具备该接口。

### 改动清单

1. 新增 [GunProjectileSpawnDataMixin.java](app://localhost/epitaxy/forge-compat/src/main/java/dev/xcolorful/customgun/forge/mixin/entity/GunProjectileSpawnDataMixin.java)：给 `GunProjectile` 注入 `IEntityAdditionalSpawnData` + `getAddEntityPacket()` + `writeSpawnData/readSpawnData`。`readSpawnData` 里**逐项直接 set**（`setDeltaMovement`/`setLifetimeTicks`/`setGravity`…），不复用 `constructInitData`。
2. 新增 [customgunforge.mixins.json](app://localhost/epitaxy/forge-compat/src/main/resources/customgunforge.mixins.json)：forge 侧**公共** mixin 配置（`writeSpawnData` 在服务端、`readSpawnData` 在客户端，必须两端都生效，所以不能放进现有的 `customgunforgeclient.mixins.json`）。
3. [gradle.properties](app://localhost/epitaxy/local_a126636b-b89c-4af3-98a9-d8944356af30) 把 `customgunforge.mixins.json` 加进 `mixin_config_file_forge`。
4. [ForgeGunProjectile.java](app://localhost/epitaxy/forge-compat/src/main/java/dev/xcolorful/customgun/forge/entity/projectile/ForgeGunProjectile.java)：删掉了它原来的 `implements IEntityAdditionalSpawnData` 和空方法——否则它的空 `readSpawnData` 会**覆盖**掉 Mixin 注入的版本（子类方法优先级更高），导致客户端啥也读不到。

### 同步字段说明

写/读顺序一一对应（ResourceLocation 走 `NetworkUtils`，其余用 `FriendlyByteBuf`）：`deltaMovement`（全精度，覆盖 ±3.9 clamp）、`shootPos`、`lifetimeTicks`、`bulletSpeed`、`gravity`、`friction`、`pierce`、`isTracer`、三个 gun 位置、owner id。

**未同步**（服务端专属、客户端移动/渲染用不到）：`damageCalculation`、`armorIgnorePercent`、`headshotMultiplier`、`knockbackStrength`、`fireAspect`、`fireAspectSeconds`、`explosionData`、`extraDataTag/extraStateTag`、`managerGroupTag`（客户端传 null 会回落到 `defaultGroup`）。

### Projectile Manager 的「仅客户端/服务端」拦截检查

结论：**设置都正确**，无需改动。

|方法|端|判断|
|---|---|---|
|`physicTick`（射线检测）|服务端|✅ 正确。但它在 return 前先给 `tickContext` 填了 `startPos/deltaMovement/endPos`，客户端 `physicMove` 依赖这个，所以必须保留在 return 之前|
|`physicMove`（位移+重力阻力）|两端|✅ 正确。客户端需要本地移动实体来渲染曳光|
|`impactTick` / `preImpactTick`|服务端|✅ 正确（伤害/命中是服务端权威）|
|`impactEffect`|服务端|✅ 正确|
|`moveEffect`|客户端|✅ 正确|
|`processTick` 的 lifetime 递减|服务端|✅ 正确。客户端不递减，靠服务端 discard 的移除包结束生命周期|

### 一个可选的后续优化（本次未动）

tacz 在 EntityType 上设了 `.setShouldReceiveVelocityUpdates(false)`，避免服务端重复下发速度包（客户端速度由本地 `physicMove` 确定性算出）。CGC 的 [ModEntities](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/core/init/registry/ModEntities.java:16) 目前没设（有 `TODO 模组做完后可考虑修改` 注释）。这不影响正确性，只影响包量，等你决定要不要一起补。

建议你在游戏里重打两发验证：第一发（朝 0 0）应该平飞很远且客户端能正确跟轨迹；第二发（朝 0 30）应该快速下坠撞地并 discard。如果客户端速度仍显示 3.9 或仍然首 tick 就 `isDiscarded=true`，告诉我日志，我再查。

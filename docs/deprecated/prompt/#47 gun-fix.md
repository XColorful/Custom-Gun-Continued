> #47 提示词

# 

```
现在需要修复安装了扩容弹夹，却不使用扩容弹夹的容量的问题

当前git暂存区是我临时加的测试日志

- 测试枪械为游戏目录（1.20.1test）下.\saves\新的世界2\datapacks\classic_battleroyale_gun\data\cbrg\data\guns\beryl_m762_data.json
- 配件为.\saves\新的世界2\datapacks\classic_battleroyale_gun\data\cbrg\data\attachments
- 可以通过IDEA的mcp访问tacz源码
- tacz类到cgc的迁移映射在./docs-tacz/TaCZ Migration Mapping.md\extended_quickdraw_mag_rifle_data.json

检查并修复问题，完成后通知我检查
```

# 

I'll start by looking at the current changes and the relevant code.

Let me find the `getMagAmmoLimit` implementation.

Found the bug. Let me confirm the imports in `GunDataAccessor`.

Diagnosis confirmed. Now applying the fix.

Now removing the temporary debug logging.

修复完成。

## 根因

`GunDataAccessor.getMagAmmoLimit()` (core/src/main/java/dev/xcolorful/customgun/core/api/item/gun/GunDataAccessor.java:385) 把**配件索引 location** 当成**配件数据 location** 去查数据：

```java
var attachmentLocation = this.getAttachmentLocation(gunItem, AttachmentCategory.MAGAZINE);
AttachmentData attachmentData = ResourceApi.getAttachmentData(attachmentLocation); // 永远为 null
```

- 枪械 NBT 里存的 `attachment_rl` 是**索引** location（`cbrg:extended_quickdraw_mag_rifle`），来源见 [AttachmentTab.java:45](core/src/main/java/dev/xcolorful/customgun/core/api/minecraft/tab/AttachmentTab.java:45)、[_TableResultRaw.java:112](core/src/main/java/dev/xcolorful/customgun/core/recipe/_TableResultRaw.java:112)。
- 但 `ResourceApi.getAttachmentData()` 按**数据** location 索引，键是 `cbrg:extended_quickdraw_mag_rifle_data`（索引 json 的 `data` 字段，由 `FileToIdConverter` 去前缀去后缀得到）。

于是查不到 → `magazineCategory` 保持 `NONE` → 直接返回 `defaultMagSize`（30），扩容弹夹等级被完全忽略。项目里其它地方（[GunDataAccessor.java:223](core/src/main/java/dev/xcolorful/customgun/core/api/item/gun/GunDataAccessor.java:223) 近战、[_DefaultGunFire.java:104](core/src/main/java/dev/xcolorful/customgun/core/gun/attack/_DefaultGunFire.java:104) 消音器、[LivingShooterAim.java:135](core/src/main/java/dev/xcolorful/customgun/core/entity/shooter/LivingShooterAim.java:135) 瞄具）用的都是 `getAttachmentIndexInstance(location)` → `getAttachmentData()`，只有这两处走错了。

## 改动

1. [GunDataAccessor.java:385](core/src/main/java/dev/xcolorful/customgun/core/api/item/gun/GunDataAccessor.java:385) `getMagAmmoLimit`：改为经 `getAttachmentIndexInstance()` 取数据（沿用项目既有模式）。
2. [IGunScriptBackCompat.java:314](core/src/main/java/dev/xcolorful/customgun/core/api/script/context/IGunScriptBackCompat.java:314) `getMagExtentLevel()`：同一处缺陷（脚本 API 的扩容等级永远返回 0），一并修正；`AttachmentData` import 换成 `AttachmentIndexInstance`。
3. 移除了你临时加的 `[CGC-DEBUG]ammo limit` 日志（[_DefaultGunAction.java](core/src/main/java/dev/xcolorful/customgun/core/gun/action/_DefaultGunAction.java) 已恢复原样）。

修复后 `beryl_m762` 装 `extended_quickdraw_mag_rifle`（`extended_mag_level: 3`）→ `extended_mag_ammo_amount[2]` = 40；装 level 1/2 分别得到 30/42。

IDEA 编译通过（仅剩项目原有的 deprecated 警告）。

## 待确认的一点（未改）

[GunDataAccessor.java:399](core/src/main/java/dev/xcolorful/customgun/core/api/item/gun/GunDataAccessor.java:399) 当扩容等级超出 `extended_mag_ammo_size` 长度时返回 `0`。TaCZ 对应逻辑（`AttachmentDataUtils.getAmmoCountWithAttachment`）在 `extended_mag_ammo_amount` 为 null 时返回 `getAmmoAmount()`。若某把枪允许装扩容弹夹却没写 `extended_mag_ammo_size`，容量会变成 0。当前枪包数据没触发这条分支，所以没动；需要的话我再改。

请检查。

# 
---

```
现在需要修复一些tacz官包里部分拉栓武器的脚本兼容问题，目前怀疑是资源包脚本API的行为跟之前不一致

目前测试：
- tacz:m870、tacz:spas_12、tacz:kar98这三个典型，在从创造模式tab（此时mag 5发子弹）拿到手后，cgc调试指令显示枪管子弹1，弹匣子弹4（自动bolt，预期行为），但之后一直反复播放拉栓动画而不能射击
- M870枪械数据在测试游戏目录（1.20.1test）下.\saves\新的世界2\datapacks\tacz_default_gun\data\tacz\data\guns\m870_data.json
- M870客户端资源包显示在.\resourcepacks\tacz_default_gun\assets\tacz\display\guns\m870_display.json，在文件内可知其状态机脚本为.\resourcepacks\tacz_default_gun\assets\tacz\scripts\m870_state_machine.lua

- 可以通过IDEA的mcp访问tacz源码
- tacz类到cgc的迁移映射在./docs-tacz/TaCZ Migration Mapping.md

检查并修复问题，完成后通知我检查
```

# 

我先读取相关的脚本文件和迁移映射文档。

资源包路径不对，我先定位测试目录下的实际文件。

现在看 CGC 侧的拉栓（bolt）触发逻辑。

等待探查的同时，我看一下 m870 的枪械数据和本次任务文档。

趁探查进行，我在 TaCZ 原版源码里看拉栓触发逻辑作为参照。

CGC 客户端 `bolt()` 的守卫与脚本 `start_bolt` 的关系是关键，我对比 TaCZ 原版调用点。

找到疑似根因。确认脚本方法解析路径。

根因已定位。我来确认一下 `_DefaultGunAction.startBolt` 里 `useBarrelAmmo` 守卫的调用方，然后修复。

修改完成，用 IDEA 编译验证。

修复已应用且无编译错误。跑一次模块编译做实际验证。

修复完成，编译通过。

## 根因

CGC 把 TaCZ 原版放在**调用方**的拉栓前置检查（[LocalPlayerBolt.bolt():56-66](app://localhost/TACZ/src/main/java/com/tacz/guns/client/gameplay/LocalPlayerBolt.java)、[LivingEntityBolt.bolt():65-75](app://localhost/TACZ/src/main/java/com/tacz/guns/entity/shooter/LivingEntityBolt.java)：是不是手动/枪管供弹、膛内有没有弹、还有没有弹可上）挪进了 `iGun.startBolt(...)`，由 [_DefaultGunAction.startBolt()](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/core/gun/action/_DefaultGunAction.java:43) 实现。

问题在于脚本调用策略：`GunActionManager.startBolt` 原本是「脚本返回非 UNKNOWN 就直接采用脚本结果」。而官方枪包的 `start_bolt` 全都只是 `return true`（TaCZ 时代检查在调用方，脚本不需要自己检查，见 [m870_gun_logic.lua](app://localhost/epitaxy/local_8c8d8788-e8fe-4664-b5ec-c5244b39b8d2)），于是**带脚本的枪完全绕过了前置检查**。

连锁反应：

1. 客户端每 tick 的 [tickAutoBolt()](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/client/entity/shooter/LocalShooterBolt.java:83) → `bolt()`；
2. `startBolt` 恒为 true（脚本），即使膛内已有子弹也照拉不误；
3. 每次触发 `INPUT_BOLT` → 拉栓动画反复重播，同时反复 `lockState(BOLT_STATE, ...)`；
4. 服务器同理反复重启 bolt，`synIsBolting` 长期为 true → 射击被 `_shouldForceDisableShoot` 挡下。

这解释了为什么只有**部分**拉栓武器出问题——只有带 `gun_logic` 脚本的枪（m870 / spas_12 / kar98，以及 m1014、hk_mk23 等）会绕过检查；纯 Java 默认实现的枪走 `UNKNOWN` 分支，检查正常。

## 改动

只改了一处：[GunActionManager.startBolt()](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/core/gun/action/GunActionManager.java:42)。把 `_DefaultGunAction.startBolt` 提为**无条件前置检查**，通过后再咨询脚本——脚本返回 `FALSE` 仍能否决，`TRUE`/`UNKNOWN` 不再改变已通过检查的结果。

这样脚本 `start_bolt` 恢复成 TaCZ 语义（只决定「是否开始 tick」，不做有效性判断），客户端与服务端共用同一条路径，前者一并通过。

编译：`:core:compileJava` 通过。按项目规则，我停在这里，未提交、未推送。

请你进游戏验证：`tacz:m870` / `tacz:spas_12` / `tacz:kar98` 从创造 tab 拿到手后应只拉一次栓（枪管 1、弹匣 4），随后可正常射击与换弹。

# 

```
现在无限拉栓的问题解决了，又有新问题：
- 切到M870后，看起来像是一直停在掏枪动画刚开始的状态，按开火有枪声，但是不消耗子弹；可以装弹，装弹后就处于正常的待机状态，但是仍然无法射击
- tacz:spas_12、tacz:kar98掏枪动画正常，初始拉栓正常，但是开火有枪声却不消耗子弹；可以换弹，装弹后仍然无法射击
- 在处于切枪结束、非换弹（可射击状态）下，均没有触发“LocalShooterProperty: operation LocalShooterShoot#shoot failed, current state:”日志

继续排查并修复
```

# 

现在射击链路出问题。我读一下开火/耗弹的实现。

让我在测试目录的游戏日志里找证据。

日志显示 kar98 状态是「barrel 1, mag 4/4」（满弹）却打不出子弹。查看 `ILivingShooter.cgc$shoot`。

`_DefaultGunAttack.gunFire` 与实际开火是异步的。现在确认网络层的异常处理，这决定了 `shootOnce` 递归是否会被静默吞掉。

找到关键机制了。先确认 `IGunAttackManager` 的结构，决定修复落点。

找到了。`shootOnce` 回调用的是**调度器** `iGun.gunFire`，而不是默认实现 —— 脚本 `shoot` → `shootOnce` → `gunFire` → 脚本 `shoot` 无限递归，异常被 Forge 的 `enqueueWork`（`submitAsync` 返回的 future 被丢弃）静默吞掉，所以「有声、无耗弹、无报错」。

用项目既有模式（`IGunScriptBackCompat` 已经这样调用 `_DefaultGunAction.consumeAmmoFromPlayer`）修复。

修复完成并编译通过。快速核对 M870 动画资源是否有脚本引用的动画名。

M870 的动画文件里**没有 `draw`**。`use_default_animation: "rifle"` 说明它复用默认步枪动画集。检查另两把枪。

M870 缺少 `draw` 动画（其余枪自带）。它靠 `use_default_animation: "rifle"` 复用默认步枪动画集。查 CGC 对默认动画集的处理。

我的前一处修复已被你提交（`ec38196a1`）。现在看 CGC 里创建动画控制器的位置，以决定 M870 缺失默认动画的修复落点。

两处修复完成、编译通过。报告如下。

## 修复 1：脚本枪「有声、不耗弹、打不出子弹」

**根因**：`_GunScriptBackCompat.shootOnce` 回调的是**调度器**而不是默认实现。

调用链：`LivingShooterShoot.shoot` → `GunAttackManager.gunFire` → 脚本 `gun_fire`（旧名 `shoot`）→ 脚本里 `api:shootOnce(...)` → 又回到 `iGun.gunFire` → `GunAttackManager.gunFire` → 脚本 `shoot` → …… 无限递归。

对比 TaCZ：`ModernKineticGunScriptAPI.shootOnce` **内部就是完整开火实现**；CGC 迁移时把它改成了重新走一遍派发器。

为什么你看到的现象是「有声但不耗弹、且无任何报错」：Forge 的 `NetworkEvent.Context.enqueueWork` 走 `BlockableEventLoop.submitAsync`，返回的 `CompletableFuture` 被丢弃，**包处理里的异常被静默吞掉**（[NetworkEvent.java:205](app://localhost/epitaxy/core/build/tmp/.cache/expanded/zip_360104c6fc04675d4421fcf7cf7ba91a/net/minecraftforge/network/NetworkEvent.java:205)）。所以服务端那一发根本没落地——不扣弹、不出弹、不报错；而 1P 枪声是客户端 `_doShoot` 自己播的，所以「有枪声」。

受影响的是所有在逻辑脚本里定义 `shoot` 的枪：m870、kar98、spas_12、m1014、hk_mk23、db_short、devotion_lmg。

**改动**（沿用项目既有模式：`IGunScriptBackCompat` 早就这么调 `_DefaultGunAction.consumeAmmoFromPlayer`）：

- [_GunScriptBackCompat.shootOnce](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/core/api/script/context/_GunScriptBackCompat.java:34) → 改为调用 `_DefaultGunAttack.gunFire(...)`
- [_DefaultGunAttack.gunFire](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/core/gun/attack/_DefaultGunAttack.java:145) → `protected static` 改为 `@ApiStatus.Internal public static`

## 修复 2 未做（需要你定夺）：M870 卡在掏枪动画

根因已确认，**不是脚本问题**：`m870.animation.json` 里**没有 `draw` 动画**，它靠 `"use_default_animation": "rifle"` 复用默认步枪动画集。TaCZ 会把内置的 `assets/tacz/animations/rifle_default.animation.json` 用 `providePrototypeIfAbsent` 合并进每把枪的控制器（枪自带的动画优先）；CGC 把这个功能删掉了——`GunDisplayTag` 里两个 tag 标了 `@Deprecated`、`GunDisplay` 里根本没解析、`AnimController.providePrototypeIfAbsent` **零调用者**。

于是 M870 的 `runAnimation("draw")` 空转 → 停在 pre-draw 姿态（就是你说的「掏枪动画刚开始」）。kar98/spas_12 自带 `draw`，所以只有 M870 犯病——与你的观察完全吻合。

要修需要：① 把 `rifle_default.animation.json` / `pistol_default.animation.json` 两个资源从 TaCZ 拷进 CGC；② 解析 `use_default_animation`/`default_animation`；③ 在 `GunDisplayInstance.loadAnimController` 里用 `providePrototypeIfAbsent` 合并。这属于**恢复一个功能 + 引入第三方资源文件**，我先不动，等你确认。

## 另一个遗留差异（未改）

`shootOnce(consumeAmmo)` 的 `consumeAmmo` 参数被忽略，恒定走 `consumeAmmoOnce`。TaCZ 里该参数为 false 时不扣弹——对应创造模式的 `isShootingNeedConsumeAmmo()`。要不要按 TaCZ 语义补上，也请你定。

两处代码改动未提交、未推送。请进游戏验证 kar98 / spas_12 / m870 的射击与换弹；M870 的掏枪动画在修复 2 落地前仍会保持原样。

# 
---

```
现在需要修复CGC第一人称和第三人称都看不到枪射物渲染

目前测试：
- 测试枪械为游戏目录（1.20.1test）下.\saves\新的世界2\datapacks\classic_battleroyale_gun\data\cbrg\data\guns\beryl_m762_data.json
- 在使用TaCZ时，第一人称和第三人称都能看到子弹射出后的白线（曳光弹）
- 在CGC时，两个人称都看不见

- CGC的渲染体系文档在./docs/architecture/client/render/
- TaCZ的渲染体系文档在./docs-tacz/client/render/
- 可以通过IDEA的mcp访问tacz源码
- tacz类到cgc的迁移映射在./docs-tacz/TaCZ Migration Mapping.md

检查并修复问题，完成后通知我检查
```

# 

我先读取相关文档，了解 CGC 与 TaCZ 的渲染体系。

I'll compare with TaCZ's bullet renderer via the IDEA MCP.

Found a strong candidate. Let me check the color utility helpers.

Let me verify the ammo display accessor can't NPE and check the resource is present.

Let me check the git history of this renderer to see if the tracer ever used normalized values.

**Root cause confirmed.** The energy-swirl shader discards any fragment with `alpha < 0.1` — and CGC feeds it channel values `0..255` instead of `0..1`, so alpha becomes `255*255 & 0xFF = 1` → `1/255 ≈ 0.004` → every tracer fragment is discarded. TaCZ passes `0..1` floats with alpha `1`. Applying the fix.

Let me verify how the shooter is resolved client-side, since the tracer bails out if the owner is null.

修复完成，编译通过。请进游戏验证。

## 根因

[GunProjectileRenderer.java:116](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/client/renderer/entity/GunProjectileRenderer.java:116) 把 `java.awt.Color` 的通道值（**0~255**）直接当成顶点色（**0~1**）传给曳光弹模型。

换算链被量化摧毁了：

1. `Color.getRed()/getAlpha()` → `255`
2. `VertexConsumer.color(float…)` 默认实现做 `(int)(v * 255f)` → `65025`
3. `BufferBuilder` 写字节 `(byte)(65025 & 255)` → `1`
4. 归一化后顶点 alpha = `1/255 ≈ 0.0039`

而原版 `rendertype_energy_swirl.fsh` 结尾是：

```glsl
vec4 color = texture(Sampler0, texCoord0) * vertexColor * ColorModulator;
if (color.a < 0.1) { discard; }
```

alpha 只有 0.004，**每一个曳光弹片元都被 discard**——所以第一/第三人称都看不见，而且和枪无关：白色兜底分支 `Color.WHITE` 同样被量化成 0.004。这解释了为什么只有曳光弹消失、其它都正常。

对照 TaCZ：它把 `tracer_color` 解析成 **0~1 的 `float[]`**（`ColorHex.colorTextToRbgFloatArray`），渲染时 alpha 还硬编码为 `1`。CGC 迁移成 `java.awt.Color` 时漏了单位换算。

## 改动

一处文件，两处编辑：

- 新增 `_toTracerColor(Color)`，四个通道统一 `/ 255f`；三处重复的 `new float[]{...}` 合并调用它。
- 顺手捋顺了 `gunDisplayInstance` → ammo display → `Color.WHITE` 的兜底链，语义不变。

`mcc:0` 的差异是：原先 `color != null` 分支不查 ammo display（行为等价，只是少一层嵌套）。

IDEA 编译 + 文件检查均无 error。

## 需要你确认的两点（未改）

1. **同源缺陷存在于所有分支**：`26.2neoforge`、`1.21.x` 的 back-port 里是同一段代码。按项目规则我只改了当前 `gun-fix` 分支，其它分支要不要一并修由你定。
2. **与 TaCZ 的语义差异**：CGC 在 [GunProjectileRenderer.java:180](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/client/renderer/entity/GunProjectileRenderer.java:180) 用 `if (bulletDistance < 2) return;` 整体提前返回，TaCZ 是 `if (tickCount >= 5 || bulletDistance > 2)`（前 5 tick 无条件画）。影响的是近距离前几 tick 的表现，不是可见性，所以我没动。

code 未提交、未推送。

# 

```
现在确实渲染了，但还有问题：
- 当视角朝上半（抬头）时，渲染的子弹是从头正上方一根从左到右向上弯的弧线的范围开始，往前射出
- 当视角朝向下半时，渲染的子弹是从玩家下方一根从左到右向下弯的弧线的范围为起点往前射出

测试：
- 当面前有铁傀儡，开镜后往其脚底射击，不会打中，子弹从更下面射出
- 当镜头往上朝其头顶射击，开F3+B能看到子弹是从屏幕左上方开始飞出的

检查并修复问题，目前怀疑是服务端逻辑问题。
```

# 

Let me check the ammo display for an entity model — that decides whether a pose-stack leak in the ammo render can corrupt the tracer.

我已经把曳光弹的整条链路和 TaCZ 逐行对齐检查了一遍，先汇报结论和需要你补充的信息。

## 已确认的部分

曳光弹的绘制代码（`_renderTracer`）与 TaCZ `EntityBulletRenderer.renderTracerAmmo` 是**逐行等价**的：

- 方向数学等价。TaCZ 的 `Vector3d.rotateX(pitch).rotateY(-yaw)` 与 CGC 的 `Vec3.xRot(-pitch).yRot(-yaw)` 展开后都得到原版约定 `(-sin(yaw)cos(pitch), -sin(pitch), cos(yaw)cos(pitch))`（[ProjectilePhysicsManager.java:176](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/core/projectile/physics/ProjectilePhysicsManager.java:176)）。
- 第一人称枪口偏移的采集与施加（`cacheMuzzlePosition` + 取消相机旋转的三明治结构）与 TaCZ 相同。
- 弹道方向取自服务端 `player::getXRot/getYRot`（[C2SMessageShooterShoot.java:47](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/core/network/message/shooter/C2SMessageShooterShoot.java:47)），与 TaCZ `ClientMessagePlayerShoot` 一致。
- `muzzle_flash` 相关：我用测试包里的 `ammo_762_display.json` 确认它**没有** `ammo_entity` 段，所以 [GunProjectileRenderer.java:131](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/client/renderer/entity/GunProjectileRenderer.java:131) 的 `_renderAmmoObject` 会提前返回——也就是说它那两行 `mulPose`（在 `pushPose` 之前，会把旋转泄漏给后面的曳光弹）这次**不会**被执行，不是本次现象的原因（但这个泄漏本身是隐患，见下）。

所以问题大概率不在「曳光弹怎么画」，而在「子弹的位置/朝向数据」。

## 找到的与 TaCZ 的实质差异

1. **实体类型配置**（[ModEntities.java:15](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/core/init/registry/ModEntities.java:15)）
    
    - TaCZ：`clientTrackingRange(5).updateInterval(5).setShouldReceiveVelocityUpdates(false)`
    - CGC：`clientTrackingRange(8).updateInterval(4)`，**缺 `setShouldReceiveVelocityUpdates(false)`**
    - 这会让服务端持续把速度/位置回灌给客户端，和客户端的本地预测（`physicMove` 每 tick 自己算）互相打架。
2. **服务端出生点被延迟回卷**（[IShooterState.java:66](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/core/api/entity/shooter/IShooterState.java:66)，由 [IGunProjectile.java:27](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/core/api/entity/IGunProjectile.java:27) 以 `latencyLerp=true` 调用）
    
    - TaCZ 直接用当前眼位 `throwerIn.getEyeY() - 0.1`；CGC 用**碰撞箱历史按延迟回卷**后的眼位。
    - 站着不动时两者等价，移动/转身时就会有位移。
3. **客户端不同步 `xRot/yRot`**：TaCZ 的 `writeSpawnData/readSpawnData` 同步子弹的 `xRot/yRot`，CGC 的 `_GunProjectileSync` 没同步（靠客户端 `physicMove` 从 `deltaMovement` 反推，慢 1 tick 才正确）。
    

以上三条都能解释「子弹位置不对」，但**都不能解释「弧线」** —— 我看不出哪段代码会让曳光弹变成弧形。所以我需要看一眼实际画面，避免我猜错方向白改。

## 需要你提供

1. **一张截图或录屏**（最关键）。我要确认那个「弧线」到底是：曳光弹拉出的长条（`trailLength` 被放大）、枪口火焰、还是连续几发的轨迹连成的扇形。
2. 打铁傀儡**脚底不中**时，铁傀儡**到底掉不掉血**？这能区分「只是画得偏」还是「弹道真的偏」。
3. 第三人称（让另一个玩家/观察者看）是否也有同样的偏移？站定不动、只单击一发（不连发、不移动）时是否也存在？

拿到画面后我直接定位改，不再盲改。

# 

```
这个弧线的意思是，当朝向上半（抬头）时，渲染的曳光弹可能从左上方开始，也可能在右上方开始，我怀疑是沿着玩家周围一圈开始发射

现在的代码状态下：
- setShouldReceiveVelocityUpdates(false)，不在_GunProjectileSync发送Rot，开F3+B看不见子弹/曳光弹
- setShouldReceiveVelocityUpdates(false)，在_GunProjectileSync发送Rot，依然看不见子弹/曳光弹

继续排查并修复
```

# 

Now I have a much better clue — the start point circles around the player. Let me look at the first-person transform chain that feeds `cacheMuzzlePosition`.

The ring clue points at the captured muzzle offset. Let me check whether `ModelObject.render` leaves the `PoseStack` balanced before `cacheMuzzlePosition` reads it.

Let me do a mechanical diff of the two tracer implementations instead of eyeballing.

I've verified the tracer code is a line-for-line port of TaCZ, so the divergence must be in runtime data. Let me add a temporary instrumented log to get ground truth.

The file has changed since I last read it — you've been editing. Let me see the current uncommitted state.

Found it. The current code has an **inverted** tracer condition — and your last two edits broke the client's bullet motion. Let me confirm the git history, then fix.

**Root cause found.** The commit `9c19a56dd` contains my color fix _plus_ an edit you made to the tracer's guard — and that guard is **inverted** relative to TaCZ:

||condition|meaning|
|---|---|---|
|TaCZ|`if (tickCount >= 5 \| bulletDistance > 2) { 渲染 }`|前 5 tick 距离 >2 才画，**5 tick 后无条件画**|
|CGC 当前|`if (tickCount >= 5 \| bulletDistance < 2) return;`|**5 tick 之后一律不画**，前 5 tick 还要距离 >2|

`tickCount >= 5` 对每一发子弹都成立，所以曳光弹只在射出后的 **前 5 tick（250ms）** 才可能出现，而那时子弹还没飞远 → 枪口偏移 `offsetReducer ≈ (50-disToEye)/50 ≈ 0.9~1` 全量作用，把曳光弹按「相机坐标系下的枪口位移」整体搬走，随视角旋转就成了你说的「绕玩家一圈」。

先修这两处，并加一段临时日志把真实数值打出来。

The add-entity packet **clamps velocity to ±3.9 blocks/tick** — that's a major finding. Let me check where `shouldReceiveVelocityUpdates` is actually used.

Now I have the full picture of the packet path. Let me verify the last piece — whether the periodic motion packets clobber the client's velocity.

The ring implies the captured muzzle offset is corrupted. `cacheMuzzlePosition` runs right after the model render — let me check whether the delegate renderers leave the `PoseStack` unbalanced.

I have enough to act on the two definite defects. Let me add a temporary probe so the next run gives us the runtime numbers.

两处已定位并修复，另有一处需要你跑一次日志确认。

## 根因 1（确定，已修）：曳光弹的可见窗口判断写反了

你自己在 `9c19a56dd` 里加的这行（和我的颜色修复一起提交的）：

```java
if (gunProjectile.tickCount >= 5 || bulletDistance < 2) return;   // 当前
```

TaCZ 的语义是 **渲染** 条件，不是跳过条件：

```java
if (bullet.tickCount >= 5 || bulletDistance > 2) { /* 渲染 */ }    // TaCZ
```

`tickCount >= 5` 对每一发子弹都成立，所以曳光弹**只在射出后前 5 tick（250ms）才可能出现**。而这 250ms 恰好是子弹还没飞远、`offsetReducer = (50-disToEye)/50 ≈ 0.9~1` 全量生效的窗口——枪口偏移被 100% 施加，曳光弹的起点就整体离开了弹道。已改为：

```java
// 距离两格外才渲染，只在前 5 tick 内判定；5 tick 之后无条件渲染 (与原模组一致)
if (gunProjectile.tickCount < 5 && bulletDistance <= 2) return;
```

## 根因 2（确定，你的改动方向是对的，请保留）：速度被 ±3.9 截断

我把原版和 Forge 的源码翻出来了：

- 原版 `ClientboundAddEntityPacket`：`xa/ya/za = clamp(vel, ±3.9) * 8000`（`MAGICAL_QUANTIZATION`，`LIMIT = 3.9`）
- Forge 的 `PlayMessages.SpawnEntity`（`NetworkHooks.getEntitySpawningPacket` 实际发的包）同样 `Mth.clamp(vec3d.x, -3.9D, 3.9D)`
- `ServerEntity.sendChanges`：`if ((this.trackDelta || ...))` → **每 tick 都发 `ClientboundSetEntityMotionPacket`**（同样截断到 ±3.9）
- `ChunkMap.addEntity`：`new TrackedEntity(..., entitytype.trackDeltas())` ← **就是 `setShouldReceiveVelocityUpdates`**
- `PlayMessages.SpawnEntity.handle`：先 `lerpMotion(截断值)`，**再** `readSpawnData(msg.buf)` —— 所以 `_GunProjectileSync` 里的未截断速度能盖回去（这正是你那个 Forge Mixin 注释里说的"绕过 ±3.9 clamp"）

这把枪 `speed: 740 × GLOBAL_BULLET_SPEED_MODIFIER 2.0 / 20 = 74 格/tick`。于是：

|`setShouldReceiveVelocityUpdates`|客户端子弹速度|表现|
|---|---|---|
|默认 `true`|每 tick 被 motion 包覆盖成 **≤3.9 格/tick**|子弹一直贴在身边 → `offsetReducer` 恒定 ≈0.9 → 约 1 格的枪口偏移全量叠加 → 曳光弹起点脱离弹道、随视角转|
|`false`（TaCZ / 你的改法）|保持 `_GunProjectileSync` 的 **74 格/tick**|子弹 1~2 tick 就飞出 128 格追踪范围，客户端实体被移除|

所以你观察到「`false` 时看不见」我判断是：**它并没有消失，而是变成了约 2 帧（33ms）的远处一闪**——74 格/tick 的子弹在射手侧本来就只存在 1~2 tick。这也解释了为什么你把 Rot 加进 `_GunProjectileSync` 没变化：那条同步路径本来就没断，问题在速度被截断。

**结论：`setShouldReceiveVelocityUpdates(false)` 保留，`_GunProjectileSync` 的 Rot 同步也保留（与原模组一致）。**

## 需要你跑一次

我在渲染器里加了临时日志（`[TEMP DEBUG]` 标注，定位后我删），`latest.log` 里会打 `[TRACER] ...`，含 tick、dist、`|dv|`、trailLen、是否第一人称、缓存的相机旋转、offset、reducer、子弹坐标、眼位。

请用当前代码编译进游戏，**对着天空抬头打几发（腰射 + 开镜各几发）**，然后把 `latest.log` 里的 `[TRACER]` 行贴给我。我需要确认两件事：

1. 客户端子弹的 `|dv|` 是 74 还是 3.9（确认 clamp 分析）；
2. 第一人称的 `offset` 三个分量有多大——如果它远大于 1 格，那「绕一圈」就还有第三个原因（`cacheMuzzlePosition` 采到的是坏的矩阵）。

## 附带发现（未改，等你定）

[GunProjectileRenderer.java:155](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/client/renderer/entity/GunProjectileRenderer.java:155) `_renderAmmoObject` 的前两个 `mulPose` 写在 `pushPose` **之前**，`popPose` 收不回来，会把子弹的旋转泄漏给紧接其后的曳光弹。当前这把枪因为 `ammo_762_display.json` 里没有 `ammo_entity_display`，该方法提前 return，**没有触发**；但任何配了子弹实体的枪都会中招。要不要一并修，你说。

代码未提交、未推送。

# 

```
日志如下，从我发送1之后开了几枪，依然没渲染子弹/曳光弹：
[11Sep2026 17:07:06.613] [Server thread/INFO] [net.minecraft.server.MinecraftServer/]: [Not Secure] <XiaoColorful> 1
[11Sep2026 17:07:06.624] [Render thread/INFO] [net.minecraft.client.gui.components.ChatComponent/]: [CHAT] <XiaoColorful> 1
[11Sep2026 17:07:07.218] [Render thread/INFO] [dev.xcolorful.customgun.CustomGun/]: [TRACER] tick=0 dist=0.00 |dv|=74.00 trailLen=-1.00 fp=false camRot=(0.0,0.0) offset=null reducer=0.00 bulletPos=(46.30,-57.38,35.02) eye=(46.30,-57.38,35.02)
[11Sep2026 17:07:07.388] [Render thread/INFO] [dev.xcolorful.customgun.CustomGun/]: [TRACER] tick=0 dist=0.00 |dv|=74.00 trailLen=-1.00 fp=false camRot=(0.0,0.0) offset=null reducer=0.00 bulletPos=(46.30,-57.38,35.02) eye=(46.30,-57.38,35.02)
[11Sep2026 17:07:07.491] [Render thread/INFO] [dev.xcolorful.customgun.CustomGun/]: [TRACER] tick=0 dist=0.00 |dv|=74.00 trailLen=-1.00 fp=false camRot=(0.0,0.0) offset=null reducer=0.00 bulletPos=(46.30,-57.38,35.02) eye=(46.30,-57.38,35.02)
[11Sep2026 17:07:07.646] [Render thread/INFO] [dev.xcolorful.customgun.CustomGun/]: [TRACER] tick=0 dist=0.00 |dv|=74.00 trailLen=-1.00 fp=false camRot=(0.0,0.0) offset=null reducer=0.00 bulletPos=(46.30,-57.38,35.02) eye=(46.30,-57.38,35.02)

旋转泄漏也要修复
```

# 

The log reveals the key fact: `|dv|=74.00` **and** `dist=0.00 tick=0` — the client's bullet has the right velocity but is not moving. Let me check the tick path.

Two things to do: your requested leak fix, and better logging — the `[TRACER]` lines all being `tick=0 dist=0` means my 100 ms throttle is hiding the later frames.

Let me also log the client-side bullet tick, so we can see whether it ever advances.

日志很有价值——它确认了速度截断的判断，但也暴露出我的日志节流正好卡在你的射速上，把关键帧全吞了。三处改动 + 需要再跑一次。

## 日志读出来的结论

```
[TRACER] tick=0 dist=0.00 |dv|=74.00 ... bulletPos=(46.30,-57.38,35.02) eye=(46.30,-57.38,35.02)
```

1. **`|dv|=74.00`** —— 客户端子弹的速度是 **74 格/tick**，正是 `_GunProjectileSync` 里绕过 ±3.9 clamp 的未截断值。所以你对 `setShouldReceiveVelocityUpdates(false)` 的判断是对的（TaCZ 也是这么做的），这条同步链路是通的。
2. **四条日志全是 `tick=0 dist=0.00`**，即子弹正好在你的眼睛里，被「距离不足两格」挡掉。这有两种可能：
    - **良性的**：我的节流是 100ms，而你的射速约 110ms/发 —— 每发子弹的 tick=0 那帧正好卡在窗口边界被记下，而它 tick=1（dist≈74）那帧在 50ms 后就到，**必然被节流吞掉**。
    - **恶性**：客户端子弹在第 1 个 tick 之前就被移除，`tickCount` 永远停在 0 → 曳光弹永远被跳过。
3. 另外注意：74 格/tick 的子弹会在 **2 个 tick 内飞出 `clientTrackingRange(8)`=128 格**，服务端随即停发追踪并通知客户端移除实体。也就是说曳光弹在射手侧**天生只有 1~2 帧**，而且那 1~2 帧里子弹已经在 15~74 格外了——这就是「看不见」。

## 改动

1. **修掉你点的旋转泄漏**（[GunProjectileRenderer.java:152](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/client/renderer/entity/GunProjectileRenderer.java:152)）：`_renderAmmoObject` 现在先 `pushPose` 再 `mulPose(Y/X)`，子弹的朝向不再泄漏给后面的曳光弹（TaCZ 原版就是错的写法，CGC 照抄了；你这份枪包因为 `ammo_762_display.json` 没有 `ammo_entity_display` 才没被触发，但配了子弹实体的枪会中招）。
2. **日志改成按子弹计数**：每发子弹记录前 8 帧（不再按时间节流），并加上 `removed` 状态与实体 id。
3. **新增客户端子弹 tick 日志**（[GunProjectile.java:126](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/core/entity/projectile/GunProjectile.java:126)）：`[BULLET-CLIENT]` 打印客户端每 tick 的 `tickCount`、位置、速度。**这是能区分上面「良性/恶性」的关键。**

编译通过。

## 附带发现（未改）

`AmmoParticleSpawner.addParticle` **全项目零调用**——子弹的粒子拖尾是死代码。TaCZ 会沿弹道刷粒子，这可能是你在 TaCZ 里能明显看到「子弹在飞」而 CGC 只有一根瞬时曳光弹的原因之一。要不要接上，等这轮排查完你说。

## 请再跑一次

用当前代码编译进游戏，**对着天空或墙连打一梭子**，然后把 `latest.log` 里这两类行给我：

```
[BULLET-CLIENT] ...
[TRACER] ...
```

我要看的是：客户端子弹的 `tick` 是否推进到 1、2、3，以及 `pos` 有没有真的在变。如果 `tick` 一直是 0，那就是客户端实体在首个 tick 前就被移除了，得从追踪范围/生命周期下手；如果 `tick` 会推进，那就只是「曳光弹只有 1~2 帧」的问题，得改 `clientTrackingRange` 或重新考虑曳光弹的存在时长。

代码未提交、未推送。

# 

```
连续开了10枪，日志如下：
[11Sep2026 17:16:36.051] [Render thread/INFO] [dev.xcolorful.customgun.CustomGun/]: [TRACER] id=47 n=0 tick=0 dist=0.00 |dv|=74.00 trailLen=-1.00 fp=false removed=false camRot=(0.0,0.0) offset=null reducer=0.00 bulletPos=(45.78,-56.38,35.31) eye=(45.78,-56.38,35.31)
[11Sep2026 17:16:36.063] [Render thread/INFO] [dev.xcolorful.customgun.CustomGun/]: [TRACER] id=47 n=1 tick=0 dist=0.00 |dv|=74.00 trailLen=-1.00 fp=false removed=false camRot=(0.0,0.0) offset=null reducer=0.00 bulletPos=(45.78,-56.38,35.31) eye=(45.78,-56.38,35.31)
[11Sep2026 17:16:36.069] [Render thread/INFO] [dev.xcolorful.customgun.CustomGun/]: [TRACER] id=47 n=2 tick=0 dist=0.00 |dv|=74.00 trailLen=-1.00 fp=false removed=false camRot=(0.0,0.0) offset=null reducer=0.00 bulletPos=(45.78,-56.38,35.31) eye=(45.78,-56.38,35.31)
[11Sep2026 17:16:36.076] [Render thread/INFO] [dev.xcolorful.customgun.CustomGun/]: [TRACER] id=47 n=3 tick=0 dist=0.00 |dv|=74.00 trailLen=-1.00 fp=false removed=false camRot=(0.0,0.0) offset=null reducer=0.00 bulletPos=(45.78,-56.38,35.31) eye=(45.78,-56.38,35.31)
[11Sep2026 17:16:36.086] [Render thread/INFO] [dev.xcolorful.customgun.CustomGun/]: [TRACER] id=47 n=4 tick=0 dist=0.00 |dv|=74.00 trailLen=-1.00 fp=false removed=false camRot=(0.0,0.0) offset=null reducer=0.00 bulletPos=(45.78,-56.38,35.31) eye=(45.78,-56.38,35.31)
[11Sep2026 17:16:36.093] [Render thread/INFO] [dev.xcolorful.customgun.CustomGun/]: [BULLET-CLIENT] id=47 n=0 tick=1 pos=(-26.16,-60.40,18.45) dv=(-69.551,-3.913,-16.303) removed=false
[11Sep2026 17:16:36.137] [Render thread/INFO] [dev.xcolorful.customgun.CustomGun/]: [TRACER] id=49 n=0 tick=0 dist=0.00 |dv|=74.00 trailLen=-1.00 fp=false removed=false camRot=(0.0,0.0) offset=null reducer=0.00 bulletPos=(45.78,-56.38,35.31) eye=(45.78,-56.38,35.31)
[11Sep2026 17:16:36.141] [Render thread/INFO] [dev.xcolorful.customgun.CustomGun/]: [BULLET-CLIENT] id=49 n=0 tick=1 pos=(-26.24,-58.94,18.51) dv=(-69.630,-2.502,-16.239) removed=false
[11Sep2026 17:16:36.192] [Render thread/INFO] [dev.xcolorful.customgun.CustomGun/]: [BULLET-CLIENT] id=49 n=1 tick=2 pos=(-95.87,-61.44,2.27) dv=(-67.317,-2.443,-15.699) removed=false
[11Sep2026 17:16:36.223] [Render thread/INFO] [dev.xcolorful.customgun.CustomGun/]: [TRACER] id=51 n=0 tick=0 dist=0.00 |dv|=74.00 trailLen=-1.00 fp=false removed=false camRot=(0.0,0.0) offset=null reducer=0.00 bulletPos=(45.78,-56.38,35.31) eye=(45.78,-56.38,35.31)
[11Sep2026 17:16:36.233] [Render thread/INFO] [dev.xcolorful.customgun.CustomGun/]: [TRACER] id=51 n=1 tick=0 dist=0.00 |dv|=74.00 trailLen=-1.00 fp=false removed=false camRot=(0.0,0.0) offset=null reducer=0.00 bulletPos=(45.78,-56.38,35.31) eye=(45.78,-56.38,35.31)
[11Sep2026 17:16:36.239] [Render thread/INFO] [dev.xcolorful.customgun.CustomGun/]: [BULLET-CLIENT] id=51 n=0 tick=1 pos=(-26.23,-53.20,18.59) dv=(-69.623,3.047,-16.168) removed=false
[11Sep2026 17:16:36.286] [Render thread/INFO] [dev.xcolorful.customgun.CustomGun/]: [BULLET-CLIENT] id=51 n=1 tick=2 pos=(-95.85,-50.16,2.42) dv=(-67.310,2.921,-15.631) removed=false
[11Sep2026 17:16:36.307] [Render thread/INFO] [dev.xcolorful.customgun.CustomGun/]: [TRACER] id=53 n=0 tick=0 dist=0.00 |dv|=74.00 trailLen=-1.00 fp=false removed=false camRot=(0.0,0.0) offset=null reducer=0.00 bulletPos=(45.78,-56.38,35.31) eye=(45.78,-56.38,35.31)
[11Sep2026 17:16:36.317] [Render thread/INFO] [dev.xcolorful.customgun.CustomGun/]: [TRACER] id=53 n=1 tick=0 dist=0.00 |dv|=74.00 trailLen=-1.00 fp=false removed=false camRot=(0.0,0.0) offset=null reducer=0.00 bulletPos=(45.78,-56.38,35.31) eye=(45.78,-56.38,35.31)
[11Sep2026 17:16:36.324] [Render thread/INFO] [dev.xcolorful.customgun.CustomGun/]: [TRACER] id=53 n=2 tick=0 dist=0.00 |dv|=74.00 trailLen=-1.00 fp=false removed=false camRot=(0.0,0.0) offset=null reducer=0.00 bulletPos=(45.78,-56.38,35.31) eye=(45.78,-56.38,35.31)
[11Sep2026 17:16:36.331] [Render thread/INFO] [dev.xcolorful.customgun.CustomGun/]: [TRACER] id=53 n=3 tick=0 dist=0.00 |dv|=74.00 trailLen=-1.00 fp=false removed=false camRot=(0.0,0.0) offset=null reducer=0.00 bulletPos=(45.78,-56.38,35.31) eye=(45.78,-56.38,35.31)
[11Sep2026 17:16:36.339] [Render thread/INFO] [dev.xcolorful.customgun.CustomGun/]: [BULLET-CLIENT] id=53 n=0 tick=1 pos=(-25.96,-48.95,18.78) dv=(-69.363,7.157,-15.981) removed=false
[11Sep2026 17:16:36.388] [Render thread/INFO] [dev.xcolorful.customgun.CustomGun/]: [BULLET-CLIENT] id=53 n=1 tick=2 pos=(-95.33,-41.80,2.80) dv=(-67.059,6.894,-15.450) removed=false
[11Sep2026 17:16:36.389] [Render thread/INFO] [dev.xcolorful.customgun.CustomGun/]: [BULLET-CLIENT] id=55 n=0 tick=1 pos=(-25.55,-46.00,18.57) dv=(-68.961,10.010,-16.183) removed=false
[11Sep2026 17:16:36.438] [Render thread/INFO] [dev.xcolorful.customgun.CustomGun/]: [BULLET-CLIENT] id=55 n=1 tick=2 pos=(-94.51,-35.99,2.39) dv=(-66.670,9.653,-15.645) removed=false
[11Sep2026 17:16:36.484] [Render thread/INFO] [dev.xcolorful.customgun.CustomGun/]: [TRACER] id=57 n=0 tick=0 dist=0.00 |dv|=74.00 trailLen=-1.00 fp=false removed=false camRot=(0.0,0.0) offset=null reducer=0.00 bulletPos=(45.78,-56.38,35.31) eye=(45.78,-56.38,35.31)
[11Sep2026 17:16:36.493] [Render thread/INFO] [dev.xcolorful.customgun.CustomGun/]: [BULLET-CLIENT] id=57 n=0 tick=1 pos=(-25.15,-44.48,17.90) dv=(-68.576,11.477,-16.832) removed=false
[11Sep2026 17:16:36.540] [Render thread/INFO] [dev.xcolorful.customgun.CustomGun/]: [BULLET-CLIENT] id=57 n=1 tick=2 pos=(-93.72,-33.01,1.07) dv=(-66.297,11.071,-16.273) removed=false
[11Sep2026 17:16:36.573] [Render thread/INFO] [dev.xcolorful.customgun.CustomGun/]: [TRACER] id=59 n=0 tick=0 dist=0.00 |dv|=74.00 trailLen=-1.00 fp=false removed=false camRot=(0.0,0.0) offset=null reducer=0.00 bulletPos=(45.78,-56.38,35.31) eye=(45.78,-56.38,35.31)
[11Sep2026 17:16:36.580] [Render thread/INFO] [dev.xcolorful.customgun.CustomGun/]: [TRACER] id=59 n=1 tick=0 dist=0.00 |dv|=74.00 trailLen=-1.00 fp=false removed=false camRot=(0.0,0.0) offset=null reducer=0.00 bulletPos=(45.78,-56.38,35.31) eye=(45.78,-56.38,35.31)
[11Sep2026 17:16:36.586] [Render thread/INFO] [dev.xcolorful.customgun.CustomGun/]: [BULLET-CLIENT] id=59 n=0 tick=1 pos=(-25.51,-45.98,18.42) dv=(-68.925,10.026,-16.328) removed=false
[11Sep2026 17:16:36.637] [Render thread/INFO] [dev.xcolorful.customgun.CustomGun/]: [BULLET-CLIENT] id=59 n=1 tick=2 pos=(-94.43,-35.96,2.09) dv=(-66.635,9.668,-15.785) removed=false
[11Sep2026 17:16:36.652] [Render thread/INFO] [dev.xcolorful.customgun.CustomGun/]: [TRACER] id=61 n=0 tick=0 dist=0.00 |dv|=74.00 trailLen=-1.00 fp=false removed=false camRot=(0.0,0.0) offset=null reducer=0.00 bulletPos=(45.78,-56.38,35.31) eye=(45.78,-56.38,35.31)
[11Sep2026 17:16:36.659] [Render thread/INFO] [dev.xcolorful.customgun.CustomGun/]: [TRACER] id=61 n=1 tick=0 dist=0.00 |dv|=74.00 trailLen=-1.00 fp=false removed=false camRot=(0.0,0.0) offset=null reducer=0.00 bulletPos=(45.78,-56.38,35.31) eye=(45.78,-56.38,35.31)
[11Sep2026 17:16:36.669] [Render thread/INFO] [dev.xcolorful.customgun.CustomGun/]: [TRACER] id=61 n=2 tick=0 dist=0.00 |dv|=74.00 trailLen=-1.00 fp=false removed=false camRot=(0.0,0.0) offset=null reducer=0.00 bulletPos=(45.78,-56.38,35.31) eye=(45.78,-56.38,35.31)
[11Sep2026 17:16:36.676] [Render thread/INFO] [dev.xcolorful.customgun.CustomGun/]: [TRACER] id=61 n=3 tick=0 dist=0.00 |dv|=74.00 trailLen=-1.00 fp=false removed=false camRot=(0.0,0.0) offset=null reducer=0.00 bulletPos=(45.78,-56.38,35.31) eye=(45.78,-56.38,35.31)
[11Sep2026 17:16:36.682] [Render thread/INFO] [dev.xcolorful.customgun.CustomGun/]: [TRACER] id=61 n=4 tick=0 dist=0.00 |dv|=74.00 trailLen=-1.00 fp=false removed=false camRot=(0.0,0.0) offset=null reducer=0.00 bulletPos=(45.78,-56.38,35.31) eye=(45.78,-56.38,35.31)
[11Sep2026 17:16:36.687] [Render thread/INFO] [dev.xcolorful.customgun.CustomGun/]: [BULLET-CLIENT] id=61 n=0 tick=1 pos=(-25.38,-43.48,19.67) dv=(-68.805,12.445,-15.119) removed=false
[11Sep2026 17:16:36.737] [Render thread/INFO] [dev.xcolorful.customgun.CustomGun/]: [BULLET-CLIENT] id=61 n=1 tick=2 pos=(-94.19,-31.04,4.55) dv=(-66.520,12.007,-14.616) removed=false
[11Sep2026 17:16:36.737] [Render thread/INFO] [dev.xcolorful.customgun.CustomGun/]: [BULLET-CLIENT] id=63 n=0 tick=1 pos=(-24.92,-41.29,19.54) dv=(-68.361,14.562,-15.241) removed=false
[11Sep2026 17:16:36.795] [Render thread/INFO] [dev.xcolorful.customgun.CustomGun/]: [BULLET-CLIENT] id=63 n=1 tick=2 pos=(-93.29,-26.73,4.30) dv=(-66.090,14.053,-14.735) removed=false
[11Sep2026 17:16:36.826] [Render thread/INFO] [dev.xcolorful.customgun.CustomGun/]: [TRACER] id=65 n=0 tick=0 dist=0.00 |dv|=74.00 trailLen=-1.00 fp=false removed=false camRot=(0.0,0.0) offset=null reducer=0.00 bulletPos=(45.78,-56.38,35.31) eye=(45.78,-56.38,35.31)
[11Sep2026 17:16:36.833] [Render thread/INFO] [dev.xcolorful.customgun.CustomGun/]: [TRACER] id=65 n=1 tick=0 dist=0.00 |dv|=74.00 trailLen=-1.00 fp=false removed=false camRot=(0.0,0.0) offset=null reducer=0.00 bulletPos=(45.78,-56.38,35.31) eye=(45.78,-56.38,35.31)
[11Sep2026 17:16:36.840] [Render thread/INFO] [dev.xcolorful.customgun.CustomGun/]: [BULLET-CLIENT] id=65 n=0 tick=1 pos=(-24.28,-37.77,20.43) dv=(-67.734,17.963,-14.380) removed=false
[11Sep2026 17:16:36.890] [Render thread/INFO] [dev.xcolorful.customgun.CustomGun/]: [BULLET-CLIENT] id=65 n=1 tick=2 pos=(-92.01,-19.81,6.05) dv=(-65.484,17.342,-13.902) removed=false

另外，粒子拖尾也要补上
```

# 

**FOUND IT.** The log is decisive — the client bullet ticks normally (tick 1 → 74 blocks, tick 2 → 145 blocks) but the **renderer is never called after tick 0**. That's entity culling. Let me confirm against vanilla.

**Root cause of "invisible" confirmed.** `Entity.shouldRenderAtSqrDistance` limits rendering to `boundingBoxSize × 64` blocks — the projectile's box is 0.0625, so it's culled beyond **4 blocks**. TaCZ overrides `shouldRender` in its bullet renderer precisely to bypass this; CGC's `GunProjectileRenderer` is missing that override. Let me look at the effect manager for the particle trail.

Now I have the complete picture. Implementing the missing `shouldRender` override and the particle trail.

**根因找到了，是实体裁剪。** 日志给出了决定性证据。

## 日志说明了什么

```
[TRACER] id=47 n=0..4  tick=0 dist=0.00 ...          ← 同一个 tick 内渲染了 5 帧（dist=0，被「距离两格」挡掉）
[BULLET-CLIENT] id=47 n=0 tick=1 pos=(-26.16,-60.40,18.45)   ← 子弹确实 tick 了，飞到 74 格外
（然后 id=47 再也没有任何 [TRACER] 行）
```

子弹在**客户端确实按 74 格/tick 正常飞行**（tick=1 距眼 74 格，tick=2 距眼约 145 格），但 **`render` 在 tick=1 之后就再也没被调用过**。原因在原版：

```java
// Entity.shouldRenderAtSqrDistance
double d0 = this.getBoundingBox().getSize();   // 枪射物碰撞箱 0.0625
d0 *= 64.0D * viewScale;                       // = 4.0
return pDistanceSqr < d0 * d0;                 // → 只能在 4 格内渲染
```

`GunProjectileRenderer` 没有覆写 `shouldRender`，于是走默认逻辑：**子弹一出 4 格就被裁掉，`render` 根本不执行**。tick=0 时子弹在摄像机里（所以 render 被调了 5 次，都被「距离两格」跳过），tick=1 一飞出去就没了。

TaCZ 的 `EntityBulletRenderer` 正是为此覆写了 `shouldRender`（跳过距离判定，只留视锥体裁剪），CGC 迁移时漏了。

**这同时解释了之前那个「绕玩家一圈」**：当时 `setShouldReceiveVelocityUpdates` 还是默认 true，客户端子弹被 motion 包截断成 ≤3.9 格/tick，恰好留在 4 格内所以能渲染，但 `offsetReducer≈0.92` 把枪口偏移全量叠加 → 起点脱离弹道。现在速度正确（74），`offsetReducer = max(0, 50-74)/50 = 0`，偏移自然归零。

## 本轮改动

1. **补上 `shouldRender` 覆写**（[GunProjectileRenderer.java:239](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/client/renderer/entity/GunProjectileRenderer.java:239)）——就是上面那条，曳光弹从此能画出来。
2. **修旋转泄漏**（你点的）：`_renderAmmoObject` 改为先 `pushPose` 再 `mulPose(Y/X)`，子弹朝向不再污染曳光弹。
3. **接上粒子拖尾**：客户端 Mixin 在 `GunProjectile.tick()` 的 RETURN 处调 `AmmoParticleSpawner.addParticle`（[GunProjectileMixin.java:44](app://localhost/epitaxy/forge-compat/src/main/java/dev/xcolorful/customgun/forgeclient/mixin/entity/GunProjectileMixin.java:44)），对应 TaCZ `EntityKineticBullet.tick` 里那句 `if (level.isClientSide) AmmoParticleSpawner.addParticle(this)`。

编译通过（只剩项目原有的 deprecated 警告）。

## 两点提醒

- **粒子拖尾取决于资源包数据**：`AmmoParticleSpawner` 先取枪械 display 的 `ammo.particle`，再取弹药 display 的 `ammo_particle`。`beryl_m762_display.json` 的 `ammo` 段只有 `tracer_color`，`ammo_762_display.json` 也没有 `particle` —— 所以这把枪可能依旧没有粒子（TaCZ 默认枪包是有的）。要看到效果可能得换 `tacz_default_gun` 的枪测。
- **曳光弹会是一道「远处的短闪」**：子弹飞出 74 格才第一次能被画，而客户端实体最多活 2 tick（tick=2 已到 145 格，超出 `clientTrackingRange(8)`=128 格，服务端停发追踪并移除实体）。要让它更耐久，得调大 `clientTrackingRange`。这个我没动，等你决定。

## 请再跑一次

`[TRACER]` / `[BULLET-CLIENT]` 两行探测日志我先留着，方便你这次确认。预期能看到形如：

```
[TRACER] ... tick=1 dist=74.xx ... fp=true ... offset=(...) reducer=0.00
```

即 render 真的被调到了。确认没问题我就把探测代码删掉。代码未提交、未推送。

# 

```
成功修复，日志如下：
[11Sep2026 17:26:11.871] [Server thread/DEBUG] [dev.xcolorful.customgun.CustomGun/cgc_sync_base_timestamp]: Update XiaoColorful server base timestamp: 1789118771870
[11Sep2026 17:26:14.798] [Render thread/INFO] [dev.xcolorful.customgun.CustomGun/]: [BULLET-CLIENT] id=65 n=0 tick=1 pos=(-23.41,-47.85,40.20) dv=(-64.301,9.193,29.977) removed=false
[11Sep2026 17:26:14.807] [Render thread/INFO] [dev.xcolorful.customgun.CustomGun/]: [TRACER] id=65 n=0 tick=1 dist=1.48 |dv|=71.54 trailLen=-1.00 fp=false removed=false camRot=(0.0,0.0) offset=null reducer=0.00 bulletPos=(41.77,-57.19,9.81) eye=(43.10,-57.38,9.19)
[11Sep2026 17:26:14.823] [Render thread/INFO] [dev.xcolorful.customgun.CustomGun/]: [TRACER] id=65 n=1 tick=1 dist=28.12 |dv|=71.54 trailLen=22.50 fp=true removed=false camRot=(-8.6,64.8) offset=(0.24,-0.33,-2.07) reducer=0.44 bulletPos=(17.82,-53.76,20.97) eye=(43.10,-57.38,9.19)
[11Sep2026 17:26:14.838] [Render thread/INFO] [dev.xcolorful.customgun.CustomGun/]: [TRACER] id=65 n=2 tick=1 dist=50.32 |dv|=71.54 trailLen=40.26 fp=true removed=false camRot=(-8.6,64.8) offset=(0.24,-0.33,-2.07) reducer=0.00 bulletPos=(-2.13,-50.90,30.28) eye=(43.10,-57.38,9.19)
[11Sep2026 17:26:14.853] [Render thread/INFO] [dev.xcolorful.customgun.CustomGun/]: [TRACER] id=67 n=0 tick=0 dist=0.00 |dv|=74.00 trailLen=-1.00 fp=false removed=false camRot=(0.0,0.0) offset=null reducer=0.00 bulletPos=(43.10,-57.38,9.19) eye=(43.10,-57.38,9.19)
[11Sep2026 17:26:14.860] [Render thread/INFO] [dev.xcolorful.customgun.CustomGun/]: [BULLET-CLIENT] id=67 n=0 tick=1 pos=(-23.42,-48.65,40.43) dv=(-64.305,8.413,30.198) removed=false
[11Sep2026 17:26:14.872] [Render thread/INFO] [dev.xcolorful.customgun.CustomGun/]: [TRACER] id=67 n=1 tick=1 dist=19.24 |dv|=71.54 trailLen=15.39 fp=true removed=false camRot=(-9.7,64.7) offset=(0.24,-0.33,-2.05) reducer=0.62 bulletPos=(25.80,-55.11,17.31) eye=(43.10,-57.38,9.19)
[11Sep2026 17:26:14.915] [Render thread/INFO] [dev.xcolorful.customgun.CustomGun/]: [BULLET-CLIENT] id=67 n=1 tick=2 pos=(-87.72,-40.24,70.63) dv=(-62.168,8.109,29.195) removed=false
[11Sep2026 17:26:14.926] [Render thread/INFO] [dev.xcolorful.customgun.CustomGun/]: [TRACER] id=67 n=2 tick=2 dist=98.32 |dv|=69.16 trailLen=58.79 fp=true removed=false camRot=(-9.7,64.7) offset=(0.24,-0.33,-2.05) reducer=0.00 bulletPos=(-45.28,-45.79,50.70) eye=(43.10,-57.38,9.19)
[11Sep2026 17:26:14.942] [Render thread/INFO] [dev.xcolorful.customgun.CustomGun/]: [TRACER] id=67 n=3 tick=2 dist=126.94 |dv|=69.16 trailLen=58.79 fp=true removed=false camRot=(-9.7,64.7) offset=(0.24,-0.33,-2.05) reducer=0.00 bulletPos=(-71.00,-42.43,62.77) eye=(43.10,-57.38,9.19)
[11Sep2026 17:26:14.949] [Render thread/INFO] [dev.xcolorful.customgun.CustomGun/]: [BULLET-CLIENT] id=67 n=2 tick=3 pos=(-149.89,-32.13,99.82) dv=(-60.103,7.815,28.225) removed=false
[11Sep2026 17:26:14.956] [Render thread/INFO] [dev.xcolorful.customgun.CustomGun/]: [TRACER] id=67 n=4 tick=3 dist=149.69 |dv|=66.86 trailLen=56.83 fp=true removed=false camRot=(-9.7,64.7) offset=(0.24,-0.33,-2.05) reducer=0.00 bulletPos=(-91.45,-39.75,72.38) eye=(43.10,-57.38,9.19)
[11Sep2026 17:26:14.970] [Render thread/INFO] [dev.xcolorful.customgun.CustomGun/]: [TRACER] id=67 n=5 tick=3 dist=169.05 |dv|=66.86 trailLen=56.83 fp=true removed=false camRot=(-9.7,64.7) offset=(0.24,-0.33,-2.05) reducer=0.00 bulletPos=(-108.86,-37.48,80.55) eye=(43.10,-57.38,9.19)
[11Sep2026 17:26:14.985] [Render thread/INFO] [dev.xcolorful.customgun.CustomGun/]: [TRACER] id=67 n=6 tick=3 dist=189.80 |dv|=66.86 trailLen=56.83 fp=true removed=false camRot=(-9.7,64.7) offset=(0.24,-0.33,-2.05) reducer=0.00 bulletPos=(-127.51,-35.05,89.31) eye=(43.10,-57.38,9.19)
[11Sep2026 17:26:14.996] [Render thread/INFO] [dev.xcolorful.customgun.CustomGun/]: [TRACER] id=67 n=7 tick=3 dist=207.78 |dv|=66.86 trailLen=56.83 fp=true removed=false camRot=(-9.7,64.7) offset=(0.24,-0.33,-2.05) reducer=0.00 bulletPos=(-143.67,-32.94,96.90) eye=(43.10,-57.38,9.19)

- 如果测试日志没问题，就移除掉
- 我拿狙击枪（有拖尾）试了下，粒子看起来像是从第2/后续tick才开始生成的？改成从第1tick的移动路线上就要生成
- clientTrackingRange(8)里的数字对服务端的性能影响怎么表现？改成320/16=20是否有影响？
```

# 

修复确认。先看 TaCZ 的粒子实现，再动粒子逻辑。

三处都清楚了：粒子的问题是我的注入点。TaCZ 在 `tick()` 里**移动之前**调 `addParticle`，我却注在 `RETURN`（移动之后）——粒子被推到子弹前方一个 tick 的位置，所以看起来从第 2 tick 才有拖尾。

三处都处理完了，编译通过（只剩项目原有的 deprecated 警告）。

## 1. 探测日志已移除

`[TRACER]` / `[BULLET-CLIENT]` 两段临时代码连同 `CustomGun` import 全部删掉，`grep` 已确认无残留。

## 2. 粒子拖尾：注入点错了

TaCZ 在 `EntityKineticBullet.tick()` 里是这样排的：

```java
super.tick();
if (level.isClientSide) AmmoParticleSpawner.addParticle(this);   // ← 位移之前
... 计算旋转、setPos(下一位置)、更新速度 ...                        // ← 位移
```

而 `AmmoParticleSpawner.createParticle` 撒点的公式是：

```java
double offsetX = random.nextGaussian() * delta.x() + random.nextDouble() * deltaMovement.x;
double posX = bullet.getX() + offsetX;    // 当前位置 + 一个 tick 位移的随机比例
```

**它假设 `bullet.getX()` 是本 tick 位移之前的位置**，靠 `+ rand × 位移` 把粒子铺满这一 tick 走过的整段。我上一版注在 `@At("RETURN")`，位置已经是位移之后的 → 粒子被推到了子弹**前方**一个 tick（约 74 格），所以你看到拖尾从第 2 tick 才出现。已改成 `@At("HEAD")`，与 TaCZ 等价，从第 1 tick 的路径开始铺。

## 3. `clientTrackingRange` 的性能与取值

先说结论：**改成 20 基本没有额外代价，因为原版给它加了一道 view distance 的天花板。**

`ChunkMap.TrackedEntity.updatePlayer`：

```java
Vec3 vec3 = pPlayer.position().subtract(this.entity.position());
double d0 = (double) Math.min(this.getEffectiveRange(), ChunkMap.this.viewDistance * 16);
double d1 = vec3.x * vec3.x + vec3.z * vec3.z;   // 只看水平距离，忽略 Y
boolean flag = d1 <= d0 * d0 && this.entity.broadcastToPlayer(pPlayer);
if (flag) { if (this.seenBy.add(conn)) this.serverEntity.addPairing(player); }
else if (this.seenBy.remove(conn)) this.serverEntity.removePairing(player);
```

- `clientTrackingRange(N)` 的单位是**区块**，`ChunkMap.addEntity` 里 `entitytype.clientTrackingRange() * 16` 换算成格。
- 实际生效半径 = `min(N×16, viewDistance×16)`。**所以只要服务端 view distance ≤ 20 区块，写 20 和写 10 完全等价**——不会多发一个包，也不会让曳光弹多活一帧。想让 320 真正生效，`server.properties` 的 `view-distance` 得 ≥ 20。
- 成本构成：
    - **每 tick**：`ChunkMap.tick()` 遍历所有被追踪实体，凡是**区块发生变化**的就对全部玩家扫一遍距离判定。子弹 74 格/tick ≈ 4.6 区块/tick，等于**每 tick 都在触发这个扫描**。每次判定只是一次减法加平方比较，但这个开销**与 `clientTrackingRange` 的数值无关**（取决于在场玩家数与飞行中的子弹数）。
    - **与数值相关的部分**：被 pair 的玩家数 → 每个配对 = 1 个 spawn 包 + 每 `updateInterval`(4 tick) 一次位置更新 + 1 个 remove 包。范围越大，收到子弹实体的客户端越多。
    - **客户端**：每个被 pair 的客户端会多养一个实体（tick + 渲染），成本极小。
- **实用建议**：曳光弹本来就只在客户端渲染距离内可见，把范围开到超过 view distance 纯属浪费。**8 → 到你服务端 view distance 的值**是划算的（例如 view distance 12 → 写 12，子弹能多活约 2 tick、曳光弹从 ~2 帧变成 ~4 帧）。真要更长（比如 32 = 512 格），得同时把 view distance 也拉上去，那时成本才真正上来。这个我没改，你定。

代码未提交、未推送。当前工作区改动：`GunProjectileRenderer`（颜色归一化、曳光弹渲染条件、`shouldRender`、旋转泄漏）、`_GunProjectileSync` + `ModEntities`（你的）、`forgeclient/mixin/entity/GunProjectileMixin`（粒子拖尾）。

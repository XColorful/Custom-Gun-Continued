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

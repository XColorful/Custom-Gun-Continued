> #79 提示词

# 

```
将当前最新commit的wiki部分同步到.wiki仓库，注意文件名带横杠
```

# 

我先确认两个仓库的状态以及最新提交涉及的 wiki 文件。

源码仓库最新提交正是 wiki 变更。我对比两边的 Home 与 Sidebar，再按横杠命名同步。

现在把三处变更写入 wiki 仓库：版本号、两个导航链接，以及新建 `Shooter-config.md`。

已把最新提交 `349510b48` 的 3 个 wiki 文件同步到 `.wiki` 仓库：

- [Home.md](../Custom-Gun-Continued.wiki/Home.md)：中英文版本号更新为 `26.10.29`，并在双端配置/Common configuration 下加入射手配置链接
- [_Sidebar.md](../Custom-Gun-Continued.wiki/_Sidebar.md)：中英文侧边栏各加入一条射手配置链接
- [Shooter-config.md](../Custom-Gun-Continued.wiki/Shooter-config.md)：新建，文件名按要求使用横杠（源文件为 `Shooter config.md`）

校验结果：`Home.md` 和 `_Sidebar.md` 的 diff 与源码提交完全一致；新页面内容与源码相同，仅补了一个文件末尾换行（与原仓库其他配置文档一致）。当前 `.wiki` 仓库改动未提交、未推送，待你审阅。

# 

```
补全EnableSprintWhileAiming
```

# 

我先查 `SPRINT_ON_AIMING` 的使用位置和当前语义。

`SyncConfig` 里还缺字段声明和构建项，我来补上；同时移除 `LivingShooterSprint` 中未使用的 `ShooterConfig` 导入。

编译通过。再确认 `/customgun config` 指令是否需要同步暴露这个选项。

`EnableSprintWhileAiming` 已补全：

- [SyncConfig.java:73](core/src/main/java/dev/xcolorful/customgun/core/config/SyncConfig.java:73) 新增 `ENABLE_SPRINT_WHILE_AIMING` 字段
- [SyncConfig.java:104](core/src/main/java/dev/xcolorful/customgun/core/config/SyncConfig.java:104) 在 `misc` 段注册，默认值 `false`，位置与 `ModConfigTag` 及 wiki 文档顺序一致（`MinPronePitch` 之后、`EnableDefaultGunSmithTableFilter` 之前）
- [LivingShooterSprint.java:7](core/src/main/java/dev/xcolorful/customgun/core/entity/shooter/LivingShooterSprint.java:7) 移除了未使用的 `ShooterConfig` 导入

默认 `false` 与迁移前 `SPRINT_ON_AIMING == false` 的行为一致。`:core:compileJava` 编译通过。

另外注意：源仓库的 [Sync config.md](docs/wiki/configuration/Sync%20config.md) 已有该条目，但 `.wiki` 仓库的 `Sync-config.md` 尚未同步。需要我一并同步过去吗？未提交、未推送。

# 

```
继续补ENABLE_SPRINT_WHILE_RELOADING
```

# 

`EnableSprintWhileReloading` 已补全：

- [ModConfigTag.java:144](core/src/main/java/dev/xcolorful/customgun/core/api/config/ModConfigTag.java:144) 新增 `EnableSprintWhileReloading` 的路径与注释常量
- [SyncConfig.java:108](core/src/main/java/dev/xcolorful/customgun/core/config/SyncConfig.java:108) 在 `misc` 段注册，默认 `false`，紧跟在 `EnableSprintWhileAiming` 之后
- [Sync config.md](docs/wiki/configuration/Sync%20config.md) 修正了复制粘贴导致的重复 `EnableSprintWhileAiming` 条目，中英文清单和 TOML 示例均已补上 `EnableSprintWhileReloading`

默认 `false` 保持迁移前 `SPRINT_ON_RELOADING == false` 的行为。`:core:compileJava` 编译通过。

`.wiki` 仓库的 `Sync-config.md` 仍未同步这两个新增项（之前的询问未得到指示），需要的话我可以一并处理。改动未提交、未推送。

# 

```
目前即使ENABLE_SPRINT_WHILE_RELOADING true之后，装弹仍然会中断一次疾跑，装弹时仍然能手动疾跑
排查一下哪里还有拦截
```

# 

我来排查所有会中断疾跑的位置。

找到拦截点了：`LivingShooterAim._doTickSprint()` 在服务端每 tick 无条件关闭疾跑，完全没看两个新配置。我来修复。

排查完毕，根因找到了。

## 拦截点

[LivingShooterAim.java:187](core/src/main/java/dev/xcolorful/customgun/core/entity/shooter/LivingShooterAim.java:187) 的 `_doTickSprint()` 在服务端每 tick 强制 `setSprinting(false)`，条件是「瞄准中或换弹中」，完全没有读取两个新配置。服务端把冲刺标记置为 false 后会通过实体数据同步到客户端，所以表现为：

- 换弹开始的瞬间疾跑被切断一次；
- 之后客户端本地输入重新开启疾跑，服务端也不会再回传变化，因此又能手动疾跑。

该处同时忽略了 `ENABLE_SPRINT_WHILE_AIMING`，瞄准时也存在同样的「切一次疾跑」问题。

## 已修复

现在分别按配置判断，保留原有 `aimingProgress` / 同步换弹状态的条件：

```java
boolean disableSprintByAiming = this.shooterProperty.aimingProgress > 0
        && !SyncConfig.ENABLE_SPRINT_WHILE_AIMING.get();
boolean disableSprintByReloading = reloadState.getStateType().isReloading()
        && !reloadState.getStateType().isReloadFinishing()
        && !SyncConfig.ENABLE_SPRINT_WHILE_RELOADING.get();
if (disableSprintByAiming || disableSprintByReloading) this.livingShooter.setSprinting(false);
```

`:core:compileJava` 编译通过。

## 另一个相关耦合（未改动，需你确认）

[LocalPlayerMixin.java:57](core/src/main/java/dev/xcolorful/customgun/client/mixin/entity/LocalPlayerMixin.java:57) 的 `cgc$swapSprintStatus` 在玩家开始疾跑时会无条件调用 `cancelReload()`。也就是说开启 `EnableSprintWhileReloading` 后，“手动疾跑”之所以能用，是以**取消换弹**为代价的——两者并不能真正同时进行。如果预期是“边换弹边疾跑”，这里也应加配置判断；若预期是“疾跑仍打断换弹，只是换弹不再打断疾跑”，则保持现状。要改吗？

# 

```
检查下当前工作区改动是否成立
```

# 

我完整检查一下工作区改动。

工作区相比上次又有调整：`tickSprint` 已从 `LivingShooterAim` 整体搬到 `LivingShooterSprint`，并对 `cancelReload` 加了配置判断。我做编译和引用检查来验证。

检查完成，改动成立。验证结果：

## 编译

`:core:compileJava` 与 `:forge-compat:compileJava` 均 `BUILD SUCCESSFUL`（JDK 17）。

## 逐项核对

| 改动 | 结论 |
|---|---|
| `tickSprint` 从 `LivingShooterAim` 移至 `LivingShooterSprint` | 正确。全仓库仅剩一个调用点 [LivingEntityMixin.java:71](core/src/main/java/dev/xcolorful/customgun/core/mixin/entity/LivingEntityMixin.java:71)，且 `cgc$sprint` 已正确定义；tick 内执行顺序未变（`tickReloadState` → `tickAimingProgress` → `tickSprint`） |
| `LivingShooterAim` 移除 `ReloadState` 导入 | 正确。该符号已无引用，其余导入仍被使用 |
| `_doTickSprint` 的两个配置判断 | 正确。`&&` 优先级高于 `||`，实际条件为 `(瞄准 && !配置) \|\| (换弹 && !配置)` |
| `LocalPlayerMixin` 的 `cancelReload` 加配置判断 | 正确。开启配置后手动疾跑不再取消换弹，与“边换弹边疾跑”的语义一致；服务端 `ServerGamePacketListenerImplMixin` 没有第二处取消换弹的逻辑 |
| `SyncConfig` 注册 | 完整。两个字段均声明并在 `misc` 中 `addConfig(..., false)`；`ModConfigTag` 常量齐全 |
| `.wiki` 仓库 | 工作区干净，两个配置已分别提交（`95e12de`、`57428c3`） |

## 两点提示（非阻塞）

1. [LivingShooterSprint.java:88](core/src/main/java/dev/xcolorful/customgun/core/entity/shooter/LivingShooterSprint.java:88) 的复合条件建议给两个分支各加一层括号，当前依赖优先级虽正确，但可读性和后续修改安全性差一些。
2. 该处的瞄准判断用 `shooterProperty.aimingProgress > 0`，而 `isIllegalSprintState` 用 `cgc$getSynIsAiming()`，两处语义略有差异（后者是目标状态，前者包含收镜过渡）。这是迁移前就存在的双源判断，本次未改变行为；若以后要统一，需单独确认收镜过渡期是否仍应禁止疾跑。

未提交、未推送。

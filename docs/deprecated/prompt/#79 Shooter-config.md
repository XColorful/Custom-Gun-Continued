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

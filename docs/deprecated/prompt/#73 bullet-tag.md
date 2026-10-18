> #73 提示词

# 

```
浏览当前git暂存区，需要完成一个功能：
- 注入到Entity#isInvulnerableTo，最好是能先获取原返回结果是否为false，如果为false则植入检测；否则注入到开头也行
- BypassBulletType用法同InteractKeyType，但目前只使用ENTITY

判定逻辑：
- 当伤害为ModDamageTypes#BULLET_DAMAGE（默认模组内置注册的四种伤害类型都在）时判定：
- 如果伤害不是PIERCE_DAMAGE且该实体有BypassBulletType.ENTITY#getArmorEnchantment，则视为无敌返回true
- 如果伤害不是BYPASS_DAMAGE且该实体有BypassBulletType.ENTITY#getInvulnerabilityEnchantment，则视为无敌返回true

为了测试，把展示框实体加进invulnerability_enchantment.json以测试效果
```

# 

我先查看暂存区改动和相关现有实现（InteractKeyType、伤害类型等），再规划注入方式。

查看 InteractKeyType 的完整实现、伤害类型定义和模组注册的伤害 JSON。

实现思路已明确：用 `@At("RETURN")` 拿到原返回值，仅当原值为 `false` 时按标签判定。现在修改 mixin 并补测试标签。

修改完成。按记忆中的约定，编译需指定 JDK 17，我先读一下具体路径再编译验证。

编译只报了编码问题（既有中文注释在 windows-1252 下无法映射），需要确认 Gradle 是否配置了 UTF-8。

项目未配置 UTF-8 编码，我在命令行注入 `file.encoding` 后重编译验证（不改项目文件）。

`UP-TO-DATE` 说明没有真正重编，我强制重跑以确保新代码被编译。

编译通过。按项目约定，我用 IDEA MCP 核对 1.20.1 中 `HangingEntity`/`ItemFrame` 是否也有 `isInvulnerableTo` 覆盖，确保测试用的展示框会走到注入点。

`HangingEntity` 未覆盖 `isInvulnerableTo`，且 `ItemFrame.hurt` 会调用它，注入点有效。再跑一次完整打包验证 mixin 处理链路。

功能已完成并通过编译与打包验证。

## 改动内容

**注入逻辑** — [EntityMixin.java:67](core/src/main/java/dev/xcolorful/customgun/core/mixin/entity/EntityMixin.java:67)
- 使用 `@Inject(method = "isInvulnerableTo", at = @At("RETURN"), cancellable = true)`，先通过 `cir.getReturnValueZ()` 读取原返回值
- 原值为 `true` 直接返回，不改动；为 `false` 时才进入检测
- 伤害必须命中 `ModDamageTypes.BULLET_DAMAGE`（四种内置伤害类型都在该标签内）
- 非 `PIERCE_DAMAGE` 且实体类型在 `bypass_bullet/armor_enchantment` 标签 → 无敌
- 非 `BYPASS_DAMAGE` 且实体类型在 `bypass_bullet/invulnerability_enchantment` 标签 → 无敌

**测试标签** — [invulnerability_enchantment.json](core/src/main/resources/data/customgun/tags/entity_types/bypass_bullet/invulnerability_enchantment.json) 添加了 `minecraft:item_frame`。

## 验证

- `:core:compileJava` 与 `:forge-compat:jarJar` 均构建成功（仅既有 deprecation 警告）
- 已核对 1.20.1 源码：`HangingEntity` 未覆盖 `isInvulnerableTo`，`ItemFrame.hurt` 会调用它，因此测试用例会走到注入点；普通子弹应无法破坏展示框，`piercer` 子弹可以

两点说明：
1. 只加了 `minecraft:item_frame`，荧光物品展示框（`minecraft:glow_item_frame`）是独立实体类型，未包含；需要的话我可以补上。
2. 命令行构建需要 `JAVA_HOME` 指向 JDK 17 且注入 `-Dfile.encoding=UTF-8`，否则中文注释会触发编码编译错误（此问题为既有情况，未改动构建文件）。

按项目规则未提交，改动留在工作区等你审查。

# 

```
按照git暂存区现有wiki设计，继续按相同方式完成RaytraceType，植入到ProjectilePhysicsManager的调用里，并把已有的ModBlocks#BULLET_IGNORE_BLOCKS的所有调用处替换为RaytraceType
```

# 

我先查看 `ModBlocks#BULLET_IGNORE_BLOCKS`、`ProjectilePhysicsManager` 及现有标签结构。

看起来 `RaytraceType`/`RaytraceTypeTag` 已存在（可能是最近新建），我先读取它们的现状、相关调用点和设计文档。

暂存区里已有 RaytraceType 骨架和 wiki/标签文件，我先读取这些设计内容。

设计清晰：`RaytraceType` 提供 `BLOCK`/`ENTITY` 两类 `raytrace/projectile_passthrough` 标签。现在完成枚举与常量类，并接入方块/实体两条射线过滤。

所有代码调用点已替换。现在编译验证。

依赖解析出现 `net.minecraft:client:1.20.1` 缺失，像是缓存/仓库瞬时问题；重试一次并改用 forge-compat 编译链路。

构建缓存目录消失了（上一轮还存在），先确认 Gradle 缓存位置与网络状态。

Minecraft 版本缓存还在，缺少的是 ForgeGradle 的 bundled 仓库。我查一下 FG 的实现以确定正确的恢复任务。

网络/缓存恢复后编译成功。再强制重编并打包确认。

完成。`RaytraceType` 已按暂存区的 wiki 设计实现并接入两条射线过滤。

## 改动

**[RaytraceType.java](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/core/api/resource/data/tag/RaytraceType.java)** / **[RaytraceTypeTag.java](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/core/api/resource/data/tag/RaytraceTypeTag.java)**

- 枚举 `BLOCK` / `ENTITY`，对应标签 `customgun:raytrace/projectile_passthrough`
- 提供 `getProjectilePassthrough()`，写法与 `InteractKeyType`、`BypassBulletType` 一致

**方块射线**（`RaytraceType.BLOCK`）

- [ProjectilePhysicsManager.java:105](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/core/projectile/physics/ProjectilePhysicsManager.java:105)
- [_TempExplode.java:357](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/core/projectile/impact/_TempExplode.java:357)
- [BlockRayTrace.java:26](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/core/util/BlockRayTrace.java:26)

**实体射线**（`RaytraceType.ENTITY`）

- [ProjectilePhysicsManager.java:63](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/core/projectile/physics/ProjectilePhysicsManager.java:63) 的 `PROJECTILE_TARGETS` 排除标签内实体，`_EntityUtils` 复用该谓词时同样生效

`ModBlocks#BULLET_IGNORE_BLOCKS` 的代码调用点已全部替换，当前工程内无剩余引用；`ModBlocks` 中的常量声明保留未动（已标记 `@Deprecated(forRemoval = true)`）。如果确认要删除该类和旧的 `bullet_ignore` 标签机制，我可以再处理。

## 验证

`:forge-compat:jarJar --rerun-tasks` 全量构建成功。期间遇到一次 ForgeGradle 的 `net.minecraft:client:1.20.1` 解析失败，是 `mcp_config` 缓存瞬时缺失/网络波动，重试后自动恢复，未改动任何项目或构建配置。

改动未提交，等你审查。

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

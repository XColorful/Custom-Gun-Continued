> #67 提示词

# 

```
按照dev.xcolorful.customgun.client.compat.shouldersurfing.ShoulderSurfingCompat相同的模式，完成dev.xcolorful.customgun.core.compat.playerrevive.PlayerReviveCompat和dev.xcolorful.customgun.client.compat.playerrevive.PlayerReviveClientCompat内置的反射兼容
- PlayerRevive可在CGC-Compat-Revive环境中读取
- 使用PlayerRevive API可参考BattleRoyale xiao.battleroyale.compat.playerrevive.PlayerRevive，但以CGC-Compat-Revive环境中的版本为准
- PlayerReviveClientCompat使用的API需要检查PlayerRevive在本地的信息存在哪
- 修改范围仅限于两个Compat类
```

# 

I'll start by reading the ShoulderSurfing pattern and the two PlayerRevive skeletons, plus the BattleRoyale reference.

Now let me look at how these compat methods are used in CGC, including the two modified files.

The compat mod itself is the best source of design intent — let me read its implementation.

Let me decompile the key classes via IDEA to see exactly how client-side state is stored.

Let me check the project docs and any config infrastructure for these compat hooks.

The client pattern is now confirmed — PlayerRevive's own client code calls the same API. Let me check how other compat classes handle reflection init failures.

Let me verify the project's Java level and whether `instanceof` pattern matching is used elsewhere.

I have everything I need. Implementing the core class first.

Now the client class.

Let me review both finished files.

Now let me verify it compiles. First, the project layout for this branch.

Running the compile (warm build) in the background.

Compile succeeds. Let me confirm the top of the core file and check the final diff.

两个 Compat 类的内置反射已实现，`./gradlew :core:compileJava` 通过。

**验证到的 PlayerRevive API（本地 CGC-Compat-Revive 依赖的 `playerrevive-266890-6048921.jar`，版本 2.0.31 / MC 1.20.1）**

| 反射目标 | 签名 |
|---|---|
| `team.creative.playerrevive.server.PlayerReviveServer` | `isBleeding(Player): boolean`、`getBleeding(Player): IBleeding`、`revive(Player)`、`kill(Player)` |
| `team.creative.playerrevive.api.IBleeding` | `revivingPlayers(): List<Player>` |

与 BattleRoyale 参考实现一致，包名/方法名以本地 jar 为准（`getBleeding` 确认仍叫这个名字）。

**客户端 API 的落点**：客户端倒地状态就在 `PlayerRevive.BLEEDING` 能力上——`ReviveEventServer.attachCapability` 挂在 `AttachCapabilitiesEvent<Entity>`（在 `FMLCommonSetupEvent` 注册，**双端都跑**），服务端由 `ReviveUpdatePacket.executeClient` 反序列化 NBT 同步到客户端 `Player`。`PlayerReviveServer.getBleeding(Player)` 本体只是 `player.getCapability(BLEEDING)`，所以客户端读它是对的——PlayerRevive 自己的客户端代码 `ReviveEventClient.click` 就是这么做的。我在客户端 `init()` 留了一行注释说明这点，避免以后有人"修正"成不调 `server` 包。

**两处未按骨架照抄的改动，请确认**

1. 客户端 `isBleeding(LocalPlayer)` 骨架里没有 `// mixin注入点` + `enableBuiltinCompat` 守卫（同文件其余四个方法都有，core 侧也有），我按模式补上了。
2. core 的 `init()` 失败日志原文是 `"Failed to initialize PlayerRevive client compatibility"`（应为客户端类的文案误抄），我删掉了 `client`。

**保持不变、但需要你确认语义的点**：`_shouldDisableShoot/Reload/SwitchFireMode/Melee` 我保留骨架的 `return true`。PlayerRevive 2.0.31 的 `PlayerReviveConfig.Bleeding` 里没有任何枪械相关开关（只有 `disableAllGUIAccess` / `disableInventoryAccess` / `disableChatAccess` / `disableServerCommands`），所以这四个"Config"方法我理解为 CGC 自己的硬编码策略，没有接 PlayerRevive 配置。如果它们本意要读某个配置项，告诉我读哪个。

**未能验证的部分**：只做了编译与反射目标的静态核对（javap 逐个比对），没有实机跑 Forge 1.20.1 客户端——CGC-Compat-Revive 里 PlayerRevive 是 `compileOnly`，起不了带该模组的运行环境。另外 `revive`/`kill`/`getRevivingPlayers` 目前在 CGC 里没有调用点，属于预留 API。

改动只落在这两个文件，未提交。

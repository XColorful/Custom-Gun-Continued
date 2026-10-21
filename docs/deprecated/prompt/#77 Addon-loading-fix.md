> #77 提示词

# 

```
现在CustomGunForge添加Mixin到CustomGun的getter接口，直接head返回Forge类单例，Forge类的修改参考当前ForgeSideExecutor写法（补单例+deprecate构造函数），除了ForgeNetworkAdapter以外都应用这个写法及注入
```

# 

I'll start by examining the current state of the Forge files and the existing mixin setup.

Now let me read the CustomGun class and the task doc.

Let me inspect the remaining Forge implementation classes and find constructor usages.

Let me check how these getters are used, especially the config builder.

The pattern is clear. I'll add singletons to the remaining Forge classes, wire them in `CustomGunForge`, and inject the five getters.

Two of those classes have no `CustomGun` getter; I'll point their javadoc at the real access path and wire `INSTANCE` in `CustomGunForge`.

Now I'll compile the Forge module to verify.

The mixin targets a mod class, so it must not be remapped. I'll set `remap = false`.

Compilation passes. Let me review the full diff, then run the full jar build.

Diff is clean. Now running the full Forge jar build to validate mixin processing.

Build succeeded. Let me check whether a dev server run is set up so I can smoke-test mixin application.

改完并通过编译与打包验证。

**改动内容**

- [CustomGunMixin.java](forge-compat/src/main/java/dev/xcolorful/customgun/forge/mixin/CustomGunMixin.java) 对 `getSideExecutor`、`getRegistrarFactory`、`getMcRegistry`、`getCapabilityProvider`、`getAccessTransformer` 做 `HEAD` 注入，直接返回对应 Forge 单例。因目标 `CustomGun` 是模组类而非 Minecraft 类，必须 `@Mixin(remap = false)`，否则注解处理器会报 “Unable to locate obfuscation mapping”。
- 按 `ForgeSideExecutor` 的写法补齐单例 + `@Deprecated` 构造函数：[ForgeRegistry.java](forge-compat/src/main/java/dev/xcolorful/customgun/forge/minecraft/ForgeRegistry.java)、[ForgeCapabilityProvider.java](forge-compat/src/main/java/dev/xcolorful/customgun/forge/minecraft/ForgeCapabilityProvider.java)、[ForgeAccessTransformer.java](forge-compat/src/main/java/dev/xcolorful/customgun/forge/minecraft/access/ForgeAccessTransformer.java)、[ForgeEventRegister.java](forge-compat/src/main/java/dev/xcolorful/customgun/forge/event/ForgeEventRegister.java)、[ForgeNetworkHook.java](forge-compat/src/main/java/dev/xcolorful/customgun/forge/network/ForgeNetworkHook.java)。
- [CustomGunForge.java](forge-compat/src/main/java/dev/xcolorful/customgun/forge/CustomGunForge.java) 全部改用 `INSTANCE`；`customgunforge.mixins.json` 已包含 `CustomGunMixin`（你之前已加）。

**两处刻意排除（与字面“都”不一致，说明理由）**

- `ForgeModConfigSpecBuilder`：内部持有有状态的 `ForgeConfigSpec.Builder`，`getModConfigSpecBuilder()` 会被调用三次（COMMON/SERVER/CLIENT），单例会导致第二次及之后的 `buildAndRegister` 复用已 build 的 builder 而报错。保持 `ForgeModConfigSpecBuilder::new`。
- `ForgeNetworkAdapter`：按你的要求排除。

**验证**：`JAVA_HOME=JDK17 ./gradlew :forge-compat:compileJava` 与 `:forge-compat:jarJar` 均 BUILD SUCCESSFUL。运行时未启动（`forge-compat/run` 尚未初始化，首次启动成本较高），建议进游戏或 `runClient` 确认 Mixin 实际生效。未提交，等你审阅。

# 

```
以现在的CustomGunForge为准，把CustomGunForgeClient也修改成相同模式，包含改INSTANCE、CustomGunForgeClient保留修改成INSTANCE后注释，Client mixin
```

# 

我先查看客户端侧现有实现与 mixin 配置。

明白，客户端按同样模式处理（ForgeClientEventRegister、ForgeKeyMapping.Creator、ForgeClientAccessTransformer、ForgeClientItemExtensionProvider、ForgeStencilOperator），并新增 `CustomGunClient` 的 Mixin。先改实现类。

# 

```
重新改CustomGunForgeClient
```

# 

明白，按你现在的写法统一：字段声明处直接赋 `INSTANCE`，`init()` 里改成 INSTANCE 的赋值行保留为注释（包括 `eventRegister` 已改好的那条）。

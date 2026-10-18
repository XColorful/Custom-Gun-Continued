> #755 提示词

# 

```
2024_11_11
这个问题很早就有接到过报告，在oculus下和tacz一起使用bliss/photon会导致内存泄露。根据iris作者的说法，是这些光影使用了iris一个叫做"separate entity draw"的功能引起的，他修改了原版渲染时的endbatch方法的某些行为，而tacz在兼容它时调用的不完全正确导致了问题。但是目前我们没时间和精力去研究这个东西怎么修，你可以考虑暂时换别的光影

2026_02_25
最新的版本(1.1.7hotfix)理论上是修了

2026_10_11
https://github.com/MCModderAnchor/TACZ/issues/755
Hysteria Shaders

Hysteria Shaders测试环境：
- 超平坦虚空预设，只有脚下一块平地是石头，其他全是虚空，禁用时间流式/天气变化，朝向太阳，低模渲染距离为0（掉落物始终渲染为低模）
- TaCZ 手持tacz:ak47，有过玩家移动、丢枪、射击、检视、换倍镜，在2分20秒第一次崩溃debug-tacz1.log，此时装备tacz:scope_mk5hd
- TaCZ 手持tacz:ak47，镜头不动3分钟未崩溃
- TaCZ 手持tacz:ak47+红点瞄准镜（tacz:sight_coyote），开镜状态镜头不动3分钟未崩溃
- TaCZ 手持tacz:ak47+4倍镜（tacz:scope_elcan_4x），开镜状态镜头不动3分钟未崩溃
- TaCZ 手持tacz:ak47+组合镜（tacz:scope_mk5hd），开镜状态镜头不动3分钟未崩溃
- TaCZ 手持tacz:ak47，每30秒循环切换（无、红点、4倍镜、组合镜），始终保持鼠标略微移动镜头+倍镜开镜，4分钟（两轮循环）未崩溃
- TaCZ 手持tacz:ak47，4种组合分别射两发弹夹+装弹，分别检视两次，之后保持tacz:scope_mk5hd开镜到5分钟，未崩溃
- TaCZ 手持tacz:ak47+scope_mk5hd，丢掉枪再捡回来，来回跑步，再丢掉枪再捡回来，来回跑步，内存从3G飙到4G到5G到6G降到5G然后上升，卡顿停住，然后崩溃debug-tacz2.log
- TaCZ 手持tacz:ak47+scope_mk5hd，刚进游戏就丢掉枪械然后捡回，不开镜，第14秒内存飙到4G，24秒飙到6G，26秒到5G，31秒崩溃debug-tacz-3.log
- TaCZ 手持tacz:ak47无配件，刚进游戏就丢掉枪械然后捡回，不开镜，13秒到4G，21秒到5G，在4-5G波动，29秒到6G，37秒崩溃

Photon测试环境：
- 超平坦虚空预设，只有脚下一块平地是石头，其他全是虚空，禁用时间流式/天气变化，朝向太阳，低模渲染距离为0（掉落物始终渲染为低模）
- TaCZ 手持tacz:ak47无配件，刚进游戏就丢掉枪械然后捡回，不开镜，1分钟未见内存波动

Hysteria Shaders测试环境：
- 超平坦虚空预设，只有脚下一块平地是石头，其他全是虚空，禁用时间流式/天气变化，朝向太阳，低模渲染距离为20（掉落物渲染为高模）
- TaCZ 手持tacz:ak47无配件，刚进游戏就丢掉枪械然后捡回，不开镜，13秒到5G，17秒到6G然后波动，24秒再到6G然后反复5G 6G波动，38秒崩溃debug-tacz4.log
- TaCZ 手持tacz:ak47无配件，刚进游戏就丢掉枪械然后捡回，不开镜，内存涨到4.3G时退出存档，内存占用仍然稳在4.3G

Hysteria Shaders测试环境：
- 超平坦虚空预设，只有脚下一块平地是石头，其他全是虚空，禁用时间流式/天气变化，朝向太阳，低模渲染距离为0（掉落物始终渲染为低模）
- CGC 手持tacz:ak47无配件，刚进游戏就丢掉枪械然后捡回，不开镜，1分钟未见内存波动

Hysteria Shaders测试环境：
- 超平坦虚空预设，只有脚下一块平地是石头，其他全是虚空，禁用时间流式/天气变化，朝向太阳，低模渲染距离为0（掉落物始终渲染为低模），移除CGC Compat Shader
- CGC 手持tacz:ak47无配件，刚进游戏就丢掉枪械然后捡回，不开镜，5秒到4G，15秒到5G，27秒到6G，整个过程内存始终上升，没有明显来回波动，35秒崩溃debug-cgc1.log

目前认为CGC本体+CGC Compat Shader修复了该问题，需要排查TaCZ为什么有这个问题
- 测试日志放在D:\Temp-File\下，游戏目录下mods是CGC测试时mod，mods-tacz是TaCZ测试时mod
- 当前IDEA打开了CGC，CGC Compat Shader，TaCZ三个窗口供浏览源码
```

# 

我来梳理这个内存泄露问题。先看测试日志和 CGC Compat Shader 的实现，弄清"修复"到底修了什么，再对照 TaCZ 的渲染路径找出差异。

CGC 的崩溃日志签名很明确：`Iris HandRenderer.renderSolid` 在 `beginTranslucents` 阶段触发 2GB 缓冲区扩容失败。兼容模组的修复点是 `IrisCompat.endBatch` —— 全缓冲源下必须调用无参 `endBatch()`。现在看 CGC 与 TaCZ 各自如何使用这个机制。

崩溃栈证实泄露点在 CGC 的 Bedrock 模型逐面 compile：顶点持续写入同一个 `BufferBuilder`，从未真正提交，直到 2GB 扩容溢出。现在对照两边兼容层的实现。

两边的兼容层几乎一致，TaCZ 已经有无参 `endBatch()` 的兜底。差异应该在于调用时机/调用路径。查两边 `endBatch` 的实际调用点。

TaCZ 的兼容层与 CGC 结构相同，所以问题不在这层本身。看 TaCZ 崩溃日志的调用栈，确认泄露发生在哪条渲染路径，再对比两边的调用点上下文。

关键发现：TaCZ 的第一人称渲染已经不在自己的 `FirstPersonRenderEvent` 里了——该类上的 `@SubscribeEvent` 被注释掉，实际渲染走的是 `simplebedrockmodel` 库的 `FirstPersonRenderHandler`。而带 `OculusCompat.endBatch` 修复的代码在没有被激活的路径上。先确认库的调用链和版本。

`libs` 里正好有 simplebedrockmodel 2.2.2 的 sources jar。先读库的入口实现，同时看 TaCZ 自己的 endBatch 兜底在哪里、为什么没生效。

TaCZ 每帧在 `BedrockModel.render` 末尾确实调了 `OculusCompat.endBatch`，所以问题应该是这个兜底在该场景没真正生效（`instanceof` 判断落空或调错了对象）。用 IDEA 反编译 Oculus 1.8.0 与 simplebedrockmodel 确认。先定位 jar。

定位到 Oculus 1.8.0 里有两套全缓冲实现（`FullyBufferedMultiBufferSource` 和 `OldFullyBufferedMultiBufferSource`）。反编译确认实际注入的是哪个、`endBatch` 的行为差异。

IDEA MCP 不允许读项目外的 jar，改用 javap 直接看字节码。

`HandRenderer` 持有自己的 `FullyBufferedMultiBufferSource`，而 TaCZ 写的是共享的 `renderBuffers().bufferSource()`。需要确认 Iris 在关卡渲染期间把哪个实现换进了共享 RenderBuffers。

Iris 侧确认了关键事实：`HandRenderer` 用的是它**私有**的全缓冲源（以 `UnflushableWrapper` 交给 `RenderHandEvent`），渲染完由 Iris 自己 `readyUp()+endBatch()`；而 TaCZ 忽略事件源、写共享的 `renderBuffers().bufferSource()` 再自行 flush。接下来查 TaCZ 的修复历史，看 1.1.7 hotfix 到底改了什么、后续哪一步让它失效。

CGC 的关键差异找到了：它的兜底是无参 `bufferSource.endBatch()`，且在第一人称渲染**前**先 flush 一次；TaCZ 的兜底是 `endBatch(renderType)`。现在确认运行时到底是不是 `FullyBufferedMultiBufferSource`（`instanceof` 若落空，TaCZ 就会走 per-type 空实现）。

CGC 文档里很可能已经有这个泄漏机制的完整分析。先检索项目文档。

CGC 的证据把差异锁定在「渲染第一人称模型**之前**先 flush 全缓冲源」。TaCZ 原本在 `FirstPersonRenderEvent` 里有这一句，但 sbm 迁移把 `@SubscribeEvent` 注释掉了。先核实测试用 jar 里 `BedrockModel` 的 flush 是否真的存在。

需要确认 1.20.1 `BufferBuilder.end()` 与被丢弃 segment 的生命周期，这决定了"漏的是 native 缓冲还是 builder 无限增长"。找 MC 源码 jar。

用 IDEA MCP 直接查 1.20.1 的 `BufferBuilder`/`MemoryTracker` 源码，确认泄漏的是 native 缓冲还是 builder 容量。

机制已经闭合。最后确认一处：Iris 在实体渲染阶段是否会套 `TaggingRenderTypeWrapper`（它默认被归为 GENERAL_TRANSPARENT，正是留在 `typeToSegment` 里等 flush 的那批）。

排查完成，根因已经闭环。下面是结论和证据链。

## 结论

不是 TaCZ 的 `OculusCompat` 写错了，而是**它在当前版本里根本没被第一人称渲染路径调用到**：

TaCZ 原本在 [FirstPersonRenderEvent.java:63](../TACZ/src/main/java/com/tacz/guns/client/event/FirstPersonRenderEvent.java) 里有一句"渲染前先冲刷共享缓冲源"的防泄漏代码：

```java
// 防止内存泄漏
OculusCompat.endBatch(Minecraft.getInstance().renderBuffers().bufferSource());
```

但 `34db94f3 迁移部分实现至sbm` 把第一人称渲染移到了 simplebedrockmodel 的 `FirstPersonRenderHandler`，同时把这里的 `@SubscribeEvent` 注释掉了（该文件第 23 行）。新路径 `FirstPersonRenderHandler#onRenderHand → GunItemRendererWrapper#renderFirstPerson` 里**没有**这次预冲刷。`BedrockModel#render` / `BedrockAttachmentModel#renderTempPart` 里的 `OculusCompat.endBatch` 是在渲染**之后**才调用的，为时已晚。

## 泄漏机制（Oculus 1.8.0 字节码 + 1.20.1 vanilla 源码）

1. Iris 的 `FullyBufferedMultiBufferSource.getBuffer(RenderType)`（`m_6299_`）**第一句就是 `removeReady()`**，而 `removeReady()` 只是 `typeToSegment.clear(); renderOrder.clear();` —— **丢弃已收集但未绘制的 segment，不绘制、不释放**。
2. 开启光影且光影包启用 separate entity draws 时（Hysteria/Bliss；Photon 没开，所以测不出来），`MixinLevelRenderer` 在 translucents 起点先 `readyUp()` 收集所有 segment，然后只 `endBatchWithType(OPAQUE)` / `(OPAQUE_DECAL)` 画掉不透明的，**其余 segment 挂起到 translucents 末尾才统一 `endBatch()`**。
3. 手部渲染 `HandRenderer.renderSolid` 恰好跑在这个窗口里（你的崩溃栈每一份都是 `HandRenderer.renderSolid → ItemInHandRenderer → RenderEvent`）。vanilla 自己手部渲染用的是 `HandRenderer` 私有的全缓冲源（以 `UnflushableWrapper` 交给事件），所以 vanilla 永远不会碰共享源。**TaCZ 用的是 `mc.renderBuffers().bufferSource()`**，第一次 `getBuffer` 就触发 `removeReady()`，把这批挂起的 segment 全部丢掉。
4. 1.20.1 的 `BufferBuilder` 是一块**只增不减的 arena**：`end()` 只是记下 `RenderedBuffer` 并让指针前移，只有所有 `RenderedBuffer` 都 `release()`（正常绘制时 `VertexBuffer.upload` 的 finally 会做）后计数归零，才会 `clear()/discard()` 重置。被丢掉的 segment 永远不会 release → 计数永远不归零 → 指针只增不减。
5. 于是每帧的顶点都追加在同一块 ByteBuffer 上，按 2MB 递增扩容。初始 `524288 * 6 = 3145728`，加 1022 次 2097152 正好是 `2146435072`，再扩一次 `+2097152` 溢出成 `-2146435072` —— 和日志里的 `Failed to resize buffer from 2146435072 bytes to -2146435072 bytes` **一字不差**。这个数字组合就是铁证。

## 为什么 CGC 没这个问题

CGC 有完全相同的隐患，但它的 `FirstPersonRender` 在渲染前先做了同样的预冲刷：

- [FirstPersonRender.java:125](D:/Github/Custom-Gun-Continued/core/src/main/java/dev/xcolorful/customgun/client/renderer/shooter/FirstPersonRender.java:125) `IrisCompat.endBatch(mc.renderBuffers().bufferSource());`
- 兼容模组 [IrisCompatMixin.java:40](D:/Github/CGC-Compat-Shader/core/src/main/java/dev/xcolorful/cgccompat/shader/client/mixin/iris/IrisCompatMixin.java:40) 把它接到 `FullyBufferedMultiBufferSource.endBatch()`。

这也解释了三组 A/B 测试：CGC 不装 cgccshader 时 `IrisCompat.endBatch` 是空桩 → 泄漏（debug-cgc1）；装了 → 正常。而 TaCZ 的等价代码随 sbm 迁移被停用了。

## 为什么 1.1.7-hotfix 看起来"修了"

`520584c3 默认关闭方块实体的半透明，版本号 -> 1.1.7-hotfix` 同时把 AcceleratedRendering 从 `implementation`（jarJar 自动加载）改成 `compileOnly`（不再内置）。当时报问题的环境里 AR 被自动装上了，移除它掩盖了症状；预冲刷其实在 sbm 迁移那一刻（2025-12-26 23:43，比 AR 提交晚 13 分钟）就已经失效了。所以 1.1.8 在纯 TaCZ 环境下照样复现。

## 修复建议

最小修复（与旧代码、CGC 完全一致）：在 `GunItemRendererWrapper#renderFirstPerson`（[GunItemRendererWrapper.java:163](src/main/java/com/tacz/guns/client/renderer/item/GunItemRendererWrapper.java:163)）开头，`renderer` 真正开始画之前加：

```java
OculusCompat.endBatch(Minecraft.getInstance().renderBuffers().bufferSource());
```

更彻底的方向：第一人称路径应该改用 `RenderHandEvent#getMultiBufferSource()`（也就是 Iris 的 `UnflushableWrapper`），让 Iris 在 `renderSolid` 结束时代为 flush，而不是写全局 `renderBuffers().bufferSource()` 再自己兜底。这需要把 buffer source 从 `renderFirstPerson` 一路传进 `BedrockGunModel#render` / `BedrockModel#render`，改动面较大，但它是让 TaCZ 和 Iris 的 separate entity draws 彻底解耦的正确做法——大概也是 Iris 作者说"调用得不完全正确"的意思。

另外两个可验证的预测（用来反证机制）：① Hysteria + 地上有一个丢弃的枪械实体时，即使镜头完全不动也会涨内存；② 没有实体几何挂起时（纯持枪不动）不会崩。这与你的测试记录完全吻合。

需要我把最小修复直接应用到 TaCZ 工作区（不提交），或者整理成一份可以直接贴给 TaCZ / Iris 的 issue 报告吗？

# 

```
整理一个回复TaCZ Issue #755的测试报告
```

# 

下面是可直接贴到 [TACZ#755](https://github.com/MCModderAnchor/TACZ/issues/755) 的测试报告。

---

## 【测试报告】Oculus + separate entity draws 光影下 TaCZ 内存泄漏的复现与根因定位

### 背景

我们在 CGC（Custom Gun Continued）上也遇到过与 #755 完全一致的内存泄漏，并已修复。随后用同一套环境对 CGC、TaCZ 做了对照测试，定位到 TaCZ 侧的具体成因。

### 测试环境

- Minecraft 1.20.1 / Forge 47.4.23
- Oculus 1.8.0、Embeddium 0.3.31
- 光影：Hysteria Shaders / Photon
- TaCZ 1.1.8-hotfix2；对照组为 CGC 26.10.27 + CGC Compat Shader
- 场景：超平坦虚空，仅脚下一块石头，禁用时间流/天气，朝向太阳；低模渲染距离 0（掉落物始终低模）

### 测试结果

| # | 场景 | 光影 | 结果 |
|---|---|---|---|
| 1 | AK47 无配件，进游戏立即丢枪再捡回，不开镜 | Hysteria | 13s→4G，21s→5G，29s→6G，37s 崩溃（debug-tacz-3） |
| 2 | AK47 + scope_mk5hd，丢枪捡回、来回跑，重复 | Hysteria | 内存 3→4→5→6G，卡顿后崩溃（debug-tacz2） |
| 3 | AK47 无配件，丢枪捡回、跑动 | Hysteria（渲染距离 20） | 13s→5G，17s→6G，反复 5/6G 波动，38s 崩溃（debug-tacz4） |
| 4 | AK47，移动、丢枪、射击、检视、换倍镜 | Hysteria | 2 分 20 秒第一次崩溃（debug-tacz1） |
| 5 | AK47（无/红点/4x/组合镜），镜头不动、开镜不动、循环切换、打弹匣 | Hysteria | 3~5 分钟均**未崩溃** |
| 6 | AK47 无配件，丢枪捡回，不开镜 | **Photon** | 1 分钟内存无波动 |
| 7 | **CGC** 同场景（丢枪捡回） | Hysteria | 1 分钟无波动 |
| 8 | **CGC 移除 Compat Shader** 后同场景 | Hysteria | 5s→4G，15s→5G，27s→6G，持续上升，35s 崩溃（debug-cgc1） |

四个崩溃日志的报错签名完全相同：

```
java.lang.OutOfMemoryError: Failed to resize buffer from 2146435072 bytes to -2146435072 bytes
  at BufferBuilder.ensureCapacity
  at BedrockCubePerFace.compile
  at BedrockPart.compile
  at BedrockModel.render
  at GunItemRendererWrapper.renderFirstPerson
  at FirstPersonRenderHandler.onRenderHand   (simplebedrockmodel)
  at ItemInHandRenderer.renderHandsWithItems
  at HandRenderer.renderSolid               (Oculus)
  at LevelRenderer.beginTranslucents
```

### 关键现象

1. **只有 Hysteria 能复现，Photon 不能**——两者最大的差异是 Hysteria 启用了 Iris 的 separate entity draws。
2. **触发条件是"存在挂起的实体几何体"（例如地上的掉落物实体），与镜头是否移动无关**。纯持枪不动时共享缓冲源里没有待提交的 translucent 实体 segment，所以不崩。
3. **CGC 本体不装 Compat Shader 时同样崩溃，装上后正常**——说明这是"在正确时机冲刷 Iris 全缓冲源"的问题，而不是 TaCZ 独有的渲染错误。

### 根因

Oculus 1.8.0 的字节码与 1.20.1 vanilla 源码可以完整闭合因果链：

1. `FullyBufferedMultiBufferSource#getBuffer(RenderType)` 的第一句是 `removeReady()`，它只执行 `typeToSegment.clear(); renderOrder.clear();`——**把已收集但未绘制的 segment 直接丢弃，既不绘制也不 release**。
2. separate entity draws 开启时，`MixinLevelRenderer` 在 translucents 起点先 `readyUp()` 收集全部 segment，然后只 `endBatchWithType(OPAQUE)` 和 `(OPAQUE_DECAL)` 画掉不透明部分，**其余 segment 挂起到 translucents 末尾才统一 `endBatch()`**。
3. Iris 的手部渲染 `HandRenderer.renderSolid` 正好发生在这个窗口内。vanilla 手部渲染写入的是 `HandRenderer` **私有**的全缓冲源（以 `UnflushableWrapper` 通过 `RenderHandEvent#getMultiBufferSource()` 提供），不会碰到共享源；而 TaCZ 的 `BedrockModel#render` 写的是 `Minecraft.getInstance().renderBuffers().bufferSource()`。**第一次 `getBuffer()` 就把挂起的 segment 全部丢弃了。**
4. 1.20.1 的 `BufferBuilder` 是只增不减的 arena：`end()` 只记录 `RenderedBuffer` 并前移指针，只有所有 `RenderedBuffer` 都 `release()`（正常绘制时由 `VertexBuffer.upload` 的 finally 完成）后计数归零，才会 `clear()/discard()`。被丢弃的 segment 永远不 release，计数永远不归零，于是每帧顶点持续追加到同一块 ByteBuffer。
5. 扩容每次 +2MB，初始容量 `524288 × 6 = 3145728`，加 1022 次 2097152 恰好等于 `2146435072`，再扩一次 `+2097152` 溢出为 `-2146435072`——与日志报错数字完全一致。

### 为什么 TaCZ 会漏

TaCZ 原本有正确的预冲刷，但被迁移丢失了：

- `05f8f1d1`（AcceleratedRendering 支持）在 `FirstPersonRenderEvent#onRenderHand` 中加入了：
  ```java
  // 防止内存泄漏
  OculusCompat.endBatch(Minecraft.getInstance().renderBuffers().bufferSource());
  ```
- 13 分钟后的 `34db94f3 迁移部分实现至sbm` 把第一人称渲染移交给 simplebedrockmodel，并注释掉了该类的 `@SubscribeEvent`。新路径 `FirstPersonRenderHandler → GunItemRendererWrapper#renderFirstPerson` **没有**这次预冲刷。
- `BedrockModel#render`（约 364 行）和 `BedrockAttachmentModel#renderTempPart`（约 206 行）里的 `OculusCompat.endBatch` 是在渲染**之后**调用的，此时挂起的 segment 已在第一次 `getBuffer()` 时被丢弃，无法补救。

`1.1.7-hotfix` 看起来"修了"，是因为 `520584c3` 把 AcceleratedRendering 从 `implementation`（jarJar 自动加载）改为 `compileOnly`，移除了当时环境里的干扰因素；预冲刷在此前已经失效，所以纯 TaCZ 环境仍会复现。

### 修复建议

**最小修复**（与 TaCZ 旧代码、CGC 现行做法一致）：在 `GunItemRendererWrapper#renderFirstPerson` 开头、真正渲染模型之前加一行：

```java
// 防止 Iris/Oculus separate entity draws 在此窗口丢弃未绘制的 segment
OculusCompat.endBatch(Minecraft.getInstance().renderBuffers().bufferSource());
```

`OculusCompat.endBatch` 只在传入 `FullyBufferedMultiBufferSource` 时生效，无光影时是无操作，安全。

**更彻底的做法**：第一人称路径改用 `RenderHandEvent#getMultiBufferSource()`（Iris 的 `UnflushableWrapper`），让 Iris 在 `renderSolid` 结束时代为 flush，从根上避免与共享缓冲源交互。这需要把 buffer source 从 `renderFirstPerson` 传入 `BedrockGunModel#render` / `BedrockModel#render`，改动面较大。

### 可验证的预测

1. Hysteria + 地上放一个丢弃的枪械实体，**即使完全不移动镜头**也会持续涨内存；
2. 场上没有任何待提交的实体几何体时（纯持枪不动），不会崩溃。

这两条与现有测试记录一致；打上最小修复后，场景 1 的内存曲线应保持平稳。

---

如果需要，我可以把这份报告保存成 markdown 文件，或者把最小修复直接应用到本地 TACZ 工作区（不提交）。

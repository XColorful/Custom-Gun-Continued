> #56 提示词

# 

```
现在需要修复旧枪包格式兼容问题。

- 游戏目录下./tacz/是所有枪包数据包+资源包目录（用其他模组支持了该目录加载）
- 测试枪械为TaCZ内置枪包，以ak47为例测试倍镜兼容性
- tacz:sight_coyote ocular里没渲染准心
- tacz:scope_elcan_4x开镜后视角不对，像是在机瞄视角执行倍率放大
- tacz:scope_mk5hd初始瞄准状态下，ocular里看见了一个在屏幕中心上方一点的红点；按一次切换后缩放倍率放大，枪械上scope_view_index:1，仍然只有屏幕上方的一点红点，但是因为缩放倍率大而接近屏幕顶上；再切换一次后到倍镜顶上的小红点镜，ocular里面显示的是原本在筒镜里渲染的十字准心；这三个缩放倍率大小为第三个<第一个<第二个
- 所有cbrg:开头的倍镜都没问题

目前怀疑是dev.xcolorful.customgun.client.model._AttachmentSort的问题，你排查一下。_AttachmentSort的IGNORE_NAME_MISMATCH和ALLOW_NON_NUMERIC_SUFFIX需要保持为当前设置值
```

# 

I'll start by reading the project documentation and then investigate the `_AttachmentSort` class.

Now let me look at the client architecture docs for model rendering and the NodeName enum.

Now let me look at how `divisionOcularEntries` are consumed during rendering.

Let me find the actual model files for the TaCZ built-in packs to compare node naming.

Let me check the branch's git history to see what changed for legacy compat.

Let me compare with the original TACZ implementation to understand intended semantics.

Let me examine the model structure more carefully, including nesting and the LOD models.

Let me look at a working `cbrg:` scope to compare naming conventions.

Let me check the git history for `getScopeViewPath` and `_AttachmentSort` to understand recent changes.

Let me scan all packs' attachment models for the naming conventions actually in use.

Let me check the documented node naming convention for models.

Let me quantify how many packs are affected by out-of-range `views`.

I have the root causes. Let me verify the scope-view consumer before changing it.

I've confirmed all three root causes. Now let me apply the fixes, starting with `_AttachmentSort`.

Now let me add the index-based comparator next to the existing name comparator.

Now the division classification guard in `AttachmentModelObject`.

Now the `getScopeViewPath` out-of-range fallback.

Let me verify the fix by simulating both the new logic and TaCZ's original logic across all packs.

The mismatch output is just a naming-representation artifact. Let me compare actual node pairing.

All 45 models now match TaCZ's pairing exactly. Let me confirm the pre-fix code did not.

27 of 45 models were broken; now 0. Let me compile to verify the changes.

Compiles cleanly. Let me review the full diff.

Let me confirm which scope displays rely on the out-of-range fallback.

排查完成，三处症状对应三个不同的根因——只有两个在 `_AttachmentSort`。

## 根因

**1. `sight_coyote` 没有准心 — 非 `_AttachmentSort`，是 `AttachmentModelObject` 的 division 识别过宽**

`Prefix.DIVISION` 是纯前缀匹配，所以 `division_illuminated` 也被当成准心：它被 `setVisible(false)` 隐藏，又被分到 stencil 索引 2，而索引 2 没人写 → 整个节点消失。

coyote 的 `division` 本身 **0 个 cube**，真正的准心就是它的子节点 `division_illuminated`（1 cube）。原模组的 division 列表是 `division`、`division_2`、`division_3`… 依次取到第一个不存在的编号，`division_illuminated` 从来不是 division，而是普通节点正常渲染。

**2. `scope_elcan_4x` 视角像机瞄 — `getScopeViewPath` 越界返回 null**

elcan 的 display 是 `views: [2, 2]`（1-based → 索引 1），但模型只有 1 个 `scope_view`。原模组越界时回退 `scopeViewPaths.get(0)`，CGC 返回 `null`，于是拿不到定位组、退化成机瞄视角。同一原因影响 5 把旧格式镜：contender、elcan_4x、lpvo_1_6、qmk152、scout。

**3. `scope_mk5hd` 准心/红点配对反了 — 这个才是 `_AttachmentSort`**

排序用的是字符串比较，于是 `ocular_scope_2`(→`"_scope_2"`) 排到了 `ocular_sight`(→`"_sight"`) 前面；原模组按**编号**排序，`ocular_sight` 编号 1、`ocular_scope_2` 编号 2。配上 `IGNORE_NAME_MISMATCH=true` 的按位配对，两者和 division 的配对就整体反了：

||原版配对|修复前配对|
|---|---|---|
|筒镜视野(scope_view_2)|`ocular_scope_2` ↔ `division_2`(十字)|`ocular_scope_2` ↔ `division`(红点)|
|顶部小红点(scope_view)|`ocular_sight` ↔ `division`(红点)|`ocular_sight` ↔ `division_2`(十字)|

这正是你描述的"筒镜里是红点、顶上小红点镜里是十字"。顺带说明 `compareNodeSuffix`/`getNumber` 里的数字逻辑是死代码（`getNumber` 要求后缀以 `_` 开头，而所有实际后缀都不是），实际等于纯字符串比较。

## 改动

- `_AttachmentSort`：新增 `compareNodeIndex`/`getIndex`，按**末尾编号**排序（无编号视作 1，与原模组默认值一致），仅用于排序；`compareNodeSuffix` 保留给 `IGNORE_NAME_MISMATCH=false` 的同名合并路径。两个开关保持 `true` / `false` 不变。
- `_AttachmentSort`：新增 `isDivisionSuffixInvalid`（非空且非纯数字即非法），`isStrippedSuffixInvalid` 语义保持不变。
- `AttachmentModelObject.resetCache`：division 分支加该守卫。
- `AttachmentModelObject.getScopeViewPath`：越界回退到第一个。

## 验证

把 CGC 的算法和原模组的算法都实现成脚本，对所有枪包 `geo_models/**/*.json` 逐一跑配对对比：**修复前 45 个配件模型里 27 个配对与原模组不一致，修复后 0 个不一致**（cbrg 的 9 个模型在两版下都与原模组一致，符合你"cbrg 没问题"的观察）。

`:core:compileJava` 通过，无新增告警。**未做游戏内验证**——我没有启动游戏确认渲染效果，需要你实机确认。

## 两点未改动，供你决定

- `scopeViewPaths.sort` 仍按节点名字符串排序（`"scope_view_10"` 会排到 `"scope_view_2"` 前），现实中没有 ≥10 个 scope_view 的包，按最小改动原则没动。
- 原模组取 scope_view 时遇到编号断档就停止（有 `scope_view` 和 `scope_view_3` 时只认前一个），CGC 是收集全部再排序。同样未发现实际存在断档的包。

改动未提交，等你 review。

# 

```
已经修复了。

你的根因描述有几个问题需要回答：
- ALLOW_NON_NUMERIC_SUFFIX=false的话，应该不会取到非数字后缀？第一点应该能自动生效？
- scope_elcan_4x的[2,2]是否应该写成[1,1]来对应只有一个索引的情况？这个修复是否只是为了处理旧包里美术写错配置的问题（列表里应该从1开始，最大取到view size）？如果是这样的话，应该考虑把这个修正移到AttachmentIndexInstance里进行修正（写回pojo，但不写到资源包文件）
- sort按末尾数字排序可以保留，无编号视作1
- 10排到2前面的这个问题可以不管
```

# 

Let me check the current state of the working tree before answering.

Let me look at the classification flow and the index-instance to answer precisely.

I have what I need. Let me move the correction into the index instance, as you suggest.

One correction before I finish: TaCZ falls back to the **first** view, not the highest. Let me match that exactly.

Let me verify the corrected index matches TaCZ's read-time fallback for every display.

三个问题逐一回答。

## 1. `ALLOW_NON_NUMERIC_SUFFIX=false` 不会自动生效

那个开关只作用于 **ocular**，division 走的是另一条完全没有后缀检查的分支。

`isStrippedSuffixInvalid` 全代码库只有一处调用：[AttachmentModelObject.java:141](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/client/model/AttachmentModelObject.java:141)，位于 `ocular_*` 的兜底分支。division 分支（147-154 行）此前没有任何检查，所以 `division_illuminated` 跟这个开关无关，从来都会被当成准心。两者是两条独立路径，不存在"自动生效"。

另外也不建议把同一个方法搬到 division 分支复用：该开关的语义是"允许 ocular 非数字后缀"，如果 division 也受它控制，那么为了加载非数字 ocular 名的旧包而打开开关时，`division_illuminated` 会重新变成假 division，coyote 又坏掉。所以 division 侧用的是不依赖该开关的守卫。

## 2. elcan 的 `[2,2]` 确实是配置写错，修正已移到 index instance

按 `views` 的定义（1-based，指向 `scopeViewPaths`，见 [GunRendererAddon.java:311](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/client/renderer/item/gun/GunRendererAddon.java:311) 的 `- 1`），elcan 只有 1 个 `scope_view`，正确写法就是 `[1,1]`。官方包里是同一处的共 5 个 display：contender `[2]`、elcan_4x `[2,2]`、lpvo_1_6 `[2,2]`、qmk152 `[2]`、scout `[2,2]`。所以这条上原模组的行为确实是容忍错误配置，我原来在 getter 里回退等于在渲染侧打补丁。

已按你的方案改：

- `AttachmentModelObject` 新增 `getScopeViewCount()`，`getScopeViewPath` 恢复成越界返回 `null`——越界从此只可能来自真正的 bug，不再被静默吞掉。
- `ClientAttachmentIndexInstance.resetCache` 里模型建好后调用 `_correctScopeViewIndex`，就地改 `scopeViewIndex` 数组（只改内存 pojo）。

三个实现取舍说明一下：

- **只处理 `> viewCount`**，`<= 0` 不动。否则会把本来就非法的配置静默改成合法，绕过 `checkScopeViewIndex` 的拒绝。
- **越界改成 1 而不是夹到 `viewCount`**。原模组是 `viewCount <= index` 就 `get(0)`（回退第一个），不是取最后一个。现存包两者等价（5 个受影响的 display 都只有 1 个 scope_view），但 viewCount ≥ 2 时只有改成 1 才和原模组一致。
- 模型没有 `scope_view`（`viewCount <= 0`）时直接返回，避免把索引改成 0 反而触发校验拒绝、导致整个配件加载失败。

已验证：15 个带 `views` 的 display 逐值计算修正后索引，与原模组运行期回退结果 **0 处不一致**。

## 3 / 4

排序按末尾数字、无编号视作 1 —— 保持现状，未改动。10 排到 2 前面的问题不管。

`:core:compileJava` 通过，IDEA 检查该文件无新增告警，改动未提交。

# 

```
没问题了。

改一下，ocular和sight等都使用ALLOW_NON_NUMERIC_SUFFIX控制，去掉新增的冗余复制。
```

# 

理解，改成统一用 `ALLOW_NON_NUMERIC_SUFFIX` 控制，删掉我加的那份重复。

改完了。

- 删掉了我加的 `isDivisionSuffixInvalid` 和 `isBlankOrNumeric`，`isStrippedSuffixInvalid` 的**实现体回到原样**（现在对它的 diff 只剩注释）。
- division 分支改为调用同一个 `isStrippedSuffixInvalid`，所以现在 ocular 和 division 由 `ALLOW_NON_NUMERIC_SUFFIX` 统一控制。
- 顺带更新了该开关和方法的注释，说明它现在同时管 ocular / division。

验证：`ALLOW_NON_NUMERIC_SUFFIX=false` 下，45 个配件模型的 ocular↔division 配对与原模组 **0 处不一致**；15 个带 `views` 的 display 修正后索引与原模组 **0 处不一致**。`:core:compileJava` 通过。

两点需要你确认：

1. **开关现在会连带影响 division 解析**。这正是我之前担心的耦合：一旦把 `ALLOW_NON_NUMERIC_SUFFIX` 打开（为了加载非数字 ocular 名的旧包），`division_illuminated` 会重新被当成假 division，coyote 那类准心又会消失。如果接受这个取舍就没问题，只是提醒一下现在这个开关不再只管 ocular。
    
2. **`ocular_scope_*` / `ocular_sight_*` 两条分支仍然完全不查这个开关**——它们在通用 `ocular_*` 分支之前短路返回，从不调用 `isStrippedSuffixInvalid`。所以 `ocular_sight_abc`、`ocular_scope_abc` 现在无论开关如何都会被当成准心（原模组会拒绝）。你提到"ocular和sight等"，如果指的就是这里，那还需要一并处理；不过我把所有枪包扫了一遍，实际存在的节点名只有 `ocular` / `ocular_1` / `ocular_sight` / `ocular_sight_1` / `ocular_sight_2` / `ocular_scope` / `ocular_scope_2`，没有这种名字，所以目前只是潜在偏差，不影响任何现存包。
    

另外你改的 `_correctScopeViewIndex` javadoc 里 `<ul>` 内第一行写成了普通文本、且 `tacz:scope_elcan_4x}` 后面多了一个 `）`，渲染出来会有点乱，我没动，你看要不要顺手改掉。

改动未提交。

# 

```
也要一并处理
```

# 

Now let me apply the flag to the `ocular_scope_*` / `ocular_sight_*` branches as well.

Let me verify both flag states and confirm the new rejections.

Flag=false is clean (0 mismatches); the flag=true "mismatches" are the flag doing its job. Let me check the case-by-case behavior with ASCII output.

改完了。

`ocular_scope_*` 和 `ocular_sight_*` 两条分支现在也走 `isStrippedSuffixInvalid`，四个系列（`ocular` / `ocular_sight` / `ocular_scope` / `division`）统一由 `ALLOW_NON_NUMERIC_SUFFIX` 控制。

关键点：这两条分支存进 `_OcularNodeEntry` 的名字是**保留关键字**的形式（`_sight`、`_sight_2`），排序/配对要用它，所以不能直接拿它去校验——否则 `_sight` 会被判成非数字后缀，所有 sight 全部报废。所以加了一个 `_getKeywordSuffix`，取类型名之后的部分（`ocular_sight` → `""`，`ocular_sight_2` → `"2"`）传给校验，存进 entry 的名字保持不变。

验证：

- `ALLOW_NON_NUMERIC_SUFFIX=false`：45 个配件模型的配对与原模组仍 **0 处不一致**；所有枪包里实际存在的名字（`ocular`、`ocular_1`、`ocular_sight`、`ocular_sight_1`、`ocular_sight_2`、`ocular_scope`、`ocular_scope_2`、`division`、`division_2`）分类结果不变。
- 新拒绝（原模组也拒绝）：`ocular_abc`、`ocular_sight_abc`、`ocular_scope_abc`。
- 开关打开时四个系列一致放开：`ocular_abc`、`ocular_sight_abc`、`ocular_scope_abc`、`division_illuminated` 全部接受。

`:core:compileJava` 通过，IDEA 对该文件无 error，改动未提交。

仍然要提醒一句：因为现在统一了，打开这个开关会连带把 `division_illuminated` 这类节点重新当成假 division（coyote 那类准心会消失）。这是"统一控制"的必然结果，不是漏改。

# 
---

```
现在需要修复旧枪包格式兼容问题。

- 游戏目录下./tacz/是所有枪包数据包+资源包目录（用其他模组支持了该目录加载）
- 目前suffuse:svd和suffuse:gm6的枪械模型和GUI渲染是紫黑块，提示框名称和子弹物品都是有的

目前怀疑是否是gltf还没实现导致的？
```

# 

I'll start by reading the project memory and the branch task doc, then investigate the model loading path.

Let me look at the suffuse pack's SVD and GM6 model/texture files.

Both gun models are bedrock `geo_models/*.json`, not gltf (the gltf is only in `animations/`). Let me read the index/display JSON and check the texture paths.

Both the model (`geo_models/gun/gm6_geo.json`) and texture (`textures/gun/uv/gm6.png`) exist. Let me check how CGC resolves those paths.

Important: `geo_models/gun/` contains only `gm6_geo.json` — no `svd_geo.json`, despite SVD's display referencing `suffuse:gun/svd_geo`. Let me check the full picture.

SVD's `display` references `suffuse:gun/svd_geo` and `suffuse:svd` animation — neither file exists. Let me check CGC's model/animation loading path and what happens when they're missing.

CGC maps `geo_models` as the legacy model folder. Let me compare against the cbrg pack, since suffuse's index reuses `cbrg.guns.lynx_amr` / `cbrg.guns.dragunov` names.

These packs are trimmed test fixtures. Let me look at how CGC actually resolves gun models and textures.

Let me check how CGC loads animations from `.gltf` — gm6's display points at `suffuse:gm6` which is a `.gltf` file.

Confirmed gltf support is a stub. Now let me check whether a failed model/display load produces a placeholder cube.

Let me check the game log for the actual load errors.

Found it — both displays fail `GunDisplayInstance` validation, so the renderer falls back to missing-texture. Let me read the rest of `resetCache` and `loadAnimController`.

Now let me confirm how animation lookup resolves, and check what `getBedrockAnimation` searches.

Let me quantify how widespread glTF animations are across the installed packs, and check the renderer's null-model fallback.

Only suffuse uses glTF. Let me check the original TaCZ behavior when an animation is missing, since TaCZ is the spec for this branch.

Let me verify the renderer's fallback path when the display instance is null.

Confirmed the fallback. Let me verify gm6's remaining blockers — the default script location, and that `sfuse` has no scripts dir.

Let me confirm with the debug log — it should show the exact "Animation not found" line.

Definitive. Note there's **no** "BedrockModel gm6 not found" line — gm6's model loads fine; only its animation fails. Let me size the glTF work by reading TaCZ's implementation.

Let me verify whether those other packs are genuinely trimmed, or whether those "not found" lines are expected.

排查完成。结论是：**对 `gm6` 你的怀疑是对的，对 `svd` 不是**——但机制和你设想的不一样，紫黑块不是"模型没渲染出来"，而是**整个 `GunDisplayInstance` 报废后渲染器主动画的错误材质方块**。

## 证据

`logs/debug.log`（19:51:34 / 19:52:09 两次 reload 都一样）：

```
GunDisplayInstance: BedrockModel suffuse:gun/svd_geo not found
GunDisplayInstance: Animation suffuse:svd not found
→ GunDisplayInstance suffuse:svd_display validation failed

GunDisplayInstance: Animation suffuse:gm6 not found
→ GunDisplayInstance suffuse:gm6_display validation failed
```

注意 **gm6 没有 "BedrockModel not found" 那一行**——它的模型是加载成功的，只有动画没找到。

代码链：

1. [GunDisplayInstance.java:183-199](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/client/resource/instance/assets/GunDisplayInstance.java:183) 的 `loadAnimController` 只问 `getBedrockAnimation()`，而它只查 `bedrockAnimationManager`（扩展名 `.animation.json`）。glTF 在另一个 `gltfAnimationManager`（`.gltf`）里，**从来没被查过**——第 191 行的 `// TODO glTF` 就是这个洞。
2. 查不到就 `return null`（第 193 行）。
3. [`:120-121`](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/client/resource/instance/assets/GunDisplayInstance.java:120) `resetCache` 把 `null` 当致命错误 → 返回 false。
4. [PojoInstance.java:33-38](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/core/resource/instance/PojoInstance.java:33) `isPojoValid` → false → `fromPojo` 返回 null → instance 根本不入表。
5. [GunItemRenderer.java:444-450](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/client/renderer/item/GunItemRenderer.java:444) 走 else 分支，画出那句注释说的"没有这个 gunLocation，渲染个错误材质提醒别人"。

**关键推论**：模型缺失其实是优雅降级的——[GunItemRenderer.java:399](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/client/renderer/item/GunItemRenderer.java:399) 里 `gunModelObject == null` 会退回 **slot 贴图**，而两把枪的 slot 贴图都在。所以紫黑块只可能来自 instance 为 null，不可能来自模型缺失。

## svd 是另一个问题

`./tacz/suffuse_gunsmoke_pack` 里 svd 缺三个文件：

|引用|期望文件|实际|
|---|---|---|
|`model: suffuse:gun/svd_geo`|`geo_models/gun/svd_geo.json`|无（同目录只有 `gm6_geo.json`）|
|`animation: suffuse:svd`|`animations/svd.*`|无（只有 an94/gm6/pf98a 三个 gltf）|
|`state_machine: suffuse:draw_ammo_state_machine`|`scripts/draw_ammo_state_machine.lua`|`scripts/` 是**空目录**，整个 suffuse 下一共 0 个 `.lua`|

所以 svd 即使把 glTF 实现了也还是紫黑块——它会卡在后面的脚本检查上。

## 对照 TaCZ

TaCZ 的 `checkAnimation`（[GunDisplayInstance.java:457-473](app://localhost/TACZ/src/main/java/com/tacz/guns/client/resource/GunDisplayInstance.java:457)）是 **gltf 优先、再 bedrock，两个都没有就 `throw`**。所以：

- **gm6：真 gap。** TaCZ 能加载 `gm6.gltf`，CGC 不能。这是分支要补的。
- **svd：不是 CGC 特有的 gap。** TaCZ 同样会 throw。这个副本里的文件就是缺失的。

另外全量扫了一遍：**9 个枪包里只有 suffuse 用 glTF**（总共 3 个 `.gltf`，其中 an94/pf98a 还没被 index 引用），其余全部是 `.animation.json`。所以这是测试集里唯一的 glTF 用户。

## 工作量

补 glTF 要移植 TaCZ 的一整包：`api/client/animation/gltf`（accessor/buffer/访问器解码）+ `pojo/animation/gltf`（15 个 POJO）+ `AnimationStructure`(427行) + `createControllerFromGltf`，约 **6000 行**。CGC 这边的 `GltfStructure` 现在是空 stub，`GltfAnimation` 也是跳过所有字段的占位解析。

这不是小改动，动手前想先跟你确认两件事：

明白了：**glTF 分支返回空控制器，svd 不管**。开始改。

Now let me compile.

Build passes. Let me review the diff and check how the mod reaches the test game dir.

Let me check the debug command that lists loaded assets — useful for verification.

Let me check one risk: whether the state machine tolerates an empty controller (no animation prototypes).

Safe: `AnimController.run()` silently returns when a prototype is missing ([AnimController.java:93-96](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/client/animation/controller/AnimController.java:93)), so an empty controller produces no errors and no per-tick spam.

Let me record the two non-obvious project facts in memory.

改完了，一处改动。

## 改动

[GunDisplayInstance.java:192-197](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/client/resource/instance/assets/GunDisplayInstance.java:192) `loadAnimController` 里补上 gltf 分支：

- bedrock 查不到后，再查 `ClientResourceApi.getGltfAnimation()`（`gltfAnimationManager`，扩展名 `.gltf`）；
- 命中就返回空 `AnimController`，并打一条 WARN 说明 gltf 动画暂不支持；
- 两个都查不到才维持原来的 `return null`（与 TaCZ 的 `throw` 等价，保留"真缺资源就失败"的语义）。

原来那句 `// TODO glTF` 被这个分支取代，注释改成说明转换本身还没实现。

## 为什么这样是安全的

`AnimController.run()` 在原型缺失时直接 `return`（[AnimController.java:93-96](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/client/animation/controller/AnimController.java:93)），所以空控制器不会抛异常、也不会每 tick 刷日志——每一次 `runAnimation` 都是静默 no-op。这个状态在"display 完全没写 animation 字段"时本来就存在，不是新路径。

`:core` 编译通过，无新增告警（构建输出里那些 deprecation 都是既有的，没有一条指向这个文件）。

## 预期效果

gm6 应该能过校验了：动画分支命中 → 空控制器；脚本走 `tacz:default_state_machine`（该文件在 `tacz_default_gun/assets/tacz/scripts/` 里，存在）→ `resetCache` 返回 true → 模型和 `suffuse:gun/uv/gm6` 贴图正常渲染。

## 还没做的（据实说明）

- **gltf 动画本身仍然不播**——gm6 会是个静止的枪，没有 idle/draw/shoot 动作。这是你选的最小解，完整移植还没开始。
- **svd 依旧紫黑块**，按你的决定没管（它缺 model/animation/script）。
- **我没有做游戏内验证。** 改动未提交，也没有构建 jar。

要我顺手 `./gradlew build` 并把 jar 覆盖到 `mods/customgun-0.0.16-forge-1.20.1.jar` 吗？还是你自己走一遍构建再实机确认（顺便也能确认一下 gm6 的静止状态能不能接受）。

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

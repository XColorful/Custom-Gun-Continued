> #82 提示词

# 

```
现在需要修复26.2起引入的渲染问题

经测试：
- 26.1.2正常
- 26.2在不装光影/装Iris+Sodium开光影+CGC Compat: Shader/装Iris+Sodium不开光影+CGC Compat: Shader均会触发
- 瞄准镜准心里_illuminated的方块，例如准心中间的红点（晚上朝地上看的时候明显），因为发亮所以还能看到红色，但是26.2开始没有变亮，导致看着是黑色（正常变暗）

当前打开了多个IDEA窗口，首要使用26.2和26.1.2，不同版本都要遵循1.20.1的语义/功能，目前认为26.3同一根因
- MC26.2强制使用延迟渲染，可查阅26.2-rendering-differences.md
```

# 

我已经定位到一条很可能是根因的线索。26.2 把自定义几何按 `RenderType.hasBlending()` 拆成 solid / translucent 两个执行时机，而目镜的 INVERT 圆用的是 `debugTriangleFan`（带 `BlendFunction.TRANSLUCENT`），会被排到黑色遮罩和准心之后执行——这正是你在 26.3 上已经确认过、并在 26.3 修掉的那个阶段错位。26.2 分支的 `PipelineModifier.NO_COLOR_WRITE` 仍然保留了混合函数，所以这个缺陷应该还在。

在动手改之前，我需要确认两件事，因为它们决定这是「阶段错位」还是另一条光照通路：

这条信息很关键，我据此修正了方向：**不是「26.2 全员都坏」，而是「只有装了光影包时才坏，且只坏 scope（走 INVERT 圆 + 模板测试的准心），sight 正常」**。26.3 那边已经把「INVERT 圆被排进 translucent 阶段」修掉了却仍然出问题，所以阶段错位可以排除。

现在的头号嫌疑是：26.2/26.3 为了做模板测试，会把**模板状态烘焙进一个运行时新建的 RenderPipeline**（`.../cgc_stencil`），而 26.1.2 是直接把 `glStencilFunc` 写进即时 GL 状态。开了光影包后 Iris 按管线实例查 program，这些运行时新建的管线它不认识——所以只有开光影才出问题。

要确认是「整条准心 draw 被丢掉」还是「draw 在、只是丢了光照」，有一个很快的判别法：

现在的证据已经能排掉大部分分支了：准心线条正常、只有 `_illuminated` 那块丢光，且只在**装了光影包**时发生、sight 正常 scope 异常——这更像是「被模板状态烘焙过的那条管线在 Iris 下没匹配到 program」，而不是模板/顺序问题。

再确认两点就能定死方向（不需要构建）：

模型结构我看清楚了，这条信息非常关键：

- 准心 dot 走的是 `divisions → division_2 → scope_division_illuminated → bone(260 cube) → dot_illuminated`
- 同一模型里 sight 那个是 `divisions → division → sight_division_illuminated`（测试正常）
- 同一模型还有 `lens_illuminated`（15 cube，父节点 `main`，走 `super_render`，不经过准心模板路径）

两者走的是同一条 `renderOcularAndDivision`，但 sight 在组合镜里是 `selective` 分支（`EQUAL i+1`），scope 是 else 分支（`EQUAL ~(i+1)` + INVERT 圆）。再确认最后一点：

我把这条链摸到了一半，说明一下现在的判断和依据。

**已排除的**

- 不是发光标记没设上：`ModelObject.resetCache()` 在 26.1.2 和 26.2 逐字相同，`dot_illuminated` / `scope_division_illuminated` 都会命中 `_illuminated` 后缀。
- 不是顶点数据写错：`BedrockPart.render` 写 `pack(15,15)` 的那段代码跨版本完全一致，`entity` 着色器、`lightmap.fsh`、`sample_lightmap.glsl` 两个版本也逐字相同，`(15,15)` 在任何时间都是白的。
- 不是 26.3 那个「INVERT 圆被排进 translucent 阶段」：26.3 已经修了它却仍然复现，而且你现在能看到准心线条，说明 254 在准心 draw 之前就已经写进模板了。
- 不是「模板烘焙管线整体丢光照」：枪体在装瞄具时也走模板烘焙，而它的明暗正常跟随环境；同一模型的 `lens_illuminated` 也正常发光。

**现在的矛盾点**

同一份模型、同一次 `renderOcularAndDivision` 调用里：

| 节点 | 路径 | 光影下 |
|---|---|---|
| `sight_division_illuminated`（`division`） | selective 分支，模板 `EQUAL i+1` | 正常发光 |
| `dot_illuminated`（`division_2 → scope_division_illuminated → bone → dot`） | else 分支，模板 `EQUAL ~(i+1)`（=254） | 不发光 |
| `lens_illuminated`（`main` 下） | `super_render`，无模板 | 正常发光 |

三者的发光数据完全相同，26.2 相对 26.1.2 的差别只有「模板状态被烘焙进运行时新建的 RenderPipeline」加上这一条 draw 用的模板 ref 不同。你描述的「开镜过程偶尔亮几帧、像侧面或者被压在下面」更像遮挡/深度而不是纯光照，但也可能只是缩放到某个 FOV 时才露出一点点发光面——单靠推理已经分不出来了。

所以我想在不改逻辑的前提下先拿到一份硬证据：要么一张 26.2 开光影和 26.1.2 开光影的同角度截图（我能看清到底是「整块暗」还是「被盖住」），要么我加一段临时诊断（画序 + 每条 draw 的 pipeline/模板/颜色掩码 + 发光件的 light），你构建跑一遍把 `latest.log` 给我——26.3 那次就是靠这个定死的。

# 

```
看着像是被盖住，开镜后受待机动作本身的视角晃动的时候，在准心边缘会露出一点带颜色的亮的，加诊断日志
```

# 

我来加临时诊断（画序 + 每条 draw 的管线/模板/掩码 + 发光件实际写入的 light），先看一下现有的日志与模板状态结构。

开始加临时诊断。先加诊断工具类与 draw 顺序拦截 mixin。

诊断代码已经加好并构建通过了，**只加了日志，没有改任何渲染逻辑**。

**新增/改动**

- [RenderDiag.java](core/src/main/java/dev/xcolorful/customgun/client/util/RenderDiag.java)：抓取窗口（每秒最多抓一次、窗口 150ms），避免刷屏。
- [PreparedRenderTypeDiagMixin.java](core/src/main/java/dev/xcolorful/customgun/client/mixin/renderer/PreparedRenderTypeDiagMixin.java)：拦 `PreparedRenderType.drawFromBuffer`，记录 **flush 阶段真实的绘制顺序**。
- 提交侧打点：[ModelObject.java:87](core/src/main/java/dev/xcolorful/customgun/client/model/ModelObject.java:87)（发光标记落在哪些节点）、[ModelObject.java:205](core/src/main/java/dev/xcolorful/customgun/client/model/ModelObject.java:205)（整模型提交）、[_AttachmentModelRender.java:232](core/src/main/java/dev/xcolorful/customgun/client/model/_AttachmentModelRender.java:232)（每个 `renderModelPart` 的路径/模板状态）、[_AttachmentModelRender.java:306](core/src/main/java/dev/xcolorful/customgun/client/model/_AttachmentModelRender.java:306)（INVERT 圆半径与条目数）、[BedrockPart.java:226](core/src/main/java/dev/xcolorful/customgun/client/model/bedrock/BedrockPart.java:226) 与 [FunctionalBedrockPart.java:57](core/src/main/java/dev/xcolorful/customgun/client/model/bedrock/FunctionalBedrockPart.java:57)（发光件在 flush 时实际拿到的 light）。

日志会带 `[CGC-RENDER-DIAG]` 前缀，形如：

```
[submit] attach render fp=true scope=true sight=false light=00F000F0 irisPack=?
[submit] part=division_2 path=[divisions/division_2] baked=true enabled=true func=...,ref=254...
[submit] illumPart name=dot_illuminated ... light=00F000F0 in=00F000F0 visible=true
[draw] idx=... pipe=.../cgc_stencil out=... stencil[ref=254 ...] depth[...] tex[Sampler0@...,Sampler1@...,Sampler2@...]
```

**麻烦你跑一次**

1. 用这份代码构建（jar 已经在 `neoforge-compat/build/libs/customgun-26.10.29-neoforge-26.2.jar`，或者你直接 `./gradlew build`）。
2. 装光影包、拿 mk5hd 开镜，**保持静止 5 秒左右**（每秒会抓一帧窗口），再轻微晃一下视角几秒。
3. 把日志给我：

```bash
grep "CGC-RENDER-DIAG" latest.log > diag.txt
```

我重点看三件事：`dot_illuminated` 在 flush 时到底有没有被画、它拿到的 light 是不是 `00F000F0`、以及准心那条 `cgc_stencil` draw 之后还有没有别的 draw 开着颜色写/深度写把它盖掉。拿到日志就能定位到具体环节，之后再决定改哪里并把诊断代码全部撤掉。

# 

```
日志放D:\Temp-File\了
```

# 

```
iris jar放在D:\Temp-File了
```

# 

日志给出了关键线索：**唯一一条 Iris 报错**是 INVERT 圆用的 `debug_triangle_fan` 管线（`Missing program ... in override list`）——Iris 对 `debug_triangle_fan` 没有 program 映射，而其它 `entity_cutout_cull/cgc_stencil` 都能正常映射（Iris 的 `PipelineBuilderMixin` 会在 `toBuilder()` 时把映射继承下来）。circle 的 draw 仍然发出了，但这条告警和你的「边缘才露一点亮」高度吻合：孔和准心没对齐。

我再加一条对照日志，把「孔的中心/半径」和「准心自己的中心」同时打出来，一次就能判定是不是错位。

新的诊断已构建（`neoforge-compat/build/libs/customgun-26.10.29-neoforge-26.2.jar`，13:32）。这次加了两条对照数据，用来一次判定「孔和准心错位」：

- `[submit] circle i=1 ... ocularCenter=(..) divisionCenter=(..) delta=(..)` —— 孔心与准心在视图空间的偏差
- `mv=[...]` —— 分别打**提交期**和**绘制期**的 modelview

重点怀疑对象已经缩小到一个：`getBedrockPartCenter()` 在**提交时**把当时的 modelview 乘进圆心偏移（`centerX/centerY` 是带 modelview 的视图空间量），而圆本身是用提交时的 pose 快照、在**绘制时**用当时的 view 矩阵画出来的。Iris 的手部通道（`HandRenderer.renderSolid`）如果提交期与绘制期的 modelview 不一致，孔就会整体平移，而准心（不带 modelview 偏移）位置正确——正好表现为「准心中心被裁掉、只有边缘落进孔里发亮」。

麻烦再跑一次，抓这几行给我就行：

```bash
grep -E "CGC-RENDER-DIAG.*(circle|attach render|\[draw\].*cgc_stencil|\[draw\].*debug_triangle)" latest.log > diag.txt
```

对数方法：同一条 `[draw]` 的 `mv=` 和它前面 `attach render` 的 `mv=` 是否一致；`circle` 那行的 `delta` 是否明显偏大。如果两者都正常，我就转向下一条路（把 INVERT 圆从 `debug_triangle_fan` 换成 Iris 有映射的管线再验证）。

# 

```
日志好了，继续排查
```

# 

两条对照都排除了：提交期和绘制期的 modelview 都是单位阵，`delta` 每帧恒定（就是模型里两个 pivot 的固定差值），所以孔没有因为矩阵问题错位。

现在只剩「准心那条 draw 的片段到底有没有真正落屏」没确认。我做一个**一次性判定实验**：把 scope 准心改成**无条件绘制**（模板 ALWAYS + 关深度测试），绕开孔、深度、遮挡所有环节。

- 如果红点**亮了** → 说明几何/光照通路没问题，问题在「孔/深度/遮挡」把它裁掉了，后面就顺着这条线修；
- 如果红点**还是黑的** → 说明这条 draw 在光影下丢了光照（采样/程序问题），方向完全不同。

这一版是**一次性判定实验**，只改了 scope 准心的绘制条件（[_AttachmentModelRender.java:405](core/src/main/java/dev/xcolorful/customgun/client/model/_AttachmentModelRender.java:405) 附近，用 `RenderDiag.ENABLED` 包着，随时可以关掉）：

```java
ClientRenderHelper.GL._disableDepthTest();
ClientRenderHelper.GL._stencilFunc(GL11.GL_ALWAYS, 0, 0xFF);
renderModelPart(... divisionNodePath);
ClientRenderHelper.GL._enableDepthTest();
```

也就是把模板孔和深度测试**同时绕过**，让准心无条件画出来。所以这版你会看到准心不再被镜筒裁切（会画到镜片外面），这是预期现象，不用管。

请装光影、拿倍镜开镜看一眼中心那个红点：

- 如果**亮了** → 几何和光照通路都是好的，问题出在「孔/深度/遮挡」把它裁掉了，下一步就在这三者里二分（先试只关模板、再试只关深度）；
- 如果**还是黑的** → 说明这条 draw 在光影下确实丢了光照，方向转到 Iris 的 program/属性绑定那条线。

顺手把这次日志也给我（`grep -E "CGC-RENDER-DIAG.*(unconditional|circle|\[draw\].*cgc_stencil)" latest.log > diag.txt`），我看它现在用的是哪条管线、有没有新的 Iris 报错。

# 

```
日志好了，现在准心中间是亮的，而且整个准心会在ocular以外的部分渲染，并且还会闪烁着叠在枪的渲染之上
```

# 

实验结论明确了：**几何和光照都没问题**，问题就是「准心这条 draw 的片段被裁掉」——而且现在能定位到具体环节了。

原因：26.2 把手部几何**延迟提交**后，Iris 会在 `beginTranslucents`（不透明地形之后）重放这些 draw（你日志里的栈 `HandRenderer.renderSolid → renderAllFeatures`）。这时深度缓冲里**已经有世界几何**，而倍镜准心在模型里位于 z≈-180（Bedrock 单位，约 **11 格**远），于是被近处地面/墙体深度剔掉；sight 准心只有约 1.75 格，所以不受影响；`renderDivisionOnly` 本来就关了深度测试，也不需要。26.1.2 是即时绘制、而且手部渲染前会清深度，所以正常。

修法：把准心当作光学元件，绘制时关掉深度测试（和 sight 路径保持一致），模板仍然保留。

根因和修法都定下来了，一句话总结：

**26.2 把手部几何改成延迟提交后，Iris 会把这些 draw 重放到 `beginTranslucents`（不透明地形之后）执行**（你日志里的栈：`HandRenderer.renderSolid → FeatureRenderDispatcher.renderAllFeatures`）。这时深度缓冲里已经有世界几何，而倍镜准心在模型里位于 `z≈-180`（Bedrock 单位，约 **11 格**远），于是被近处地面/墙体按深度剔掉——晚上朝地面、或关在密闭空间里时最明显。sight 准心只有约 1.75 格，而且 `renderDivisionOnly` 本来就关了深度测试，所以一直正常。26.1.2 是即时绘制、手部渲染前还会清深度，因此也没事。

修法是一行级别，落在 [_AttachmentModelRender.java:405](core/src/main/java/dev/xcolorful/customgun/client/model/_AttachmentModelRender.java:405)：保留模板孔（`EQUAL ~(i+1)`），只把准心绘制包进 `_disableDepthTest()` / `_enableDepthTest()`，与 sight 路径保持一致——准心是光学元件，不该被世界深度裁剪。

新 jar：`neoforge-compat/build/libs/customgun-26.10.29-neoforge-26.2.jar`（13:41）。请重点验两个场景：

1. 装光影、**晚上朝地面**开倍镜 —— 红点应正常发亮；
2. 开镜时准心**不能**跑到镜片外面/叠到枪体上（这版模板孔还在，理论上不会）。

顺带也看一眼不开光影是否照旧正常。确认没问题后，我会把 `RenderDiag`、`PreparedRenderTypeDiagMixin`、`GL.diagState()` 以及各处临时日志全部删掉，再给你一版干净的改动（并把这个坑记进项目记忆，26.3 同一根因也照这个思路修）。

# 

```
修好了，红点和组合镜开不开光影都亮了
并且比26.1.2还要亮一点，1.20.1-26.1.2是否有亮度没对上的问题?
```

# 

修好了就先把临时诊断全部撤掉，只留下修复本身。

修复已验证、临时诊断已全部移除，最终改动只有 8 行。

修复已经收尾，最终改动只有 [_AttachmentModelRender.java:405](core/src/main/java/dev/xcolorful/customgun/client/model/_AttachmentModelRender.java:405) 这 8 行（准心绘制包 `_disableDepthTest`/`_enableDepthTest`，模板孔不动），`RenderDiag`、`PreparedRenderTypeDiagMixin`、`GL.diagState()` 和所有临时日志都已删干净，`git status` 只剩这一个文件。

**关于「比 26.1.2 亮一点」**

这次改动**没有碰任何颜色/光照**——发光值仍然是同一个 `0xF0F0` 满亮度，只是把深度测试关掉了。所以更可能的解释是：**1.20.1–26.1.2 在光影下也有同样的部分截断，只是没那么彻底**。依据是我们自己代码里的注释：那几版在光影下走 Iris 的 `UnflushableWrapper`，顶点进的是 HandRenderer 的私有缓冲，同样由 Iris 在 `renderSolid`（不透明地形之后）代为 flush——和 26.2 被重放的位置是同一个。也就是说老版本里那颗红点一直有部分像素被世界深度剔掉，只是缺得少，看着像「暗一点」，你之前不会怀疑它。

对照组也支持这个判断：sight 准心走 `renderDivisionOnly`，那条路径**本来就关了深度测试**，所以它在老版本里一直是对的——如果只有倍镜那颗点出现「亮度对不上」，基本就是这条原因。

想验证的话，在 26.1.2 + 光影下，晚上朝**近处（10 格内）**的地面或墙开倍镜，和同一场景关光影对比：如果红点变暗/缺形状，而 sight 那颗不变，就坐实了。

**一个要提前说的坑**：26.3 不能照抄这行。26.3 的 `NO_DEPTH_TEST` 是 `withDepthStencilState(Optional.empty())`，而 26.3 把模板测试塞进了 `DepthStencilState`，一起丢掉就没有模板孔了（就是你之前修过的那个问题）。26.3 要保留模板、只让深度比较恒通过，得走单独一个 modifier。

这条已经记进项目记忆（`cgc-iris-hand-replay-depth-per-version`），包含机制、判定依据和 26.3 的差异，方便下次移植时直接查。

# 

```
那1.20.1-26.1.2的问题先不管，26.3不能复用26.2的修改的话怎么修？现在开始修26.3
```

# 

26.3 的做法要换：那边模板测试在 `DepthStencilState` 里，`NO_DEPTH_TEST`（`withDepthStencilState(Optional.empty())`）会连模板一起丢。先看一下 26.3 现在的实现和相关 API。

26.3 的改法已经落地，关键点是不用改 `NO_DEPTH_TEST`、也不用新增修饰器——**26.3 里已经有能保住模板的通路**。

**为什么同样的两行在 26.3 也能用**

26.3 的 `NeoStencilOperator.applyStencilToPipeline` 里有一段你之前为红点 sight 修的兜底：

```java
if (existing == null) {
    existing = new DepthStencilState(CompareOp.ALWAYS_PASS, false, 0.0F, 0.0F, null);
}
```

管线被 `NO_DEPTH_TEST` 抹掉深度状态后，注入模板时会把深度状态补成「深度恒过、不写深度」，并把模板挂上去。也就是说在 26.3：

- `_disableDepthTest()` = 移走深度状态；
- 紧接着的模板注入 = 补回 `ALWAYS_PASS + writeDepth=false + 当前 stencil`。

净效果和 26.2 完全一样：**深度恒过 + 不写深度 + 模板孔保留**。sight 那条路径本来就依赖这个兜底，所以这条通路是已经在跑的，不是新引入的机制。

**没有改 `NO_DEPTH_TEST` 本身**：你自己的移植文档里记了原因——GUI（`AttachmentCategorySlot` / `RefitTurnPageButton`）也用它，而 26.3 的 GUI 渲染通道没有深度附件，带深度状态的管线会被 `FrontendRenderPass` 直接抛 `IllegalStateException`。只在有模板要挂时才补状态，正好绕开这个雷。

**改动**

只动了 [_AttachmentModelRender.java:372](D:/Github/Custom-Gun-Continued-26.3neoforge/core/src/main/java/dev/xcolorful/customgun/client/model/_AttachmentModelRender.java:372)：scope 的 division 绘制包一层 `_disableDepthTest()` / `_enableDepthTest()`，模板 `EQUAL ~(i+1)` 不动，注释里写清了依赖上面那条兜底的行为。

构建已通过，jar：`Custom-Gun-Continued-26.3neoforge/neoforge-compat/build/libs/customgun-26.10.29-neoforge-26.3.jar`（13:54）。两个分支的改动都还没提交。

验证时建议一起看这几项：装光影、晚上朝近处地面开倍镜（红点应亮）、准心不越出镜片、红点 sight 和 4x 的模板遮罩仍然正常（确认没把兜底那条路走坏）、以及改装界面里的配件槽/翻页按钮（GUI 不受影响）。

# 

```
26.3没问题，1.20.1应用这个修改也没问题，把26.2和26.3的说明都整合到1.20.1的注释里
```

# 

说明整合到 1.20.1 分支的注释里。

已把两边的说明合并成一份跨版本注释，写进 1.20.1 分支的 [_AttachmentModelRender.java:378](D:/Github/Custom-Gun-Continued/core/src/main/java/dev/xcolorful/customgun/client/model/_AttachmentModelRender.java:378)，内容分三段：

**机制**（与版本无关的部分）
光影下 Iris 把第一人称几何重放到不透明地形之后（1.20.1–26.1.x 是 `renderSolid` 里对私有缓冲的 flush，26.2 起是 `SubmitNodeCollector` 提交后 Iris 在 `beginTranslucents` 重放，出口都是 `renderAllFeatures`），此时深度缓冲里已有世界几何；倍镜准心在模型里位于 `z≈-180`（约 11 格），被近处地面/墙体按深度剔除 → 准心、特别是 `_illuminated` 发光点不显示，或只在视角晃动时从准心边缘露一点。sight 准心只有约 1.75 格，所以一直正常。

**版本差异**
- `[1.20.1, 26.2)`：即时绘制，直接关深度测试即可
- `[26.2, 26.3)`：模板测试烘焙进运行时新建的 `RenderPipeline`，不影响这里的深度语义
- `[26.3, )`：模板测试挪进 `DepthStencilState`，`NO_DEPTH_TEST` 会连模板一起移除；但 `NeoStencilOperator#applyStencilToPipeline` 会给「没有深度状态」的管线补 `DepthStencilState(ALWAYS_PASS, writeDepth=false, …, stencil)`，模板因此保留

**并且写清了“别改 `NO_DEPTH_TEST` 本身”的原因**：GUI（`AttachmentCategorySlot` / `RefitTurnPageButton`）也在用它，而 26.3 的 GUI 渲染通道没有深度附件，带深度状态的管线会被 `FrontendRenderPass` 抛 `IllegalStateException`。

编译用 1.20.1 自己的 Gradle 8.8 wrapper 跑过 `:core:compileJava`，通过（注意 26.2 的 wrapper 是 Gradle 9.2.1，ForgeGradle 不认，别拿它去构建 1.20.1）。

现在三个分支的改动都还没提交：1.20.1（注释整合 + 你已验过的代码改动，工作区里还有个 `docs/deprecated/prompt/#82 illuminated-fix.md`）、26.2（8 行）、26.3（11 行）。

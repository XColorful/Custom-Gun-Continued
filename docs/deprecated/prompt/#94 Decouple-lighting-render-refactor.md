> #94 提示词

# 

```
读`./docs/architecture/client/render/PLAN.md`，`#93 Dirty-subtree-render-refactor.md`第590-632行
- 现在先做B，如果能测到提升就不额外加探针
- 预计跑26.35.11.1-6/b1-c-b（跟26.35.11.1-5/b1-b-3）一样，但报告合并到26.35.11.1-6/根目录，对比A的收益
```

# 

我先读 PLAN.md 和 prompt 文档的相关部分。

prompt 文档只有 14 行;用户指的是 `#93 Dirty-subtree-render-refactor.md`。我来找它。

任务清楚了:实现 B 方案(CPU 副本 + 原地重写 uv2 + 原 VBO 重传),跑 `26.35.11.1-6/b1-c-b`。先读方案文档、TODO 和已建好的测试目录。

现在读烘焙实现代码。

我需要确认 `light` 在顶点发射中具体怎么写,这决定 B 方案能否按步长安全重写 uv2。

`light` 在 `BedrockPart` 只作为 `cubePackedLight`(render light 或固定 15/15)写进 uv2。我需要确认 `core` 的跨版本编译方式,以及 `bakeChunkGeometry` 的发射细节。

现在确认 1.20.1 里 `RenderedBuffer` / `VertexBuffer.upload` 的具体 API,决定原地重传怎么实现。

方案与实现路径已确认。现在开始改代码：`BakedMesh` 保留 CPU 副本并在光照变化时原地改写 uv2 + 重传原 VBO；缓存键去掉 `light`，保留少量变体供主/阴影 pass 复用。

编译通过。用 IDEA 检查新增/改动文件的问题。

核心实现完成并编译通过。现在同步设计文档。

我发现一个必须先确认的风险点：静态整模路径的同一份 mesh 会被**多个展示框实例**共享，各自光照不同。先确认两条路径的调用场景。

静态整模网格会被多个展示实例共享（同款枪的展示框共用一个 `ModelObject`），盲目按 LRU 改写会在每帧变成多次整段重传。**我把 B 的作用范围收窄到第一人称分块路径**，静态路径保持按光照建键。先回退静态路径。

静态路径也会为每条 mesh 保留一份 CPU 副本，那是纯浪费显存/内存（多展示框场景每枪 ≈1 MB）。把保留副本改成只给第一人称分块路径用。

实现与文档已同步完成，编译通过。收尾更新进度。

B1-c-B 已实现，未提交。

## 改了什么

**[BakedMesh.java](core/src/main/java/dev/xcolorful/customgun/client/model/bake/BakedMesh.java)**
- 新增 `bakePatchable(...)`：烘焙时按 `RenderedBuffer.vertexBuffer()` 拷一份交错顶点字节（direct buffer、与 `BufferBuilder` 同字节序），并记下 vertexCount / stride / uv2 偏移（按实际写入格式算，`NEW_ENTITY` 与 Oculus 的 `ENTITY` 都是 offset 28，不写死）。
- `updateLight(int)`：按步长改写 uv2，只改「等于旧光照」的顶点，发光节点（`LightTexture.pack(15,15)`）原样保留；然后整段重传**原 VBO**——用 `vertexBuffer.bind()` 的 VAO 取回 VBO id（`glGetVertexAttribi(0, …)`），`glBufferData` 覆盖，不新建缓冲、不重新发射几何。
- 无法原地改写时返回 false 并让调用方重烘：格式无 uv2、**烘焙时光照恰为满亮**（发光顶点与普通顶点无法区分）、取不到 VBO。最坏只是回到旧成本，不会画错。
- 普通 `bake(...)` 不保留副本，静态路径不多占内存。

**[ModelObject.java](core/src/main/java/dev/xcolorful/customgun/client/model/ModelObject.java)**
- 第一人称分块路径的 `Cgc$MeshKey` 去掉 `light`；每个键保留 2 份光照变体（`accessOrder` LRU），命中不到时改写最久未用的那份，正在用的那份不动。
- 上限取 2：主 pass + 阴影 pass；`b1-b-3` 实测光影下重建次数与无光影持平（2.8 vs 2.6 次/秒），说明同帧只有两种光照。上限放大只会让首次光照变化前多烘焙几次。

## 一个范围收窄（需要你知道）

整模静态路径（`StaticBake.drawOrBake`：展示框 / 地面 / 第三人称）**仍按光照建键**。原因是同款枪的多个展示实例共享同一个 `ModelObject`，各自 `packedLight` 不同；按 LRU 改写复用的话，实例数一超过变体数就会退化成每帧多次整段重传，比现状更差。这条留给 B2（按实例记顶点区间、按实例存光照）。已写进 [lighting-decoupling.md](docs/architecture/client/render/refactor/lighting-decoupling.md) 的「作用范围」。

## 测试与文档

- 未加新探针：判据是移动段 `Bake rebuilds` 应接近 0（最初一两次光照变化仍会烘焙）+ Afterburner 1% low / 0.1% low，已记到 `26.35.11.1-6/b1-c-b/标准.txt`。
- 同步更新：`lighting-decoupling.md`（方案 B 改成已实现形态）、`TODO.md`、`PLAN.md`、`refactor/Home.md`。顺手修正原文一处事实错误：`NEW_ENTITY` 的 uv2 在 offset 28（原文写 24）。
- 本轮新增的平台 API：`GlStateManager._glBindBuffer`、`RenderSystem.glBufferData`、LWJGL `GL15/GL20`（core 里已有其他 LWJGL 使用），没有新增 AT。

`core` 与 `forge-compat` 均编译通过。跑完把数据发回来，我按约定把报告写到 `26.35.11.1-6/` 根目录并与 A 的推算收益对比。

# 

```
数据跑好了，写报告
- 另外观测到m4光影下有周期性low，low程度跟上一次ak光影的low波动程度相同，比上一次m4光影下low程度小，第二次走同一段路时帧数图非常平无波动
```

# 

先看采集数据。

数据到手：`Bake rebuilds` 从 0.34 降到 0.07/帧，但满配+光影移动段仍有 6.6 ms 的重建。先查还剩哪些路径在建键烘。

数据齐了（4 次 Afterburner + 4 次 F12）。先写分析脚本。

数据核对完毕，写入批次报告。

报告与索引已写完。

报告写好了：[26.35.11.1-6/测试数据计算报告.md](docs/architecture/client/render/benchmark/26.35.11.1-6/测试数据计算报告.md)，分析脚本在同目录 [analyze.py](docs/architecture/client/render/benchmark/26.35.11.1-6/analyze.py)。

## 主要结果（n=4）

| | 本批 | b1-b-3 |
|---|---|---|
| 满配+光影移动 1% low | **101.8** | 59.1 |
| 满配+光影移动 0.1% low | **58.5** | 41.1 |
| 重建字节（整段） | 12.2 MB | 56.7 MB |
| 单次重建 | 5.327 ms | 9.246 ms |

- **ak47（有无光影）、满配（无光影）三段重建次数全部归零**——这些场景光照变化已完全不发射顶点。
- 满配+光影重建次数 0.34 → 0.07/帧、单块字节 105.3 KB 与旧值 102.0 KB 一致（分块粒度没变，只是次数少了）。
- 平均帧时间不变（6.90 → 6.87 ms），收益全在 low 帧。

## 关于你观察到的 m4 光影周期性 low

数据和你看到的一致：

- **幅度变小**：单次 9.2 → 5.3 ms，落在 6.87 ms 的帧上 p99 从 15.4 降到 11.6 ms，所以还能看见但更浅。
- **和上一批 ak 光影的波动相当**：ak 旧尖峰 2.7 ms / 帧 6.35 ms，m4 现在 5.3 / 6.87，相对深度接近。
- **第二次走变平**：该段的光照值已被现有变体覆盖，绝大多数帧只命中或原地改写，不再发射顶点。

## 一个未定位的残留

满配+光影仍有约 1.16 次/秒的重建，每次约一半几何（8.8 块 × 105 KB）；成本的 **92% 仍是顶点发射，上传只占 6.4%**。所以：

- 这解释了为什么 1% low 还停在 101.8、低于同场景静止的 132.6；
- **方案 A 消不掉它**（A 省的正是上传），A 的额外收益只有约 0.1% 平均帧时间，维持「后续优化」的定位。

但它的触发条件还没定死：既有证据（第二次走变平）指向「新光照值的变体填充」，可 F12 窗口按标准也是第二次走，仍有 118 次重建，说明不是简单的一次填满。现有探针区分不了「填充」和「`updateLight` 回退重建」两类原因。

文档已同步：`benchmark/Home.md` 加了本批条目，`TODO.md`、`PLAN.md`、`refactor/Home.md`、`lighting-decoupling.md` 的结论换成实测值。代码和文档都还没提交。

# 

```
加探针
- 并且A的收益不要看平均帧时间的提升，最后上线的场景会压低枪的比例，如果同一模型上传是固定ms，在场景部分的帧时间增加时A的收益会变
```

# 

明白。两件事：加针对性探针定位残留重建；A 的收益改按绝对毫秒表达（不按平均帧时间占比）。先看 profiler 的接口。

探针已加，文档与 A 的收益口径都已更新。

探针加好了，A 的收益口径也按你的意思改了。

## 新探针（1 个探针 + 8 个计数器）

放在现有 F12 流程里输出，没有新增操作步骤：

|字段|读什么|
|---|---|
|`Light update`|一次原地改写（patch uv2 + 重传原 VBO）的耗时|
|`Light patches` / `Light patch bytes`|改写次数与被重传的交错字节 —— 用它和 A 的 4 B/顶点直接对比|
|`Bake: new key`|某条几何的第一次烘焙|
|`Bake: light fill`|光照没命中、变体还没满，被迫再烘一份|
|`Bake: patch failed`|改写失败后退回烘焙|
|`Bake: chunk invalid`|块被失效（脏 / 被剪枝 / 绘制失败）后重烘|
|`Light patch: no copy` / `no vbo`|改写失败的两种原因（满亮烘焙 / 取不到 VBO id）|

判读很简单：`Bake rebuilds` 减去前四项之和，就是**整模静态路径**的烘焙量（展示框/第三人称那条仍按光照建键的路径）。所以复测一次就能把满配+光影那 118 次重建归到具体来源：

- `light fill` 占多数 → 一次性填充，可忽略；
- `chunk invalid` 或静态路径有量 → 有别的失效源在抖，要找；
- `patch failed` / `no copy` / `no vbo` → 对应分支修掉就直接归零。

`BakedMesh.updateLight` 的两个失败分支各自计数；`ModelObject.cgc$getOrBake` 在烘焙前判定成因（块的第一次烘焙算「新键」，不算「失效」）。

## A 的收益改成绝对口径

报告与 [lighting-decoupling.md](docs/architecture/client/render/refactor/lighting-decoupling.md) 都改了：A 的收益写成**每次光照变化固定少传约 1.6 MB / 0.3–0.45 ms**，并明确写了不能用「占平均帧时间的百分比」衡量——本批是 145 FPS、枪几乎占满整帧的合成场景，上线场景枪的比例更低、帧时间更长，同一个固定毫秒折出的百分比会跟着变小，按它会得出错误结论。判断 A 值不值得做，看单次事件的绝对开销和它是否仍能把某些帧推过预算。

## 其他

- `chain/Home.md` 的探针表与计数器说明已补；`b1-c-b/标准.txt` 注明复测会多出这些字段，流程不变，数据追加到同目录即可。
- `analyze.py` 已能解析新字段（旧数据里显示 `-`），并多打一行「整模静态路径烘焙」。
- `core` 与 `forge-compat` 编译通过；代码和文档都还没提交。复测把数据发回来后，我按第 4 节的判读表填结论。

# 

```
看一下新测的一次

以及在光照解耦的文档里对AB方案需要加图：
- 假设已经按A完全去掉开销，则在60, 90, 120, 144, 165, 240FPS（不是离散值，需要连续）的情况下加入固定ms开销导致的另一条FPS线，超过300FPS（如1000）时这个开销对人眼没多大意义
```

# 

先看第 5 次复测数据。

新测的这一次把成因定死了。先确认图表工具可用性。

图表已生成。写进文档。

第 5 次复测把残留定死了，图也加好了。

## 复测结论：残留 100% 来自整模静态路径

|字段|满配+光影 移动|
|---|---|
|`Bake rebuilds`|0.070 / 帧|
|`Bake: new key` / `light fill` / `patch failed` / `chunk invalid`|**全 0**|
|整模静态路径烘焙（差额）|**0.070 / 帧 = 100%**|
|`Light patches` / `Light patch bytes`|0.110 次/帧（≈16 次/秒）/ 31 176 B/帧|
|`Light update`|0.412 ms（≈82 µs/次）|

即第一人称分块路径已经彻底干净——四个烘焙成因全 0，全部走原地改写，一次光照变化约 **0.45 ms + 1.6 MB**（旧的是 9.2 ms + 1.73 MB 顶点发射）。

剩下的 8.8 块 / 921 KB / 4.4 ms 全是**瞄具子网格**（`_AttachmentModelRender.renderModelPart` → `StaticBake.drawOrBake`：瞄具模板、目镜环、分划）。这条路径还在按光照建键，所以跨一格就整段重烘；无光影时它不进烘焙缓存（`Static bake` 零命中），所以只有光影下才有。

这跟之前「不能给静态路径做改写」不冲突：`renderModelPart` 是**第一人称专用、单实例**的路径，可以套同一套原地改写；只有展示框 / 第三人称那种多实例共享网格的才要等 B2。

n=5 的帧时间也顺手更新了：满配+光影移动 1% low **97.5**、0.1% low **58.0**（b1-b-3 是 59.1 / 41.1），同场景静止 126.6 / 99.2。

## AB 方案图

[光照解耦-固定开销对帧率.png](docs/architecture/client/render/refactor/光照解耦-固定开销对帧率.png)（脚本 [plot_fixed_overhead.py](docs/architecture/client/render/refactor/plot_fixed_overhead.py)，`DELTA_MS` 改一个数就能换开销），已嵌入 [lighting-decoupling.md](docs/architecture/client/render/refactor/lighting-decoupling.md)。

横轴是连续的基础帧率（对数），两条线分别是「A 完全去掉开销」和「再叠加固定 0.45 ms」。参考点下降幅度：

- 60 / 90 / 120 / 144 / 165 / 240 FPS：−2.6% / −3.9% / −5.1% / −6.1% / −6.9% / −9.7%
- 1000 FPS：掉到 690（−31%），但两条线都在 >300 FPS 的无感区，这个开销没有意义

文档里同时点明了：固定毫秒换算成「占平均帧时间的百分比」会随基础帧率单调变化，上线场景枪比例更低、帧更长时百分比更小，所以不能用它评价 A。反过来，图上也能看出本次 4.4 ms 的残留为什么仍然可见（60 FPS → 47.5、240 → 117）。

代码这轮没动，改动仍未提交。

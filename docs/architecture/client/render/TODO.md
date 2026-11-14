# 待完成

## 渲染重构（方案见 [refactor/Home.md](./refactor/Home.md)）

路线已定：**B1**。B2（跨实例合批）已实现、实测后**否决并回退**（2026-11-14），结论与原因见阶段 3。

- [x] **阶段 0 测量**：扩展探针已实现并采集完成，见 [`benchmark/26.35.11.1-2/测试数据计算报告.md`](./benchmark/26.35.11.1-2/测试数据计算报告.md)
- [x] **阶段 0 补测：满配场景**：`tacz:m4a1` 裸配 / 满配 + 9 个满配展示框，见 [`benchmark/26.35.11.1-3/测试数据计算报告.md`](./benchmark/26.35.11.1-3/测试数据计算报告.md)
- [x] **探针新增（随 B1 一起加，不单独跑批次）**
  - [x] 全场景不同 `RenderType` 去重计数 → B2 的 draw 下限（`Distinct RenderTypes`，只统计 CGC 经过的位置）
  - [x] 包住「本帧所有 CGC draw」的 `GL_TIME_ELAPSED` 查询 → 定位 multi 的 0.97 ms / 26.3 的 4.38 ms 未归因项（分段计时器 + 查询池，整帧与 CGC 段共用）
  - [x] 按节点的脏计数 → B1-b 的脏子树比例（`Distinct nodes` / `Dirty nodes`，按帧去重，只在即时管线统计）
  - [x] 把整帧 GPU 查询与分配采样改为每 N 帧一次（N = 4，`ClientRenderProfiler.SAMPLE_EVERY`）
  - [x] 采集验证：按 [`b1-a-5/标准.txt`](./benchmark/26.35.11.1-4/b1-a-5/标准.txt) 跑完，报告见 [`b1-a-5/测试数据计算报告.md`](./benchmark/26.35.11.1-4/b1-a-5/测试数据计算报告.md)
  - [x] 脏 cube 计数（`Distinct cubes` / `Dirty cubes` / `Dirty cube share`）：重烘焙成本按顶点计，这个口径直接可用；已实现，待补采
- [x] **阶段 1 B1-a 静态烘焙**（模板每帧一清转入 B2）：展示框 / 地面 / 第三人称（节点静态，`State changes = 0`）；含瞄具模板的那组 pass；顺带合并同纹理 flush、去掉 1.20.1 双重节点快照、模板每帧一清。验证缓存失效与光影表现
  - 已实现（`StaticBake.ENABLED = true`）：无光影走 `customgun:cgc_baked_entity`，开光影包走光影包自己的实体 program（Oculus 在 `GameRenderer.getRendertype*Shader()` 换 shader，实例矩阵同时驱动位置 / 法线 / 雾）
  - 1.20.1 阶段验收通过（2026-11-11，[报告](./benchmark/26.35.11.1-4/测试数据计算报告.md)）：功能项通过；光影 A/B 3.3×（烘焙关 23.9 → 7.19 ms）；内置探针见 [`b1-a-5`](./benchmark/26.35.11.1-4/b1-a-5/测试数据计算报告.md)
  - 已修复：F12 崩溃（`BakedMesh` 每次烘焙新建 `BufferBuilder`，1.20.1 的原生缓冲从不释放）
  - 实现与验收清单见 [`refactor/b1a-static-bake.md`](./refactor/b1a-static-bake.md)；26.3 的绘制方式（自定义 `RenderPipeline` + `assignPipeline`）见同文档「26.3 / 跨版本」
  - 已顺带完成：移除 1.20.1 双重节点快照（multi 实测 0.135 ms/帧）
  - 未做：模板每帧一清（多枪共用模板缓冲时语义会变，收益 0.059 ms/帧，留给 B2 重排 pass）
- [x] **阶段 2 B1-b 第一人称**：已实现并完成性能验收（2026-11-13），实现事实见 [`refactor/b1b-first-person-bake.md`](./refactor/b1b-first-person-bake.md)，数据见 [`benchmark/26.35.11.1-5`](./benchmark/26.35.11.1-5/测试数据计算报告.md)；画面仍需人工验收
  - [x] root 变换提升为绘制矩阵 + 脏子树分块重烘（实测 14 条活跃通道）
  - [x] `MAG_ADDITIONAL` 换弹几何改为**单节点退回**：不再让整个模型退回即时管线
  - [x] 瞄具模板从烘焙网格重放：`renderModelPart` 把路径前缀变换提升为绘制矩阵
  - [x] 验收采集：静止 帧时间 裸配 1.004 / 满配 1.330 ms（目标 1.2 / 1.6）；CGC 0.214 / 0.540；换弹段 `Bake rebuilds = 0`
  - [x] 换弹切块后的静态待机（[`b1-b-1`](./benchmark/26.35.11.1-5/b1-b-1/测试数据计算报告.md)）：**+0.023 / +0.026 ms（约 2%）**；换弹是脏集上界，拓扑饱和后不再增长
  - [x] 光影下的烘焙 A/B（[`b1-b-2`](./benchmark/26.35.11.1-5/b1-b-2/测试数据计算报告.md)）：无光影与上批一致（−26% / −55%）；光影下满配 −0.52 ms（−6.9%），ak47 在噪声内，画面仍需人工确认
  - [x] 光照变化（[`b1-b-3`](./benchmark/26.35.11.1-5/b1-b-3/测试数据计算报告.md)）：平均 CGC 不变；跨亮度边界那帧 0.60 / 1.37 ms，光影 2.70 / 9.25 ms，每秒 2.6–2.9 次；只有满配+光影的 1% low 明显下降（115.7 → 59.1），ak47+光影在噪声内（138.2 → 136.7）；拆分实测 9.2 ms 的 94% 是 `Bake emit`
- [ ] **阶段 2.5 B1-c 光照解耦**（必需，方案见 [`refactor/lighting-decoupling.md`](./refactor/lighting-decoupling.md)）：把 uv2 从几何里拆出来，光照变化不再重建几何
  - [x] 拆分探针 `Bake emit` / `Bake buffer create` / `Bake upload` + `Bake upload bytes`，随现有 `b1-b-3` F12 流程输出
  - [x] 复跑 `b1-b-3`：光影下 9.2 ms 的 94% 是 `Bake emit`，上传仅 0.49 ms、新建缓冲 0.05 ms；`BufferBuilder` 因 Oculus 扩展格式失去快速路径
  - [x] **B（先做）** 交错缓冲原地重写：保留 CPU 副本，光照变化 patch uv2 并重传原 VBO；不改顶点格式 / attribute，跨版本最保守；第一人称分块路径的缓存键去掉 `light`（整模静态路径仍按光照建键，留给 B2；方案细节见 [`lighting-decoupling.md`](./refactor/lighting-decoupling.md)「方案 B」）
  - [x] 实测 `b1-c-b`（[报告](./benchmark/26.35.11.1-6/测试数据计算报告.md)）：ak47（有无光影）与满配（无光影）的重建次数**归零**；满配+光影重建字节 −79%、单次 9.2 → 5.3 ms，1% low 59.1 → 101.8、0.1% low 41.1 → 58.5
  - [x] 残留定位（第 5 次复测，见 [报告](./benchmark/26.35.11.1-6/测试数据计算报告.md) 第 3 节）：分块路径的四个烘焙成因全为 0，`Bake rebuilds` 100% 来自**整模静态路径**——第一人称瞄具模板 / 目镜环 / 分划这类经 `renderModelPart` 的子网格仍按光照建键
  - [x] 给 `renderModelPart`（第一人称专用、单实例）补同一套原地改写（`StaticBake.drawOrBakePatchable` + `StaticBakeCache.PatchKey`）
  - [x] 复测（第 6 次）：满配+光影移动段 `Bake rebuilds = 0`、`Static bake` 不再出现；1% low 130.5（同场景静止 139.1）、一次光照变化 9.2 → 0.68 ms
  - [ ] 验收：画面与 `OFF` 的一致性未人工确认；1% / 0.1% low 只有 1 次采集，建议后续批次补重复
  - [ ] A（26.3 验证第二 binding 可行则必做）独立 uv2 流：4 B/顶点光照 VBO + 绘制时把 attribute 4 指向它；1.20.1 已核实 UV2 = slot 4，26.3 第二 binding 待验证。每次光照变化再省 ≈0.6 ms（2.5 MB → ≈133 KB），120 / 240 档分别值 9 / 30 FPS
  - [ ] 验收：移动段 `Bake rebuilds = 0`；光照变化帧 ≤ 普通帧 + 0.2 ms（无光影）/ + 0.5 ms（光影）；1% low 回到静止值附近；画面与 `OFF` 一致
- [x] **阶段 3 B2 跨实例合并：已实现、实测后否决，代码已回退（2026-11-14）**
  - **结论**：收益只出现在「同一把枪被多个展示框重复展示」或「多个展示框共用同一配件纹理」，这两类场景在实际游玩中几乎不存在；非数量级的收益不足以抵消新增的兼容性风险。**不保留开关**。
  - 实测（[报告](./benchmark/26.35.11.1-7/测试数据计算报告.md)）：同款满配 multi 的展示部分 draw 72 → **24**（合批 6 + 瞄具镜身 18 未合批）、同款 ak47 9 → **1**；满配空手帧时间 2.146 → **1.725 ms**（−19.6%）；**9 把不同裸枪为 0**，不同满配共用配件才省 40 次 draw。
  - 回退范围：commit `ed78e3b4e` 的**代码部分**已在工作区回退（`StaticBatch`、`LevelRendererMixin`、实体登记、`ModelObject` 接入、`BakedMesh.emitGeometry`、缓存清理调用、profiler 探针、mixin 配置），展示实例的模板测试也恢复原状；**文档保留**。
  - 若要重新实现：读 commit `ed78e3b4e`「Add render type merge render refactor」，并先看报告第 8 节的收益边界与风险表。
- [ ] **阶段 4 26.3**：确认自定义 `FeatureRenderer` 路径，实现同一套烘焙抽象；**26.3 的 render pass 是它最大的单项，必须按 pass 计时验收**
- [ ] **待评估**：方案 C（动态实例 draw 合并 / 实例化）、方案 D 的剔除保留现状
- [ ] **待评估：第一人称的分块 draw 数**：满配 35 次 CGC draw ≈0.27 ms，块切得越多 draw 越多；`b1-b-1` 实测换弹后的待机只 +2%，暂不紧迫。若要做，可考虑「长时间不脏的块回并到父块」或按 `RenderType` 合并相邻块（B2 只覆盖静态展示实例，不覆盖第一人称）
- [ ] **外部参照验证**：[`refactor/reference-tacz-1.2.0.md`](./refactor/reference-tacz-1.2.0.md) 的 V1–V9，随对应阶段一起验证；只作验证项，不改变路线，也不改动既有 benchmark 数值

## 已测得 / 仍缺的数据

- [x] 上传 / draw 拆分、draw 数、上传字节、渲染线程分配、动画通道、状态变化（26.35.11.1-2）
- [x] multi 场景基线：1.20.1 0.852 ms / 26.3 1.109 ms（`multi-baseline/`）
- [x] 满配（瞄具 + 激光 + 握把 + 枪口 + 枪托）的单枪成本、每枪 draw 数、瞄具模板占比（26.35.11.1-3）
- [x] 全场景不同 `RenderType` 数（B2 下限）：**6**（`b1-a-5`，9 把满配展示框）
- [x] CGC 绘制区间的 GPU 时间：multi 0.260 ms、第一人称满配 0.419 ms（`b1-a-5`）；深队列下只作量级参考
- [x] 按节点的脏计数（第一人称动画覆盖比例）：静止 0.06%–0.27%（`b1-a-5`）；换弹峰值 9–14 个节点（`b1-a-7`）
  - B1-b 后这组探针只统计即时块与即时管线（见 [`b1b`](./refactor/b1b-first-person-bake.md)），分块路径的失效看 `Bake rebuilds` / `Static bake`
- [x] B1-b 第一人称静止 / 换弹：帧时间 裸配 1.004 / 满配 1.330 ms，CGC 0.214 / 0.540，换弹段 `Bake rebuilds = 0`、满配 43 次 draw（`26.35.11.1-5`）
- [x] B1-b 后的开光影第一人称耗时：满配 7.520 → 7.001 ms（`b1-b-2`）；画面仍需人工确认
- [x] multi 的「9 个空展示框」基线：**0.864 ms**（`b1-a-6`），坐实 `b1-a-5` 的 0.947 ms 未归因项是场景基线
- [x] 换弹时的**脏子树覆盖**（`b1-a-7`，修复后重采）：峰值 198 / 154 / 156 个节点，p99 覆盖 53%（ak47）、22%（裸配）、8.8%（满配）；`Dirty nodes` 只有 9–14，两者差一个数量级
- [x] 换弹的**脏 cube 采样**（`b1-a-8`）：p99 脏 cube 占比 44.0% / 20.7% / 9.3%，与节点口径相差不到 3.3 个百分点；换弹峰值遍历残留 ≈0.15 / 0.11 / 0.08 ms
- [x] 开火 / 检视不单独测：开火只动栓与整枪（整枪走根矩阵），检视是换弹的慢放且驱动通道更少，脏集以换弹为上界
- [ ] 26.3 满配第一人称比 1.20.1 多 296 个立方体的原因（`Cubes submitted` 2031 vs 1735）；注意 B1-b 后该探针只统计烘焙与即时块，跨构建不可比
- [x] 开 / 关光影下烘焙路径的画面与耗时对照（B 阶段）：画面通过；耗时 23.949 → 7.193 ms（`26.35.11.1-4` 的 `ENABLED = false` A/B）

> #88 提示词

# 

```
读`docs/architecture/client/render/PLAN.md`和git工作区，现在对1.20.1最终状态新增报告，需要作横条图：
- 基准帧数
- 单枪渲染帧数
- 单枪帧时间
- 1% low 帧

需要说明单枪渲染帧时间降低多少百分比，单枪帧时间渲染性能提升多少倍，1%low帧提升多少倍
```

# 

This batch (`26.35.11.1-9`) has no script or report yet. Adding both, following the existing batch conventions.

Chart regenerated. Now writing the report.

报告已写入 [测试数据计算报告.md](docs/architecture/client/render/benchmark/26.35.11.1-9/测试数据计算报告.md)，图是 [测试数据总览-1-端间对比.png](docs/architecture/client/render/benchmark/26.35.11.1-9/测试数据总览-1-端间对比.png)（2×2 横条图），生成脚本 [analyze.py](docs/architecture/client/render/benchmark/26.35.11.1-9/analyze.py)。

**四个指标（n=4，均值 ± 1 样本标准差）**

|指标|CGC 1.20.1 最终|TaCZ 1.2.0|差|
|---|---:|---:|---:|
|基准帧数（空手）|1283.0 ± 32.4 FPS|1268.5 ± 11.9 FPS|+14.4（0.8σ，噪声内）|
|单枪渲染帧数|1017.7 ± 23.4 FPS|630.8 ± 12.7 FPS|1.61×（+61.3%）|
|单枪帧时间|0.9830 ± 0.0228 ms|1.5858 ± 0.0315 ms|−0.6028 ms（31.0σ）|
|1% low 帧|472.0 ± 7.8 FPS|334.0 ± 5.4 FPS|1.41×|

**要说明的三个结论**

- 单枪渲染帧时间（= 持枪帧时间 − 同端空手帧时间）：**0.2032 ms vs 0.7975 ms，降低 74.5%**（成本比 3.92×，差 26.8σ）。
- 单枪帧时间（整帧口径）：**1.61×**。注意它含两端相同的 ~0.78 ms 空手基线；重构本身在枪械渲染上的收益是 3.92×，报告里两个口径分开写了。
- 1% low 帧：**1.41×**（1% low 帧时间 2.1188 → 2.9942 ms，−29.2%）；它低于平均帧倍数，是因为 1% low 里混有与枪无关的偶发长帧。

两端空手基线相等（0.8σ），所以这次同批相减得出的单枪成本没有基线偏差。

已把这个批次登记到 [benchmark/Home.md](docs/architecture/client/render/benchmark/Home.md) 的批次表与分工，PLAN.md「当前结论」加了一条带链接的摘要（数值只在报告里写全）。

两点未覆盖：图我没法在本会话里目视查看，只做了文字不越界的程序检查；未提交，等你审阅。

# 

```
图需要这样画：
- 一个坐标系里各自基准帧数的条、各自单枪渲染的条、各自low帧的条
- 一个坐标系画帧时间
- 一个坐标系画渲染性能（tacz为1.0x）
- 三个坐标系从上往下排
```

# 

重画：三个坐标系上下排列，帧数一张、帧时间一张、渲染性能（TaCZ=1.00×）一张。

图已重画为三个坐标系纵排（[测试数据总览-1-端间对比.png](docs/architecture/client/render/benchmark/26.35.11.1-9/测试数据总览-1-端间对比.png)，脚本 [analyze.py](docs/architecture/client/render/benchmark/26.35.11.1-9/analyze.py)）：

- **A 帧数**：基准帧数、单枪渲染帧数、1% low 帧，每个指标一组，CGC 在上、TaCZ 在下，带 1σ 误差条。
- **B 帧时间**：基准帧时间、单枪帧时间、单枪渲染帧时间（增量）、1% low 帧时间；标题直接标注增量降低 74.5%。
- **C 渲染性能**：TaCZ 用 1.00× 的虚线作基准，CGC 分别 1.01×（基准）、1.61×（单枪整帧）、1.41×（1% low）、3.92×（单枪渲染成本倒数）。

B 面板的「单枪渲染帧时间」就是 0.203 vs 0.797 ms 那组，误差按两端帧时间标准差的合成值画。报告正文的表格和结论没动。

同样两点：本会话看不到图片渲染结果，我只做了程序化的文字不越界/图例不压标题检查；未提交。

# 

```
去掉“最终”和“最终状态”
```

# 

“最终 / 最终状态”已全部去掉：

- 图：标题 `CGC 1.20.1 vs TaCZ 1.2.0（Afterburner，n=4，不开光影）`，图例 `CGC 1.20.1`（[测试数据总览-1-端间对比.png](docs/architecture/client/render/benchmark/26.35.11.1-9/测试数据总览-1-端间对比.png) 已重新生成）
- 报告：[测试数据计算报告.md](docs/architecture/client/render/benchmark/26.35.11.1-9/测试数据计算报告.md) 标题与表格列头改为 `CGC 1.20.1`
- 索引：[benchmark/Home.md](docs/architecture/client/render/benchmark/Home.md) 批次表范围与分工条目、[PLAN.md](docs/architecture/client/render/PLAN.md) 第 7 条同步改为「1.20.1 的端间对比」

数值没动；图仍只做了程序化越界检查，未提交。

# 

```
图也要改，并且是CGC 26.11.36
```

# 

图已改为 **CGC 26.11.36**：

- 图标题：`CGC 26.11.36 vs TaCZ 1.2.0（Afterburner，n=4，不开光影）`，图例 `CGC 26.11.36`（[PNG](docs/architecture/client/render/benchmark/26.35.11.1-9/测试数据总览-1-端间对比.png) 已重新生成）
- 报告标题与表头、[benchmark/Home.md](docs/architecture/client/render/benchmark/Home.md) 的批次行与分工条目、[PLAN.md](docs/architecture/client/render/PLAN.md) 第 7 条同步改为 26.11.36
- 报告里「环境与采集」仍写 Forge 1.20.1，那是平台版本，没有动

需要你确认一处不一致：本批的测试说明 [新宿决战.txt](docs/architecture/client/render/benchmark/26.35.11.1-9/新宿决战.txt) 里列的是 `customgun-26.11.35.1`，当前 `gradle.properties` 也是 `mod_version=26.11.35.1`（git describe 为 26.11.35.1-97）。要一并改成 26.11.36，还是这两个文件保持原样？

# 

```
文件夹都改成26.11.35.xxx（原本26.35.11顺序错了），并且检查下文档链接也要改
```

# 

理解：`26.35.11.x` 是版本号顺序写反了，应改为 `26.11.35.x`。先盘点目录和所有引用。

检查到 5 个既有相对路径链接本身就少了一级（与改名无关，但属于这次要检查的链接），一并修掉。

目录与引用已改完：

**改名（git mv，保留重命名历史）**
`docs/architecture/client/render/benchmark/` 下 10 个目录：`26.35.11.1` → `26.11.35.1`，`26.35.11.1-1`…`-9` → `26.11.35.1-1`…`-9`。

**引用更新（36 个文件，151 处）**
- [PLAN.md](docs/architecture/client/render/PLAN.md)、[TODO.md](docs/architecture/client/render/TODO.md)、[benchmark/Home.md](docs/architecture/client/render/benchmark/Home.md)
- `chain/`、`refactor/` 下所有提到批次号的正文与链接
- 各批次的 `测试数据计算报告.md` 与 `analyze.py`（含注释里的批次号）

**链接检查**
- `docs/architecture/client/render` 子树共 346 个相对链接，改后 **0 失效**。
- 顺带修了 5 处**原本就少一级目录**的链接（与这次改名无关）：`b1-a-5` 指向 `-3`、`b1-a-7` 指向 `refactor/` 与 `TODO.md`、`-5` 主报告指向 `refactor/Home.md`、`b1-b-1` 指向 `TODO.md`、`b1-b-2` 指向 `b1b-first-person-bake.md`。
- `docs/architecture` 其余 177 个「失效」都是 `/docs/...` 站点根路径（core/api/wiki），属文档站的写法，未动。

**未改**
- `docs/deprecated/prompt/` 的 7 个提示词归档仍写着旧名——它们是历史记录，且其中的链接本来就按仓库根写的，不是可点击的相对链接。要一并批量替换可以说一声。
- 批次 9 的 `analyze.py` 已在新目录下重跑通过，图正常生成。

工作区仍未提交；提交时 Git 会把这批目录识别为重命名。

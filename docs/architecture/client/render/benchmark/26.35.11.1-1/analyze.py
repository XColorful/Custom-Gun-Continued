# -*- coding: utf-8 -*-
"""解析 CGC 单端基准数据（MSI Afterburner + 模组内置探针），输出统计并绘图。

数据来源：
- `cgc1 (N).txt`                Afterburner，标准测试 4 步
- `cgc1-builtin (N).txt`        模组内置探针，标准测试 4 步
- `cgc1-multi (N).txt`          Afterburner，multi 2 步
- `cgc1-multi-builtin (N).txt`  模组内置探针，multi 2 步

注意：
- `cgc1 (3).txt` 里混入了 Minecraft.exe 的基准块（启动器），解析时只取 javaw.exe。
- multi 两段的实际内容与 `标准.txt` 描述的顺序相反：按「是否含第一人称枪」标注，
  不按「持枪 / 清空」标注。
"""
import os
import re
import statistics
import sys

import matplotlib
matplotlib.use("Agg")
import numpy as np
from matplotlib import font_manager
import matplotlib.pyplot as plt

sys.stdout.reconfigure(encoding="utf-8")

BASE = os.path.dirname(os.path.abspath(__file__))
REFRESH = 165.0
BUDGET_MS = 1000.0 / REFRESH

STEPS = ["空快捷栏", "第一人称持枪", "切换槽位", "清空 /clear"]

AF_REC = re.compile(
    r"(\d{2}-\d{2}-\d{4}, \d{2}:\d{2}:\d{2}) (\S+) benchmark completed, (\d+) frames rendered in ([\d.]+) s\s*\n"
    r"\s*Average framerate\s*:\s*([\d.]+) FPS\s*\n"
    r"\s*Minimum framerate\s*:\s*([\d.]+) FPS\s*\n"
    r"\s*Maximum framerate\s*:\s*([\d.]+) FPS\s*\n"
    r"\s*1% low framerate\s*:\s*([\d.]+) FPS\s*\n"
    r"\s*0\.1% low framerate\s*:\s*([\d.]+) FPS")

BT_HEAD = re.compile(
    r"(\d{2}-\d{2}-\d{4}, \d{2}:\d{2}:\d{2}) customgun render profile completed, (\d+) frames rendered in ([\d.]+) s")
BT_LINE = re.compile(r"^\s*(.+?)\s+:\s+(.+?)\s*$", re.M)
NUM = r"[+-]?\d+(?:\.\d+)?"

PROBE_KEYS = [
    "Hand event entry", "First-person submit", "World-item submit", "State update",
    "Data refresh", "Scope render", "Model render total", "Node snapshot",
    "Functional nodes", "Geometry traverse", "endBatch flush", "Delegate render",
    "Muzzle cache", "Stencil clear", "GUI slot icon", "Camera angles",
    "Hand camera", "FOV",
]
COUNTER_KEYS = [
    "First-person submissions", "World-item submissions", "endBatch calls",
    "Cubes submitted", "Nodes captured", "Geometry batches", "Stencil windows", "LOD hits",
]
PROBE_CN = {
    "Hand event entry": "入口事件",
    "First-person submit": "第一人称提交",
    "World-item submit": "展示框提交",
    "State update": "状态更新",
    "Data refresh": "数据刷新",
    "Scope render": "瞄具模板",
    "Model render total": "模型渲染总计",
    "Node snapshot": "节点快照",
    "Functional nodes": "功能节点",
    "Geometry traverse": "几何遍历",
    "endBatch flush": "flush",
    "Delegate render": "delegate",
    "Muzzle cache": "枪口缓存",
    "Stencil clear": "模板清空",
    "GUI slot icon": "GUI 图标",
    "Camera angles": "相机角度",
    "Hand camera": "手部相机",
    "FOV": "FOV",
}
COUNTER_CN = {
    "First-person submissions": "第一人称提交",
    "World-item submissions": "展示框提交",
    "endBatch calls": "endBatch 次数",
    "Cubes submitted": "立方体数",
    "Nodes captured": "节点数",
    "Geometry batches": "几何批次",
    "Stencil windows": "模板窗口",
    "LOD hits": "LOD 命中",
}


def parse_afterburner(path):
    txt = open(path, encoding="utf-8", errors="replace").read()
    out = []
    for m in AF_REC.finditer(txt):
        ts, proc, frames, secs, avg, mn, mx, low1, low01 = m.groups()
        if proc.lower() != "javaw.exe":
            continue
        out.append(dict(
            ts=ts, frames=int(frames), seconds=float(secs),
            avg=float(avg), mn=float(mn), mx=float(mx),
            low1=float(low1), low01=float(low01),
            frame_ms=1000.0 / float(avg),
        ))
    return out


def parse_builtin(path):
    txt = open(path, encoding="utf-8", errors="replace").read()
    heads = list(BT_HEAD.finditer(txt))
    blocks = []
    for i, h in enumerate(heads):
        end = heads[i + 1].start() if i + 1 < len(heads) else len(txt)
        fields = {}
        for m in BT_LINE.finditer(txt[h.end():end]):
            fields[m.group(1).strip()] = m.group(2).strip()
        blk = dict(ts=h.group(1), frames=int(h.group(2)), seconds=float(h.group(3)), raw=fields)

        def ms(key):
            v = fields.get(key)
            if not v:
                return None
            m2 = re.search(rf"({NUM})\s*ms", v)
            return float(m2.group(1)) if m2 else None

        def per_frame(key):
            v = fields.get(key)
            if not v:
                return None
            m2 = re.search(rf"({NUM})\s*/\s*frame", v)
            return float(m2.group(1)) if m2 else None

        blk["avg_ms"] = ms("Average frame time")
        fps_m = re.search(rf"({NUM})\s*FPS", fields.get("Average frame time") or "")
        blk["avg_fps"] = float(fps_m.group(1)) if fps_m else None
        blk["min_ms"] = ms("Minimum frame time")
        blk["max_ms"] = ms("Maximum frame time")
        blk["p99_ms"] = ms("1% high frame time")
        blk["cgc_ms"] = ms("CGC render per frame")
        share = fields.get("CGC render per frame") or ""
        m3 = re.search(rf"({NUM})\s*%", share)
        blk["cgc_share"] = float(m3.group(1)) if m3 else None
        blk["counters"] = {k: per_frame(k) for k in COUNTER_KEYS}
        blk["probes"] = {k: ms(k) for k in PROBE_KEYS}
        init = fields.get("State machine init (since last report)")
        blk["init_ms"] = None
        if init:
            m4 = re.search(rf"({NUM})\s*ms", init)
            blk["init_ms"] = float(m4.group(1)) if m4 else None
        blocks.append(blk)
    return blocks


def load():
    std, multi = [], []
    for rep in range(1, 7):
        af = parse_afterburner(os.path.join(BASE, f"cgc1 ({rep}).txt"))
        bt = parse_builtin(os.path.join(BASE, f"cgc1-builtin ({rep}).txt"))
        assert len(af) == 4 and len(bt) == 4, (rep, len(af), len(bt))
        std.append(dict(rep=rep, af=af, bt=bt))

        afm = parse_afterburner(os.path.join(BASE, f"cgc1-multi ({rep}).txt"))
        btm = parse_builtin(os.path.join(BASE, f"cgc1-multi-builtin ({rep}).txt"))
        assert len(afm) == 2 and len(btm) == 2, (rep, len(afm), len(btm))
        # 用内置探针的「第一人称提交次数」判定哪一段含手持枪，不依赖文件顺序
        pairs = []
        for a, b in zip(afm, btm):
            fp = b["counters"].get("First-person submissions") or 0.0
            pairs.append(dict(af=a, bt=b, with_hand=fp > 0.5))
        multi.append(dict(rep=rep, pairs=pairs))
    return std, multi


def mean_sd(vals):
    vals = [v for v in vals if v is not None]
    if not vals:
        return (None, None)
    return (statistics.mean(vals), statistics.stdev(vals) if len(vals) > 1 else 0.0)


def summarize(std, multi):
    rows = []
    for i in range(4):
        r = {}
        af = [s["af"][i] for s in std]
        bt = [s["bt"][i] for s in std]
        r["af_fps"] = mean_sd([v["avg"] for v in af])
        r["af_ft"] = mean_sd([v["frame_ms"] for v in af])
        r["af_low1"] = mean_sd([v["low1"] for v in af])
        r["af_low01"] = mean_sd([v["low01"] for v in af])
        r["bt_ft"] = mean_sd([v["avg_ms"] for v in bt])
        r["bt_p99"] = mean_sd([v["p99_ms"] for v in bt])
        r["bt_cgc"] = mean_sd([v["cgc_ms"] for v in bt])
        r["bt_share"] = mean_sd([v["cgc_share"] for v in bt])
        r["bt_overhead"] = (r["bt_ft"][0] - r["af_ft"][0]) if r["bt_ft"][0] and r["af_ft"][0] else None
        rows.append(r)
    base_af = rows[0]["af_ft"][0]
    base_bt = rows[0]["bt_ft"][0]
    for r in rows:
        r["af_dft"] = r["af_ft"][0] - base_af
        r["bt_dft"] = r["bt_ft"][0] - base_bt
        r["headroom"] = BUDGET_MS / r["af_ft"][0]

    # multi：按内容分组，每组 6 次
    groups = {"仅 9 个展示框": [], "9 个展示框 + 手持枪": []}
    for s in multi:
        for p in s["pairs"]:
            key = "9 个展示框 + 手持枪" if p["with_hand"] else "仅 9 个展示框"
            groups[key].append(p)
    mg = {}
    for key, ps in groups.items():
        if not ps:
            continue
        mg[key] = dict(
            n=len(ps),
            af_fps=mean_sd([p["af"]["avg"] for p in ps]),
            af_ft=mean_sd([p["af"]["frame_ms"] for p in ps]),
            af_low1=mean_sd([p["af"]["low1"] for p in ps]),
            bt_ft=mean_sd([p["bt"]["avg_ms"] for p in ps]),
            bt_p99=mean_sd([p["bt"]["p99_ms"] for p in ps]),
            bt_cgc=mean_sd([p["bt"]["cgc_ms"] for p in ps]),
            bt_share=mean_sd([p["bt"]["cgc_share"] for p in ps]),
            counters={k: mean_sd([p["bt"]["counters"].get(k) for p in ps]) for k in COUNTER_KEYS},
            probes={k: mean_sd([p["bt"]["probes"].get(k) for p in ps]) for k in PROBE_KEYS},
        )

    probes = {k: dict(
        std_gun=mean_sd([s["bt"][1]["probes"].get(k) for s in std]),
        multi_hand=mean_sd([p["bt"]["probes"].get(k) for p in groups.get("9 个展示框 + 手持枪", [])]),
        multi_frame=mean_sd([p["bt"]["probes"].get(k) for p in groups.get("仅 9 个展示框", [])]),
    ) for k in PROBE_KEYS}

    counters = {k: dict(
        std_gun=mean_sd([s["bt"][1]["counters"].get(k) for s in std]),
        multi_hand=mean_sd([p["bt"]["counters"].get(k) for p in groups.get("9 个展示框 + 手持枪", [])]),
        multi_frame=mean_sd([p["bt"]["counters"].get(k) for p in groups.get("仅 9 个展示框", [])]),
    ) for k in COUNTER_KEYS}

    init_vals = []
    for s in std:
        for b in s["bt"]:
            if b["init_ms"] is not None:
                init_vals.append(b["init_ms"])
    for s in multi:
        for p in s["pairs"]:
            if p["bt"]["init_ms"] is not None:
                init_vals.append(p["bt"]["init_ms"])
    init_mean, init_sd = mean_sd(init_vals)
    init = dict(mean=init_mean, sd=init_sd, n=len(init_vals))

    return rows, mg, probes, counters, init, groups


def fmt_ms(t):
    return f"{t[0]:.4f}" if t[0] is not None else "-"


def print_summary(rows, mg, probes, counters, init):
    print("== 标准测试（6 次重复）==")
    print("步骤 | AF FPS | AF 帧时间(ms) | BT 帧时间(ms) | 采集开销(ms) | CGC(ms) | 占比 | 帧时间增量 AF/BT | 1%low(AF) | p99(BT)")
    for i, r in enumerate(rows):
        print(f"{STEPS[i]} | {r['af_fps'][0]:.1f}±{r['af_fps'][1]:.1f} | "
              f"{r['af_ft'][0]:.4f}±{r['af_ft'][1]:.4f} | {r['bt_ft'][0]:.4f}±{r['bt_ft'][1]:.4f} | "
              f"{r['bt_overhead']:+.4f} | {fmt_ms(r['bt_cgc'])} | {r['bt_share'][0]:.1f}% | "
              f"{r['af_dft']:+.4f} / {r['bt_dft']:+.4f} | {r['af_low1'][0]:.1f} | {r['bt_p99'][0]:.4f}")
    print("\n== multi ==")
    for k, g in mg.items():
        print(f"{k}: n={g['n']}  AF {g['af_fps'][0]:.1f}±{g['af_fps'][1]:.1f} FPS "
              f"({g['af_ft'][0]:.4f} ms) | BT {g['bt_ft'][0]:.4f} ms | "
              f"CGC {fmt_ms(g['bt_cgc'])} ms ({g['bt_share'][0]:.1f}%) | 1%low {g['af_low1'][0]:.1f}")
    print("\n== 探针（ms）==")
    print(f"{'探针':24s} {'标准持枪':>16s} {'multi+手':>16s} {'multi 无手':>16s}")
    for k in PROBE_KEYS:
        v = probes[k]
        print(f"{PROBE_CN[k]:24s} {fmt_ms(v['std_gun']):>16s} {fmt_ms(v['multi_hand']):>16s} {fmt_ms(v['multi_frame']):>16s}")
    print("\n== 计数器（/帧）==")
    print(f"{'计数器':24s} {'标准持枪':>10s} {'multi+手':>10s} {'multi 无手':>10s}")
    for k in COUNTER_KEYS:
        v = counters[k]
        f = lambda t: f"{t[0]:.2f}" if t[0] is not None else "-"
        print(f"{COUNTER_CN[k]:24s} {f(v['std_gun']):>10s} {f(v['multi_hand']):>10s} {f(v['multi_frame']):>10s}")
    if init["n"]:
        print(f"\n状态机初始化（窗口外）：{init['mean']:.3f} ± {init['sd']:.3f} ms（{init['n']} 个样本）")
    else:
        print("\n状态机初始化：无样本")


def setup_font():
    for f in (r"C:\Windows\Fonts\msyh.ttc", r"C:\Windows\Fonts\simhei.ttf"):
        if os.path.exists(f):
            font_manager.fontManager.addfont(f)
    plt.rcParams["font.sans-serif"] = ["Microsoft YaHei", "SimHei", "DejaVu Sans"]
    plt.rcParams["axes.unicode_minus"] = False
    plt.rcParams["axes.spines.top"] = False
    plt.rcParams["axes.spines.right"] = False


C_AF = "#b8442f"
C_BT = "#2f6db3"
C_AF2 = "#e8a090"
C_BT2 = "#7fb3e6"


def fig_standard(rows, probes, out):
    setup_font()
    fig, axes = plt.subplots(1, 3, figsize=(18, 5.6))
    fig.suptitle("CGC 单端标准测试（6 次重复）：Afterburner 与内置探针对照", fontsize=14, y=0.99)

    # A 平均帧率
    ax = axes[0]
    x = np.arange(4)
    w = 0.36
    af = [r["af_fps"][0] for r in rows]
    bt = [r["bt_ft"][0] for r in rows]
    bt_fps = [1000.0 / v for v in bt]
    ax.bar(x - w / 2, af, w, label="Afterburner", color=C_AF,
           yerr=[r["af_fps"][1] for r in rows], capsize=2, error_kw=dict(lw=0.8))
    ax.bar(x + w / 2, bt_fps, w, label="内置探针", color=C_BT,
           yerr=[1000.0 / v[0] ** 2 * v[1] for v in [r["bt_ft"] for r in rows]],
           capsize=2, error_kw=dict(lw=0.8))
    for xi, v in zip(x - w / 2, af):
        ax.annotate(f"{v:.0f}", (xi, v), textcoords="offset points", xytext=(0, 3), ha="center", fontsize=7)
    for xi, v in zip(x + w / 2, bt_fps):
        ax.annotate(f"{v:.0f}", (xi, v), textcoords="offset points", xytext=(0, 3), ha="center", fontsize=7)
    ax.set_xticks(x)
    ax.set_xticklabels(STEPS, fontsize=9)
    ax.set_ylabel("平均帧率 (FPS)")
    ax.set_title("A. 平均帧率（±1 样本标准差）", fontsize=11)
    ax.legend(fontsize=8)
    ax.grid(axis="y", alpha=0.25)

    # B 帧时间增量
    ax = axes[1]
    d_af = [r["af_dft"] for r in rows]
    d_bt = [r["bt_dft"] for r in rows]
    ax.bar(x - w / 2, d_af, w, label="Afterburner", color=C_AF)
    ax.bar(x + w / 2, d_bt, w, label="内置探针", color=C_BT)
    for xi, v in list(zip(x - w / 2, d_af)) + list(zip(x + w / 2, d_bt)):
        ax.annotate(f"{v:+.3f}", (xi, v), textcoords="offset points",
                    xytext=(0, 3 if v >= 0 else -9), ha="center", fontsize=7)
    ax.axhline(0, color="black", lw=0.8)
    ax.set_xticks(x)
    ax.set_xticklabels(STEPS, fontsize=9)
    ax.set_ylabel("相对空快捷栏的帧时间增量 (ms)")
    ax.set_title("B. 每帧额外耗时（0 = 空手基线）", fontsize=11)
    ax.legend(fontsize=8)
    ax.grid(axis="y", alpha=0.25)

    # C 持枪段探针（互相包含，不做堆叠）
    ax = axes[2]
    g = {k: v["std_gun"] for k, v in probes.items()}
    cgc = g["First-person submit"][0]
    keys = ["Geometry traverse", "endBatch flush", "Delegate render", "State update",
            "Node snapshot", "Stencil clear", "Functional nodes", "Data refresh", "Muzzle cache"]
    vals = [g[k][0] for k in keys][::-1]
    labels = [f"{PROBE_CN[k]}" for k in keys][::-1]
    colors = ["#c9c9c9", "#a9c7e2", "#c78fb0", "#5f9ea0", "#e0a35c", "#b0a0d0", "#d08a8a", "#9ab87a", "#8fb8de"][::-1]
    ax.barh(labels, vals, color=colors)
    for yi, v in enumerate(vals):
        ax.annotate(f"{v:.3f} ms ({v / cgc * 100:.1f}%)", (v, yi), textcoords="offset points",
                    xytext=(4, 0), va="center", fontsize=7.5)
    ax.set_xlim(0, max(vals) * 1.6)
    ax.set_xlabel("每帧耗时 (ms)")
    ax.set_title(f"C. 第一人称持枪探针（CGC 总计 {cgc:.3f} ms）", fontsize=11)
    ax.text(0.02, 0.03, "部分项相互包含（delegate 内会再次进入 model render），不可相加",
            transform=ax.transAxes, fontsize=7, color="#666666")
    ax.grid(axis="x", alpha=0.25)

    fig.tight_layout(rect=[0, 0, 1, 0.94])
    fig.savefig(out, dpi=130)
    plt.close(fig)


def fig_multi(mg, probes, counters, out):
    setup_font()
    fig, axes = plt.subplots(1, 3, figsize=(18, 5.6))
    fig.suptitle("CGC multi 场景：9 个展示框（± 手持枪）", fontsize=14, y=0.99)

    # A 帧时间
    ax = axes[0]
    keys = list(mg.keys())
    x = np.arange(len(keys))
    w = 0.36
    ax.bar(x - w / 2, [mg[k]["af_ft"][0] for k in keys], w, label="Afterburner", color=C_AF,
           yerr=[mg[k]["af_ft"][1] for k in keys], capsize=2, error_kw=dict(lw=0.8))
    ax.bar(x + w / 2, [mg[k]["bt_ft"][0] for k in keys], w, label="内置探针", color=C_BT,
           yerr=[mg[k]["bt_ft"][1] for k in keys], capsize=2, error_kw=dict(lw=0.8))
    for xi, k in zip(x - w / 2, keys):
        ax.annotate(f"{mg[k]['af_ft'][0]:.3f}", (xi, mg[k]["af_ft"][0]), textcoords="offset points",
                    xytext=(0, 3), ha="center", fontsize=7)
    for xi, k in zip(x + w / 2, keys):
        ax.annotate(f"{mg[k]['bt_ft'][0]:.3f}", (xi, mg[k]["bt_ft"][0]), textcoords="offset points",
                    xytext=(0, 3), ha="center", fontsize=7)
    ax.set_xticks(x)
    ax.set_xticklabels(keys, fontsize=9)
    ax.set_ylabel("平均帧时间 (ms)")
    ax.set_title("A. 帧时间（±1 样本标准差）", fontsize=11)
    ax.legend(fontsize=8)
    ax.grid(axis="y", alpha=0.25)

    # B 每把展示框枪的成本构成
    ax = axes[1]
    cnt = counters["World-item submissions"]
    n = cnt["multi_frame"][0] or 9.0
    per = {
        "几何遍历": probes["Geometry traverse"]["multi_frame"][0] / n,
        "flush": probes["endBatch flush"]["multi_frame"][0] / n,
        "节点快照": probes["Node snapshot"]["multi_frame"][0] / n,
        "模板清空": probes["Stencil clear"]["multi_frame"][0] / n,
        "数据刷新": probes["Data refresh"]["multi_frame"][0] / n,
    }
    labels = list(per.keys())[::-1]
    vals = [per[k] for k in labels]
    ax.barh(labels, vals, color=["#c9c9c9", "#a9c7e2", "#e0a35c", "#b0a0d0", "#8fb8de"][::-1])
    for yi, v in enumerate(vals):
        ax.annotate(f"{v:.3f}", (v, yi), textcoords="offset points", xytext=(4, 0), va="center", fontsize=7.5)
    ax.set_xlim(0, max(vals) * 1.35)
    ax.set_xlabel("每把展示框枪的耗时 (ms)")
    ax.set_title(f"B. 单把展示框枪成本（全帧 {probes['World-item submit']['multi_frame'][0]:.3f} ms / {n:.0f} 把）", fontsize=11)
    ax.grid(axis="x", alpha=0.25)

    # C CGC 占比与 165Hz 余量
    ax = axes[2]
    labels = keys
    share = [mg[k]["bt_share"][0] for k in keys]
    bars = ax.bar(np.arange(len(keys)), share, 0.55, color="#4a7fa5")
    for xi, v, k in zip(range(len(keys)), share, keys):
        ax.annotate(f"{v:.1f}%\n({mg[k]['bt_cgc'][0]:.3f} ms)", (xi, v), textcoords="offset points",
                    xytext=(0, 3), ha="center", fontsize=8)
    ax.set_xticks(range(len(keys)))
    ax.set_xticklabels(labels, fontsize=9)
    ax.set_ylim(0, 100)
    ax.set_ylabel("CGC 占帧时间比例")
    ax.set_title("C. CGC 占比（括号内为每帧耗时）", fontsize=11)
    ax.grid(axis="y", alpha=0.25)
    head = [BUDGET_MS / mg[k]["af_ft"][0] for k in keys]
    for xi, h in zip(range(len(keys)), head):
        ax.annotate(f"165Hz 余量 {h:.2f}×", (xi, 4), ha="center", fontsize=8, color="#b8442f")

    fig.tight_layout(rect=[0, 0, 1, 0.94])
    fig.savefig(out, dpi=130)
    plt.close(fig)


def fig_stability(std, probes, out):
    setup_font()
    fig, axes = plt.subplots(1, 3, figsize=(18, 5.6))
    fig.suptitle("重复性与稳定性（6 次重复）", fontsize=14, y=0.99)

    # A 每次重复的持枪帧率
    ax = axes[0]
    reps = [s["rep"] for s in std]
    vals = [s["af"][1]["avg"] for s in std]
    ax.plot(reps, vals, "o-", color=C_AF, label="Afterburner")
    ax.set_xticks(reps)
    ax.set_xlabel("重复编号")
    ax.set_ylabel("第一人称持枪平均帧率 (FPS)")
    ax.set_title(f"A. 持枪帧率逐次（均值 {statistics.mean(vals):.1f}）", fontsize=11)
    ax.grid(alpha=0.25)

    # B 持枪探针的重复分布
    ax = axes[1]
    keys = ["State update", "Geometry traverse", "endBatch flush", "Delegate render", "Model render total"]
    data = [[s["bt"][1]["probes"].get(k) for s in std] for k in keys]
    data = [[v for v in d if v is not None] for d in data]
    bp = ax.boxplot(data, tick_labels=[PROBE_CN[k] for k in keys], patch_artist=True,
                    medianprops=dict(color="black", lw=1.2))
    for patch in bp["boxes"]:
        patch.set_facecolor("#9cc0dd")
    for yi, d in enumerate(data, start=1):
        ax.scatter([yi] * len(d), d, s=12, color=C_AF, zorder=3, alpha=0.8)
    ax.set_ylabel("每帧耗时 (ms)")
    ax.tick_params(axis="x", labelsize=8)
    ax.set_title("B. 持枪段主要探针的重复分布", fontsize=11)
    ax.grid(axis="y", alpha=0.25)

    # C 尾帧对比
    ax = axes[2]
    x = np.arange(4)
    w = 0.36
    ax.bar(x - w / 2, [r["af_low1"][0] for r in
                       [dict(af_low1=mean_sd([s["af"][i]["low1"] for s in std])) for i in range(4)]],
           w, label="Afterburner 1% low (FPS)", color=C_AF)
    ax.bar(x + w / 2, [mean_sd([s["bt"][i]["p99_ms"] for s in std])[0] for i in range(4)],
           w, label="内置探针 p99 帧时间 (ms)", color=C_BT)
    ax.set_xticks(x)
    ax.set_xticklabels(STEPS, fontsize=9)
    ax.set_ylabel("数值（单位不同，仅看趋势）")
    ax.set_title("C. 尾帧：1% low 帧率 vs p99 帧时间", fontsize=11)
    ax.legend(fontsize=8)
    ax.grid(axis="y", alpha=0.25)

    fig.tight_layout(rect=[0, 0, 1, 0.94])
    fig.savefig(out, dpi=130)
    plt.close(fig)


def main():
    std, multi = load()
    rows, mg, probes, counters, init, groups = summarize(std, multi)
    print_summary(rows, mg, probes, counters, init)

    fig_standard(rows, probes, os.path.join(BASE, "测试数据总览-1-标准.png"))
    fig_multi(mg, probes, counters, os.path.join(BASE, "测试数据总览-2-multi.png"))
    fig_stability(std, probes, os.path.join(BASE, "测试数据总览-3-稳定性.png"))
    print("\n图已保存：测试数据总览-1-标准.png / -2-multi.png / -3-稳定性.png")


if __name__ == "__main__":
    main()

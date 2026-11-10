# -*- coding: utf-8 -*-
"""解析 CGC 双端基准数据（MSI Afterburner + 模组内置探针），输出统计并绘图。

数据来源（每个端 6 次重复）：
- `cgc1 (N).txt`                Afterburner，1.20.1 标准测试 4 步
- `cgc1-builtin (N).txt`        内置探针，1.20.1 标准测试 4 步
- `cgc1-multi (N).txt`          Afterburner，1.20.1 multi 2 步
- `cgc1-multi-builtin (N).txt`  内置探针，1.20.1 multi 2 步
- `cgc2*`                       同上，NeoForge 26.3

注意：
- 只取 `javaw.exe` 记录（Afterburner 会连启动器一起录制）。
- multi 两段按「是否含第一人称枪」标注，不按文件顺序。
- 26.3 的 CGC = 提交阶段 + flush 回调；render pass 的 GPU 提交不在探针内，
  因此跨版本只能比「CGC 采样到的部分」，不能直接当整帧成本比较。
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

NODES = ["cgc1", "cgc2"]
NODE_LABEL = {"cgc1": "1.20.1 (Forge)", "cgc2": "26.3 (NeoForge)"}
NODE_COLOR = {"cgc1": "#b8442f", "cgc2": "#2f6db3"}
NODE_LIGHT = {"cgc1": "#e8a090", "cgc2": "#7fb3e6"}
STEPS = ["空快捷栏", "第一人称持枪", "切换槽位", "清空 /clear"]
MULTI_LABEL = {"仅 9 个展示框": "仅 9 个展示框", "9 个展示框 + 手持枪": "9 个展示框 + 手持枪"}

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
    # 26.2+ 延迟渲染的对应项（解析后归一到上面的名字）
    "Model submit", "Model flush",
]
COUNTER_KEYS = [
    "First-person submissions", "World-item submissions", "endBatch calls",
    "Cubes submitted", "Nodes captured", "Geometry batches", "Stencil windows", "LOD hits",
    # 26.2+ 延迟渲染的对应项
    "Geometry submits", "Flush callbacks",
]
PROBE_CN = {
    "Hand event entry": "入口事件", "First-person submit": "第一人称提交",
    "World-item submit": "展示框提交", "State update": "状态更新",
    "Data refresh": "数据刷新", "Scope render": "瞄具模板",
    "Model render total": "模型渲染总计", "Node snapshot": "节点快照",
    "Functional nodes": "功能节点", "Geometry traverse": "几何遍历",
    "endBatch flush": "模型 flush", "Delegate render": "delegate",
    "Muzzle cache": "枪口缓存", "Stencil clear": "模板清空",
    "GUI slot icon": "GUI 图标", "Camera angles": "相机角度",
    "Hand camera": "手部相机", "FOV": "FOV",
}
COUNTER_CN = {
    "First-person submissions": "第一人称提交", "World-item submissions": "展示框提交",
    "endBatch calls": "几何提交/endBatch", "Cubes submitted": "立方体数",
    "Nodes captured": "节点数", "Geometry batches": "几何批次",
    "Stencil windows": "模板窗口", "LOD hits": "LOD 命中",
}
PROBE_SHOW = [k for k in PROBE_KEYS if k not in ("Model submit", "Model flush")]
COUNTER_SHOW = [k for k in COUNTER_KEYS if k not in ("Geometry submits", "Flush callbacks")]


def parse_afterburner(path):
    txt = open(path, encoding="utf-8", errors="replace").read()
    out = []
    for m in AF_REC.finditer(txt):
        ts, proc, frames, secs, avg, mn, mx, low1, low01 = m.groups()
        if proc.lower() != "javaw.exe":
            continue
        out.append(dict(ts=ts, frames=int(frames), seconds=float(secs),
                        avg=float(avg), mn=float(mn), mx=float(mx),
                        low1=float(low1), low01=float(low01),
                        frame_ms=1000.0 / float(avg)))
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
        blk = dict(ts=h.group(1), frames=int(h.group(2)), seconds=float(h.group(3)))

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
        blk["p99_ms"] = ms("1% high frame time")
        blk["cgc_ms"] = ms("CGC render per frame")
        share = fields.get("CGC render per frame") or ""
        m3 = re.search(rf"({NUM})\s*%", share)
        blk["cgc_share"] = float(m3.group(1)) if m3 else None
        blk["counters"] = {k: per_frame(k) for k in COUNTER_KEYS}
        if blk["counters"].get("endBatch calls") is None:
            blk["counters"]["endBatch calls"] = blk["counters"].get("Geometry submits")
        blk["probes"] = {k: ms(k) for k in PROBE_KEYS}
        if blk["probes"].get("Model render total") is None:
            blk["probes"]["Model render total"] = blk["probes"].get("Model submit")
        if blk["probes"].get("endBatch flush") is None:
            blk["probes"]["endBatch flush"] = blk["probes"].get("Model flush")
        init = fields.get("State machine init (since last report)")
        blk["init_ms"] = None
        if init:
            m4 = re.search(rf"({NUM})\s*ms", init)
            blk["init_ms"] = float(m4.group(1)) if m4 else None
        blocks.append(blk)
    return blocks


def load():
    data = {}
    for node in NODES:
        std, multi = [], []
        for rep in range(1, 7):
            af = parse_afterburner(os.path.join(BASE, f"{node} ({rep}).txt"))
            bt = parse_builtin(os.path.join(BASE, f"{node}-builtin ({rep}).txt"))
            assert len(af) == 4 and len(bt) == 4, (node, rep, len(af), len(bt))
            std.append(dict(rep=rep, af=af, bt=bt))

            afm = parse_afterburner(os.path.join(BASE, f"{node}-multi ({rep}).txt"))
            btm = parse_builtin(os.path.join(BASE, f"{node}-multi-builtin ({rep}).txt"))
            assert len(afm) == 2 and len(btm) == 2, (node, rep, len(afm), len(btm))
            pairs = [dict(af=a, bt=b,
                          with_hand=(b["counters"].get("First-person submissions") or 0.0) > 0.5)
                     for a, b in zip(afm, btm)]
            multi.append(dict(rep=rep, pairs=pairs))
        data[node] = dict(std=std, multi=multi)
    return data


def mean_sd(vals):
    vals = [v for v in vals if v is not None]
    if not vals:
        return (None, None)
    return (statistics.mean(vals), statistics.stdev(vals) if len(vals) > 1 else 0.0)


def summarize_node(entry):
    std, multi = entry["std"], entry["multi"]
    rows = []
    for i in range(4):
        af = [s["af"][i] for s in std]
        bt = [s["bt"][i] for s in std]
        r = dict(
            af_fps=mean_sd([v["avg"] for v in af]),
            af_ft=mean_sd([v["frame_ms"] for v in af]),
            af_low1=mean_sd([v["low1"] for v in af]),
            bt_ft=mean_sd([v["avg_ms"] for v in bt]),
            bt_p99=mean_sd([v["p99_ms"] for v in bt]),
            bt_cgc=mean_sd([v["cgc_ms"] for v in bt]),
            bt_share=mean_sd([v["cgc_share"] for v in bt]),
        )
        r["overhead"] = r["bt_ft"][0] - r["af_ft"][0]
        rows.append(r)
    base_af, base_bt = rows[0]["af_ft"][0], rows[0]["bt_ft"][0]
    for r in rows:
        r["af_dft"] = r["af_ft"][0] - base_af
        r["bt_dft"] = r["bt_ft"][0] - base_bt
        r["headroom"] = BUDGET_MS / r["af_ft"][0]

    groups = {"仅 9 个展示框": [], "9 个展示框 + 手持枪": []}
    for s in multi:
        for p in s["pairs"]:
            groups["9 个展示框 + 手持枪" if p["with_hand"] else "仅 9 个展示框"].append(p)
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
    m, sd = mean_sd(init_vals)
    return dict(rows=rows, mg=mg, probes=probes, counters=counters,
                init=dict(mean=m, sd=sd, n=len(init_vals)), std=std)


def print_summary(summary):
    for node in NODES:
        s = summary[node]
        print(f"\n===== {NODE_LABEL[node]} · 标准测试（6 次重复）=====")
        print("步骤 | AF FPS | AF 帧时间 | 内置帧时间 | 采集开销 | CGC | 占比 | Δft(AF/内置) | 1%low | p99")
        for i, r in enumerate(s["rows"]):
            print(f"{STEPS[i]} | {r['af_fps'][0]:.1f}±{r['af_fps'][1]:.1f} | "
                  f"{r['af_ft'][0]:.4f}±{r['af_ft'][1]:.4f} | {r['bt_ft'][0]:.4f}±{r['bt_ft'][1]:.4f} | "
                  f"{r['overhead']:+.4f} | {r['bt_cgc'][0]:.4f} | {r['bt_share'][0]:.1f}% | "
                  f"{r['af_dft']:+.4f}/{r['bt_dft']:+.4f} | {r['af_low1'][0]:.1f} | {r['bt_p99'][0]:.4f}")
        print("multi:")
        for k, g in s["mg"].items():
            print(f"  {k}: n={g['n']} AF {g['af_fps'][0]:.1f}±{g['af_fps'][1]:.1f} FPS "
                  f"({g['af_ft'][0]:.4f} ms) BT {g['bt_ft'][0]:.4f} ms "
                  f"CGC {g['bt_cgc'][0]:.4f} ({g['bt_share'][0]:.1f}%)")
        if s["init"]["n"]:
            print(f"  状态机初始化：{s['init']['mean']:.3f} ± {s['init']['sd']:.3f} ms（{s['init']['n']} 样本）")

    print("\n===== 持枪探针（ms，跨端对照）=====")
    print(f"{'探针':24s} {'1.20.1':>12s} {'26.3':>12s}")
    for k in PROBE_SHOW:
        a = summary["cgc1"]["probes"][k]["std_gun"]
        b = summary["cgc2"]["probes"][k]["std_gun"]
        fa = f"{a[0]:.4f}" if a[0] is not None else "-"
        fb = f"{b[0]:.4f}" if b[0] is not None else "-"
        print(f"{PROBE_CN[k]:24s} {fa:>12s} {fb:>12s}")

    print("\n===== 计数器（/帧，跨端对照）=====")
    print(f"{'计数器':20s} {'1.20.1-持枪':>14s} {'26.3-持枪':>14s} {'1.20.1-9框':>14s} {'26.3-9框':>14s}")
    for k in COUNTER_SHOW:
        def f(node, group):
            t = summary[node]["counters"][k][group]
            return f"{t[0]:.2f}" if t[0] is not None else "-"
        print(f"{COUNTER_CN[k]:20s} {f('cgc1','std_gun'):>14s} {f('cgc2','std_gun'):>14s} "
              f"{f('cgc1','multi_frame'):>14s} {f('cgc2','multi_frame'):>14s}")

    print("\n===== multi（每帧 / 每把枪）=====")
    for node in NODES:
        g = summary[node]["mg"]
        n = (g.get("仅 9 个展示框", {}).get("counters", {}).get("World-item submissions", (9, 0))[0]) or 9.0
        for k, d in g.items():
            per = "" if k == "9 个展示框 + 手持枪" else f", 每把 {d['bt_cgc'][0]/n:.4f} ms"
            print(f"{NODE_LABEL[node]} {k}: CGC {d['bt_cgc'][0]:.4f} ms{per}, "
                  f"占比 {d['bt_share'][0]:.1f}%, 165Hz 余量 {BUDGET_MS/d['af_ft'][0]:.3f}x")


def setup_font():
    for f in (r"C:\Windows\Fonts\msyh.ttc", r"C:\Windows\Fonts\simhei.ttf"):
        if os.path.exists(f):
            font_manager.fontManager.addfont(f)
    plt.rcParams["font.sans-serif"] = ["Microsoft YaHei", "SimHei", "DejaVu Sans"]
    plt.rcParams["axes.unicode_minus"] = False
    plt.rcParams["axes.spines.top"] = False
    plt.rcParams["axes.spines.right"] = False


def fig_standard(summary, out):
    setup_font()
    fig, axes = plt.subplots(1, 3, figsize=(18, 5.6))
    fig.suptitle("CGC 双端标准测试（各 6 次重复）：Afterburner 与内置探针对照", fontsize=14, y=0.99)
    x = np.arange(4)
    w = 0.36

    ax = axes[0]
    for k, node in enumerate(NODES):
        vals = [summary[node]["rows"][i]["af_ft"][0] for i in range(4)]
        errs = [summary[node]["rows"][i]["af_ft"][1] for i in range(4)]
        bars = ax.bar(x + (k - 0.5) * w, vals, w, label=NODE_LABEL[node], color=NODE_COLOR[node],
                      yerr=errs, capsize=2, error_kw=dict(lw=0.8))
        for xi, v in zip(x + (k - 0.5) * w, vals):
            ax.annotate(f"{v:.2f}", (xi, v), textcoords="offset points", xytext=(0, 3),
                        ha="center", fontsize=7)
    ax.set_xticks(x)
    ax.set_xticklabels(STEPS, fontsize=9)
    ax.set_ylabel("平均帧时间 (ms)")
    ax.set_title("A. 帧时间（±1 样本标准差）", fontsize=11)
    ax.legend(fontsize=8)
    ax.grid(axis="y", alpha=0.25)

    ax = axes[1]
    for k, node in enumerate(NODES):
        vals = [summary[node]["rows"][i]["af_dft"] for i in range(4)]
        ax.bar(x + (k - 0.5) * w, vals, w, label=NODE_LABEL[node], color=NODE_COLOR[node])
        for xi, v in zip(x + (k - 0.5) * w, vals):
            ax.annotate(f"{v:+.3f}", (xi, v), textcoords="offset points",
                        xytext=(0, 3 if v >= 0 else -9), ha="center", fontsize=7)
    ax.axhline(0, color="black", lw=0.8)
    ax.set_xticks(x)
    ax.set_xticklabels(STEPS, fontsize=9)
    ax.set_ylabel("相对空快捷栏的帧时间增量 (ms)")
    ax.set_title("B. 每帧额外耗时（Afterburner）", fontsize=11)
    ax.legend(fontsize=8)
    ax.grid(axis="y", alpha=0.25)

    ax = axes[2]
    keys = ["State update", "Data refresh", "Node snapshot", "Geometry traverse",
            "endBatch flush", "Delegate render"]
    vals1 = [summary["cgc1"]["probes"][k]["std_gun"][0] for k in keys]
    vals2 = [summary["cgc2"]["probes"][k]["std_gun"][0] for k in keys]
    y = np.arange(len(keys))
    ax.barh(y - 0.2, vals1, 0.4, label=NODE_LABEL["cgc1"], color=NODE_COLOR["cgc1"])
    ax.barh(y + 0.2, vals2, 0.4, label=NODE_LABEL["cgc2"], color=NODE_COLOR["cgc2"])
    for yi, v in zip(y - 0.2, vals1):
        ax.annotate(f"{v:.3f}", (v, yi), textcoords="offset points", xytext=(3, 0), va="center", fontsize=7)
    for yi, v in zip(y + 0.2, vals2):
        ax.annotate(f"{v:.3f}", (v, yi), textcoords="offset points", xytext=(3, 0), va="center", fontsize=7)
    ax.set_yticks(y)
    ax.set_yticklabels([PROBE_CN[k] for k in keys], fontsize=9)
    ax.set_xlim(0, max(vals1 + vals2) * 1.3)
    ax.set_xlabel("每帧耗时 (ms)")
    ax.set_title("C. 持枪段探针（互相包含，不可相加）", fontsize=11)
    ax.legend(fontsize=8)
    ax.grid(axis="x", alpha=0.25)

    fig.tight_layout(rect=[0, 0, 1, 0.94])
    fig.savefig(out, dpi=130)
    plt.close(fig)


def fig_multi(summary, out):
    setup_font()
    fig, axes = plt.subplots(1, 3, figsize=(18, 5.6))
    fig.suptitle("CGC multi：9 个展示框（± 手持枪）", fontsize=14, y=0.99)
    keys = ["仅 9 个展示框", "9 个展示框 + 手持枪"]
    x = np.arange(len(keys))
    w = 0.36

    ax = axes[0]
    for k, node in enumerate(NODES):
        vals = [summary[node]["mg"][g]["af_ft"][0] for g in keys]
        errs = [summary[node]["mg"][g]["af_ft"][1] for g in keys]
        ax.bar(x + (k - 0.5) * w, vals, w, label=NODE_LABEL[node], color=NODE_COLOR[node],
               yerr=errs, capsize=2, error_kw=dict(lw=0.8))
        for xi, v in zip(x + (k - 0.5) * w, vals):
            ax.annotate(f"{v:.3f}", (xi, v), textcoords="offset points", xytext=(0, 3), ha="center", fontsize=7)
    ax.set_xticks(x)
    ax.set_xticklabels(keys, fontsize=9)
    ax.set_ylabel("平均帧时间 (ms)")
    ax.set_title("A. 帧时间（±1 样本标准差）", fontsize=11)
    ax.legend(fontsize=8)
    ax.grid(axis="y", alpha=0.25)

    ax = axes[1]
    vals = []
    for node in NODES:
        g = summary[node]["mg"]["仅 9 个展示框"]
        n = g["counters"]["World-item submissions"][0] or 9.0
        vals.append(g["bt_cgc"][0] / n)
    ax.bar(np.arange(len(NODES)), vals, 0.5,
           color=[NODE_COLOR[n] for n in NODES])
    for xi, v in enumerate(vals):
        ax.annotate(f"{v:.3f}", (xi, v), textcoords="offset points", xytext=(0, 3), ha="center", fontsize=8)
    ax.set_xticks(np.arange(len(NODES)))
    ax.set_xticklabels([NODE_LABEL[n] for n in NODES], fontsize=9)
    ax.set_ylabel("每把枪的 CGC 耗时 (ms)")
    ax.set_title("B. 单把展示框枪成本（仅 9 框组 CGC / 9）", fontsize=11)
    ax.grid(axis="y", alpha=0.25)

    ax = axes[2]
    for k, node in enumerate(NODES):
        g = summary[node]["mg"]
        vals = [g[key]["bt_share"][0] for key in keys]
        ax.bar(x + (k - 0.5) * w, vals, w, label=NODE_LABEL[node], color=NODE_COLOR[node])
        for xi, v, key in zip(x + (k - 0.5) * w, vals, keys):
            ax.annotate(f"{v:.1f}%\n{g[key]['bt_cgc'][0]:.2f}ms", (xi, v), textcoords="offset points",
                        xytext=(0, 3), ha="center", fontsize=7)
    ax.set_xticks(x)
    ax.set_xticklabels(keys, fontsize=9)
    ax.set_ylim(0, 100)
    ax.set_ylabel("CGC 占帧时间比例")
    ax.set_title("C. CGC 占比（括号内为每帧耗时）", fontsize=11)
    ax.legend(fontsize=8)
    ax.grid(axis="y", alpha=0.25)

    fig.tight_layout(rect=[0, 0, 1, 0.94])
    fig.savefig(out, dpi=130)
    plt.close(fig)


def fig_stability(summary, out):
    setup_font()
    fig, axes = plt.subplots(1, 3, figsize=(18, 5.6))
    fig.suptitle("重复性与尾帧（各 6 次重复）", fontsize=14, y=0.99)

    ax = axes[0]
    reps = list(range(1, 7))
    for node in NODES:
        vals = [s["af"][1]["avg"] for s in summary[node]["std"]]
        ax.plot(reps, vals, "o-", color=NODE_COLOR[node], label=NODE_LABEL[node])
    ax.set_xticks(reps)
    ax.set_xlabel("重复编号")
    ax.set_ylabel("第一人称持枪平均帧率 (FPS)")
    ax.set_title("A. 持枪帧率逐次", fontsize=11)
    ax.legend(fontsize=8)
    ax.grid(alpha=0.25)

    ax = axes[1]
    x = np.arange(4)
    w = 0.36
    for k, node in enumerate(NODES):
        vals = [summary[node]["rows"][i]["af_low1"][0] for i in range(4)]
        ax.bar(x + (k - 0.5) * w, vals, w, label=NODE_LABEL[node], color=NODE_COLOR[node])
    ax.set_xticks(x)
    ax.set_xticklabels(STEPS, fontsize=9)
    ax.set_ylabel("1% low 帧率 (FPS)")
    ax.set_title("B. 1% low（Afterburner）", fontsize=11)
    ax.legend(fontsize=8)
    ax.grid(axis="y", alpha=0.25)

    ax = axes[2]
    for k, node in enumerate(NODES):
        vals = [summary[node]["rows"][i]["bt_p99"][0] for i in range(4)]
        ax.bar(x + (k - 0.5) * w, vals, w, label=NODE_LABEL[node], color=NODE_COLOR[node])
    ax.set_xticks(x)
    ax.set_xticklabels(STEPS, fontsize=9)
    ax.set_ylabel("p99 帧时间 (ms)")
    ax.set_title("C. 尾帧（内置探针）", fontsize=11)
    ax.legend(fontsize=8)
    ax.grid(axis="y", alpha=0.25)

    fig.tight_layout(rect=[0, 0, 1, 0.94])
    fig.savefig(out, dpi=130)
    plt.close(fig)


def main():
    data = load()
    summary = {node: summarize_node(data[node]) for node in NODES}
    print_summary(summary)
    fig_standard(summary, os.path.join(BASE, "测试数据总览-1-标准.png"))
    fig_multi(summary, os.path.join(BASE, "测试数据总览-2-multi.png"))
    fig_stability(summary, os.path.join(BASE, "测试数据总览-3-稳定性.png"))
    print("\n图已保存：测试数据总览-1-标准.png / -2-multi.png / -3-稳定性.png")


if __name__ == "__main__":
    main()
